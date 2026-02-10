package com.nextorm.portal.service.rule;

import com.nextorm.common.db.entity.Rule;
import com.nextorm.common.db.repository.DcpConfigRepository;
import com.nextorm.common.db.repository.RuleRepository;
import com.nextorm.portal.common.exception.rule.RuleErrorCode;
import com.nextorm.portal.common.exception.rule.RuleNameDuplicationException;
import com.nextorm.portal.common.exception.rule.RuleNotFoundException;
import com.nextorm.portal.dto.rule.RuleCreateRequestDto;
import com.nextorm.portal.dto.rule.RuleUpdateRequestDto;
import com.nextorm.portal.service.RuleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleExceptionTest {

	@InjectMocks
	RuleService ruleService;

	@Mock
	RuleRepository ruleRepository;

	@Mock
	DcpConfigRepository dcpConfigRepository;
	
	@DisplayName("createRule 요청 시 중복된 Rule Name을 요청했을 경우: RuleNameDuplicationException")
	@Test
	void givenRuleCreateRequestDtoThenRuleNameDuplicationExceptionWhenCreateRule() {

		Rule entity = Rule.builder()
						  .name("test-rule")
						  .build();

		when(ruleRepository.findByName(entity.getName())).thenReturn(Optional.of(entity));

		RuleCreateRequestDto ruleCreateRequestDto = RuleCreateRequestDto.builder()
																		.name("test-rule")
																		.build();

		assertThatThrownBy(() -> ruleService.createRule(ruleCreateRequestDto)).isInstanceOf(RuleNameDuplicationException.class)
																			  .hasMessage(RuleErrorCode.RULE_NAME_DUPLICATION.getMessage());
	}

	@DisplayName("modifyRule 요청 시 잘못된 RuleId를 요청했을 경우: RuleNotFoundException")
	@Test
	void givenIdThenRuleNotFoundExceptionWhenModifyRule() {
		RuleUpdateRequestDto request = RuleUpdateRequestDto.builder()
														   .name("test")
														   .build();

		assertThatThrownBy(() -> ruleService.modifyRule(111111L, request)).isInstanceOf(RuleNotFoundException.class)
																		  .hasMessage(RuleErrorCode.RULE_NOT_FOUND_EXCEPTION.getMessage());
	}
}
