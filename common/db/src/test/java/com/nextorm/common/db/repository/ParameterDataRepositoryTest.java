package com.nextorm.common.db.repository;

import com.nextorm.common.db.DataJpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
class ParameterDataRepositoryTest extends DataJpaTestBase {

	@Autowired
	private ParameterDataRepository repository;

	@Test
	void findMonthsStatistics() {
		Long parameterId = 6L;
		String timeZone = "Asia/Seoul";

		Instant now = Instant.now();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
		LocalDate localDate = localDateTime.minusMonths(12)
										   .withDayOfMonth(1)
										   .toLocalDate();
		Instant start = localDate.atStartOfDay(ZoneId.systemDefault())
								 .toInstant();

		log.info("now: {}", now);
		log.info("localDate: {}", localDate);
		log.info("start: {}", start);

		// List<Tuple> result = repository.findMonthsStatistics(parameterId, start, now, timeZone);
		// result.forEach(tuple -> log.info("{}", tuple));
	}

	@Test
	void find() {
		long s = System.currentTimeMillis();
		repository.findLimitRecentByParameterIdOrderByTraceAtDesc(6L, 10);
		log.info("end: {}ms", (System.currentTimeMillis() - s));
	}

	@Test
	void find2() {
		long s = System.currentTimeMillis();
		repository.findLimitRecentByIdGreaterThanAndParameterIdOrderByTraceAtDesc(37365635L, 6L, 10);
		log.info("end: {}ms", (System.currentTimeMillis() - s));
	}
}