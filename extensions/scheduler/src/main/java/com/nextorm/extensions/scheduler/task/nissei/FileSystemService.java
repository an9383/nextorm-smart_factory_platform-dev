package com.nextorm.extensions.scheduler.task.nissei;

import java.util.Optional;

/**
 * 파일 시스템 작업을 추상화한 인터페이스
 */
public interface FileSystemService {
	/**
	 * 주어진 디렉토리에서 가장 최근에 수정된 CSV 파일명을 반환
	 *
	 * @param directoryPath 검색할 디렉토리 경로
	 * @return 가장 최근 CSV 파일명, 없으면 empty
	 */
	Optional<String> findLatestCsvFile(String directoryPath);
}
