package com.nextorm.extensions.scheduler.task.nissei;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 데이터베이스 작업의 실제 구현체
 */
@Slf4j
@RequiredArgsConstructor
public class TraceDataRepositoryImpl implements TraceDataRepository {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public Optional<TraceRaw> findLatestTraceRaw(String toolName) {
		String sql = """
			select p.id as parameterId, trd.id as id, trd.trace_raw_data as trace_raw_data, trd.trace_dt as trace_dt
			from tool t
			left join parameter p on t.id = p.tool_id
			left join trace_raw_data trd on p.id = trd.parameter_id
			where t.name = ?
			""";

		TraceRaw result = jdbcTemplate.query(sql, rs -> {
			if (rs.next()) {
				Long parameterId = rs.getLong("parameterId");
				Long id = rs.getLong("id");
				String value = rs.getString("trace_raw_data");

				Timestamp traceDt = rs.getTimestamp("trace_dt");
				LocalDateTime convertedTraceDt = traceDt == null
												 ? null
												 : traceDt.toLocalDateTime();

				return new TraceRaw(parameterId, id, value, convertedTraceDt);
			}
			return null;
		}, toolName);

		return Optional.ofNullable(result);
	}

	@Override
	public void insertAllToHistory(
		Long parameterId,
		List<ShotData> shotDataList
	) {
		String historySql = "INSERT INTO trace_raw_data_h(parameter_id, trace_raw_data, trace_dt, create_at) VALUES (?, ?, ?, ?)";
		List<Object[]> historyArgs = shotDataList.stream()
												 .map(sd -> new Object[] {parameterId, String.valueOf(sd.shotCount()),
													 Timestamp.valueOf(sd.traceDt()),
													 Timestamp.valueOf(LocalDateTime.now())})
												 .toList();

		log.debug("히스토리 데이터 일괄 삽입 시작: {} 건", shotDataList.size());
		LocalDateTime start = LocalDateTime.now();

		jdbcTemplate.batchUpdate(historySql, historyArgs, 1000, (ps, args) -> {
			ps.setLong(1, (Long)args[0]);
			ps.setString(2, (String)args[1]);
			ps.setTimestamp(3, (Timestamp)args[2]);
			ps.setTimestamp(4, (Timestamp)args[3]);
		});

		log.debug("히스토리 데이터 일괄 삽입 완료: {} ms",
			java.time.Duration.between(start, LocalDateTime.now())
							  .toMillis());
	}

	@Override
	public void insertLastToRaw(
		Long parameterId,
		ShotData lastShot
	) {
		String insertSql = "INSERT INTO trace_raw_data(parameter_id, trace_raw_data, trace_dt, create_at) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(insertSql,
			parameterId,
			String.valueOf(lastShot.shotCount()),
			Timestamp.valueOf(lastShot.traceDt()),
			Timestamp.valueOf(LocalDateTime.now()));

		log.debug("Raw 데이터 삽입 완료: parameterId={}, shotCount={}", parameterId, lastShot.shotCount());
	}

	@Override
	public void updateLastToRaw(
		Long parameterId,
		ShotData lastShot
	) {
		String updateSql = "UPDATE trace_raw_data SET trace_raw_data = ?, trace_dt = ? WHERE parameter_id = ?";
		jdbcTemplate.update(updateSql,
			String.valueOf(lastShot.shotCount()),
			Timestamp.valueOf(lastShot.traceDt()),
			parameterId);

		log.debug("Raw 데이터 업데이트 완료: parameterId={}, shotCount={}", parameterId, lastShot.shotCount());
	}
}
