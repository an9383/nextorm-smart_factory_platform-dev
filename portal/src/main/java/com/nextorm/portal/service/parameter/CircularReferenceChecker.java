package com.nextorm.portal.service.parameter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CircularReferenceChecker {
	private final Set<Long> visited;
	private final Map<Long, ReferenceNode> allNodes;

	public CircularReferenceChecker(Map<Long, ReferenceNode> allNodes) {
		this.visited = new HashSet<>();
		this.allNodes = allNodes;
	}

	public boolean hasCircularReference(ReferenceNode node) {
		if (visited.contains(node.id())) {
			return true; // 순환 참조 발견
		}

		visited.add(node.id());

		for (Long requiredId : node.requiredIds()) {
			ReferenceNode nextNode = allNodes.get(requiredId);
			if (nextNode != null && hasCircularReference(nextNode)) {
				return true;
			}
		}

		visited.remove(node.id());
		return false;
	}
}
