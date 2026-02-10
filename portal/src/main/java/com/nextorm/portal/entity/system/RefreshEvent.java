package com.nextorm.portal.entity.system;

public class RefreshEvent {
	private final Long toolId;
	private final String actionType;

	public RefreshEvent(
		Long toolId,
		String actionType
	) {
		this.toolId = toolId;
		this.actionType = actionType;
	}

	public Long getToolId() {
		return toolId;
	}

	public String getActionType() {
		return actionType;
	}
}
