package com.nextorm.portal.repository;

import com.nextorm.common.db.entity.FaultHistory;
import com.nextorm.common.db.repository.FaultHistoryRepository;
import com.nextorm.portal.DataJpaTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class FaultHistoryTemplateRepositoryTest extends DataJpaTestBase {
	@Autowired
	FaultHistoryRepository faultHistoryRepository;

	@DisplayName("스펙오버 기록 데이터를 1건 저장한다")
	@Test
	void insert_fault_history_1() {
		// given
		LocalDateTime faultAt = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
		FaultHistory faultHistory = FaultHistory.builder()
												.parameterId(1L)
												.paramValue("101")
												.faultAt(faultAt)

												.lsl(-50d)
												.lcl(-100d)
												.usl(50d)
												.ucl(100d)

												.isLslOver(false)
												.isLclOver(false)
												.isUslOver(false)
												.isUclOver(true)

												.isSpecLimitOver(false)
												.isCtrlLimitOver(true)
												.build();

		// when
		faultHistoryRepository.bulkInsertFaultHistory(List.of(faultHistory));

		// then
		List<FaultHistory> insertedDataList = faultHistoryRepository.findByParameterIdAndFaultAtBetween(1L,
			faultAt,
			faultAt);

		assertThat(insertedDataList).hasSize(1);

		FaultHistory firstData = insertedDataList.get(0);
		assertThat(firstData.getParameterId()).isEqualTo(1L);
		assertThat(firstData.getParamValue()).isEqualTo("101");
		assertThat(firstData.getFaultAt()).isEqualTo(faultAt);
		assertThat(firstData.getLsl()).isEqualTo(-50d);
		assertThat(firstData.getLcl()).isEqualTo(-100d);
		assertThat(firstData.getUsl()).isEqualTo(50d);
		assertThat(firstData.getUcl()).isEqualTo(100d);
		assertThat(firstData.isLslOver()).isFalse();
		assertThat(firstData.isLclOver()).isFalse();
		assertThat(firstData.isUslOver()).isFalse();
		assertThat(firstData.isUclOver()).isTrue();
		assertThat(firstData.isSpecLimitOver()).isFalse();
		assertThat(firstData.isCtrlLimitOver()).isTrue();
	}
}
