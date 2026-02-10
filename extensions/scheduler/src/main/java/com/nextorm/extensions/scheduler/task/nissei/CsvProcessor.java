package com.nextorm.extensions.scheduler.task.nissei;

import java.util.List;

/**
 * CSV 파일 처리를 추상화한 인터페이스
 */
public interface CsvProcessor {
	/**
	 * CSV 파일을 읽어서 ShotData 리스트로 변환
	 *
	 * @param directoryPath CSV 파일이 있는 디렉토리 경로
	 * @param fileName      CSV 파일명
	 * @return 읽어온 ShotData 리스트
	 */
	List<ShotData> readCsvFile(
		String directoryPath,
		String fileName
	);
}
