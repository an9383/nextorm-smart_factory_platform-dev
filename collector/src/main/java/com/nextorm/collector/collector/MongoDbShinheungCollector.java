package com.nextorm.collector.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.nextorm.collector.properties.MongoDbCollectorProperties;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.*;

@Slf4j
public class MongoDbShinheungCollector implements Collector {
	private final ObjectMapper mapper;
	private final DataCollectPlan config;
	private final Sender sender;
	private final MongoDbCollectorProperties mongoDbCollectorProperties;
	private String mongoDBUrl;
	private String dbName;
	private final String L1SIGNAL_ACTIVE_COLLECTION = "L1Signal_Pool_Active";
	private final String L1Signal_POOL_COLLECTION = "L1Signal_Pool";
	private final String PRODUCT_RESULT_HISTORY_COLLECTION = "ProductResult_History";
	ConnectionString connectionString;
	MongoClientSettings settings;
	MongoClient mongoClient;
	MongoDatabase db;
	MongoCollection<Document> collection;
	Document allQuery;
	Document fields;

	HashMap<String, Object> paramValueMap = null;

	private final List<String> collectParameterNames;

	public MongoDbShinheungCollector(
		DataCollectPlan config,
		ObjectMapper objectMapper,
		Sender sender,
		MongoDbCollectorProperties mongoDbCollectorProperties
	) {
		this.mapper = objectMapper;
		this.config = config;
		this.sender = sender;
		this.collectParameterNames = config.getCollectParameters()
										   .stream()
										   .map(DataCollectPlan.Parameter::getName)
										   .toList();
		this.mongoDbCollectorProperties = mongoDbCollectorProperties;
		mongoDBUrl = mongoDbCollectorProperties.getUrl();
		dbName = mongoDbCollectorProperties.getDbName();
		mongoDBConnection(L1SIGNAL_ACTIVE_COLLECTION, mongoDBUrl);
	}

	private void mongoDBConnection(
		String mongoCollection,
		String mongoDBUrl
	) {
		if (this.mongoClient == null) {
			connectionString = new ConnectionString(mongoDBUrl);
			settings = MongoClientSettings.builder()
										  .applyConnectionString(connectionString)
										  .build();
			this.mongoClient = MongoClients.create(settings);
		}
		db = mongoClient.getDatabase(dbName);
		collection = db.getCollection(mongoCollection);
	}

	@Override
	public void run() {
		while (!Thread.currentThread()
					  .isInterrupted()) {
			try {
				Map<String, Object> data = getData();
				if (!data.isEmpty()) {
					SendMessage sendMessage = paramValuesToMessage(data);
					sender.send(config.getTopic(), sendMessage);
				}
			} catch (RuntimeException e) {
				log.error("컬렉터 종료 가능 에러가 발생. DCP_ID: {}", config.getDcpId(), e);
			}

			try {
				Thread.sleep(config.getDataInterval() * 1000L);
			} catch (InterruptedException e) {
				Thread.currentThread()
					  .interrupt();
				// throw new RuntimeException(e);
			}

		}
	}

	private Map<String, Object> getData() {
		String toolName = config.getToolName();

		Map<String, Object> data = new HashMap<>();
		getL1SignalActiveData(data, toolName);
		getProductResult(data, toolName);
		getUpTime(data, toolName);
		getStopTime(data, toolName);
		getProductLeadTime(data, toolName);

		for (String name : collectParameterNames) {
			Object value = data.get(name);
			data.put(name, value);
		}

		return data;
	}

	private void getProductLeadTime(
		Map<String, Object> data,
		String toolName
	) {
		Date[] dates = getStartAndEndDates();
		List<Document> pipeline = Arrays.asList(new Document("$match",
				new Document("updatedate", new Document("$gte", dates[0]).append("$lt", dates[1])).append("L1Name",
					toolName)),
			new Document("$sort", new Document("updatedate", -1)),
			new Document("$limit", 1),
			new Document("$project", new Document("_id", 0).append("timespan", 1)));
		aggregateAndStoreData(data,
			toolName,
			"timespan",
			"ProductLeadTime",
			PRODUCT_RESULT_HISTORY_COLLECTION,
			pipeline);
	}

	private void getStopTime(
		Map<String, Object> data,
		String toolName
	) {
		Date[] dates = getStartAndEndDates();
		List<Document> pipeline = Arrays.asList(new Document("$match",
				new Document("updatedate", new Document("$gte", dates[0]).append("$lt", dates[1])).append("signalname",
																									  new Document("$in", Arrays.asList("STOP", "SUSPEND", "EMERGENCY")))
																								  .append("L1Name",
																									  toolName)
																								  .append("value", true)
																								  .append("timespan",
																									  new Document("$gt",
																										  1L))),
			new Document("$group",
				new Document("_id", null).append("daily_stop_time_total", new Document("$sum", "$timespan"))),
			new Document("$project", new Document("_id", 0).append("daily_stop_time_total", 1)));
		aggregateAndStoreData(data, toolName, "daily_stop_time_total", "StopTime", L1Signal_POOL_COLLECTION, pipeline);
	}

