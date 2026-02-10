package com.nextorm.portal.service;

import com.nextorm.common.db.entity.UiLanguage;
import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.repository.CollectorDefineRepository;
import com.nextorm.common.db.repository.UiLanguageRepository;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import com.nextorm.portal.constant.CodeCategory;
import com.nextorm.portal.dto.meta.CollectorTypeMetaResponseDto;
import com.nextorm.portal.dto.meta.LanguageSetResponseDto;
import com.nextorm.portal.dto.system.SystemInfoDto;
import com.nextorm.portal.entity.system.SystemInfo;
import com.nextorm.portal.repository.system.SystemInfoRepository;
import com.nextorm.portal.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetaDataService {
	private final CollectorDefineRepository collectorDefineRepository;
	private final UiLanguageRepository uiLanguageRepository;
	private final CodeRepository codeRepository;
	private final SystemInfoRepository systemInfoRepository;

	public List<CollectorTypeMetaResponseDto> getCollectorTypes() {
		return collectorDefineRepository.findAll()
										.stream()
										.map(CollectorTypeMetaResponseDto::of)
										.toList();
	}

	public List<String> getI18nLanguages() {
		return codeRepository.findByCategory(CodeCategory.I18N_LANGUAGE_CATEGORY)
							 .stream()
							 .map(Code::getCode)
							 .toList();
	}

	public LanguageSetResponseDto getI18nMessages(Locale locale) {
		Map<String, String> messages = uiLanguageRepository.findAllByLang(locale.getLanguage())
														   .stream()
														   .collect(Collectors.toMap(UiLanguage::getKey,
															   UiLanguage::getMessage));
		return new LanguageSetResponseDto(messages);
	}

	public Map<String, String> getSystemInfo() {
		Function<SystemInfo, String> getValueIfLargeValueExists = systemInfo -> systemInfo.getLargeValue() != null
																				? new String(systemInfo.getLargeValue())
																				: systemInfo.getValue();

		return systemInfoRepository.findAll()
								   .stream()
								   .collect(Collectors.toMap(SystemInfo::getKey, getValueIfLargeValueExists));
	}

	@Transactional
	public List<Map<String, String>> modifySystemInfo(List<Map<String, String>> systemInfoMap) {
		List<Map<String, String>> modifyResults = new ArrayList<>();

		List<SystemInfoDto> systemInfoList = new ArrayList<>();
		for (Map<String, String> entry : systemInfoMap) {
			String systemKey = entry.get("key");
			String value = entry.get("value");
			String largeValue = entry.get("largeValue");

			systemInfoList.add(new SystemInfoDto(systemKey, value, largeValue));
		}

		List<SystemInfo> updateSystemInfos = this.handleSystemInfoUpdate(systemInfoList);

		for (SystemInfo systemInfo : updateSystemInfos) {
			Map<String, String> resultMap = new HashMap<>();
			resultMap.put("key", systemInfo.getKey());
			if (systemInfo.getLargeValue() != null) {
				resultMap.put("largeValue", systemInfo.getLargeValue());
			} else {
				resultMap.put("value", systemInfo.getValue());
			}
			modifyResults.add(resultMap);
		}

		return modifyResults;
	}

	private List<SystemInfo> handleSystemInfoUpdate(List<SystemInfoDto> systemInfoDtos) {
		// systemKey 목록 추출
		List<String> systemKeys = systemInfoDtos.stream()
												.map(v -> v.getSystemKey())
												.collect(Collectors.toList());

		// systemKeys로 SystemInfo 객체 find
		List<SystemInfo> existingSystemInfos = systemInfoRepository.findByKeyIn(systemKeys);

		// 기존 데이터들을 Map으로 변환
		// systemKey를 Map의 키로 사용하고, SystemInfo 객체 자체를 값으로 사용
		Map<String, SystemInfo> existingSystemInfoMap = existingSystemInfos.stream()
																		   .collect(Collectors.toMap(SystemInfo::getKey,
																			   Function.identity()));

		List<SystemInfo> systemInfosToSave = new ArrayList<>();

		for (SystemInfoDto dto : systemInfoDtos) {
			byte[] resizedImage = null;
			if (dto.getLargeValue() != null) {
				resizedImage = ImageUtil.resizeBase64Image(dto.getLargeValue(), 500, 0.75f);
			}

			SystemInfo systemInfo = existingSystemInfoMap.get(dto.getSystemKey());
			if (systemInfo == null) {    // 새로운 SystemInfo 객체 생성
				systemInfo = SystemInfo.builder()
									   .key(dto.getSystemKey())
									   .value(dto.getValue())
									   .largeValue(resizedImage)
									   .build();
			} else {
				systemInfo.modify(dto.getValue(), resizedImage);
			}
			systemInfosToSave.add(systemInfo);
		}
		// 모든 SystemInfo 객체를 한 번에 저장
		return systemInfoRepository.saveAll(systemInfosToSave);

	}
}
