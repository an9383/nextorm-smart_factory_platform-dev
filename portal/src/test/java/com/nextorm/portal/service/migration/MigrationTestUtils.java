package com.nextorm.portal.service.migration;

import com.nextorm.common.db.entity.Parameter;

import java.time.LocalDateTime;

public class MigrationTestUtils {
	public static final String SMALL_CSV_PATH = "csv/ecobot_2024-02-17_ecobot_status_temp.csv";
	public static final int SMALL_DATA_SIZE = 4359;
	public static final LocalDateTime SMALL_DATA_START_AT = LocalDateTime.of(2000, 2, 17, 22, 45, 20, 001);
	public static final LocalDateTime SMALL_DATA_END_AT = LocalDateTime.of(2000, 2, 18, 0, 0, 0, 0);

	public static Parameter createNoSpecParameter(
		Long id,
		String name
	) {
		return Parameter.builder()
						.id(id)
						.name(name)
						.dataType(Parameter.DataType.DOUBLE)
						.isSpecAvailable(false)
						.build();
	}
}
