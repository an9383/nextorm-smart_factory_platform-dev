package com.nextorm.portal.service;

import com.nextorm.common.db.dto.ai.ModelBuildRequestDto;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.entity.ai.AiModel;
import com.nextorm.common.db.entity.ai.ParameterDetail;
import com.nextorm.common.db.repository.AiModelRepository;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.portal.client.inference.InferenceRangeClient;
import com.nextorm.portal.client.inference.InferenceRangeResponse;
import com.nextorm.portal.common.constant.ErrorCode;
import com.nextorm.portal.common.exception.CommonException;
import com.nextorm.portal.common.exception.ai.AiModelNotFoundException;
import com.nextorm.portal.common.exception.ai.InferenceFailException;
import com.nextorm.portal.common.exception.parameter.ParameterNotFoundException;
import com.nextorm.portal.dto.aimodel.AiInferenceResponseDto;
import com.nextorm.portal.dto.aimodel.ModelResponseDto;
import com.nextorm.portal.dto.aimodel.correlation.CorrelationRankDto;
import com.nextorm.portal.dto.aimodel.correlation.CorrelationRequest;
import com.nextorm.portal.entity.processoptimization.ProcessOptimization;
import com.nextorm.portal.event.message.ModelBuildRequestEvent;
import com.nextorm.portal.repository.processoptimization.ProcessOptimizationAnalysisRepository;
import com.nextorm.portal.repository.processoptimization.ProcessOptimizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

	private static final long CORRELATION_ROW_THRESHOLD = 5000L; // 자동 분기 기준으로 임계값(ROW 수) 비교
	private final AiModelRepository aiModelRepository;
	private final ParameterRepository parameterRepository;
	private final ParameterDataRepository parameterDataRepository;
	private final ProcessOptimizationRepository processOptimizationRepository;
	private final ProcessOptimizationAnalysisRepository processOptimizationAnalysisRepository;
	private final InferenceRangeClient inferenceRangeClient;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public List<ModelResponseDto> getModels() {
		List<Parameter> parameters = parameterRepository.findAll(Sort.by(Sort.Direction.ASC, "createAt"));
		Map<Long, String> paramMap = parameters.stream()
											   .collect(Collectors.toMap(Parameter::getId, Parameter::getName));

		return aiModelRepository.findAll()
								.stream()
								.map(aiModel -> {
									ModelResponseDto aiModelDto = ModelResponseDto.toModelResponseDto(aiModel);
									// yParameterName만 추가로 세팅 (builder 기반)
									return aiModelDto.toBuilder()
													 .yParameterName(paramMap.get(aiModel.getYParameterId()))
													 .build();
								})
								.toList();
	}

	@Transactional
	public ModelResponseDto buildModel(ModelBuildRequestDto request) {
		Long toolSearchParameterId = request.getXParameters()
											.get(0)
											.getId();

		Tool tool = parameterRepository.findById(toolSearchParameterId)
									   .orElseThrow(ParameterNotFoundException::new)
									   .getTool();

		List<ParameterDetail> parameterDetails = new ArrayList<>();

		request.getXParameters()
			   .forEach((xParameter) -> {
				   Parameter parameter = parameterRepository.findById(xParameter.getId())
															.orElseThrow(ParameterNotFoundException::new);
				   parameterDataRepository.findByParameterIdAndTraceAtBetween(xParameter.getId(),
					   request.getFrom(),
					   request.getTo(),
					   Sort.by("traceAt"));

				   parameterDetails.add(ParameterDetail.builder()
													   .id(parameter.getId())
													   .name(parameter.getName())
													   .min(xParameter.getMin())
													   .max(xParameter.getMax())
													   .build());
			   });

		AiModel aiModel = AiModel.toAiModel(request, tool, parameterDetails);
		aiModelRepository.save(aiModel);
		eventPublisher.publishEvent(new ModelBuildRequestEvent(aiModel.getId()));
		return ModelResponseDto.toModelResponseDto(aiModel);
	}

	@Transactional
	public void delete(Long id) {
		List<ProcessOptimization> optimizations = processOptimizationRepository.findAllWithAiModelByAiModelId(id);
		if (optimizations.isEmpty()) {
			aiModelRepository.deleteById(id);
			return;
		}
		List<Long> optimizationIds = optimizations.stream()
												  .map(ProcessOptimization::getId)
												  .toList();

		processOptimizationAnalysisRepository.deleteAllByProcessOptimizationIdIn(optimizationIds);
		processOptimizationRepository.deleteAllByIdIn(optimizationIds);
		aiModelRepository.deleteById(id);
	}

	/**
	 * 상관관계 계산 방법을 데이터 양 (row 갯수) 기준으로 자동 분기
	 * <p>
	 * - 5000건 이하 : calculateCorrelationRanksLegacy 사용
	 * 장점: 쿼리 횟수는 적지만 데이터량이 작기 때문에 빠름
	 * 각 파라미터별로 traceAt 정렬된 데이터 조회 → 정렬 및 그룹핑 불필요
	 * <p>
	 * - 데이터 많음 (예: 10만건 이상) : calculateCorrelationRanks 사용
	 * 장점: 전체 데이터를 한 번에 조회해서 메모리에서 그룹핑 처리 → 대용량에 적합
	 * 후처리 정렬 및 계산 필요하지만 쿼리 효율 높음
	 */
	@Transactional(readOnly = true)
	public List<CorrelationRankDto> calculateCorrelationRanksAutoSelect(CorrelationRequest request) {
		long totalRowCount = parameterDataRepository.countByParameterIdInAndTraceAtBetween(request.getXParameterIds(),
			request.getFrom(),
			request.getTo());

		// Legacy 방식 사용 여부 결정
		if (totalRowCount <= CORRELATION_ROW_THRESHOLD) {
			return calculateCorrelationRanksLegacy(request);
		} else {
			return calculateCorrelationRanks(request);
		}
	}

	public List<CorrelationRankDto> calculateCorrelationRanksLegacy(CorrelationRequest request) {
		Long yParameterId = request.getYParameterId();
		List<Long> xParameterIds = request.getXParameterIds();
		LocalDateTime from = request.getFrom();
		LocalDateTime to = request.getTo();

		if (xParameterIds == null || xParameterIds.isEmpty()) {
			throw new IllegalArgumentException("X 파라미터 ID 리스트가 비어 있습니다.");
		}
		if (yParameterId == null) {
			throw new IllegalArgumentException("Y 파라미터 ID가 없습니다.");
		}
		if (from == null || to == null) {
			throw new IllegalArgumentException("조회 기간(from, to)이 없습니다.");
		}

		Map<Long, double[]> xParameterDataMap = getXParameterDataMap(xParameterIds, from, to);
		double[] yParameterDataList = getYParameterDataList(yParameterId, from, to);

		Map<Long, Double> mapX = new HashMap<>();
		Map<Long, Double> mapInf = new HashMap<>();
		Map<Long, Double> mapNaN = new HashMap<>();

		int yLength = yParameterDataList.length;
		for (Map.Entry<Long, double[]> entry : xParameterDataMap.entrySet()) {
			double[] xData = entry.getValue();
			int length = Math.min(yLength, xData.length);
			double coeff = getCorrelationCoefficient(xData, yParameterDataList, length);

			if (Double.isInfinite(coeff)) {
				mapInf.put(entry.getKey(), coeff);
			} else if (Double.isNaN(coeff)) {
				mapNaN.put(entry.getKey(), coeff);
			} else {
				mapX.put(entry.getKey(), coeff);
			}
		}

		Map<Long, Parameter> parameterMap = parameterRepository.findByIdIn(xParameterIds)
															   .stream()
															   .collect(Collectors.toMap(Parameter::getId, p -> p));

		return convertToDtoList(mapX, mapInf, mapNaN, parameterMap);
	}

	private Map<Long, double[]> getXParameterDataMap(
		List<Long> xParameterIds,
		LocalDateTime from,
		LocalDateTime to
	) {
		Map<Long, double[]> result = new HashMap<>();
		for (Long id : xParameterIds) {
			List<ParameterData> dataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(id,
				from,
				to,
				Sort.by("traceAt"));
			double[] values = dataList.stream()
									  .map(ParameterData::getNumberValue)
									  .mapToDouble(Number::doubleValue)
									  .toArray();
			result.put(id, values);
		}
		return result;
	}

	private double[] getYParameterDataList(
		Long yParameterId,
		LocalDateTime from,
		LocalDateTime to
	) {
		return parameterDataRepository.findByParameterIdAndTraceAtBetween(yParameterId, from, to, Sort.by("traceAt"))
									  .stream()
									  .map(ParameterData::getNumberValue)
									  .mapToDouble(Number::doubleValue)
									  .toArray();
	}

	public List<CorrelationRankDto> calculateCorrelationRanks(CorrelationRequest request) {
		Long yParameterId = request.getYParameterId();
		List<Long> xParameterIds = request.getXParameterIds();
		LocalDateTime from = request.getFrom();
		LocalDateTime to = request.getTo();

		if (xParameterIds == null || xParameterIds.isEmpty()) {
			throw new IllegalArgumentException("X 파라미터 ID 리스트가 비어 있습니다.");
		}
		if (yParameterId == null) {
			throw new IllegalArgumentException("Y 파라미터 ID가 없습니다.");
		}
		if (from == null || to == null) {
			throw new IllegalArgumentException("조회 기간(from, to)이 없습니다.");
		}

		Set<Long> allParamIds = new HashSet<>(xParameterIds);
		allParamIds.add(yParameterId);

		List<ParameterData> allData = parameterDataRepository.findUnsortedByParameterIdInAndTraceAtBetween(new ArrayList<>(
			allParamIds), from, to);
		allData.sort(Comparator.comparing(ParameterData::getTraceAt));

		Map<Long, List<Number>> grouped = new HashMap<>();
		for (ParameterData data : allData) {
			grouped.computeIfAbsent(data.getParameterId(), k -> new ArrayList<>())
				   .add(data.getNumberValue());
		}

		Map<Long, double[]> paramDataMap = new HashMap<>();
		for (Map.Entry<Long, List<Number>> entry : grouped.entrySet()) {
			paramDataMap.put(entry.getKey(),
				entry.getValue()
					 .stream()
					 .mapToDouble(Number::doubleValue)
					 .toArray());
		}

		double[] yData = paramDataMap.getOrDefault(yParameterId, new double[0]);
		int yLength = yData.length;

		Map<Long, Double> mapX = new HashMap<>();
		Map<Long, Double> mapInf = new HashMap<>();
		Map<Long, Double> mapNaN = new HashMap<>();

		for (Long xId : xParameterIds) {
			double[] xData = paramDataMap.getOrDefault(xId, new double[0]);
			int length = Math.min(yLength, xData.length);
			double coeff = getCorrelationCoefficient(xData, yData, length);

			if (Double.isInfinite(coeff)) {
				mapInf.put(xId, coeff);
			} else if (Double.isNaN(coeff)) {
				mapNaN.put(xId, coeff);
			} else {
				mapX.put(xId, coeff);
			}
		}

		Map<Long, Parameter> parameterMap = parameterRepository.findByIdIn(xParameterIds)
															   .stream()
															   .collect(Collectors.toMap(Parameter::getId, p -> p));

		return convertToDtoList(mapX, mapInf, mapNaN, parameterMap);
	}

	private List<CorrelationRankDto> convertToDtoList(
		Map<Long, Double> mapX,
		Map<Long, Double> mapInf,
		Map<Long, Double> mapNaN,
		Map<Long, Parameter> parameterMap
	) {
		List<Map.Entry<Long, Double>> combined = new ArrayList<>();
		combined.addAll(mapX.entrySet()
							.stream()
							.sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()))
							.toList());
		combined.addAll(mapInf.entrySet());
		combined.addAll(mapNaN.entrySet());

		List<CorrelationRankDto> list = new ArrayList<>();
		for (int i = 0; i < combined.size(); i++) {
			Map.Entry<Long, Double> entry = combined.get(i);
			Parameter parameter = parameterMap.get(entry.getKey());
			if (parameter == null) {
				throw new CommonException(ErrorCode.RELATED_DATA_EXISTS);
			}

			CorrelationRankDto dto = new CorrelationRankDto();
			dto.setParameterId(entry.getKey());
			dto.setParameterName(parameter.getName());
			dto.setRank(i + 1);

			double value = entry.getValue();
			dto.setCorCoefficient(Double.isInfinite(value) || Double.isNaN(value)
								  ? "0.000"
								  : BigDecimal.valueOf(value)
											  .setScale(3, RoundingMode.HALF_EVEN)
											  .toString());
			list.add(dto);
		}
		return list;
	}

	private double getCorrelationCoefficient(
		double[] xArr,
		double[] yArr,
		int n
	) {
		double sum_X = 0, sum_Y = 0, sum_XY = 0;
		double squareSum_X = 0, squareSum_Y = 0;

		for (int i = 0; i < n; i++) {
			sum_X = sum_X + xArr[i];
			sum_Y = sum_Y + yArr[i];
			sum_XY = sum_XY + xArr[i] * yArr[i];
			squareSum_X = squareSum_X + xArr[i] * xArr[i];
			squareSum_Y = squareSum_Y + yArr[i] * yArr[i];
		}

		// covariation
		double cov = sum_XY / n - sum_X * sum_Y / n / n;
		// standard error of x
		double sigmax = Math.sqrt(squareSum_X / n - sum_X * sum_X / n / n);
		// standard error of y
		double sigmay = Math.sqrt(squareSum_Y / n - sum_Y * sum_Y / n / n);

		// correlation is just a normalized covariation
		return Math.abs(cov / sigmax / sigmay);
	}

	public ModelResponseDto getModelById(Long id) {
		return aiModelRepository.findById(id)
								.map(ModelResponseDto::toModelResponseDto)
								.orElse(null);
	}

	@Transactional
	public void updateModelFailureStatus(
		Long modelId,
		String failureReason
	) {
		aiModelRepository.findById(modelId)
						 .ifPresent(model -> model.updateFailure(failureReason));
	}

	@Transactional(readOnly = true)
	public AiInferenceResponseDto getInferenceData(
		Long modelId,
		LocalDateTime from,
		LocalDateTime to
	) {

		AiModel model = aiModelRepository.findById(modelId)
										 .orElseThrow(AiModelNotFoundException::new);

		InferenceRangeResponse inferenceRangeResponse = inferenceRangeClient.requestInference(modelId, from, to);

		if (!inferenceRangeResponse.isSuccess()) {
			throw new InferenceFailException(inferenceRangeResponse.getMessage());
		}

		Parameter yParameter = parameterRepository.findById(model.getYParameterId())
												  .orElseThrow(ParameterNotFoundException::new);

		InferenceRangeResponse.InferenceData inferenceData = inferenceRangeResponse.getData();

		return new AiInferenceResponseDto(inferenceData.getParameterId(),
			yParameter.getName(),
			inferenceData.getItems()
						 .stream()
						 .map(it -> new AiInferenceResponseDto.Item(it.getTime(), it.getValue(), it.getOriginalValue()))
						 .toList());
	}

	@Transactional(readOnly = true)
	public List<ModelResponseDto> getModelByToolId(Long toolId) {
		return aiModelRepository.findByToolId(toolId)
								.stream()
								.map(ModelResponseDto::toModelResponseDto)
								.collect(Collectors.toList());
	}
}
