package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.UiLanguage;
import com.nextorm.common.db.entity.UiLanguageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UiLanguageRepository extends JpaRepository<UiLanguage, UiLanguageId> {
	List<UiLanguage> findAllByLang(@Param("lang") String lang);

	@Modifying(clearAutomatically = true)
	@Query("DELETE from UiLanguage u where u.key in :keys")
	void deleteAllByKeyIn(@Param("keys") List<String> keys);
}