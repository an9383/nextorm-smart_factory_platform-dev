package com.nextorm.portal.service.processoptimization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nextorm.portal.common.exception.ConstraintViloationException;
import com.nextorm.portal.common.exception.processoptimizationanalysis.ProcessOptimizationAnalysisNotFoundException;
import com.nextorm.portal.dto.common.ConstraintViloationDto;
import com.nextorm.portal.entity.processoptimization.ProcessOptimizationAnalysis;
import com.nextorm.portal.repository.processoptimization.ProcessOptimizationAnalysisRepository;
import com.nextorm.portal.repository.processoptimization.ProcessOptimizationRepository;
import com.nextorm.portal.util.GZipUtility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProcessOptimizationAnalysisService {

	private final ProcessOptimizationAnalysisRepository processOptimizationAnalysisRepository;
	private final ProcessOptimizationRepository processOptimizationRepository;
	private final ObjectProvider<ProcessOptimizationAnalysisService> selfProvider;

	public ProcessOptimizationAnalysis save(ProcessOptimizationAnalysis analysis) throws Exception {
		return processOptimizationAnalysisRepository.save(analysis);
	}

	public void deleteAnalysisByProcessOptimizationId(Long id) throws ConstraintViloationException {
		Optional<ProcessOptimizationAnalysis> findAnalysis = processOptimizationAnalysisRepository.findByProcessOptimizationId(
			id);
		if (findAnalysis.isPresent()) {
			ProcessOptimizationAnalysis analysis = findAnalysis.get();
			processOptimizationAnalysisRepository.delete(analysis);
		}
	}

	public void deleteAnalyses(List<Long> ids) throws ConstraintViloationException {
		List<ConstraintViloationDto> constraintDatas = new ArrayList<>();
		for (Long id : ids) {
			try {
				this.deleteAnalysisByProcessOptimizationId(id);
			} catch (ConstraintViloationException e) {
				constraintDatas.addAll(e.getData());
			}
		}
		if (constraintDatas.size() > 0) {
			throw new ConstraintViloationException(constraintDatas);
		}
	}

	public List<Map<String, Double>> getOptimalYValueList(Long processOptimizationId) throws Exception {
		ProcessOptimizationAnalysis analysis = processOptimizationAnalysisRepository.findByProcessOptimizationId(
																						processOptimizationId)
																					.orElseThrow(
																						ProcessOptimizationAnalysisNotFoundException::new);
		byte[] data = analysis.getAnalysisData();
		List<Map<String, Double>> dataList = GZipUtility.unzipByteToJsonObject(data,
			new TypeReference<List<Map<String, Double>>>() {
			});
		double targetValue = analysis.getProcessOptimization()
									 .getTargetValue();

		return dataList.stream()
					   .sorted(Comparator.comparingDouble(map -> Math.abs(map.get("y") - targetValue)))
					   .limit(10)
					   .collect(Collectors.toList());
	}
}
