package com.nextorm.processor.service;

import com.nextorm.common.db.entity.DcpConfig;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.DcpConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkerService {
	private final DcpConfigRepository dcpConfigRepository;
	private final JdbcTemplate jdbcTemplate;

	public List<ParameterData> getParameterDataByParameterIdInAndTraceAtGreaterThanEquals(
		List<Long> parameterIds,
		LocalDateTime baseDateTime
	) {
		String sql = """
			( SELECT parameter_id, d_value, i_value, s_value, trace_at, data_type
				FROM parameter_data 
				WHERE parameter_id = ? 
					AND trace_at >= ? 
				ORDER BY trace_at DESC 
				LIMIT 1 )
			""";

		String joinedQuery = String.join(" UNION ALL ",
			IntStream.range(0, parameterIds.size())
					 .mapToObj(i -> sql)
					 .toList());

		return jdbcTemplate.query(joinedQuery,
			pss -> setStatementParameters(pss, parameterIds, baseDateTime),
			(rs, rowNum) -> toParameterData(rs));
	}

	private void setStatementParameters(
		PreparedStatement pss,
		List<Long> parameterIds,
		LocalDateTime baseDateTime
	) throws SQLException {
		Timestamp timestamp = Timestamp.valueOf(baseDateTime);
		for (int i = 0; i < parameterIds.size(); i++) {
			int parameterIdIndex = i * 2 + 1;
			pss.setLong(parameterIdIndex, parameterIds.get(i));
			pss.setTimestamp(parameterIdIndex + 1, timestamp);
		}
	}

	private ParameterData toParameterData(ResultSet rs) throws SQLException {
		return ParameterData.builder()
							.parameterId(rs.getLong("parameter_id"))
							.dValue(rs.getDouble("d_value"))
							.iValue(rs.getInt("i_value"))
							.sValue(rs.getString("s_value"))
							.traceAt(rs.getTimestamp("trace_at")
									   .toLocalDateTime())
							.dataType(Parameter.DataType.valueOf(rs.getString("data_type")))
							.build();
	}

	public void updateLastCollectedAt(
		Long dcpId,
		LocalDateTime collectedAt
	) {
		dcpConfigRepository.updateLastCollectedAtById(dcpId, collectedAt);
	}

	public DcpConfigGeoInfo getDcpGeoInfo(Long dcpId) {
		DcpConfig dcpConfig = dcpConfigRepository.findById(dcpId)
												 .orElse(null);
		if (dcpConfig == null) {
			log.error("Dcp ID: {}의 GeoInfo 데이터 조회 결과가 없음", dcpId);
			return DcpConfigGeoInfo.builder()
								   .isGeoDataType(false)
								   .build();
		}
		return DcpConfigGeoInfo.builder()
							   .isGeoDataType(dcpConfig.isGeoDataType())
							   .latitudeParameterName(dcpConfig.getLatitudeParameterName())
							   .longitudeParameterName(dcpConfig.getLongitudeParameterName())
							   .build();
	}
}
