package com.nextorm.portal.dto.ocap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.common.db.entity.OcapAlarm;
import com.nextorm.common.db.entity.OcapAlarmRecipient;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.entity.enums.OcapAlarmNotificationType;
import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.portal.entity.system.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class OcapResponseDto {
	private Long id;
	private String name;
	@JsonProperty("isAlarmControlSpecOver")
	private boolean isAlarmControlSpecOver;
	@JsonProperty("isAlarmSpecOver")
	private boolean isAlarmSpecOver;
	private Long alarmIntervalCodeId;

	private Long toolId;
	private String toolName;

	private Long parameterId;
	private String parameterName;
	private String alarmNotificationType;  // 알림 타입 (EMAIL, SMS, KAKAO) - 콤마(,)로 구분된 문자열
	private List<RecipientDto> recipients;

	public static OcapResponseDto from(
		OcapAlarm ocapAlarm,
		List<OcapAlarmRecipient> alarmRecipients,
		List<User> users
	) {
		Tool tool = ocapAlarm.getTool();
		Parameter parameter = ocapAlarm.getParameter();
		Code alarmIntervalCode = ocapAlarm.getAlarmIntervalCode();
		
		// userId -> User 맵 생성
		Map<Long, User> userMap = users.stream()
			.collect(Collectors.toMap(User::getId, user -> user));
		
		// 알림 타입별 수신자 정보 생성
		List<RecipientDto> recipients = alarmRecipients.stream()
			.map(recipient -> {
				User user = userMap.get(recipient.getUserId());
				if (user == null) return null;
				
				OcapAlarmNotificationType type = recipient.getOcapAlarmNotificationType();
				String contact = switch (type) {
					case EMAIL -> user.getEmail();
					case SMS, KAKAO ->  // 카카오톡도 전화번호 사용
						user.getPhone();
				};
				
				// 알림 타입에 따른 연락처 선택

				return new RecipientDto(
					recipient.getId(),
					user.getId(),
					user.getName(),
					contact,
					type
				);
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		
		// 활성화된 알림 타입들 추출 (중복 제거)
		String notificationTypes = alarmRecipients.stream()
			.map(r -> r.getOcapAlarmNotificationType().name())
			.distinct()
			.collect(Collectors.joining(","));

		return OcapResponseDto.builder()
							  .id(ocapAlarm.getId())
							  .name(ocapAlarm.getName())
							  .isAlarmControlSpecOver(ocapAlarm.isAlarmControlSpecOver())
							  .isAlarmSpecOver(ocapAlarm.isAlarmSpecOver())
							  .alarmIntervalCodeId(alarmIntervalCode.getId())
							  .toolId(tool.getId())
							  .toolName(tool.getName())
							  .parameterId(parameter.getId())
							  .parameterName(parameter.getName())
							  .alarmNotificationType(notificationTypes)
							  .recipients(recipients)
							  .build();
	}

	/**
	 * 		한 사용자가 여러 수단을 등록했을 경우
	 *       i. {userId: 1, userName: "admin", contact: "admin@example.com", notificationType: EMAIL}
	 *       ii. {userId: 1, userName: "admin", contact: "010-1234-5678", notificationType: SMS}
	 *       iii. {userId: 1, userName: "admin", contact: "010-1234-5678", notificationType: KAKAO}
	 */

	@Data
	@AllArgsConstructor
	public static class RecipientDto {
		private Long id;
		private Long userId;
		private String userName;
		private String contact;  // email 또는 phone (타입에 따라)
		private OcapAlarmNotificationType notificationType;
	}
}
