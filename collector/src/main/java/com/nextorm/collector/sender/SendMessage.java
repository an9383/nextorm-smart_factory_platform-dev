package com.nextorm.collector.sender;

import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class SendMessage {
	private Long dcpId;
	private Long toolId;
	private String command;
	private Long dateTime;
	private Map<String, Object> values;

	private SendMessage(
		Long dcpId,
		Long toolId,
		String command,
		Long dateTime,
		Map<String, Object> values
	) {
		this.dcpId = dcpId;
		this.toolId = toolId;
		this.command = command;
		this.dateTime = dateTime;
		this.values = values;

		if (values == null) {
			this.values = new HashMap<>();
		}
	}

	/**
	 * DataCollectPlan의 메타데이터와 수집된 값을 병합하여 SendMessage 생성
	 *
	 * @param plan:          DataCollectPlan
	 * @param timestamp:     데이터 타임스탬프
	 * @param collectValues: 수집된 값 맵
	 * @return
	 */
	public static SendMessage createMergedMetadataMessage(
		DataCollectPlan plan,
		long timestamp,
		Map<String, Object> collectValues
	) {
		Map<String, Object> mergeValues = new HashMap<>();
		plan.getMetaParameters()
			.forEach(param -> mergeValues.put(param.getName(), param.getMetaValue()));
		if (collectValues != null) {
			mergeValues.putAll(collectValues);
		}
		return new SendMessage(plan.getDcpId(), plan.getToolId(), plan.getCommand(), timestamp, mergeValues);
	}
}
