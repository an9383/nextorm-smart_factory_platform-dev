package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.OcapAlarmRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcapAlarmRecipientRepository extends JpaRepository<OcapAlarmRecipient, Long> {
	List<OcapAlarmRecipient> findByOcapAlarmId(Long ocapAlarmId);

	void deleteByOcapAlarmId(Long ocapAlarmId);
}