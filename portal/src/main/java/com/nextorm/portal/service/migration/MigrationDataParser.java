package com.nextorm.portal.service.migration;

import com.nextorm.common.db.entity.Parameter;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MigrationDataParser {
	private static final int TIME_STRING_INDEX = 0;
	private static final int LATITUDE_INDEX = 1;
	private static final int LONGITUDE_INDEX = 2;

	private static final DateTimeFormatter FORMATTER_WITH_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static final Function<NamedCsvRecord, MigrationBase> recordToMigrationBaseWithoutGeoData = (csvRecord -> new MigrationBase(
		parseDateTime(csvRecord.getField(TIME_STRING_INDEX))));

	public static final Function<NamedCsvRecord, MigrationBase> recordToMigrationBase = (csvRecord -> {
		LocalDateTime traceAt = parseDateTime(csvRecord.getField(TIME_STRING_INDEX));
		Double latitude = Double.parseDouble(csvRecord.getField(LATITUDE_INDEX));
		Double longitude = Double.parseDouble(csvRecord.getField(LONGITUDE_INDEX));
		return new MigrationBase(traceAt, latitude, longitude);
	});

	private static LocalDateTime parseDateTime(String dateTimeString) {
		try {
			return toLocalDateTime(dateTimeString);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("날짜 형식이 잘못되었습니다", e.getCause());
		}
	}

	private static LocalDateTime toLocalDateTime(String dateString) {
		try {
			return LocalDateTime.parse(dateString, FORMATTER_WITH_MILLIS);
		} catch (DateTimeParseException e) {
			return LocalDateTime.parse(dateString, FORMATTER);
		}
	}

	public List<MigrationDataWrapper> parse(
		CsvHeaderNameParameterMap headerNameParameterMap,
		InputStream inputStream,
		Function<NamedCsvRecord, MigrationBase> recordToMigrationBaseFunction
	) {
		Map<Long, MigrationDataWrapper> wrapperMap = new HashMap<>();
		try (CsvReader<NamedCsvRecord> namedCsvRecords = ofNamedCsvRecord(inputStream)) {
			for (NamedCsvRecord csvRecord : namedCsvRecords) {
				MigrationBase migrationBase = recordToMigrationBaseFunction.apply(csvRecord);
				parseAndAppendMigrationData(csvRecord, migrationBase, headerNameParameterMap, wrapperMap);
			}
		} catch (IOException e) {
			throw new CsvParsingException(e);
		}

		return wrapperMap.values()
						 .stream()
						 .toList();
	}

	private void parseAndAppendMigrationData(
		NamedCsvRecord csvRecord,
		MigrationBase migrationBase,
		CsvHeaderNameParameterMap headerNameParameterMap,
		Map<Long, MigrationDataWrapper> wrapperMap
	) {
		for (String header : headerNameParameterMap.getHeaders()) {
			Parameter parameter = headerNameParameterMap.getParameter(header);
			MigrationDataWrapper wrapper = getOrCreateWrapper(wrapperMap, parameter);

			String value = csvRecord.getField(header);
			wrapper.addData(MigrationData.of(value,
				migrationBase.getTraceAt(),
				migrationBase.getLatitude(),
				migrationBase.getLongitude()));
		}
	}

	private CsvReader<NamedCsvRecord> ofNamedCsvRecord(InputStream inputStream) {
		return CsvReader.builder()
						.ofNamedCsvRecord(new InputStreamReader(inputStream));
	}

	private MigrationDataWrapper getOrCreateWrapper(
		Map<Long, MigrationDataWrapper> wrapperMap,
		Parameter parameter
	) {
		return wrapperMap.computeIfAbsent(parameter.getId(), k -> new MigrationDataWrapper(parameter));
	}
}
