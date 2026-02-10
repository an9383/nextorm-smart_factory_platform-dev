package com.nextorm.extensions.misillan.alarm.service;

import com.nextorm.extensions.misillan.alarm.dto.AlarmHistoryDto;
import com.nextorm.extensions.misillan.alarm.entity.AlarmHistory;
import com.nextorm.extensions.misillan.alarm.entity.AlarmReadUser;
import com.nextorm.extensions.misillan.alarm.repository.AlarmHistoryRepository;
import com.nextorm.extensions.misillan.alarm.repository.AlarmReadUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmHistoryService {
	private final AlarmHistoryRepository alarmHistoryRepository;
	private final AlarmReadUserRepository alarmReadUserRepository;

	public List<AlarmHistoryDto> getAlarmList(
		String startDt,
		String endDt
	) {
		LocalDate start = LocalDate.parse(startDt, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		LocalDate end = LocalDate.parse(endDt, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		LocalDateTime startDateTime = start.atStartOfDay();
		LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

		return alarmHistoryRepository.findAllByAlarmDtBetween(startDateTime, endDateTime)
									 .stream()
									 .map(alarmHistory -> AlarmHistoryDto.from(alarmHistory))
									 .toList();
	}

	public List<AlarmHistoryDto> getUnreadAlarmList(Long userId) {
		return alarmHistoryRepository.findUnreadAlarms(userId)
									 .stream()
									 .map(alarmHistory -> AlarmHistoryDto.from(alarmHistory))
									 .toList();
	}

	@Transactional
	public AlarmHistoryDto markAlarmAsRead(
		Long alarmId,
		Long userId
	) {
		AlarmHistory alarmHistory = alarmHistoryRepository.findByIdWithReadUsers(alarmId);

		// 이미 읽음 처리 되어 있는지 확인
		boolean alreadyRead = alarmHistory.getReadUsers()
										  .stream()
										  .anyMatch(r -> r.getUserId()
														  .equals(userId));
		if (!alreadyRead) {
			AlarmReadUser alarmReadUser = new AlarmReadUser();
			alarmReadUser.setAlarmHistory(alarmHistory);
			alarmReadUser.setUserId(userId);

			alarmReadUserRepository.save(alarmReadUser);
		}

		return AlarmHistoryDto.from(alarmHistory);
	}

	public void readAllAlarm(Long userId) {
		List<AlarmHistory> unreadAlarms = alarmHistoryRepository.findUnreadAlarms(userId);

		List<AlarmReadUser> readUsers = new ArrayList<>();

		for (AlarmHistory alarm : unreadAlarms) {
			AlarmReadUser readUser = new AlarmReadUser();
			readUser.setUserId(userId);
			readUser.setAlarmHistory(alarm);

			readUsers.add(readUser);
		}

		alarmReadUserRepository.saveAll(readUsers);
	}
}
