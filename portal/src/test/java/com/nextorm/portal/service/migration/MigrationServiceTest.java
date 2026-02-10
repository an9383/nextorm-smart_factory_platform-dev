package com.nextorm.portal.service.migration;

import com.nextorm.common.db.entity.FaultHistory;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.nextorm.common.db.repository.*;
import com.nextorm.portal.DataJpaTestBase;
import com.nextorm.processor.scriptengine.ScriptEngineFactory;
import com.nextorm.summarizer.service.summaryprocessor.SummaryProcessor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class MigrationServiceTest extends DataJpaTestBase {

	@Autowired
	ParameterRepository parameterRepository;

	@Autowired
	ParameterDataRepository parameterDataRepository;

	@Autowired
	FaultHistoryRepository faultHistoryRepository;

	@Autowired
	SummaryDataRepository summaryDataRepository;

	@Autowired
	HealthSummaryDataRepository healthSummaryDataRepository;

	@Autowired
	VirtualParameterParameterMappingRepository virtualParameterParameterMappingRepository;

	@Autowired
	DcpConfigRepository dcpConfigRepository;

	@Autowired
	ApplicationContext context;

	@PersistenceContext
	EntityManager em;

	@DisplayName("스펙 데이터가 없는 파라미터를 마이그레이션 한다")
	@Test
	void migrate_csv() throws IOException {
		// given
		Parameter p1 = parameterRepository.save(Parameter.builder()
														 .dataType(Parameter.DataType.DOUBLE)
														 .type(Parameter.Type.TRACE)
														 .build());
		Parameter p2 = parameterRepository.save(Parameter.builder()
														 .dataType(Parameter.DataType.DOUBLE)
														 .type(Parameter.Type.TRACE)
														 .build());

		parameterRepository.saveAll(List.of(p1, p2));
		em.flush();
		em.clear();

		Map<String, Long> headerParameterIdMap = new HashMap();
		headerParameterIdMap.put("battery_soc", p1.getId());
		headerParameterIdMap.put("solar_voltage", p2.getId());

		ClassPathResource resource = new ClassPathResource(MigrationTestUtils.SMALL_CSV_PATH);
		MockMultipartFile file = new MockMultipartFile("file", resource.getInputStream());

		SummaryProcessor summaryProcessor = new SummaryProcessor(parameterDataRepository,
			summaryDataRepository,
			healthSummaryDataRepository,
			parameterRepository);

		MigrationService service = new MigrationService(parameterRepository,
			parameterDataRepository,
			faultHistoryRepository,
			virtualParameterParameterMappingRepository,
			dcpConfigRepository,
			summaryProcessor,
			new ScriptEngineFactory(context));

		// when
		service.migrate(headerParameterIdMap, file, false);

		// then
		LocalDateTime from = MigrationTestUtils.SMALL_DATA_START_AT;
		LocalDateTime to = MigrationTestUtils.SMALL_DATA_END_AT;

		List<ParameterData> p1DataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(p1.getId(),
			from,
			to,
			null);
		List<ParameterData> p2DataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(p2.getId(),
			from,
			to,
			null);

		assertThat(p1DataList).hasSize(MigrationTestUtils.SMALL_DATA_SIZE);
		assertThat(p2DataList).hasSize(MigrationTestUtils.SMALL_DATA_SIZE);

		assertThat(faultHistoryRepository.findByParameterIdAndFaultAtBetween(p1.getId(), from, to)).isEmpty();
		assertThat(faultHistoryRepository.findByParameterIdAndFaultAtBetween(p2.getId(), from, to)).isEmpty();

		assertThat(summaryDataRepository.findAllByParameterIdAndPeriodTypeAndTrxStartAtBetween(p1.getId(),
			SummaryPeriodType.DAILY,
			from,
			to)).hasSize(2);

		assertThat(summaryDataRepository.findAllByParameterIdAndPeriodTypeAndTrxStartAtBetween(p2.getId(),
			SummaryPeriodType.DAILY,
			from,
			to)).hasSize(2);

		log.info("p1: {}, p2: {}", p1.getId(), p2.getId());
	}

	@DisplayName("스펙 데이터가 있는 파라미터를 마이그레이션 한다")
	@Test
	void migrate_with_spec() throws IOException {
		// given
		// 스펙 허용 데이터와, 아닌 데이터가 섞여있는 파일
		final String testDataFilePath = "csv/use_spec_migration_data.csv";
		final int testDataTotalSize = 20;
		final int testDataFaultSize = 10;

		Parameter p1 = parameterRepository.save(Parameter.builder()
														 .dataType(Parameter.DataType.DOUBLE)
														 .type(Parameter.Type.TRACE)
														 .isSpecAvailable(true)
														 .ucl(100d)
														 .usl(50d)
														 .build());

		parameterRepository.save(p1);
		em.flush();
		em.clear();

		Map<String, Long> headerParameterIdMap = Map.of("test_param", p1.getId());
		ClassPathResource resource = new ClassPathResource(testDataFilePath);
		MockMultipartFile file = new MockMultipartFile("file", resource.getInputStream());

		SummaryProcessor summaryProcessor = new SummaryProcessor(parameterDataRepository,
			summaryDataRepository,
			healthSummaryDataRepository,
			parameterRepository);

		MigrationService service = new MigrationService(parameterRepository,
			parameterDataRepository,
			faultHistoryRepository,
			virtualParameterParameterMappingRepository,
			dcpConfigRepository,
			summaryProcessor,
			new ScriptEngineFactory(context));

		// when
		service.migrate(headerParameterIdMap, file, false);

		// then
		LocalDateTime from = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
		LocalDateTime to = LocalDateTime.of(2000, 1, 1, 0, 0, 19, 0);

		List<ParameterData> p1DataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(p1.getId(),
			from,
			to,
			null);

		List<FaultHistory> faultHistories = faultHistoryRepository.findByParameterIdAndFaultAtBetween(p1.getId(),
			from,
			to);

		assertThat(p1DataList).hasSize(testDataTotalSize);
		assertThat(faultHistories).hasSize(testDataFaultSize);

		FaultHistory firstFault = faultHistories.get(0);
		assertThat(firstFault.isOverSpec()).isTrue();
		assertThat(firstFault.isUclOver()).isTrue();
		assertThat(firstFault.isUslOver()).isTrue();
		assertThat(firstFault.isLclOver()).isFalse();
		assertThat(firstFault.isLslOver()).isFalse();
		assertThat(firstFault.isCtrlLimitOver()).isTrue();
		assertThat(firstFault.isSpecLimitOver()).isTrue();
	}
}