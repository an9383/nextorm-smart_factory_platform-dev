package com.nextorm.processor.service;

import com.nextorm.common.db.entity.FaultHistory;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.FaultHistoryRepository;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.processor.event.application.FaultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProcessedDataService {
	private final FaultHistoryRepository faultHistoryRepository;
	private final ParameterDataRepository parameterDataRepository;
	private final ApplicationEventPublisher eventPublisher;

	public void saveAndFaultEventPublish(
		List<ParameterData> parameterDataList,
		List<FaultHistory> faultHistoryList
	) {
		if (!parameterDataList.isEmpty()) {
			parameterDataRepository.bulkInsertParameterData(parameterDataList);
		}

		if (!faultHistoryList.isEmpty()) {
			faultHistoryRepository.bulkInsertFaultHistory(faultHistoryList);
			for (FaultHistory faultHistory : faultHistoryList) {
				FaultEvent.Message eventMessage = FaultEvent.Message.builder()
																	.parameterId(faultHistory.getParameterId())
																	.isControlSpecOver(faultHistory.isCtrlLimitOver())
																	.isSpecOver(faultHistory.isSpecLimitOver())
																	.traceAt(faultHistory.getFaultAt())
																	.build();
				eventPublisher.publishEvent(new FaultEvent(this, eventMessage));
			}
		}
	}
}
