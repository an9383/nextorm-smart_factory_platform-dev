package com.nextorm.processor.service;

import com.nextorm.common.db.entity.DcpConfig;
import com.nextorm.common.db.entity.ToolKafka;
import com.nextorm.common.db.repository.DcpConfigRepository;
import com.nextorm.common.db.repository.ToolKafkaRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.processor.common.exception.toolkafka.ToolKafkaNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class HandlerService {
	private final ToolRepository toolRepository;
	private final DcpConfigRepository dcpConfigRepository;
	private final ToolKafkaRepository toolKafkaRepository;

	public List<ProcessingToolInfo> getProcessingToolInfos(String processConfigName) {
		return toolRepository.findAllByProcessConfigName(processConfigName)
							 .stream()
							 .map(tool -> {
								 List<DcpConfig> dcpConfigs = dcpConfigRepository.findByToolId(tool.getId());
								 if (dcpConfigs.isEmpty()) {
									 return null;
								 }
								 ToolKafka toolKafka = toolKafkaRepository.findByToolId(tool.getId())
																		  .orElseThrow(ToolKafkaNotFoundException::new);

								 List<Long> dcpIds = dcpConfigs.stream()
															   .map(DcpConfig::getId)
															   .toList();
								 return new ProcessingToolInfo(tool.getId(),
									 tool.getName(),
									 toolKafka.getBootstrapServer(),
									 toolKafka.getTopic(),
									 dcpIds);
							 })
							 .filter(Objects::nonNull)
							 .toList();
	}
}
