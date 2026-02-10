package com.nextorm.portal.event;

import com.nextorm.portal.entity.system.RefreshEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RefreshEventListener {
	private final RefreshMessageService refreshMessageService;

	public RefreshEventListener(RefreshMessageService refreshMessageService) {
		this.refreshMessageService = refreshMessageService;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleRefreshEvent(RefreshEvent event) {
		String actionType = event.getActionType();
		Long toolId = event.getToolId();

		refreshMessageService.sendRefreshMessageForCollector(actionType, toolId);
		refreshMessageService.sendRefreshMessageForProcessor(actionType, toolId);
	}
}
