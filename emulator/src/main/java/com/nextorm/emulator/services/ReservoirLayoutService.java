package com.nextorm.emulator.services;

import com.nextorm.common.db.entity.ReservoirLayout;
import com.nextorm.common.db.repository.ReservoirLayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservoirLayoutService {
	private final ReservoirLayoutRepository reservoirLayoutRepository;

	public List<ReservoirLayout> getReservoirLayout(Long id) {
		return reservoirLayoutRepository.findByToolId(id);
	}
}
