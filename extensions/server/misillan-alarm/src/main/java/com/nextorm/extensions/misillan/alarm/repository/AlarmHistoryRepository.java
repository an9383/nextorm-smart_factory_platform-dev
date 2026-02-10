package com.nextorm.extensions.misillan.alarm.repository;

import com.nextorm.extensions.misillan.alarm.entity.AlarmHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmHistoryRepository extends JpaRepository<AlarmHistory, Long> {

	@Query("""
		    SELECT DISTINCT h
		    FROM AlarmHistory h
		    LEFT JOIN FETCH h.readUsers
		    WHERE h.alarmDt BETWEEN :startDt AND :endDt
		    ORDER BY h.alarmDt DESC
		""")
	List<AlarmHistory> findAllByAlarmDtBetween(
		@Param("startDt") LocalDateTime startDt,
		@Param("endDt") LocalDateTime endDt
	);

	@Query("SELECT DISTINCT h FROM AlarmHistory h LEFT JOIN FETCH h.readUsers WHERE h.id = :id")
	AlarmHistory findByIdWithReadUsers(@Param("id") Long id);

	@Query("""
		    SELECT DISTINCT ah
		    FROM AlarmHistory ah
		    LEFT JOIN FETCH ah.readUsers
		    WHERE NOT EXISTS (
		        SELECT 1
		        FROM AlarmReadUser aru
		        WHERE aru.alarmHistory = ah
		          AND aru.userId = :userId
		    )
		""")
	List<AlarmHistory> findUnreadAlarms(@Param("userId") Long userId);
}
