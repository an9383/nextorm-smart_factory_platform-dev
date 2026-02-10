package com.nextorm.portal.service.migration;

import com.nextorm.common.db.entity.FaultHistory;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MigrationDataWrapper {
	@Getter
	private final Parameter parameter;
	@Getter
	private final List<MigrationData> migrationData = new ArrayList<>();
	@Getter
	private final List<FaultHistory> faultDataList = new ArrayList<>();

	private LocalDateTime startAt = null;
	private LocalDateTime endAt = null;
	private List<ParameterData> parameterDataList = null;

	public MigrationDataWrapper(Parameter parameter) {
		this.parameter = parameter;
	}

	public void addData(MigrationData data) {
		migrationData.add(data);
	}

	public Long getParameterId() {
		return parameter.getId();
	}

	public List<ParameterData> getParameterDataList() {
		if (parameterDataList != null) {
			return parameterDataList;
		}

		parameterDataList = new ArrayList<>();
		for (MigrationData data : migrationData) {
			ParameterData parameterData = ParameterData.createOf(parameter,
				data.getValue(),
				data.getTraceAt(),
				data.getLatitude(),
				data.getLongitude());
			parameterDataList.add(parameterData);

			if (parameterData.isSpecOver()) {
				FaultHistory faultHistory = parameterData.toFaultHistory();
				faultDataList.add(faultHistory);
			}
		}
		return parameterDataList;
	}

	public LocalDateTime getStartAt() {
		if (startAt != null) {
			return startAt;
		}
		startAt = migrationData.stream()
							   .map(MigrationData::getTraceAt)
							   .min(LocalDateTime::compareTo)
							   .orElse(null);
		return startAt;
	}

	public LocalDateTime getEndAt() {
		if (endAt != null) {
			return endAt;
		}
		endAt = migrationData.stream()
							 .map(MigrationData::getTraceAt)
							 .max(LocalDateTime::compareTo)
							 .orElse(null);
		return endAt;
	}
}
