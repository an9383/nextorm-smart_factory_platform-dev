package com.nextorm.extensions.scheduler.task.nissei;

import com.nextorm.extensions.scheduler.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Nissei CSV 파일을 읽어서 trace_raw_data 테이블에 적재하는 스케줄러 작업
 *
 * <p>처리 흐름:
 * 1. 최근 trace_raw 데이터 조회
 * 2. 최신 CSV 파일 탐색 및 읽기
 * 3. 초기 실행 시: 모든 데이터를 history와 raw 테이블에 적재
 * 4. 이후 실행 시: 새로운 데이터만 필터링하여 적재 및 업데이트
 */
@Slf4j
@RequiredArgsConstructor
public class NisseiCsvProcessingTask implements Task {

	private final NisseiCsvProcessingTaskProperties properties;
	private final FileSystemService fileSystemService;
	private final CsvProcessor csvProcessor;
	private final TraceDataRepository traceDataRepository;

	@Override
	public void execute() {
		log.debug("Nissei CSV 처리 작업 시작");

		// 1. 최신 trace raw 데이터 조회
		Optional<TraceRaw> latestTraceRaw = traceDataRepository.findLatestTraceRaw(properties.getToolName());
		if (latestTraceRaw.isEmpty()) {
			throw new IllegalStateException("니세이 설비 이름으로 파라미터 정보를 찾을 수 없음. 툴 이름: " + properties.getToolName());
		}

		// 2. 최신 CSV 파일 찾기
		Optional<String> latestCsvFile = fileSystemService.findLatestCsvFile(properties.getCsvFilePath());

		if (latestCsvFile.isEmpty()) {
			log.debug("CSV 파일이 없어 작업을 종료합니다.");
			return;
		}

		// 3. CSV 파일 읽기
		List<ShotData> shotDataList = csvProcessor.readCsvFile(properties.getCsvFilePath(), latestCsvFile.get());

		if (shotDataList.isEmpty()) {
			log.debug("CSV 파일에 데이터가 없어 작업을 종료합니다.");
			return;
		}

		// 4. 데이터 처리 (초기 vs 증분)

		TraceRaw traceRaw = latestTraceRaw.get();
		if (traceRaw.isEmpty()) {
			processInitialData(traceRaw.parameterId(), shotDataList);
		} else {
			processIncrementalData(latestTraceRaw.get(), shotDataList);
		}

		log.debug("Nissei CSV 처리 작업 완료");
	}

	private void processInitialData(
		Long parameterId,
		List<ShotData> shotDataList
	) {
		log.info("초기 데이터 처리 시작 - 전체 데이터: {}", shotDataList.size());

		// 모든 데이터를 히스토리에 삽입
		traceDataRepository.insertAllToHistory(parameterId, shotDataList);

		// 마지막 데이터를 raw에 삽입
		ShotData lastShot = shotDataList.get(shotDataList.size() - 1);
		traceDataRepository.insertLastToRaw(parameterId, lastShot);

		log.info("초기 데이터 처리 완료");
	}

	private void processIncrementalData(
		TraceRaw latestTraceRaw,
		List<ShotData> shotDataList
	) {
		log.debug("증분 데이터 처리 시작 - Latest trace raw: {}", latestTraceRaw);

		// 새로운 데이터만 필터링
		List<ShotData> filteredShotDataList = shotDataList.stream()
														  .filter(shotData -> shotData.traceDt()
																					  .isAfter(latestTraceRaw.traceDt()))
														  .toList();

		if (filteredShotDataList.isEmpty()) {
			log.debug("새로운 데이터가 없습니다.");
			return;
		}

		// 히스토리에 삽입
		traceDataRepository.insertAllToHistory(latestTraceRaw.parameterId(), filteredShotDataList);

		// raw 데이터 업데이트
		ShotData lastShot = filteredShotDataList.get(filteredShotDataList.size() - 1);
		traceDataRepository.updateLastToRaw(latestTraceRaw.parameterId(), lastShot);

		log.info("증분 데이터 처리 완료 - 처리된 데이터: {}", filteredShotDataList.size());
	}
}
