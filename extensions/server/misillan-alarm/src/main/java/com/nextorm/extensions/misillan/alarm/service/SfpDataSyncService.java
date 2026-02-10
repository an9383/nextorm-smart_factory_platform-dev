package com.nextorm.extensions.misillan.alarm.service;

import com.nextorm.extensions.misillan.alarm.client.SfpPortalClient;
import com.nextorm.extensions.misillan.alarm.entity.SfpParameter;
import com.nextorm.extensions.misillan.alarm.entity.SfpParameter.DataType;
import com.nextorm.extensions.misillan.alarm.repository.SfpParameterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SfpDataSyncService {
	private final SfpPortalClient sfpPortalClient;
	private final SfpParameterRepository sfpParameterRepository;

	@Transactional
	public void syncParameter() {
		List<SfpPortalClient.SfpParameterDto> getParameters = getParametersWithKoreanNames();

		Map<Long, SfpParameter> currentSystemParameters = sfpParameterRepository.findAll()
																				.stream()
																				.collect(Collectors.toMap(SfpParameter::getId,
																					param -> param));

		// 기존에 저장된 것이 있는지 확인
		// 있으면 업데이트 시간을 보고 변경된 것 같으면 갱신처리
		getParameters.forEach(param -> {
			SfpParameter currentSystemParameter = currentSystemParameters.get(param.getId());

			DataType dataType = DataType.valueOf(param.getDataType());
			SfpParameter convertedParam = SfpParameter.builder()
													  .id(param.getId())
													  .name(param.getName())
													  .dataType(dataType)
													  .updatedAt(param.getUpdatedAt())
													  .build();

			if (currentSystemParameter == null) {
				sfpParameterRepository.save(convertedParam);
			} else if (currentSystemParameter.getUpdatedAt()
											 .isBefore(param.getUpdatedAt())) {
				currentSystemParameter.modify(convertedParam);
			}
		});

		// TODO:: SFP에서 삭제된 파라미터에 대한 싱크 처리가 필요함
	}

	/**
	 * 파라미터 목록을 가져와서 한국어 이름으로 변경한 후 반환
	 *
	 * @return 한국어 이름이 적용된 SfpParameter 목록
	 */
	public List<SfpPortalClient.SfpParameterDto> getParametersWithKoreanNames() {
		List<SfpPortalClient.SfpParameterDto> parameters = sfpPortalClient.getParameters();
		if (parameters.isEmpty()) {
			log.warn("조회된 파라미터가 없습니다.");
			return List.of();
		}

		// 3. 한국어 이름으로 변경
		return updateParameterNamesWithKorean(parameters);
	}

	/**
	 * i18n 정보를 이용하여 파라미터 이름을 한국어로 변경하는 메소드
	 * - dataType 필드의 값이 enum에 정의되지 않은 값일 경우 파싱하지 말고 버린다.
	 *
	 * @param parameters 파라미터 목록
	 * @return 한국어 이름이 적용된 파라미터 목록
	 */
	public List<SfpPortalClient.SfpParameterDto> updateParameterNamesWithKorean(List<SfpPortalClient.SfpParameterDto> parameters) {
		try {
			log.info("i18n 정보를 조회하여 파라미터 이름을 한국어로 변경합니다.");

			Map<String, Map<String, String>> i18nData = sfpPortalClient.getI18nData();
			if (i18nData.isEmpty()) {
				log.warn("i18n 정보 응답이 비어있습니다. 원본 이름을 그대로 사용합니다.");
				return parameters;
			}

			Map<String, String> koreanTranslations = extractKoreanParameterTranslations(i18nData);

			// 파라미터 이름을 한국어로 변경
			List<SfpPortalClient.SfpParameterDto> updatedParameters = parameters.stream()
																				.map(param -> {
																					String koreanName = koreanTranslations.get(
																						param.getName());
																					if (koreanName != null && !koreanName.trim()
																														 .isEmpty()) {
																						param.setName(koreanName);
																						log.debug("파라미터 이름 변경: {} -> {}",
																							param.getName(),
																							koreanName);
																					}
																					return param;
																				})
																				.toList();

			log.info("파라미터 이름 한국어 변경 완료.");
			return updatedParameters;

		} catch (Exception e) {
			log.error("파라미터 이름 한국어 변경 중 오류가 발생했습니다.", e);
			return parameters; // 오류 시 원본 파라미터 목록 반환
		}
	}

	/**
	 * i18n 데이터에서 파라미터의 한국어 번역만 추출
	 *
	 * @param i18nData
	 * @return
	 */
	private Map<String, String> extractKoreanParameterTranslations(Map<String, Map<String, String>> i18nData) {
		return i18nData.entrySet()
					   .stream()
					   .filter(entry -> entry.getKey()
											 .startsWith("param."))
					   .filter(entry -> entry.getValue()
											 .getOrDefault("ko", null) != null)
					   .map(entry -> {
						   String parameterName = entry.getKey()
													   .replaceFirst("param\\.", "");
						   String koreanName = entry.getValue()
													.get("ko");
						   return Map.entry(parameterName, koreanName);
					   })
					   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
