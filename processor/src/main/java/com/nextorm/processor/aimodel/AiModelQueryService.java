package com.nextorm.processor.aimodel;

import com.nextorm.common.db.repository.AiModelRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Transactional(readOnly = true)
@Service
@Slf4j
@AllArgsConstructor
public class AiModelQueryService {
	private final AiModelRepository aiModelRepository;

	private final Map<Long, AiModelData> aiModelCache = new HashMap<>();

	public AiModelData findById(Long aiModelId) {
		if (aiModelCache.containsKey(aiModelId)) {
			return aiModelCache.get(aiModelId);
		}

		AiModelData aiModel = aiModelRepository.findById(aiModelId)
											   .map(AiModelData::of)
											   .orElse(null);
		if (aiModel != null) {
			aiModelCache.put(aiModelId, aiModel);
		}
		return aiModel;
	}
}
