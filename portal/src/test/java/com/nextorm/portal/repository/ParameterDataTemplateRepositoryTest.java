package com.nextorm.portal.repository;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.portal.DataJpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ParameterDataTemplateRepositoryTest extends DataJpaTestBase {
	@Autowired
	ParameterDataRepository parameterDataRepository;

	@DisplayName("스펙 정보가 없는 데이터를 1건 저장한다")
	@Test
	void no_spec_data_insert_1() {
		// given
		Parameter parameter = Parameter.builder()
									   .id(1L)
									   .name("latitude")
									   .dataType(Parameter.DataType.DOUBLE)
									   .isSpecAvailable(false)
									   .build();

		LocalDateTime firstTraceAt = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);

		List<ParameterData> parameterDataList = IntStream.range(0, 2001)
														 .mapToObj(i -> ParameterData.createOf(parameter,
															 33d + (i * 1d),
															 firstTraceAt.plusSeconds(i * 2L),
															 0d + (i * 0.1d),
															 0d + (i * 0.01d)))
														 .toList();

		// when
		parameterDataRepository.bulkInsertParameterDataRequireValue(parameterDataList);

		// then
		LocalDateTime lastTraceAt = parameterDataList.get(2000)
													 .getTraceAt();
		List<ParameterData> insertedDataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(1L,
			firstTraceAt,
			lastTraceAt,
			null);

		assertThat(insertedDataList).hasSize(2001);

		ParameterData firstData = insertedDataList.get(0);
		assertThat(firstData.getUcl()).isNull();
		assertThat(firstData.getLcl()).isNull();
		assertThat(firstData.getUsl()).isNull();
		assertThat(firstData.getLsl()).isNull();
		assertThat(firstData.getValue(firstData.getDataType())).isEqualTo(33d);

		ParameterData lastData = insertedDataList.get(2000);
		assertThat(lastData.getValue(firstData.getDataType())).isEqualTo(2033d);
	}

	@DisplayName("스펙 정보가 없는 데이터를 2001건 저장한다")
	@Test
	void no_spec_data_insert_2001() {
		// given
		Parameter parameter = Parameter.builder()
									   .id(1L)
									   .name("latitude")
									   .dataType(Parameter.DataType.DOUBLE)
									   .isSpecAvailable(false)
									   .build();

		LocalDateTime traceAt = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
		ParameterData parameterData = ParameterData.createOf(parameter, 33d, traceAt, 0.1d, 0.01d);

		// when
		parameterDataRepository.bulkInsertParameterDataRequireValue(List.of(parameterData));

		// then
		List<ParameterData> insertedDataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(1L,
			traceAt,
			traceAt,
			null);

		assertThat(insertedDataList).hasSize(1);

		ParameterData firstData = insertedDataList.get(0);
		assertThat(firstData.getUcl()).isNull();
		assertThat(firstData.getLcl()).isNull();
		assertThat(firstData.getUsl()).isNull();
		assertThat(firstData.getLsl()).isNull();
		assertThat(firstData.getValue(firstData.getDataType())).isEqualTo(33d);
		assertThat(firstData.getTraceAt()).isAfterOrEqualTo(traceAt);
	}

	@DisplayName("스펙 정보가 있는 데이터를 1건 저장한다")
	@Test
	void spec_data_insert_1() {
		// given
		Parameter parameter = Parameter.builder()
									   .id(1L)
									   .name("latitude")
									   .dataType(Parameter.DataType.DOUBLE)
									   .isSpecAvailable(true)
									   .ucl(100d)
									   .lcl(0d)
									   .usl(100d)
									   .lsl(0d)
									   .build();

		LocalDateTime traceAt = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
		ParameterData parameterData = ParameterData.createOf(parameter, 101d, traceAt, 0.1d, 0.01d);

		// when
		parameterDataRepository.bulkInsertParameterData(List.of(parameterData));

		// then
		List<ParameterData> insertedDataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(parameterData.getParameterId(),
			traceAt,
			traceAt,
			null);

		assertThat(insertedDataList).hasSize(1);

		ParameterData firstData = insertedDataList.get(0);
		assertThat(firstData.getUcl()).isEqualTo(100d);
		assertThat(firstData.getLcl()).isEqualTo(0d);
		assertThat(firstData.getUsl()).isEqualTo(100d);
		assertThat(firstData.getLsl()).isEqualTo(0d);
		assertThat(firstData.isCtrlLimitOver()).isTrue();
		assertThat(firstData.getValue(firstData.getDataType())).isEqualTo(101d);
	}

	@DisplayName("스펙 정보가 있는 데이터를 2001건 저장한다")
	@Test
	void spec_data_insert_2001() {
		// given
		Parameter parameter = Parameter.builder()
									   .id(1L)
									   .name("latitude")
									   .dataType(Parameter.DataType.DOUBLE)
									   .isSpecAvailable(true)
									   .ucl(100d)
									   .lcl(0d)
									   .usl(100d)
									   .lsl(0d)
									   .build();

		LocalDateTime firstTraceAt = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);

		List<ParameterData> parameterDataList = IntStream.range(0, 2001)
														 .mapToObj(i -> ParameterData.createOf(parameter,
															 33d + (i * 1d),
															 firstTraceAt.plusSeconds(i * 2L),
															 0d + (i * 0.1d),
															 0d + (i * 0.01d)))
														 .toList();

		// when
		parameterDataRepository.bulkInsertParameterData(parameterDataList);

		// then
		// 마지막 인덱스 직접조회
		LocalDateTime lastTraceAt = parameterDataList.get(2000)
													 .getTraceAt();
		List<ParameterData> insertedDataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(parameter.getId(),
			firstTraceAt,
			lastTraceAt,
			null);

		assertThat(insertedDataList).hasSize(2001);

		ParameterData firstData = insertedDataList.get(0);
		assertThat(firstData.getUcl()).isEqualTo(100d);
		assertThat(firstData.getLcl()).isEqualTo(0d);
		assertThat(firstData.getUsl()).isEqualTo(100d);
		assertThat(firstData.getLsl()).isEqualTo(0d);
		assertThat(firstData.getValue(firstData.getDataType())).isEqualTo(33d);

		ParameterData lastData = insertedDataList.get(2000);
		assertThat(lastData.getValue(firstData.getDataType())).isEqualTo(2033d);
	}
}
