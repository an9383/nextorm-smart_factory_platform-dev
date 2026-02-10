package com.nextorm.processor.parametercontainer;

import com.nextorm.common.db.entity.DcpConfig;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.repository.DcpConfigRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.processor.model.ParameterModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ParameterQueryService {
	private final DcpConfigRepository dcpConfigRepository;
	private final ParameterRepository parameterRepository;

	public List<ParameterModel> findParametersByToolIdOrderByOrderAsc(Long toolId) {
		return parameterRepository.findByToolId(toolId)
								  .stream()
								  .sorted(Comparator.comparing(Parameter::getOrder))
								  .map(ParameterModel::of)
								  .toList();
	}

	public List<Long> findParameterIdsByDcpId(Long dcpId) {
		return dcpConfigRepository.findById(dcpId)
								  .map(dcpConfig -> dcpConfig.getParameters()
															 .stream()
															 .map(Parameter::getId)
															 .toList())
								  .orElseThrow(() -> new IllegalArgumentException("해당하는 DcpConfig가 없습니다. DcpId: " + dcpId));

	}

	public List<Long> findDcpIdsByToolId(Long toolId) {
		return dcpConfigRepository.findByToolId(toolId)
								  .stream()
								  .map(DcpConfig::getId)
								  .toList();
	}
}
