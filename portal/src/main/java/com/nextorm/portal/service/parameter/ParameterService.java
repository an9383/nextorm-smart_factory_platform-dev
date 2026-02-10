package com.nextorm.portal.service.parameter;

import com.nextorm.common.db.entity.BaseEntity;
import com.nextorm.common.db.entity.DcpConfig;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.DcpConfigRepository;
import com.nextorm.common.db.repository.ParameterExtraDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.common.db.repository.dto.ParameterSearchParam;
import com.nextorm.common.define.event.redis.message.ParameterEventMessage;
import com.nextorm.portal.common.exception.ConstraintViloationException;
import com.nextorm.portal.common.exception.dcpconfig.RelateDcpConfigNotFoundException;
import com.nextorm.portal.common.exception.parameter.*;
import com.nextorm.portal.common.exception.tool.RelateToolNotFoundException;
import com.nextorm.portal.dto.base.BaseTreeItem;
import com.nextorm.portal.dto.common.ConstraintViloationDto;
import com.nextorm.portal.dto.parameter.*;
import com.nextorm.portal.entity.system.RefreshEvent;
import com.nextorm.portal.util.TreeUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ParameterService {
	private final ApplicationEventPublisher eventPublisher;
	private final ParameterRepository parameterRepository;
	private final DcpConfigRepository dcpConfigRepository;
	private final ToolRepository toolRepository;
	private final ParameterExtraDataRepository parameterExtraDataRepository;

	public List<ParameterResponseDto> getParameters(ParameterSearchRequestDto searchRequestDto) {
		ParameterSearchParam parameterSearchParam = ParameterSearchParam.builder()
																		.ids(searchRequestDto.getId())
																		.toolId(searchRequestDto.getToolId())
																		.name(searchRequestDto.getName())
																		.isVirtual(searchRequestDto.getIsVirtual())
																		.type(searchRequestDto.getType())
																		.dataTypes(searchRequestDto.getDataTypes())
																		.build();

		return parameterRepository.findBySearchParams(parameterSearchParam)
								  .stream()
								  .map(ParameterResponseDto::from)
								  .toList();
	}

	public ParameterResponseDto getParameterByNameAndToolId(
		Long toolId,
		String name
	) {
		return parameterRepository.findByToolIdAndName(toolId, name)
								  .map(ParameterResponseDto::from)
								  .orElseThrow(() -> new ParameterNotFoundException(toolId, name));
	}

	public ParameterResponseDto getParameter(Long parameterId) {
		return parameterRepository.findById(parameterId)
								  .map(ParameterResponseDto::from)
								  .orElseThrow(() -> new ParameterNotFoundException(parameterId));
	}

	public ParameterResponseDto createParameter(ParameterCreateRequestDto parameterCreateRequestDto) {
		Tool tool = toolRepository.findById(parameterCreateRequestDto.getToolId())
								  .orElseThrow(RelateToolNotFoundException::new);

		Parameter parameter = parameterCreateRequestDto.toEntity(tool);
		ParameterResponseDto parameterResponseDto = ParameterResponseDto.from(parameterRepository.save(parameter));
		eventPublisher.publishEvent(new RefreshEvent(tool.getId(), "CREATE_PARAMETER"));
		return parameterResponseDto;
	}

	public ParameterResponseDto modifyParameter(
		Long parameterId,
		ParameterUpdateRequestDto parameterUpdateRequestDto
	) {
		Parameter parameter = parameterRepository.findById(parameterId)
												 .orElseThrow(ParameterNotFoundException::new);

		parameter.modify(parameterUpdateRequestDto.toEntity());
		eventPublisher.publishEvent(new RefreshEvent(parameter.getTool()
															  .getId(), "MODIFY_PARAMETER"));

		eventPublisher.publishEvent(ParameterEventMessage.modifyEvent(parameterId));
		return ParameterResponseDto.from(parameter);
	}

	public void deleteParameter(Long id) {
		Parameter parameter = parameterRepository.findById(id)
												 .orElseThrow(() -> new ParameterNotFoundException(id));

		List<ConstraintViloationDto> exists = new ArrayList<>();
		List<DcpConfig> dcpConfigs = dcpConfigRepository.findAllByParameterId(parameter.getId());
		if (!dcpConfigs.isEmpty()) {
			exists.addAll(dcpConfigs.stream()
									.map(dcpConfig -> new ConstraintViloationDto("Modeling > Dcp Config",
										dcpConfig.getId(),
										dcpConfig.getTopic(),
										null,
										null))
									.toList());
		}

		if (!exists.isEmpty()) {
			throw new ConstraintViloationException(exists);
		}

		parameterRepository.delete(parameter);
		eventPublisher.publishEvent(new RefreshEvent(parameter.getTool()
															  .getId(), "DELETE_PARAMETER"));
	}

	public List<ParameterResponseDto> getToolAssignedParameters(Long toolId) {
		List<Long> dcpConfigIds = dcpConfigRepository.findByToolId(toolId)
													 .stream()
													 .map(DcpConfig::getId)
													 .toList();

		return parameterRepository.findDistanceAllByDcpConfigIds(dcpConfigIds)
								  .stream()
								  .map(ParameterResponseDto::from)
								  .toList();
	}

	public VirtualParameterResponseDto createVirtualParameter(VirtualParameterCreateRequestDto createRequestDto) {
		Tool tool = toolRepository.findById(createRequestDto.getToolId())
								  .orElseThrow(() -> new RelateToolNotFoundException(createRequestDto.getToolId()));

		Parameter virtualParameter = createRequestDto.toEntity(tool);
		addMappingParameters(virtualParameter, createRequestDto.getMappingParameters());

		DcpConfig dcpConfig = dcpConfigRepository.findById(createRequestDto.getDcpConfigId())
												 .orElseThrow(() -> new RelateDcpConfigNotFoundException(
													 createRequestDto.getDcpConfigId()));
		dcpConfig.addParameter(virtualParameter);

		List<Long> mappingParameterIds = virtualParameter.getMappingParameters()
														 .stream()
														 .map(BaseEntity::getId)
														 .toList();

		parameterRepository.save(virtualParameter);
		eventPublisher.publishEvent(new RefreshEvent(tool.getId(), "CREATE_VIRTUAL_PARAMETER"));
		return VirtualParameterResponseDto.of(virtualParameter, dcpConfig, mappingParameterIds);
	}

	private boolean checkVirtualParameterCircularReference(Parameter virtualParameter) {
		Map<Long, ReferenceNode> allNodes = new HashMap<>();
		containReferenceNodeMap(allNodes, virtualParameter);

		CircularReferenceChecker circularReferenceChecker = new CircularReferenceChecker(allNodes);
		return circularReferenceChecker.hasCircularReference(allNodes.get(virtualParameter.getId()));
	}

	private void containReferenceNodeMap(
		Map<Long, ReferenceNode> container,
		Parameter owner
	) {
		List<Parameter> requiredVirtualParameters = owner.getCalculationRequiredParameters()
														 .stream()
														 .filter(Parameter::isVirtual)
														 .toList();

		container.put(owner.getId(),
			new ReferenceNode(owner.getId(),
				requiredVirtualParameters.stream()
										 .map(Parameter::getId)
										 .toList()));

		boolean allContainsKey = requiredVirtualParameters.stream()
														  .allMatch(it -> container.containsKey(it.getId()));

		if (!allContainsKey) {
			for (Parameter parameter : requiredVirtualParameters) {
				containReferenceNodeMap(container, parameter);
			}
		}
	}

	private void addMappingParameters(
		Parameter virtualParameter,
		List<MappingParameterDto> mappingParameters
	) {
		virtualParameter.addMappingParameters(parameterRepository.findByIdIn(toParameterIds(mappingParameters)),
			toCalculationUsingParameterIdSet(mappingParameters));
	}

	private List<Long> toParameterIds(List<MappingParameterDto> mappingParameters) {
		return mappingParameters.stream()
								.map(MappingParameterDto::getId)
								.toList();
	}

	private Set<Long> toCalculationUsingParameterIdSet(List<MappingParameterDto> mappingParameters) {
		return mappingParameters.stream()
								.filter(MappingParameterDto::isUsingCalculation)
								.map(MappingParameterDto::getId)
								.collect(Collectors.toSet());
	}

	public VirtualParameterResponseDto getVirtualParameterById(Long id) {
		Parameter parameter = parameterRepository.findByIdWithMappingParameters(id)
												 .orElseThrow(() -> new RelateParameterNotFoundException(id));

		DcpConfig dcpConfig = dcpConfigRepository.findAllByParameterId(id)
												 .stream()
												 .findFirst()
												 .orElseThrow(() -> new RelateDcpConfigNotFoundException(id));

		List<Long> mappingParameterIds = parameter.getMappingParameters()
												  .stream()
												  .map(it -> it.getParameter()
															   .getId())
												  .toList();

		return VirtualParameterResponseDto.of(parameter, dcpConfig, mappingParameterIds);
	}

	public VirtualParameterResponseDto modifyVirtualParameter(
		Long parameterId,
		VirtualParameterUpdateRequestDto virtualParameterDto
	) {
		Parameter parameter = parameterRepository.findByIdWithMappingParameters(parameterId)
												 .orElseThrow(() -> new RelateParameterNotFoundException(parameterId));

		DcpConfig currentDcp = dcpConfigRepository.findAllByParameterId(parameterId)
												  .stream()
												  .findFirst()
												  .orElseThrow(() -> new RelateDcpConfigNotFoundException(parameterId));

		List<MappingParameterDto> mappingParametersBase = virtualParameterDto.getMappingParameters();
		List<Parameter> mappingParameters = parameterRepository.findByIdIn(toParameterIds(mappingParametersBase));

		Parameter updateData = virtualParameterDto.toEntity();
		parameter.modifyVirtualParameter(updateData,
			mappingParameters,
			toCalculationUsingParameterIdSet(mappingParametersBase));

		if (!currentDcp.getId()
					   .equals(virtualParameterDto.getDcpConfigId())) {
			DcpConfig newDcp = dcpConfigRepository.findById(virtualParameterDto.getDcpConfigId())
												  .orElseThrow(RelateDcpConfigNotFoundException::new);
			currentDcp.removeParameter(parameter);
			newDcp.addParameter(parameter);
			currentDcp = newDcp;
		}

		boolean isCircular = checkVirtualParameterCircularReference(parameter);
		if (isCircular) {
			throw new VirtualParameterRecursiveException();
		}

		List<Long> parameterIds = mappingParameters.stream()
												   .map(Parameter::getId)
												   .toList();
		eventPublisher.publishEvent(new RefreshEvent(parameter.getTool()
															  .getId(), "MODIFY_VIRTUAL_PARAMETER"));
		return VirtualParameterResponseDto.of(parameter, currentDcp, parameterIds);
	}

	public List<ParameterResponseDto> copyParametersByToolIds(
		ParameterCopyRequestDto parameterCopyRequestDto
	) {
		List<ParameterResponseDto> parameterResponseDtos = new ArrayList<>();
		List<Long> targetToolIds = parameterCopyRequestDto.getTargetToolIds();
		List<ParameterCreateRequestDto> parameterCreateRequestDtos = parameterCopyRequestDto.getParameterCreateRequestDtos();
		for (Long targetToolId : targetToolIds) {
			List<ParameterCreateRequestDto> parameterDtos = parameterCreateRequestDtos;

			parameterDtos.stream()
						 .forEach(v -> v.setToolId(targetToolId));

			Tool tool = toolRepository.findById(targetToolId)
									  .orElseThrow(RelateToolNotFoundException::new);
			for (ParameterCreateRequestDto parameterDto : parameterDtos) {

				Optional<Parameter> optionalParameter = parameterRepository.findByToolIdAndName(tool.getId(),
					parameterDto.getName());
				if (optionalParameter.isPresent()) {
					throw new ParameterNameDuplicationException(parameterDto.getName());
				}
			}
			List<Parameter> parameters = parameterDtos.stream()
													  .map(v -> v.toEntity(tool))
													  .toList();

			List<ParameterResponseDto> responseDtos = parameterRepository.saveAll(parameters)
																		 .stream()
																		 .map(ParameterResponseDto::from)
																		 .toList();
			parameterResponseDtos.addAll(responseDtos);
		}
		return parameterResponseDtos;
	}

	public List<ParameterExtraDataResponseDto> getParameterExtraDataList(List<Long> parameterIds) {
		return parameterExtraDataRepository.findByParameterIdIn(parameterIds)
										   .stream()
										   .map(ParameterExtraDataResponseDto::from)
										   .toList();
	}

	public List<BaseTreeItem> getToolParameterTree() {
		List<Tool> tools = toolRepository.findAllWithLocation(Sort.by(Sort.Direction.ASC, "createAt"));
		List<BaseTreeItem> toolItems = tools.stream()
											.map(t -> new BaseTreeItem.Builder(t.getId(),
												t.getName(),
												null).type("TOOL")
													 .parentType(null)
													 .build())
											.collect(Collectors.toList());

		List<Parameter> parameters = parameterRepository.findAll(Sort.by(Sort.Direction.ASC, "createAt"));
		List<BaseTreeItem> paramItems = parameters.stream()
												  .map(p -> new BaseTreeItem.Builder(p.getId(),
													  p.getName(),
													  p.getTool()
													   .getId()).type("PARAMETER")
																.parentType("TOOL")
																.build())
												  .collect(Collectors.toList());

		toolItems.addAll(paramItems);

		return TreeUtil.generateTreeHierarchy(toolItems);
	}

	public List<ParameterToolResponseDto> getToolsByParameters(List<Long> parameterIds) {
		return parameterRepository.findByIdInWithToolLocation(parameterIds)
								  .stream()
								  .map(ParameterToolResponseDto::from)
								  .toList();
	}

	/**
	 * 파라미터 타입이 METE_DATA인 값 수정
	 */
	public ParameterResponseDto modifyMetaDataParameter(
		Long parameterId,
		MetaDataParameterUpdateRequestDto requestDto
	) {
		Parameter parameter = parameterRepository.findByIdAndType(parameterId, Parameter.Type.META_DATA)
												 .orElseThrow(ParameterNotFoundException::new);

		// 타입이 META_DATA가 아니면 에러
		if (parameter.getType() != Parameter.Type.META_DATA) {
			throw new MetaDataInvalidTypeException();
		}

		// Parameter에 설정된 데이터 타입으로 검증
		validateMetaDataValue(parameter.getDataType(), requestDto.getValue());

		parameter.modifyMetaParameterValue(requestDto.getValue());

		eventPublisher.publishEvent(new RefreshEvent(parameter.getTool()
															  .getId(), "MODIFY_META_DATA"));
		eventPublisher.publishEvent(ParameterEventMessage.modifyEvent(parameter.getId()));

		return ParameterResponseDto.from(parameter);
	}

	/**
	 * 메타데이터 값이 지정된 데이터 타입과 일치하는지 검증
	 *
	 * @param dataType 파라미터에 정의된 데이터 타입
	 * @param value    검증할 값
	 */
	private void validateMetaDataValue(
		Parameter.DataType dataType,
		String value
	) {
		// null이거나 빈 문자열인 경우
		if (value == null || value.isEmpty()) {
			throw new MetaDataEmptyValueException(dataType.toString());
		}

		try {
			switch (dataType) {
				case DOUBLE -> Double.parseDouble(value);
				case INTEGER -> Integer.parseInt(value);
				case STRING -> {
					// STRING 타입은 이미 위에서 empty 체크를 했으므로 추가 검증 불필요
				}
				default -> throw new MetaDataTypeMismatchException("지원하지 않는 타입: " + dataType);
			}
		} catch (NumberFormatException e) {
			throw new MetaDataTypeMismatchException(String.format("%s 타입에 맞지 않는 값: %s", dataType, value));
		}
	}

	/**
	 * 메타데이터 타입 파라미터들의 값 일괄 수정
	 * 전체 성공 또는 전체 실패 (트랜잭션)
	 *
	 * @param requestList 수정할 파라미터 ID와 값 목록
	 */
	@Transactional
	public void modifyMetaDataParametersBulk(List<MetaDataParameterBulkUpdateRequestDto> requestList) {
		// 1. ID 목록 추출
		List<Long> parameterIds = requestList.stream()
											 .map(MetaDataParameterBulkUpdateRequestDto::getId)
											 .toList();

		// 2. META_DATA 타입 파라미터들만 조회 (한 번에)
		List<Parameter> parameters = parameterRepository.findByIdInAndType(parameterIds, Parameter.Type.META_DATA);

		// 3. ID로 빠른 조회를 위한 Map 생성
		Map<Long, Parameter> parameterMap = parameters.stream()
													  .collect(Collectors.toMap(Parameter::getId, Function.identity()));

		// 4. 요청된 파라미터가 모두 존재하는지 확인 (META_DATA 타입인지도 포함)
		List<Long> notFoundIds = parameterIds.stream()
											 .filter(id -> !parameterMap.containsKey(id))
											 .toList();

		if (!notFoundIds.isEmpty()) {
			// 첫 번째 파라미터 ID로 예외 발생
			throw new ParameterNotFoundException(notFoundIds.get(0));
		}

		// 5. 각 파라미터 값 검증 및 수정
		List<Parameter> modifiedParameters = new ArrayList<>();
		Set<Long> affectedToolIds = new HashSet<>();

		for (MetaDataParameterBulkUpdateRequestDto request : requestList) {
			Parameter parameter = parameterMap.get(request.getId());

			// 데이터 타입 검증
			validateMetaDataValue(parameter.getDataType(), request.getValue());

			// 값 수정
			parameter.modifyMetaParameterValue(request.getValue());

			modifiedParameters.add(parameter);
			affectedToolIds.add(parameter.getTool()
										 .getId());
		}

		// 6. 이벤트 발행 (영향받은 Tool별로)
		for (Long toolId : affectedToolIds) {
			eventPublisher.publishEvent(new RefreshEvent(toolId, "MODIFY_META_DATA_BULK"));
		}

		// 7. 각 파라미터에 대한 이벤트 발행
		modifiedParameters.forEach(param -> eventPublisher.publishEvent(ParameterEventMessage.modifyEvent(param.getId())));
	}

}
