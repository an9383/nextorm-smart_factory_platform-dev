package com.nextorm.portal.service.location;

import com.nextorm.common.db.entity.Location;
import com.nextorm.common.db.repository.LocationRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.location.LocationErrorCode;
import com.nextorm.portal.common.exception.location.LocationNotFoundException;
import com.nextorm.portal.common.exception.location.ParentLocationNotFoundException;
import com.nextorm.portal.dto.location.LocationCreateRequestDto;
import com.nextorm.portal.dto.location.LocationModifyRequestDto;
import com.nextorm.portal.service.LocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class LocationExceptionTest {
	@InjectMocks
	LocationService locationService;

	@Mock
	LocationRepository locationRepository;

	@Mock
	ToolRepository toolRepository;

	@DisplayName("create 요청 시 Parent Id를 잘못 요청한 경우 : ParentLocationNotFoundException")
	@Test
	void givenLocationThenLocationDuplicateExceptionWhenCreateLocation() {

		LocationCreateRequestDto request = new LocationCreateRequestDto(Location.Type.FAB,
			"test-fab",
			"dd",
			13L,
			1,
			3.33,
			5.55);

		assertThatThrownBy(() -> locationService.create(request)).isInstanceOf(ParentLocationNotFoundException.class)
																 .hasMessage(LocationErrorCode.PARENT_LOCATION_NOT_FOUND.getMessage());

	}

	@DisplayName("modify 요청 시 LocationId를 잘못 요청한 경우: LocationUnProcessableException")
	@Test
	void givenIdAndModifyRequestDtoThenLocationUnProcessableExceptionWhenModifyLocation() {
		LocationModifyRequestDto request = new LocationModifyRequestDto("test", "test", 1, 1.1, 2.2);

		assertThatThrownBy(() -> locationService.modify(1111111L,
			request)).isInstanceOf(LocationNotFoundException.class)
					 .hasMessage(LocationErrorCode.LOCATION_NOT_FOUND.getMessage());
	}

	@DisplayName("delete 요청 시 LocationId를 잘못 요청한 경우: LocationUnProcessableException")
	@Test
	void givenIdThenLocationUnProcessableExceptionWhenDeleteLocation() {

		assertThatThrownBy(() -> locationService.delete(1111111L)).isInstanceOf(LocationNotFoundException.class)
																  .hasMessage(LocationErrorCode.LOCATION_NOT_FOUND.getMessage());
	}

}
