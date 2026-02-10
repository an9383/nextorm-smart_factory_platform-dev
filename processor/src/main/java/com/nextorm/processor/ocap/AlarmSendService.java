package com.nextorm.processor.ocap;

import com.nextorm.processor.dto.SmsRequestDto;
import com.nextorm.processor.dto.SmsResponseDto;
import com.nextorm.processor.dto.SnsRequestDto;
import com.nextorm.processor.dto.SnsResponseDto;
import com.nextorm.processor.ocap.properties.SmsProperties;
import com.nextorm.processor.ocap.properties.SnsProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmSendService {
	private final JavaMailSender mailSender;
	private final WebClient webClient;
	private final SmsProperties smsProperties;
	private final SnsProperties snsProperties;

	public void sendKakaoMessage(List<SnsRequestDto.SnsMessage> snsMessageList) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		httpHeaders.set(snsProperties.getTimeStampHeader(), timestamp);
		httpHeaders.set(snsProperties.getAccessKeyHeader(), snsProperties.getAccessKey());
		try {
			httpHeaders.set(snsProperties.getSignatureHeader(), makeSignature(timestamp, snsProperties.getSignatureUrl()));
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			log.error("카카오톡 서명 생성 실패: {}", e.getMessage(), e);
			return;
		}


		SnsRequestDto requestDto = SnsRequestDto.builder()
										   .plusFriendId(snsProperties.getPlusFriendId())
										   .templateCode(snsProperties.getTemplateCode())
										   .messages(snsMessageList)
										   .build();

			webClient.post()
				 .uri(snsProperties.getApiUrl())
				 .headers(headers -> headers.addAll(httpHeaders))
				 .bodyValue(requestDto)
				 .retrieve()
				 .bodyToMono(SnsResponseDto.class)
				 .subscribe(response -> {
					 if (String.valueOf(HttpStatus.ACCEPTED.value())
							   .equals(response.getStatusCode())) {
						 log.info("카카오톡 전송 성공 - requestId: {}, requestTime: {}, statusName: {}",
							 response.getRequestId(),
							 response.getRequestTime(),
							 response.getStatusName());
					 }
				 }, error -> log.error("카카오톡 전송 실패: {}", error.getMessage(), error));
	}

	public void sendSms(
		List<SmsRequestDto.smsRecipients> smsRecipients,
		String subject,
		String content
	) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		httpHeaders.set(smsProperties.getTimeStampHeader(), timestamp);
		httpHeaders.set(smsProperties.getAccessKeyHeader(), smsProperties.getAccessKey());

		try {
			httpHeaders.set(smsProperties.getSignatureHeader(), makeSignature(timestamp, smsProperties.getSignatureUrl()));
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			log.error("SMS 서명 생성 실패: {}", e.getMessage(), e);
			return;
		}

		SmsRequestDto requestDto = SmsRequestDto.builder()
												.type("SMS")
												.from(smsProperties.getFrom())
												.subject(subject)
												.content(content)
												.messages(smsRecipients)
												.build();

		webClient.post()
				 .uri(smsProperties.getApiUrl())
				 .headers(headers -> headers.addAll(httpHeaders))
				 .bodyValue(requestDto)
				 .retrieve()
				 .bodyToMono(SmsResponseDto.class)
				 .subscribe(response -> {
					 if (String.valueOf(HttpStatus.ACCEPTED.value())
							   .equals(response.getStatusCode())) {
						 log.info("SMS 전송 성공 - requestId: {}, requestTime: {}, statusName: {}",
							 response.getRequestId(),
							 response.getRequestTime(),
							 response.getStatusName());
					 }
				 }, error -> log.error("SMS 전송 실패: {}", error.getMessage(), error));
	}

	public void sendMail(MimeMessage mimeMessage) {
		mailSender.send(mimeMessage);
	}

	private String makeSignature(
		String timestamp,
		String url
	) throws NoSuchAlgorithmException, InvalidKeyException {
		String space = " ";
		String newLine = "\n";
		String method = "POST";

		String message = method + space + url + newLine + timestamp + newLine + smsProperties.getAccessKey();
		SecretKeySpec signingKey = new SecretKeySpec(smsProperties.getSecretKey()
																  .getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(signingKey);

		byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder()
					 .encodeToString(rawHmac);
	}

}
