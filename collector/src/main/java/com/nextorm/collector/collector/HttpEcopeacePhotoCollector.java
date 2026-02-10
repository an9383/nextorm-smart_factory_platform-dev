package com.nextorm.collector.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.properties.PhotoCollectorProperties;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.UnresolvedAddressException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class HttpEcopeacePhotoCollector implements Collector {
	private final ObjectMapper mapper;
	private final DataCollectPlan collectPlan;
	private final Sender sender;
	private final PhotoCollectorProperties photoCollectorProperties;

	private final String photoParameterName;
	private final HttpClient httpClient;

	private final HttpRequest photoRequest;
	private final HttpRequest geoRequest;

	public HttpEcopeacePhotoCollector(
		DataCollectPlan collectPlan,
		ObjectMapper objectMapper,
		Sender sender,
		PhotoCollectorProperties photoCollectorProperties
	) {
		this.mapper = objectMapper;
		this.collectPlan = collectPlan;
		this.sender = sender;
		this.photoCollectorProperties = photoCollectorProperties;

		String photoApiUrl = collectPlan.getCollectorArguments()
										.get("photoApiUrl")
										.toString();

		String geoApiUrl = collectPlan.getCollectorArguments()
									  .get("geoApiUrl")
									  .toString();

		this.photoParameterName = collectPlan.getCollectParameters()
											 .get(0)
											 .getName();

		this.httpClient = HttpClient.newHttpClient();
		this.photoRequest = HttpRequest.newBuilder()
									   .uri(URI.create(photoApiUrl))
									   .build();
		this.geoRequest = HttpRequest.newBuilder()
									 .uri(URI.create(geoApiUrl))
									 .build();
	}

	@Override
	public void run() {
		while (!Thread.currentThread()
					  .isInterrupted()) {

			try {
				SendMessage collect = collect();
				sender.send(collectPlan.getTopic(), collect);
			} catch (RuntimeException e) {
				log.error("컬렉터 종료 가능 에러가 발생. DCP_ID: {}", collectPlan.getDcpId(), e);
			}

			try {
				Thread.sleep(collectPlan.getDataInterval() * 1000L);
			} catch (InterruptedException e) {
				Thread.currentThread()
					  .interrupt();
				// throw new RuntimeException(e);
			}
		}
	}

	public SendMessage collect() {
		int maxRetryCount = 5;
		int retryCount = 0;
		long retryInterval = 3000L;

		String photoImgPath = null;
		Map<String, String> goeDataIncludeMap;
		try {
			while (true) {
				try {
					photoImgPath = savePhotoAndGetImagePath(photoParameterName);
					goeDataIncludeMap = getGeoDataIncludeMap();
					break;
				} catch (ConnectException e) {
					log.warn("ConnectException 발생. 재시도합니다. 현재: {}, {}", retryCount, e.getMessage());

					if (photoImgPath != null) {
						Path fullPath = Paths.get(joinPath(photoCollectorProperties.getBaseDirectory(), photoImgPath));
						Files.deleteIfExists(fullPath);
						photoImgPath = null;
					}

					if (retryCount >= maxRetryCount) {
						String url = getFailedUrl(e);
						log.error("ConnectException 발생. 재시도 횟수 초과. 재시도를 종료합니다. url: {}", url);
						throw e;
					}
					retryCount++;
					Thread.sleep(retryInterval);
				}
			}
		} catch (UnresolvedAddressException | IOException | InterruptedException e) {
			log.error("collect Error, TOOL ID: {}, DCP ID: {}", collectPlan.getToolId(), collectPlan.getDcpId(), e);
			throw new RuntimeException(e);
		}

		String latitude = String.valueOf(goeDataIncludeMap.get(collectPlan.getLatitudeParameterName()));
		String longitude = String.valueOf(goeDataIncludeMap.get(collectPlan.getLongitudeParameterName()));

		Map<String, Object> paramsValueMap = Map.of(photoParameterName,
			photoImgPath,
			collectPlan.getLatitudeParameterName(),
			latitude,
			collectPlan.getLongitudeParameterName(),
			longitude);

		return SendMessage.createMergedMetadataMessage(collectPlan, System.currentTimeMillis(), paramsValueMap);
	}

	private String getFailedUrl(ConnectException e) {
		String thisClassName = this.getClass()
								   .getName();
		String url = String.format("%s or %s", photoRequest.uri(), geoRequest.uri());

		for (StackTraceElement stackTrace : e.getStackTrace()) {
			String className = stackTrace.getClassName();
			String methodName = stackTrace.getMethodName();

			if (className.equalsIgnoreCase(thisClassName)) {
				if (methodName.equalsIgnoreCase("savePhotoAndGetImagePath")) {
					url = photoRequest.uri()
									  .toString();
					break;
				} else if (methodName.equalsIgnoreCase("getGeoDataIncludeMap")) {
					url = geoRequest.uri()
									.toString();
					break;
				}
			}
		}

		return url;
	}

	private String savePhotoAndGetImagePath(String photoParameterName) throws IOException, InterruptedException {
		HttpResponse<byte[]> response = httpClient.send(photoRequest, HttpResponse.BodyHandlers.ofByteArray());
		List<String> contentTypes = response.headers()
											.map()
											.getOrDefault(HttpHeaders.CONTENT_TYPE, List.of());

		String extension = parseFileExtension(contentTypes, "jpg");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		String dateString = formatter.format(LocalDateTime.now());

		String fileName = createFileName(extension);
		String filePath = joinPath(photoParameterName.replace("-", "_"), dateString, fileName);
		String fullFilePath = joinPath(photoCollectorProperties.getBaseDirectory(), filePath);
		saveFile(fullFilePath, response.body());
		return filePath;
	}

	private String parseFileExtension(
		List<String> contentTypes,
		String defaultExtension
	) {
		String extension = defaultExtension;
		if (!contentTypes.isEmpty()) {
			String contentType = contentTypes.get(0);
			if (contentType.equalsIgnoreCase("png")) {
				extension = "png";
			}
		}
		return extension;
	}

	private String createFileName(String fileExtension) {
		String randomString = UUID.randomUUID()
								  .toString()
								  .split("-")[0];
		return String.format("%s_%s.%s", System.currentTimeMillis(), randomString, fileExtension);
	}

	private String joinPath(String... paths) {
		return String.join("/", paths);
	}

	private void saveFile(
		String filePath,
		byte[] body
	) throws IOException {
		Path path = Paths.get(filePath);
		Path directory = path.getParent();
		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}
		Files.write(path, body);
	}

	private Map<String, String> getGeoDataIncludeMap() throws IOException, InterruptedException {
		HttpResponse<byte[]> response = httpClient.send(geoRequest, HttpResponse.BodyHandlers.ofByteArray());

		if (response.statusCode() != HttpStatus.OK.value()) {
			throw new IllegalStateException("geo API 호출이 정상적으로 처리되지 않았습니다. (statusCode: " + response.statusCode() + ")");
		}

		byte[] body = response.body();

		List<Map<String, String>> dataList = mapper.readValue(body, List.class);
		Optional<Map<String, String>> latest = dataList.stream()
													   .max((o1, o2) -> {
														   LocalDateTime date1 = parseTimestamp(o1);
														   LocalDateTime date2 = parseTimestamp(o2);
														   return date1.compareTo(date2);
													   });

		if (latest.isEmpty()) {
			throw new IllegalStateException("geo API 결과로부터 데이터를 가져올 수 없습니다");
		}
		return latest.get();
	}

	private LocalDateTime parseTimestamp(Map<String, String> map) {
		String timestamp = map.get("timestamp");
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		return LocalDateTime.parse(timestamp, formatter);
	}

	@Override
	public DataCollectPlan getConfig() {
		return collectPlan;
	}
}
