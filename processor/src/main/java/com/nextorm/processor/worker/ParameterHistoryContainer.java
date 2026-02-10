package com.nextorm.processor.worker;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
public class ParameterHistoryContainer {
	private static final int DEFAULT_MAX_HISTORY_SIZE = 10;

	private final Map<Long, ConcurrentSkipListMap<LocalDateTime, Object>> parameterContainer = new HashMap<>();
	private final int maxHistorySize;

	public ParameterHistoryContainer() {
		this(DEFAULT_MAX_HISTORY_SIZE);
	}

	public ParameterHistoryContainer(int maxHistorySize) {
		if (maxHistorySize <= 0) {
			throw new IllegalArgumentException("maxHistorySize는 0보다 커야 합니다");
		}
		this.maxHistorySize = maxHistorySize;
	}

	public List<Long> getContainsParameterIds() {
		return List.copyOf(parameterContainer.keySet());
	}

	public ConcurrentSkipListMap<LocalDateTime, Object> getParameterHistory(Long parameterId) {
		return parameterContainer.getOrDefault(parameterId, new ConcurrentSkipListMap<>(Comparator.reverseOrder()));
	}

	/**
	 * globalParameterHistoriesMap에 모든 파라미터 값을 저장
	 *
	 * @param paramValueMap: key: parameterId, value: parameterValue
	 * @param traceAt
	 */
	public void putAll(
		Map<Long, Object> paramValueMap,
		LocalDateTime traceAt
	) {
		for (Map.Entry<Long, Object> entry : paramValueMap.entrySet()) {
			put(entry.getKey(), traceAt, entry.getValue());
		}
	}

	public void put(
		Long parameterId,
		LocalDateTime traceAt,
		Object value
	) {
		long s = System.currentTimeMillis();
		ConcurrentSkipListMap<LocalDateTime, Object> container = parameterContainer.computeIfAbsent(parameterId,
			k -> new ConcurrentSkipListMap<>(Comparator.reverseOrder()));
		container.put(traceAt, value);

		if (container.size() > maxHistorySize) {
			int diff = container.size() - maxHistorySize;
			for (int i = 0; i < diff; i++) {
				container.pollLastEntry();
			}
		}

		log.info("putGlobalParameterMap: {}ms, id: {}, traceAt: {}, value: {}, size: {}",
			System.currentTimeMillis() - s,
			parameterId,
			traceAt,
			value,
			container.size());
	}
}
