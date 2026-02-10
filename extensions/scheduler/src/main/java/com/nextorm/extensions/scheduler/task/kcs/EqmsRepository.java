package com.nextorm.extensions.scheduler.task.kcs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class EqmsRepository {
	private final JdbcTemplate eqmsJdbcTemplate;

	public List<EqmsProductDto> findMoldProducts() {
		String sql = "SELECT id, name, ext FROM product where ext like '%cavity%'";

		return eqmsJdbcTemplate.query(sql, (rs, rowNum) -> {
			return new EqmsProductDto(rs.getLong("id"), rs.getString("name"), rs.getString("ext"));
		});
	}

	public void updateProductExt(
		Long productId,
		String extJsonString
	) {
		String sql = "UPDATE product SET ext = ? WHERE id = ?";
		eqmsJdbcTemplate.update(sql, extJsonString, productId);
	}
}
