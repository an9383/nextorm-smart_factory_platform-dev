package com.nextorm.portal.service.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static com.nextorm.portal.service.migration.MigrationTestUtils.createNoSpecParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MigrationDataParserTest {
	MigrationDataParser parser = new MigrationDataParser();

	@DisplayName("csv 파일 파싱한 결과 리스트가 파라미터 개수와 같다")
	@Test
	void result_length_eq_parameter_size() throws IOException {
		// given
		CsvHeaderNameParameterMap csvHeaderNameParameterMap = new CsvHeaderNameParameterMap();
		csvHeaderNameParameterMap.addMapping("latitude", createNoSpecParameter(1L, "latitude"));
		csvHeaderNameParameterMap.addMapping("solar_voltage", createNoSpecParameter(2L, "태양전압"));

		ClassPathResource resource = new ClassPathResource(MigrationTestUtils.SMALL_CSV_PATH);

		// when
		List<MigrationDataWrapper> result = parser.parse(csvHeaderNameParameterMap,
			resource.getInputStream(),
			MigrationDataParser.recordToMigrationBase);

		// then
		assertThat(result).hasSize(2);
	}

	@DisplayName("csv 파일 파싱한 결과의 데이터 개수가 파일과 같다")
	@Test
	void result_data_length_eq_csv_data_length() throws IOException {
		// given
		CsvHeaderNameParameterMap csvHeaderNameParameterMap = new CsvHeaderNameParameterMap();
		csvHeaderNameParameterMap.addMapping("latitude", createNoSpecParameter(1L, "latitude"));
		csvHeaderNameParameterMap.addMapping("solar_voltage", createNoSpecParameter(2L, "태양전압"));

		ClassPathResource resource = new ClassPathResource(MigrationTestUtils.SMALL_CSV_PATH);

		// when
		List<MigrationDataWrapper> result = parser.parse(csvHeaderNameParameterMap,
			resource.getInputStream(),
			MigrationDataParser.recordToMigrationBase);

		// then
		assertThat(result.get(0)
						 .getParameterDataList()).hasSize(MigrationTestUtils.SMALL_DATA_SIZE);
		assertThat(result.get(1)
						 .getParameterDataList()).hasSize(MigrationTestUtils.SMALL_DATA_SIZE);
	}

	@DisplayName("csv 파일의 시간 문자열이 잘못 되어있으면 예외가 발생한다")
	@Test
	void invalid_time_string_exception() {
		// given
		CsvHeaderNameParameterMap csvHeaderNameParameterMap = new CsvHeaderNameParameterMap();
		csvHeaderNameParameterMap.addMapping("latitude", createNoSpecParameter(1L, "latitude"));
		csvHeaderNameParameterMap.addMapping("solar_voltage", createNoSpecParameter(2L, "태양전압"));

		ClassPathResource resource = new ClassPathResource("csv/invalid_time_string_test.csv");

		// then
		assertThatThrownBy(() -> parser.parse(csvHeaderNameParameterMap,
			resource.getInputStream(),
			MigrationDataParser.recordToMigrationBaseWithoutGeoData)).isInstanceOf(IllegalArgumentException.class)
																	 .hasMessage("날짜 형식이 잘못되었습니다");
	}
}