package com.nextorm.apc.repository;

import com.nextorm.apc.scriptengine.ExternalSourceRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MockExternalSourceRepository implements ExternalSourceRepository {

	@Override
	public String sample() {
		return "FROM MockExternalSourceRepository String";
	}

}
