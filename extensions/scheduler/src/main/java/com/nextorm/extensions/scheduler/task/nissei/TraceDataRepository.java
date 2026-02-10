package com.nextorm.extensions.scheduler.task.nissei;

import java.util.List;
import java.util.Optional;

/**
 * 데이터베이스 작업을 추상화한 인터페이스
 */
public interface TraceDataRepository {
	/**
	 * 특정 툴의 최신 trace raw 데이터를 조회
	 *
	 * @param toolName 툴명
	 * @return 최신 trace raw 데이터, 없으면 empty
	 */
	Optional<TraceRaw> findLatestTraceRaw(String toolName);

	/**
	 * 히스토리 테이블에 여러 데이터를 일괄 삽입
	 *
	 * @param parameterId  파라미터 ID
	 * @param shotDataList 삽입할 샷 데이터 리스트
	 */
	void insertAllToHistory(
		Long parameterId,
		List<ShotData> shotDataList
	);

	/**
	 * raw 테이블에 마지막 데이터를 삽입
	 *
	 * @param parameterId 파라미터 ID
	 * @param lastShot    마지막 샷 데이터
	 */
	void insertLastToRaw(
		Long parameterId,
		ShotData lastShot
	);

	/**
	 * raw 테이블의 데이터를 업데이트
	 *
	 * @param parameterId 파라미터 ID
	 * @param lastShot    업데이트할 샷 데이터
	 */
	void updateLastToRaw(
		Long parameterId,
		ShotData lastShot
	);
}

