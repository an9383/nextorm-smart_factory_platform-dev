package com.nextorm.common.db.repository.template;

import com.nextorm.common.db.BooleanToStringConverter;
import com.nextorm.common.db.entity.FaultHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FaultHistoryTemplateRepositoryImpl implements FaultHistoryTemplateRepository {
	private static final int BATCH_SIZE = 1000;

	private final JdbcTemplate jdbcTemplate;
	private final BooleanToStringConverter booleanToStringConverter;

	@Override
	public void bulkInsertFaultHistory(List<FaultHistory> faultDataList) {
		if (faultDataList.isEmpty()) {
			return;
		}

		final String sql = """
			INSERT INTO 
			 		fault_history (create_at, parameter_id, param_value, fault_at, 
						lsl, lcl, usl, ucl, 
						is_lsl_over, is_lcl_over, is_usl_over, is_ucl_over, is_spec_limit_over, is_ctrl_limit_over)
					values 
						(?, ?, ?, ?,
							?, ?, ?, ?, 
							?, ?, ?, ?, ?, ?)
			""";

		jdbcTemplate.batchUpdate(sql, faultDataList, BATCH_SIZE, (ps, faultData) -> {
			ps.setTimestamp(1, toTimestamp(LocalDateTime.now()));
			ps.setLong(2, faultData.getParameterId());
			ps.setString(3, faultData.getParamValue());
			ps.setTimestamp(4, Timestamp.valueOf(faultData.getFaultAt()));

			ps.setObject(5, faultData.getLsl(), java.sql.Types.DOUBLE);
			ps.setObject(6, faultData.getLcl(), java.sql.Types.DOUBLE);
			ps.setObject(7, faultData.getUsl(), java.sql.Types.DOUBLE);
			ps.setObject(8, faultData.getUcl(), java.sql.Types.DOUBLE);

			ps.setString(9, booleanToString(faultData.isLslOver()));
			ps.setString(10, booleanToString(faultData.isLclOver()));
			ps.setString(11, booleanToString(faultData.isUslOver()));
			ps.setString(12, booleanToString(faultData.isUclOver()));
			ps.setString(13, booleanToString(faultData.isSpecLimitOver()));
			ps.setString(14, booleanToString(faultData.isCtrlLimitOver()));
		});
	}

	private Timestamp toTimestamp(LocalDateTime localDateTime) {
		return Timestamp.valueOf(localDateTime);
	}

	private String booleanToString(boolean value) {
		return booleanToStringConverter.convertToDatabaseColumn(value);
	}
}
