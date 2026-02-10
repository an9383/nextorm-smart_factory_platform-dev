package com.nextorm.extensions.scheduler.task.kcs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceFactory {
	public static DataSource createDataSource(
		String jdbcUrl,
		String username,
		String password,
		String driverClassName
	) {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(username);
		config.setPassword(password);
		config.setDriverClassName(driverClassName);
		return new HikariDataSource(config);
	}
}
