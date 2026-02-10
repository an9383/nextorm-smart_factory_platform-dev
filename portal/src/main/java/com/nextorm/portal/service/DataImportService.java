package com.nextorm.portal.service;

import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.DataImportBulkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataImportService {
	private final DataImportBulkRepository dataImportBulkRepository;

	public void saveBulk(List<ParameterData> parameterDataList) {
		dataImportBulkRepository.saveAll(parameterDataList);
	}
}
