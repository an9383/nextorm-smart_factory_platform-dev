package com.nextorm.collector.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;

import java.io.RandomAccessFile;
import java.util.*;

@Slf4j
public class VirtualCsvCollector implements Collector {
	private final ObjectMapper mapper;
	private final DataCollectPlan config;
	private final Sender sender;
	private final boolean isRepeat;
	private final boolean isPastData;
	private List<String> parameterList;
	private final String csvFilePath;
	final int readSize = 100000;
	private String incompleteLine = null;

	public VirtualCsvCollector(
		ObjectMapper mapper,
		DataCollectPlan config,
		Sender sender
	) {
		this.mapper = mapper;
		this.config = config;
		this.sender = sender;
		isRepeat = Boolean.parseBoolean(String.valueOf(config.getCollectorArguments()
															 .get("isRepeat")));
		isPastData = Boolean.parseBoolean(String.valueOf(config.getCollectorArguments()
															   .get("isPastData")));
		csvFilePath = String.valueOf(config.getCollectorArguments()
										   .get("csvFilePath"));
		log.info("isRepeat: {}", isRepeat);
		log.info("isPastData: {}", isPastData);
		log.info("csvFilePath: {}", csvFilePath);
	}

	@Override
	public DataCollectPlan getConfig() {
		return config;
	}

	@Override
	public void run() {
		long totalPos = 0;    //파일 읽기 시작 위치
		boolean isFirstRow = true;

		try (RandomAccessFile raf = new RandomAccessFile(csvFilePath, "r")) {
			while (!Thread.currentThread()
						  .isInterrupted()) {
				List<String> lines = readChunk(raf, totalPos, readSize);

				//읽은 데이터 처리
				for (String line : lines) {
					if (isFirstRow) {
						// 첫 번째 행을 읽어 날짜빼고(첫컬럼) parameterList에 저장
						String[] headers = line.trim()
											   .split(",");
						parameterList = Arrays.asList(Arrays.copyOfRange(headers, 1, headers.length));
						log.info("헤더 읽기 완료: {} ", line);
						isFirstRow = false;
						continue;
					}
					Map<String, Object> data = parseCsvLine(line);
					if (!data.isEmpty()) {
						SendMessage message = paramValueToMessage(data);
						sender.send(config.getTopic(), message);
					}

					try {
						Thread.sleep(config.getDataInterval() * 1000L);
					} catch (InterruptedException e) {
						Thread.currentThread()
							  .interrupt();
					}
				}
				//다음 청크로 이동
				totalPos = raf.getFilePointer();

				//CSV 파일 끝까지 처리되었다면 반복 처리 여부 확인
				if (totalPos >= raf.length()) {

					if (isRepeat) {
						totalPos = 0;
						raf.seek(totalPos);
						isFirstRow = true;
					} else {
						break;
					}
				}
			}
		} catch (Exception e) {
			log.error("CSV 파일 처리 중 에러 발생: {}", csvFilePath, e);
		}
	}

	private SendMessage paramValueToMessage(Map<String, Object> paramsValueMap) {
		int collectParameterSize = config.getCollectParameters()
										 .size();
		if (Boolean.TRUE.equals(config.getIsGeoDataType())) {
			collectParameterSize = collectParameterSize + 2;    // Geo Type 일때 파라미터 size 2 추가
		}
		// if (paramsValueMap.size() - 1 != collectParameterSize) {
		// 	String message = "수집대상 파라미터와 수집된 파라미터의 크기가 일치하지 않습니다. (parameter: %s, paramValue: %s)".formatted(
		// 		collectParameterSize,
		// 		paramsValueMap.size());
		// 	throw new IllegalArgumentException(message);
		// }

		return SendMessage.createMergedMetadataMessage(config, (long)paramsValueMap.get("datetime"), paramsValueMap);
	}

	private List<String> readChunk(
		RandomAccessFile raf,
		long startPos,
		int size
	) throws Exception {
		raf.seek(startPos);

		byte[] buffer = new byte[size];
		int bytesRead = raf.read(buffer);

		if (bytesRead == -1) {
			return List.of();
		}

		String chunk = new String(buffer, 0, bytesRead);

		// 이전 청크에서 잘린 줄이 있다면 현재 청크 앞에 붙여줌
		if (this.incompleteLine != null) {
			chunk = this.incompleteLine + chunk;
			this.incompleteLine = null;
		}

		String[] lines = chunk.split("\n");

		// 마지막 줄이 완전하지 않다면, 다음 청크에서 처리
		List<String> result = new ArrayList<>(List.of(lines));
		if (!chunk.endsWith("\n")) {
			this.incompleteLine = result.remove(result.size() - 1);
		} else {
			this.incompleteLine = null;
		}

		return result;
	}

	public Map<String, Object> parseCsvLine(String line) {
		try {
			String[] csvValues = line.split(",");
			Map<String, Object> result = new LinkedHashMap<>();

			// 첫번째 컬럼 날짜 처리
			long datetime;
			if (isPastData) {
				datetime = parseDate(csvValues[0]);
			} else {
				datetime = System.currentTimeMillis();
			}
			result.put("datetime", datetime);

			//두번째 컬럼부터 parameterList와 매핑
			List<String> parameters = config.getCollectParameters()
											.stream()
											.map(DataCollectPlan.Parameter::getName)
											.toList();
			for (int i = 1; i < csvValues.length && i - 1 < parameterList.size(); i++) {
				String parameterName = parameterList.get(i - 1);

				//parameterList의 i번째 항목이 parameters에 포함되어 있는지 확인
				if (!parameters.contains(parameterName)) {
					log.warn("ParameterList의 항목 '{}'가 parameters에 포함되지 않았습니다. 스킵합니다.", parameterName);
					continue;
				}

				String csvValue = csvValues[i].trim();
				Object value = paseValue(csvValue);
				if (value == null || (value instanceof Double && Double.isNaN((Double)value)) || (value instanceof String && "NaN".equals(
					value))) {
					value = null;
				}

				result.put(parameterName, value);
			}

			return result;
		} catch (Exception e) {
			log.warn("CSV 라인 파싱 실패: {}", line, e);
			return Map.of();
		}
	}

	private Object paseValue(String value) {
		try {
			// 숫자 변환 가능한 경우 Double로 변환
			if (value.matches("-?\\d+(\\.\\d+)?")) {
				return Double.parseDouble(value);
			}
			return value; // 숫자가 아닌 경우 그대로 반환
		} catch (Exception e) {
			log.warn("값 변환 실패: {}", value, e);
			return value; // 실패 시 원본 값 반환
		}
	}

	private long parseDate(String dateStr) {
		String[] dateFormats = {"yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss"};

		for (String format : dateFormats) {
			try {
				// 날짜 포맷에 맞게 파싱 (예: yyyy-MM-dd HH:mm:ss)
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
				return sdf.parse(dateStr)
						  .getTime();
			} catch (Exception e) {
				// log.warn("포맷 불일치: {}, 시도한 포맷: {}", dateStr, format);
			}
		}

		log.error("날짜 변환 실패: {}", dateStr);
		throw new RuntimeException();
	}

}
