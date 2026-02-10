package com.nextorm.common.db.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

	@DisplayName("modify 메서드 호출시 데이터가 변경된다")
	@Test
	void modify_test() {
		// Given
		Location originalLocation = Location.builder()
											.type(Location.Type.SITE)
											.name("Original Name")
											.description("Original Description")
											.parent(null)
											.seq(1)
											.build();

		Location newLocation = Location.builder()
									   .name("New Name")
									   .description("New Description")
									   .seq(2)
									   .build();

		// When
		originalLocation.modify(newLocation);

		// Then
		assertThat(originalLocation.getName()).isEqualTo(newLocation.getName());
		assertThat(originalLocation.getDescription()).isEqualTo(newLocation.getDescription());
		assertThat(originalLocation.getSeq()).isEqualTo(newLocation.getSeq());
	}

	@Nested
	@DisplayName("Location Type enum 테스트")
	class LocationTypeTest {

		static Stream<Arguments> provideTypesForGetAncestorsTest() {
			return Stream.of(Arguments.of(Location.Type.LINE, List.of(Location.Type.SITE, Location.Type.FAB)),
				Arguments.of(Location.Type.FAB, List.of(Location.Type.SITE)),
				Arguments.of(Location.Type.SITE, List.of()));
		}

		@DisplayName("getAncestors 메서드는 올바른 조상 Type 리스트를 반환한다")
		@ParameterizedTest
		@MethodSource("provideTypesForGetAncestorsTest")
		void getAncestors_test(
			Location.Type type,
			List<Location.Type> expectedAncestors
		) {

			// When
			var ancestors = Location.Type.getAncestors(type);

			// Then
			assertThat(ancestors).containsAll(expectedAncestors);
		}
	}
}