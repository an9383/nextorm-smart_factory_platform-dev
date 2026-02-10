package com.nextorm.collector.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ScaleSocketCollector implements Collector {
	private final ObjectMapper mapper;
	private final DataCollectPlan config;
	private final Sender sender;
	private final List<String> collectParameterNames;
	private String ip;
	private String port;
	private long reconnectionInterval;
	private String resetValue;
	Socket socket;
	private long lastCheckTime;
	private InputStream inputStream;
	private OutputStream outputStream;
	private StringBuilder dataBuffer = new StringBuilder(); // 데이터 버퍼 추가

	public ScaleSocketCollector(
		ObjectMapper mapper,
		DataCollectPlan config,
		Sender sender
	) {
		this.mapper = mapper;
		this.config = config;
		this.sender = sender;
		this.collectParameterNames = config.getCollectParameters()
										   .stream()
										   .map(DataCollectPlan.Parameter::getName)
										   .toList();

		ip = config.getCollectorArguments()
				   .get("ip")
				   .toString();
		port = config.getCollectorArguments()
					 .get("port")
					 .toString();
		reconnectionInterval = Long.valueOf(String.valueOf(config.getCollectorArguments()
																 .get("reconnectionInterval")));
		resetValue = config.getCollectorArguments()
						   .get("resetValue")
						   .toString();
		
		// 설정값 확인 로그
		log.info("ScaleSocketCollector 초기화 - IP: {}, Port: {}, Interval: {}ms, ResetValue: {}", 
				ip, port, reconnectionInterval, resetValue);
	}

	@Override
	public void run() {
		while (!Thread.currentThread()
					  .isInterrupted()) {
			try {
				if (!connect()) {
					log.error("연결 실패. 재연결 대기 중...");
					waitForReconnection();
					continue;
				}

				inputStream = socket.getInputStream();

				while (socket != null && socket.isConnected() && !socket.isClosed()) {
					try {
						// health check
						if (System.currentTimeMillis() - lastCheckTime >= reconnectionInterval) {
							sendLiveCheckMessage();
							lastCheckTime = System.currentTimeMillis();
						}

						// 데이터 처리
						if (inputStream != null) {
							// 소켓 타임아웃 설정 (블로킹 방지)
							socket.setSoTimeout(100);
							
							byte[] buffer = new byte[2000];
							try {
								// available() 대신 직접 read() 호출하여 블로킹 읽기
								int bytesRead = inputStream.read(buffer);
								
								if (bytesRead > 0) {
									// 실제 읽은 바이트 수만큼만 문자열로 변환
									String receivedData = new String(buffer, 0, bytesRead).trim();
									log.debug("수신한 데이터: [{}], 길이: {}", receivedData, bytesRead);

									// 데이터 버퍼에 추가
									dataBuffer.append(receivedData);
									
									// 완전한 메시지 처리
									String bufferContent = dataBuffer.toString();
									
									// kg 또는 g가 포함된 완전한 메시지인지 확인
									if (bufferContent.contains("kg")) {
										// kg 단위 메시지 처리
										String[] kgMessages = bufferContent.split("(?<=kg)");
										for (String message : kgMessages) {
											message = message.trim();
											if (message.contains("kg") && !message.isEmpty()) {
												log.debug("kg 메시지 처리: [{}]", message);
												processScaleData(message);
											}
										}
										// 버퍼 초기화
										dataBuffer.setLength(0);
									} else if (bufferContent.contains("g")) {
									// g 단위 메시지 처리
									String[] gMessages = bufferContent.split("(?<=g)");
									for (String message : gMessages) {
										message = message.trim();
										if (message.contains("g") && !message.isEmpty()) {
												log.debug("g 메시지 처리: [{}]", message);
												processScaleData(message);
											}
										}
										// 버퍼 초기화
										dataBuffer.setLength(0);
									} else {
										// 아직 완전하지 않은 데이터 - 버퍼에 보관
										log.debug("불완전한 데이터, 버퍼에 보관: [{}]", bufferContent);
										
										// 버퍼가 너무 크면 초기화 (메모리 보호)
										if (dataBuffer.length() > 1000) {
											log.warn("버퍼 크기 초과, 초기화");
											dataBuffer.setLength(0);
										}
									}
								}
							} catch (java.net.SocketTimeoutException e) {
								// 타임아웃은 정상 - 데이터가 없는 것
								// 로깅하지 않고 계속 진행
							}
						}

						Thread.sleep(100); // CPU 사용량 감소

					} catch (IOException e) {
						log.error("소켓 통신 중 오류 발생", e);
						break;  // 내부 while 루프 종료
					} catch (InterruptedException e) {
						Thread.currentThread()
							  .interrupt();
						return;
					}
				}
			} catch (IOException e) {
				log.error("스트림 생성 중 오류 발생", e);
			} finally {
				closeSocket();
				waitForReconnection();
			}
		}
	}

	private void closeSocket() {
		try {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
			if (outputStream != null) {
				outputStream.close();
				outputStream = null;
			}
			if (socket != null) {
				socket.close();
				socket = null;
			}
			// 버퍼 초기화
			if (dataBuffer != null) {
				dataBuffer.setLength(0);
			}
		} catch (IOException e) {
			log.error("소켓 종료 중 오류", e);
		}
	}

	private void processScaleData(String data) {
		try {
			String value = parseScaleValue(data);
			Map<String, Object> dataMap = new HashMap<>();

			collectParameterNames.forEach(param -> {
				boolean isReset = param.contains("RESET");
				boolean isResetValue = resetValue.equals(value);

				if (isReset) {
					dataMap.put(param,
						isResetValue
						? 1
						: 0);
				} else {
					dataMap.put(param,
						isResetValue
						? null
						: formatValue(value));
				}
			});
			sendMessage(dataMap);
		} catch (Exception e) {
			log.error("스케일 데이터 처리 중 오류 발생: [{}] - {}", data, e.getMessage());
			// 예외 발생해도 컬렉터는 계속 동작하도록 함
		}
	}

	private double formatValue(String value) {
		// 이미 1000을 곱하는 로직이 있으므로 g 단위 처리는 여기서
		return Double.parseDouble(value) * 1000;
	}

	private String parseScaleValue(String data) throws Exception {
		try {
			String cleanData = data.trim();
			
			if (cleanData.contains("kg")) {
				String result = cleanData.split("kg")[0].trim(); // "ST,+000.4995"
				// 콤마가 있으면 콤마 뒤 부분만 사용
				if (result.contains(",")) {
					result = result.split(",")[1].trim(); // "+000.4995"
				}
				// + 제거
				if (result.startsWith("+")) {
					result = result.substring(1); // "000.4995"
				}
				return result;
			} else if (cleanData.contains("g")) {
				String result = cleanData.split("g")[0].trim();
				// 콤마가 있으면 콤마 뒤 부분만 사용  
				if (result.contains(",")) {
					result = result.split(",")[1].trim();
				}
				// + 제거
				if (result.startsWith("+")) {
					result = result.substring(1);
				}
				return result;
			} else {
				// 숫자만 있는 경우
				if (cleanData.matches("\\d+(\\.\\d+)?")) {
					return cleanData;
				}
			}
			
			// 파싱 불가능한 경우
			throw new Exception("파싱할 수 없는 데이터 형식: " + data);
			
		} catch (Exception e) {
			log.error("데이터 파싱 실패: [{}] - {}", data, e.getMessage());
			throw new Exception("Invalid scale data format", e);
		}
	}

	private void sendLiveCheckMessage() throws IOException {
		if (outputStream == null) {
			outputStream = socket.getOutputStream();
		}
		String checkMessage = "live check";
		outputStream.write(checkMessage.getBytes());
		outputStream.flush();
		log.debug("Health check 전송: {}", checkMessage);
	}

	private void sendMessage(Map<String, Object> dataMap) {
		if (dataMap.isEmpty()) {
			return;
		}

		try {
			SendMessage sendMessage = paramValuesToMessage(dataMap);
			sender.send(config.getTopic(), sendMessage);
		} catch (Exception e) {
			log.error("메시지 전송 중 오류 발생. DCP_ID: {}", config.getDcpId(), e);
			// 예외 발생해도 컬렉터는 계속 동작하도록 함
		}
	}

	private boolean connect() {
		try {
			if (socket != null && socket.isConnected() && !socket.isClosed()) {
				return true; // 이미 연결된 경우 리턴
			}
			
			log.info("연결 시도 중: {}:{}", ip, port);
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 3000);
			log.info("서버 연결 완료: {}:{}", ip, port);
			return true;
		} catch (java.net.ConnectException e) {
			log.error("연결 거부됨: {}:{} - {}", ip, port, e.getMessage());
			return false;
		} catch (java.net.SocketTimeoutException e) {
			log.error("연결 타임아웃: {}:{} - {}", ip, port, e.getMessage());
			return false;
		} catch (java.net.UnknownHostException e) {
			log.error("호스트를 찾을 수 없음: {}:{} - {}", ip, port, e.getMessage());
			return false;
		} catch (IOException e) {
			log.error("연결 오류: {}:{} - {}", ip, port, e.getMessage());
			return false;
		} catch (NumberFormatException e) {
			log.error("잘못된 포트 번호: {} - {}", port, e.getMessage());
			return false;
		}
	}

	private void waitForReconnection() {
		try {
			Thread.sleep(1000); // 1초 대기 후 재연결 시도
		} catch (InterruptedException e) {
			Thread.currentThread()
				  .interrupt();
			log.error("재연결 대기 중 인터럽트 발생", e);
		}
	}

	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		return SendMessage.createMergedMetadataMessage(config, System.currentTimeMillis(), paramsValueMap);
	}

	@Override
	public DataCollectPlan getConfig() {
		return config;
	}
}
