package com.nextorm.common.db.repository.template;

import java.util.List;

public interface LocationTemplateRepository {
	List<Long> findLocationUnderLineIds(Long locationId);
}
