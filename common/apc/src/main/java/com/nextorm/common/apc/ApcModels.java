package com.nextorm.common.apc;

import com.nextorm.common.apc.constant.ApcConstant;
import com.nextorm.common.apc.entity.ApcModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ApcModels {
	private static final String KEY_DELIMITER = ApcConstant.SPLIT_DELIMITER;

	private final List<ApcModel> models;

	public ApcModels(List<ApcModel> models) {
		this.models = models;
	}

	/**
	 * 가장 일치도가 높은 모델을 찾는다
	 */
	public Optional<ApcModel> findMostSpecificMatch(String targetCondition) {
		return models.stream()
					 .map(model -> new ModelScore(model,
						 ApcModels.calculateSpecificityScore(targetCondition, model.getCondition())))
					 .filter(modelScore -> modelScore.score >= 0)
					 .peek(modelScore -> log.info("model.id {}, model.condition: {}, score: {}",
						 modelScore.model.getId(),
						 modelScore.model.getCondition(),
						 modelScore.score))
					 .max(Comparator.comparingInt(modelScore -> modelScore.score))
					 .map(modelScore -> modelScore.model);
	}

	/**
	 * 컨디션에 맞는 모든 모델을 찾는다
	 */
	public List<ApcModel> findMatchedModels(String targetCondition) {
		return models.stream()
					 .filter(model -> ApcModels.calculateSpecificityScore(model.getCondition(), targetCondition) >= 0)
					 .toList();
	}

	/**
	 * target 문자열과 model 문자열 간의 정확도 점수를 계산합니다.
	 *
	 * @param target 매칭할 대상 문자열
	 * @param key    매칭할 키 문자열
	 * @return 정확도 점수, 일치하지 않으면 -1 반환
	 */
	public static int calculateSpecificityScore(
		String target,
		String key
	) {
		String[] targetParts = target.split(KEY_DELIMITER);
		String[] keyParts = key.split(KEY_DELIMITER);
		int targetPartsLength = targetParts.length;

		log.info("targetParts: {}, keyParts: {}", targetParts.length, keyParts.length);

		int score = 0;
		for (int i = 0; i < targetPartsLength; i++) {
			String targetPart = targetParts[i];
			String keyPart = keyParts[i];

			if (keyPart.equals("*")) {
				continue;
			}

			if (targetPart.equals(keyPart)) {
				score += (targetPartsLength - i) * 2; // 더 높은 가중치 부여
			} else if (targetPart.matches(keyPart.replace("*", ".*"))) {
				score += (targetPartsLength - i); // 앞쪽 파츠에 더 높은 가중치 부여
			} else {
				return -1;
			}
		}
		log.info("target: {}, key: {}, score: {}", target, key, score);
		return score;
	}

	private record ModelScore(ApcModel model, int score) {
	}
}
