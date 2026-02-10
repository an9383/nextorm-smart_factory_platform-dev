package com.nextorm.processor.parametercontainer;

import com.nextorm.processor.model.ParameterModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParameterContainer {
	private final ParameterQueryService parameterQueryService;

	private final Map<DcpId, List<ParameterModel>> dcpParameterMap = new HashMap<>();
	private final Map<Long, List<ParameterModel>> toolParameterMap = new HashMap<>();
	private final Map<Long, ParameterModel> parameterMap = new HashMap<>();

	public void initParametersByToolId(
		Long toolId,
		List<Long> dcpConfigIds
	) {
		Parameters parameters = new Parameters(parameterQueryService.findParametersByToolIdOrderByOrderAsc(toolId));

		List<ParameterModel> sortedParameters = new ArrayList<>();
		sortedParameters.addAll(parameters.getCollectParameters());
		sortedParameters.addAll(sortVirtualParametersByDependencyHierarchy(parameters.getVirtualParameters()));

		toolParameterMap.put(toolId, sortedParameters);

		for (ParameterModel parameter : sortedParameters) {
			parameterMap.put(parameter.getId(), parameter);
		}

		for (Long dcpId : dcpConfigIds) {
			Set<Long> dcpParameterIdsSet = new HashSet<>(parameterQueryService.findParameterIdsByDcpId(dcpId));

			List<ParameterModel> dcpParameters = sortedParameters.stream()
																 .filter(parameter -> dcpParameterIdsSet.contains(
																	 parameter.getId()))
																 .toList();

			if (dcpParameterIdsSet.size() != dcpParameters.size()) {
				throw new IllegalStateException("DcpId: " + dcpId + "에 해당하는 파라미터가 모두 존재하지 않습니다.");
			}
			dcpParameterMap.put(DcpId.of(dcpId), dcpParameters);
		}
	}

	public Parameters getParametersByToolId(Long toolId) {
		return new Parameters(toolParameterMap.get(toolId));
	}

	public Parameters getParametersByDcpId(DcpId key) {
		return new Parameters(dcpParameterMap.get(key));
	}

	public ParameterModel getParameterById(Long parameterId) {
		return parameterMap.get(parameterId);
	}

	/**
	 * toolId에 해당하는 모든 파라미터를 업데이트한다.
	 */
	public void updateParametersByToolId(Long toolId) {
		List<Long> dcpConfigIds = parameterQueryService.findDcpIdsByToolId(toolId);
		initParametersByToolId(toolId, dcpConfigIds);
	}

	/**
	 * 가상 파라미터 목록을 의존성이 적은 순서대로 정렬한다.
	 *
	 * @param virtualParameters: 정렬할 가상 파라미터 목록
	 * @return 의존성 적은 순서대로 정렬된 가상 파라미터 목록
	 */
	public List<ParameterModel> sortVirtualParametersByDependencyHierarchy(List<ParameterModel> virtualParameters) {
		Map<Long, ParameterModel> paramMap = new HashMap<>();
		Map<Long, List<Long>> graph = new HashMap<>();
		Map<Long, Integer> inDegree = new HashMap<>();

		// 그래프 및 진입 차수 맵 구축
		for (ParameterModel param : virtualParameters) {
			Long id = param.getId();
			paramMap.put(id, param);
			graph.putIfAbsent(id, new ArrayList<>());
			inDegree.putIfAbsent(id, 0);

			for (Long depId : param.getCalculationRequiredVirtualParameterIds()) {
				graph.putIfAbsent(depId, new ArrayList<>());
				graph.get(depId)
					 .add(id);
				inDegree.put(id, inDegree.getOrDefault(id, 0) + 1);
			}
		}

		// Kahn의 알고리즘을 사용한 위상 정렬
		Queue<Long> queue = new LinkedList<>();
		for (Map.Entry<Long, Integer> entry : inDegree.entrySet()) {
			if (entry.getValue() == 0) {
				queue.add(entry.getKey());
			}
		}

		List<ParameterModel> sortedList = new ArrayList<>();
		while (!queue.isEmpty()) {
			Long currentId = queue.poll();
			sortedList.add(paramMap.get(currentId));

			for (Long neighbor : graph.get(currentId)) {
				inDegree.put(neighbor, inDegree.get(neighbor) - 1);
				if (inDegree.get(neighbor) == 0) {
					queue.add(neighbor);
				}
			}
		}

		// 사이클이 있는지 확인
		if (sortedList.size() != virtualParameters.size()) {
			throw new RuntimeException("순환참조가 감지되었습니다! 정렬할 수 없습니다.");
		}

		return sortedList;
	}
}
