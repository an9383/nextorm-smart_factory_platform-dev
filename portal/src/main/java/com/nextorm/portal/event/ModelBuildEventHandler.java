package com.nextorm.portal.event;

import com.nextorm.portal.client.modelbuild.ModelBuildClient;
import com.nextorm.portal.event.message.ModelBuildRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModelBuildEventHandler {
	private final ModelBuildClient client;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleParameterEvent(ModelBuildRequestEvent event) {
		client.requestModelTraining(event.getModelId());
	}
}
