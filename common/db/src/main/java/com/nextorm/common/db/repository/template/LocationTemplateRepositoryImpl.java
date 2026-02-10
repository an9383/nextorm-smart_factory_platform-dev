package com.nextorm.common.db.repository.template;

import com.nextorm.common.db.entity.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LocationTemplateRepositoryImpl implements LocationTemplateRepository {
	private final JdbcTemplate jdbcTemplate;

	@Override
	public List<Long> findLocationUnderLineIds(Long locationId) {

		String query = """
			with recursive cte as (
				select id, parent, name, type
				from location
				where id = ?
				union all
				select l.id, l.parent, l.name, l.type
				from location l
				join cte on l.parent = cte.id
			)
			select id
				from cte 
				where type = ?									
			""";

		return jdbcTemplate.query(query, pss -> {
			pss.setLong(1, locationId);
			pss.setString(2, Location.Type.LINE.name());
		}, (rs, rowNum) -> rs.getLong("id"));
	}
}
