package com.nextorm.extensions.scheduler.task.nissei;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 파일 처리의 실제 구현체
 */
@Slf4j
public class CsvProcessorImpl implements CsvProcessor {

	private static final List<Charset> SUPPORTED_CHARSETS = List.of(Charset.forName("EUC-KR"),
		StandardCharsets.UTF_8,
		Charset.forName("CP949"),
		Charset.forName("MS949"),
		StandardCharsets.ISO_8859_1);

	@Override
	public List<ShotData> readCsvFile(
		String directoryPath,
		String fileName
	) {
		Path csvFilePath = Paths.get(directoryPath, fileName);
		log.debug("CSV 파일 읽기 시작: {}", csvFilePath);

		try {
			return readCsvWithEncodingFallback(csvFilePath).stream()
														   .map(this::toShotData)
														   .toList();
		} catch (IOException e) {
			throw new CsvProcessingException("CSV 파일 읽기에 실패하였습니다: " + csvFilePath, e);
		}
	}

	private ShotData toShotData(CsvRecord csvRecord) {
		LocalDate date = LocalDate.parse(csvRecord.getField(0), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		LocalTime time = LocalTime.parse(csvRecord.getField(1));
		int shotCount = Integer.parseInt(csvRecord.getField(2));
		return new ShotData(date, time, shotCount);
	}

	private List<CsvRecord> readCsvWithEncodingFallback(Path csvFilePath) throws IOException {
		for (Charset charset : SUPPORTED_CHARSETS) {
			try {
				log.debug("인코딩 시도: {}", charset.displayName());
				return readCsvWithCharset(csvFilePath, charset);
			} catch (MalformedInputException | UncheckedIOException e) {
				log.debug("인코딩 {} 실패: {}", charset.displayName(), e.getMessage());
			}
		}

		throw new IOException("지원하는 모든 인코딩으로 CSV 파일 읽기에 실패했습니다: " + csvFilePath);
	}

	private List<CsvRecord> readCsvWithCharset(
		Path csvFilePath,
		Charset charset
	) throws IOException {
		List<CsvRecord> rows = new ArrayList<>();

		try (var reader = CsvReader.builder()
								   .skipEmptyLines(true)
								   .ofCsvRecord(Files.newBufferedReader(csvFilePath, charset))) {
			// 헤더 라인 스킵
			reader.skipLines(1);
			for (CsvRecord csvRecord : reader) {
				rows.add(csvRecord);
			}
		}

		log.debug("인코딩 {} 성공: {} 줄 읽음", charset.displayName(), rows.size());
		return rows;
	}
}
