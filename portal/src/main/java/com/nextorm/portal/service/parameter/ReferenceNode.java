package com.nextorm.portal.service.parameter;

import java.util.List;

public record ReferenceNode(Long id, List<Long> requiredIds) {
}
