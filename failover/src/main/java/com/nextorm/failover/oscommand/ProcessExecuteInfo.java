package com.nextorm.failover.oscommand;

import java.util.ArrayList;
import java.util.List;

public class ProcessExecuteInfo {
	public String getExecutePath() {
		return executePath;
	}

	public void setExecutePath(String executePath) {
		this.executePath = executePath;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	private String executePath = "";

	private List<String> parameters = new ArrayList<String>();
}