	private void getUpTime(
		Map<String, Object> data,
		String toolName
	) {
		Date[] dates = getStartAndEndDates();
		List<Document> pipeline = Arrays.asList(new Document("$match",
				new Document("updatedate", new Document("$gte", dates[0]).append("$lt", dates[1])).append("signalname",
																									  "OPERATE")
																								  .append("L1Name",
																									  toolName)
																								  .append("value", true)),
			new Document("$group",
				new Document("_id", null).append("daily_operate_time_total", new Document("$sum", "$timespan"))),
			new Document("$project", new Document("_id", 0).append("daily_operate_time_total", 1)));
		aggregateAndStoreData(data, toolName, "daily_operate_time_total", "UpTime", L1Signal_POOL_COLLECTION, pipeline);
	}

	private void getProductResult(
		Map<String, Object> data,
		String toolName
	) {
		Date[] dates = getStartAndEndDates();
		List<Document> pipeline = Arrays.asList(new Document("$match",
				new Document("updatedate", new Document("$gte", dates[0]).append("$lt", dates[1])).append("productresult",
																									  1)
																								  .append("L1Name",
																									  toolName)),
			new Document("$group",
				new Document("_id", null).append("productresult_total", new Document("$sum", "$productresult"))),
			new Document("$project", new Document("_id", 0).append("productresult_total", 1)));

		aggregateAndStoreData(data,
			toolName,
			"productresult_total",
			"ProductResult",
			PRODUCT_RESULT_HISTORY_COLLECTION,
			pipeline);
	}

	private void aggregateAndStoreData(
		Map<String, Object> data,
		String toolName,
		String fieldName,
		String resultKey,
		String collectionName,
		List<Document> pipeline
	) {
		mongoDBConnection(collectionName, mongoDBUrl);
		Iterable<Document> result = collection.aggregate(pipeline);
		Document firstResult = result.iterator()
									 .hasNext()
							   ? result.iterator()
									   .next()
							   : null;

		if (firstResult != null) {
			data.put(resultKey, firstResult.get(fieldName));
		} else {
			data.put(resultKey, Double.valueOf(0));
		}
	}

	private void getL1SignalActiveData(
		Map<String, Object> data,
		String toolName
	) {
		mongoDBConnection(L1SIGNAL_ACTIVE_COLLECTION, mongoDBUrl);
		fields = new Document();

		// paramValueMap = new HashMap<>();
		fields.append("L1Name", toolName);
		FindIterable<Document> result = collection.find(fields);
		Iterator<Document> iterator = result.iterator();

		Document document = new Document();
		while (iterator.hasNext()) {
			document = iterator.next();
			if (String.valueOf(document.getString("signalname"))
					  .contains("Diagnosis")) {
				continue;
			}
			if (document.get("value") != null) {
				String signalName = String.valueOf(document.getString("signalname"));

				String removeToolName = signalName.replace("_" + toolName, "");

				if (document.get("value") instanceof Number) {
					if (document.get("value") instanceof Double && ((Double)document.get("value")).isNaN()) {
						data.put(removeToolName, null);
					} else {
						data.put(removeToolName, document.get("value"));
					}
				} else {
					data.put(removeToolName, String.valueOf(document.get("value")));
				}
			}
		}
	}

	/*
		날짜 관련 공통 메서드
		오늘 00시부터 다음날 00시까지
	 */
	private Date[] getStartAndEndDates() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date startDate = calendar.getTime();
		calendar.add(Calendar.DATE, 1);
		Date endDate = calendar.getTime();
		return new Date[] {startDate, endDate};
	}

	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		int collectParameterSize = config.getCollectParameters()
										 .size();
		if (Boolean.TRUE.equals(config.getIsGeoDataType())) {
			collectParameterSize = collectParameterSize + 2;    // Geo Type 일때 파라미터 size 2 추가
		}
		if (paramsValueMap.size() != collectParameterSize) {
			String message = "수집대상 파라미터와 수집된 파라미터의 크기가 일치하지 않습니다. (parameter: %s, paramValue: %s)".formatted(
				collectParameterSize,
				paramsValueMap.size());
			throw new IllegalArgumentException(message);
		}

		return SendMessage.createMergedMetadataMessage(config, System.currentTimeMillis(), paramsValueMap);
	}

	@Override
	public DataCollectPlan getConfig() {
		return config;
	}
}
