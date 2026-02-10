package com.nextorm.processor.event.application;

import com.nextorm.common.db.entity.enums.OcapAlarmCondition;
import com.nextorm.common.db.entity.enums.OcapAlarmNotificationType;
import com.nextorm.processor.config.AsyncConfig;
import com.nextorm.processor.dto.SmsRequestDto;
import com.nextorm.processor.dto.SnsRequestDto;
import com.nextorm.processor.ocap.AlarmSendService;
import com.nextorm.processor.ocap.OCAP;
import com.nextorm.processor.ocap.OcapProperty;
import com.nextorm.processor.ocap.OcapService;
import com.nextorm.processor.ocap.properties.MailProperties;
import com.nextorm.processor.ocap.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FaultEventHandler {

	private static final String RECIPIENTS_DELIMITER = ",";
	private final ThreadPoolTaskExecutor executor;
	private final OcapService ocapService;
	private final AlarmSendService alarmSendService;
	private final OcapProperty ocapProperty;
	private final MailProperties mailProperties;
	private final JavaMailSender mailSender;

	public FaultEventHandler(
		@Qualifier(AsyncConfig.OCAP_EXECUTOR_BEAN_NAME) ThreadPoolTaskExecutor executor,
		OcapService ocapService,
		AlarmSendService alarmSendService,
		OcapProperty ocapProperty,
		MailProperties mailProperties,
		JavaMailSender mailSender
	) {
		this.executor = executor;
		this.ocapService = ocapService;
		this.alarmSendService = alarmSendService;
		this.ocapProperty = ocapProperty;
		this.mailProperties = mailProperties;
		this.mailSender = mailSender;
	}

	@Async(AsyncConfig.OCAP_EXECUTOR_BEAN_NAME)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void faultAlarm(FaultEvent faultEvent) throws MessagingException {
		int queueSize = executor.getThreadPoolExecutor()
								.getQueue()
								.size();
		log.info("[FaultEvent] queue size: {}, message: {}", queueSize, faultEvent.getMessage());

		List<OCAP> alarmTargets = ocapService.detectAlarmTargets(faultEvent.getMessage());

		if (alarmTargets.isEmpty()) {
			return;
		}
		LocalDateTime faultAt = faultEvent.getMessage()
										  .getTraceAt();

		for (OCAP ocap : alarmTargets) {
			log.info("[FaultEvent] OCAP alarm - id: {}, type: {}, recipients: {}",
				ocap.getOcapAlarmId(),
				ocap.getRecipients()
					.keySet(),
				ocap.getRecipients());

			for (Map.Entry<OcapAlarmNotificationType, List<User>> entry : ocap.getRecipients()
																			  .entrySet()) {
				OcapAlarmNotificationType notificationType = entry.getKey();
				List<User> alarmTargetUserList = entry.getValue();

				if (alarmTargetUserList.isEmpty() || ocapProperty.isLogOnly()) {
					continue;
				}
				List<String> phoneNumbers = ocap.getPhoneNumbers();
				switch (notificationType) {
					case EMAIL:
						log.info("[FaultEvent] Send email to: {}", alarmTargetUserList);
						MimeMessage mimeMessage = createMimeMessage(ocap, faultAt);
						alarmSendService.sendMail(mimeMessage);
						break;

					case SMS:
						log.info("[FaultEvent] Send SMS to: {}", alarmTargetUserList);
						List<SmsRequestDto.smsRecipients> smsRecipients = phoneNumbers.stream()
																					  .map(phone -> SmsRequestDto.smsRecipients.builder()
																															   .to(phone)
																															   .build())
																					  .toList();

						String content = messageCreate(ocap, faultAt);
						String subject = "[OCAP 알람] " + ocap.getName();
						alarmSendService.sendSms(smsRecipients, subject, content);
						break;
					case KAKAO:
						log.info("[FaultEvent] Send Kakao Message to: {}", alarmTargetUserList);
						List<SnsRequestDto.SnsMessage> snsMessageList = phoneNumbers.stream()
																			 .map(phone -> SnsRequestDto.SnsMessage.builder()
																												   .to(phone)
																												   .content(
																													   messageCreate(
																														   ocap,
																														   faultAt))
																												   .build())
																			 .toList();
						alarmSendService.sendKakaoMessage(snsMessageList);
						break;
				}
			}
			ocapService.saveAlarmHistory(ocap, faultAt);
		}

	}

	private MimeMessage createMimeMessage(
		OCAP ocap,
		LocalDateTime faultAt
	) throws MessagingException {

		OcapAlarmCondition alarmCondition = ocap.isAlarmSpecOver()
											? OcapAlarmCondition.SPEC_OVER
											: OcapAlarmCondition.CONTROL_SPEC_OVER;

		MimeMessage mimeMessage = mailSender.createMimeMessage();

		mimeMessage.setFrom(mailProperties.getMailFrom());

		if (ocap.hasEmailRecipients()) {
			mimeMessage.setRecipients(MimeMessage.RecipientType.TO, joinRecipients(ocap.getEmailAddresses()));
		}

		mimeMessage.setSubject("[OCAP 알람] " + ocap.getName());
		String body = "";
		body += "<h1>" + "OCAP 알람" + "</h1>";
		body += "<br/>";
		body += "<h3>설비명: %s</h3>".formatted(ocap.getToolName());
		body += "<h3>파라미터명: %s</h3>".formatted(ocap.getParameterName());
		body += "<h3>발생시간: %s</h3>".formatted(faultAt);
		body += "<h3>%s 발생</h3>".formatted(alarmCondition.getName());

		mimeMessage.setText(body, "UTF-8", "html");
		return mimeMessage;
	}

	private String joinRecipients(List<String> recipients) {
		return String.join(RECIPIENTS_DELIMITER, recipients);
	}

	private String messageCreate(
		OCAP ocap,
		LocalDateTime faultAt
	) {
		return "설비명: %s\n파라미터명: %s\n발생시간: %s\n발생: %s".formatted(ocap.getToolName(),
			ocap.getParameterName(),
			faultAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
			ocap.isAlarmSpecOver()
			? OcapAlarmCondition.SPEC_OVER.getName()
			: OcapAlarmCondition.CONTROL_SPEC_OVER.getName());
	}
}
