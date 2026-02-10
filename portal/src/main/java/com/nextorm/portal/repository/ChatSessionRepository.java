package com.nextorm.portal.repository;

import com.nextorm.portal.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
	@Query("""
		    SELECT c
		        FROM ChatSession c
		       WHERE c.userId = :userId 
		        ORDER BY c.updateAt DESC
		""")
	List<ChatSession> findByUserIdOrderByUpdateAtDesc(@Param("userId") String userId);
}