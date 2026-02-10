package com.nextorm.extensions.scheduler.task.kcs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SfpRepository {
	private final JdbcTemplate jdbcTemplate;

	private static final RowMapper<SfpParameterDto> PARAMETER_ROW_MAPPER = (rs, rowNum) -> new SfpParameterDto(rs.getLong(
		"id"), rs.getString("name"), rs.getLong("tool_id"));

	private static final RowMapper<SfpParameterDataDto> PARAMETER_DATA_ROW_MAPPER = (rs, rowNum) -> new SfpParameterDataDto(
		rs.getLong("parameter_id"),
		rs.getTimestamp("trace_at")
		  .toLocalDateTime(),
		rs.getString("data_type"),
		rs.getDouble("d_value"),
		rs.getInt("i_value"),
		rs.getString("s_value"));

	public List<SfpParameterDto> findAllParameters(String name) {
		String sql = "SELECT id, name, tool_id FROM parameter WHERE name = ? ORDER BY id";
		List<SfpParameterDto> results = jdbcTemplate.query(sql, PARAMETER_ROW_MAPPER, name);
		return results;
	}

	/**
	 * 특정 파라미터 ID와 날짜 범위에 해당하는 파라미터 데이터 조회
	 */
	public List<SfpParameterDataDto> findParameterDataByIdAndDateRange(
		Long parameterId,
		LocalDateTime from,
		LocalDateTime to
	) {
		String sql = """
				SELECT parameter_id, trace_at, data_type, d_value, i_value, s_value 
					FROM parameter_data 
					WHERE parameter_id = ? AND trace_at BETWEEN ? AND ? 
					ORDER BY trace_at
			""";
		return jdbcTemplate.query(sql, PARAMETER_DATA_ROW_MAPPER, parameterId, from, to);
	}

	/**
	 * 특정 파라미터 ID와 traceAt 목록에 해당하는 파라미터 데이터 조회
	 */
	public List<SfpParameterDataDto> findParameterDataByIdAndTraceAtList(
		Long parameterId,
		List<LocalDateTime> traceAtList
	) {
		if (traceAtList == null || traceAtList.isEmpty()) {
			log.debug("traceAt 목록이 비어있어 빈 결과 반환");
			return List.of();
		}

		// IN 절을 위한 플레이스홀더 생성 (?, ?, ?, ...)
		String placeholders = String.join(",",
			"?".repeat(traceAtList.size())
			   .split(""));

		String sql = """
				SELECT parameter_id, trace_at, data_type, d_value, i_value, s_value 
					FROM parameter_data
					WHERE parameter_id = ? AND trace_at IN ( %s ) 
					ORDER BY trace_at
			""".formatted(placeholders);

		// 파라미터 배열 생성: [parameterId, traceAt1, traceAt2, ...]
		Object[] params = new Object[traceAtList.size() + 1];
		params[0] = parameterId;
		for (int i = 0; i < traceAtList.size(); i++) {
			params[i + 1] = traceAtList.get(i);
		}
		return jdbcTemplate.query(sql, PARAMETER_DATA_ROW_MAPPER, params);
	}
}
