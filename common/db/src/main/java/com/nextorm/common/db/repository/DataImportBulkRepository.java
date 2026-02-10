package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ParameterData;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DataImportBulkRepository {
	private final JdbcTemplate jdbcTemplate;

	public void saveAll(List<ParameterData> parameters) {
		String sql = """
						INSERT INTO 
							parameter_data(parameter_id, d_value, s_value, i_value, trace_at) 
							values(?, ?, ?, ?, ?)
			""";
		jdbcTemplate.batchUpdate(sql,
			parameters,
			parameters.size(),
			(PreparedStatement ps, ParameterData parameterData) -> {
				ps.setLong(1, parameterData.getParameterId());
				ps.setDouble(2, parameterData.getDValue());
				ps.setString(3, parameterData.getSValue());
				if (parameterData.getIValue() == null) {
					ps.setNull(4, Types.INTEGER);
				} else {
					ps.setInt(4, parameterData.getIValue());
				}
				ps.setTimestamp(5, Timestamp.valueOf(parameterData.getTraceAt()));
			});

	}
}
