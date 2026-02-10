package com.nextorm.common.db.repository.template;

import com.nextorm.common.db.BooleanToStringConverter;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Repository
@RequiredArgsConstructor
public class ParameterDataTemplateRepositoryImpl implements ParameterDataTemplateRepository {
	private static final int BATCH_SIZE = 1000;

	private final JdbcTemplate jdbcTemplate;
	private final BooleanToStringConverter booleanToStringConverter;

	@Override
	public List<ParameterData> findLatestParameterData(
		List<Long> parameterIds,
		LocalDateTime baseDateTime
	) {

		String sql = """
			( SELECT parameter_id, d_value, i_value, s_value, image_value, trace_at, data_type, latitude_value, longitude_value
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
							.imageValue(rs.getString("image_value"))
							.traceAt(rs.getTimestamp("trace_at")
									   .toLocalDateTime())
							.dataType(Parameter.DataType.valueOf(rs.getString("data_type")))
							.latitudeValue(rs.getDouble("latitude_value"))
							.longitudeValue(rs.getDouble("longitude_value"))
							.build();
	}

	@Override
	public void bulkInsertParameterData(List<ParameterData> parameterDataList) {
		if (parameterDataList.isEmpty()) {
			return;
		}

		final String sql = """
				INSERT INTO 
					parameter_data (create_at, parameter_id, trace_at, data_type, latitude_value, longitude_value, 
						ucl, lcl, usl, lsl, is_ctrl_limit_over, is_spec_limit_over,
						s_value, d_value, i_value, image_value)
					values (?, ?, ?, ?, ?, ?, 
							?, ?, ?, ?, ?, ?,
							?, ?, ?, ?)
			""";

		jdbcTemplate.batchUpdate(sql, parameterDataList, BATCH_SIZE, (ps, parameterData) -> {
			Parameter.DataType dataType = parameterData.getDataType();

			ps.setObject(1, LocalDateTime.now());
			ps.setLong(2, parameterData.getParameterId());

			ps.setTimestamp(3, Timestamp.valueOf(parameterData.getTraceAt()));
			ps.setString(4, dataType.name());
			ps.setObject(5, parameterData.getLatitudeValue(), Types.DOUBLE);
			ps.setObject(6, parameterData.getLongitudeValue(), Types.DOUBLE);

			ps.setObject(7, parameterData.getUcl(), Types.DOUBLE);
			ps.setObject(8, parameterData.getLcl(), Types.DOUBLE);
			ps.setObject(9, parameterData.getUsl(), Types.DOUBLE);
			ps.setObject(10, parameterData.getLsl(), Types.DOUBLE);

			ps.setString(11, booleanToString(parameterData.isCtrlLimitOver()));
			ps.setString(12, booleanToString(parameterData.isSpecLimitOver()));

			ps.setString(13, parameterData.getSValue());
			ps.setObject(14, parameterData.getDValue(), Types.DOUBLE);
			ps.setObject(15, parameterData.getIValue(), Types.INTEGER);
			ps.setString(16, parameterData.getImageValue());
		});

	}

	private String booleanToString(boolean value) {
		return booleanToStringConverter.convertToDatabaseColumn(value);
	}

	@Override
	public void bulkInsertParameterDataRequireValue(List<ParameterData> parameterDataList) {
		if (parameterDataList.isEmpty()) {
			return;
		}

		final String sql = """
				INSERT INTO 
					parameter_data (create_at, parameter_id, trace_at, data_type, latitude_value, longitude_value, s_value, d_value, i_value, image_value)
					values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""";

		jdbcTemplate.batchUpdate(sql, parameterDataList, BATCH_SIZE, (ps, parameterData) -> {
			Parameter.DataType dataType = parameterData.getDataType();

			ps.setObject(1, LocalDateTime.now());
			ps.setLong(2, parameterData.getParameterId());

			ps.setTimestamp(3, Timestamp.valueOf(parameterData.getTraceAt()));
			ps.setString(4, dataType.name());
			ps.setObject(5, parameterData.getLatitudeValue(), java.sql.Types.DOUBLE);
			ps.setObject(6, parameterData.getLongitudeValue(), java.sql.Types.DOUBLE);

			ps.setString(7, parameterData.getSValue());
			ps.setObject(8, parameterData.getDValue(), java.sql.Types.DOUBLE);
			ps.setObject(9, parameterData.getIValue(), java.sql.Types.INTEGER);
			ps.setString(10, parameterData.getImageValue());
		});
	}

}
