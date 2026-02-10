package com.nextorm.processor.ocap;

import com.nextorm.common.db.entity.*;
import com.nextorm.common.db.entity.enums.OcapAlarmNotificationType;
import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.processor.ocap.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder
@Getter
@ToString
public class OCAP {
	private Long ocapAlarmId;
	private String name;

	private boolean isAlarmControlSpecOver;
	private boolean isAlarmSpecOver;

	private int alertIntervalSeconds;

	private Long toolId;
	private String toolName;

	private Long parameterId;
	private String parameterName;

	@Builder.Default
	Map<OcapAlarmNotificationType, List<User>> recipients =  Map.of();

	public static OCAP from(
		OcapAlarm ocapAlarm,
		List<OcapAlarmRecipient> alarmRecipients,
		List<User> users
	) {
		Tool tool = ocapAlarm.getTool();
		Parameter parameter = ocapAlarm.getParameter();
		Code alarmIntervalCode = ocapAlarm.getAlarmIntervalCode();

		int alertIntervalSeconds = Integer.parseInt(alarmIntervalCode.getValue());

		Map<Long, User> userMap = users.stream()
			.collect(Collectors.toMap(BaseEntity::getId, user -> user,
				(existing, replacement) -> existing));

      // 알림 타입별로 User 객체를 그룹화
      Map<OcapAlarmNotificationType, List<User>> notificationUsers =
          alarmRecipients.stream()
              .collect(Collectors.groupingBy(
                  OcapAlarmRecipient::getOcapAlarmNotificationType,
                  Collectors.mapping(
                      recipient -> userMap.get(recipient.getUserId()),
                      Collectors.filtering(Objects::nonNull,
                          Collectors.toList()
                      )
                  )
              ));

		return OCAP.builder()
				   .ocapAlarmId(ocapAlarm.getId())
				   .name(ocapAlarm.getName())
				   .isAlarmControlSpecOver(ocapAlarm.isAlarmControlSpecOver())
				   .isAlarmSpecOver(ocapAlarm.isAlarmSpecOver())
				   .alertIntervalSeconds(alertIntervalSeconds)

				   .toolId(tool.getId())
				   .toolName(tool.getName())

				   .parameterId(parameter.getId())
				   .parameterName(parameter.getName())

			.recipients(notificationUsers)
				   .build();
	}
	public boolean hasEmailRecipients() {
		return recipients != null &&
			   !recipients.getOrDefault(OcapAlarmNotificationType.EMAIL, List.of()).isEmpty();
	}

	// 이메일 주소만 추출하는 메서드
	public List<String> getEmailAddresses() {
		if (recipients == null) {
			return List.of();
		}
		return recipients.getOrDefault(OcapAlarmNotificationType.EMAIL, List.of())
			.stream()
			.map(User::getEmail)
			.filter(email -> email != null && !email.isEmpty())
			.collect(Collectors.toList());
	}

	//등록한 핸드폰 번호를 추출하는 메서드
	public List<String> getPhoneNumbers(){
		if(recipients == null) {
			return List.of();
		}

		return recipients.getOrDefault(OcapAlarmNotificationType.SMS, List.of())
				.stream()
				.map(User::getPhone)
				.filter(phoneNumber -> phoneNumber != null && !phoneNumber.isEmpty())
				.collect(Collectors.toList());
	}
}
