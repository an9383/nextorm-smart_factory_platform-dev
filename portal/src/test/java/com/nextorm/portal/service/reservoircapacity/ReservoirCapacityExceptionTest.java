package com.nextorm.portal.service.reservoircapacity;

import com.nextorm.common.db.entity.Location;
import com.nextorm.common.db.entity.ReservoirCapacity;
import com.nextorm.common.db.repository.LocationRepository;
import com.nextorm.common.db.repository.ReservoirCapacityRepository;
import com.nextorm.portal.common.exception.location.LocationErrorCode;
import com.nextorm.portal.common.exception.location.RelateLocationNotFoundException;
import com.nextorm.portal.common.exception.reservoircapacity.ReservoirCapacityDuplicationException;
import com.nextorm.portal.common.exception.reservoircapacity.ReservoirCapacityErrorCode;
import com.nextorm.portal.common.exception.reservoircapacity.ReservoirCapacityNotFoundException;
import com.nextorm.portal.dto.reservoircapacity.ReservoirCapacityCreateRequestDto;
import com.nextorm.portal.service.ReservoirCapacityService;
import com.nextorm.portal.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservoirCapacityExceptionTest {
	@InjectMocks
	ReservoirCapacityService reservoirCapacityService;

	@Mock
	ReservoirCapacityRepository reservoirCapacityRepository;

	@Mock
	LocationRepository locationRepository;

	@Mock
	WeatherService weatherService;

	LocalDateTime dateTime = LocalDateTime.of(2024, 11, 20, 14, 0);

	@DisplayName("getReservoirCapacity요청 시 잘못된 Id가 요청 될 경우: ReservoirCapacityUnProcessableException")
	@Test
	void givenIdThenReservoirCapacityUnProcessableExceptionWhengetReservoirCapacity() {
		assertThatThrownBy(() -> reservoirCapacityService.getReservoirCapacity(111111111L)).isInstanceOf(
																							   ReservoirCapacityNotFoundException.class)
																						   .hasMessage(
																							   ReservoirCapacityErrorCode.RESERVOIR_CAPACITY_NOT_FOUND.getMessage());
	}

	@DisplayName("createReservoirCapacity 요청 시 잘못된 LocationId를 요청했을 경우: RelateLocationNotFoundException")
	@Test
	void givenReservoirCapacityCreateRequestDtoThenRelateLocationNotFoundExceptionWhencreateReservoirCapacity() {
		ReservoirCapacityCreateRequestDto request = ReservoirCapacityCreateRequestDto.builder()
																					 .locationId(1111111111L)
																					 .build();

		assertThatThrownBy(() -> reservoirCapacityService.createReservoirCapacity(request)).isInstanceOf(
																							   RelateLocationNotFoundException.class)
																						   .hasMessage(LocationErrorCode.RELATE_LOCATION_NOT_FOUND.getMessage());
	}

	@DisplayName("createReservoirCapacity 요청 시 중복된 ReservoirCapacity인 경우: ReservoirCapacityDuplicationException")
	@Test
	void givenReservoirCapacityCreateRequestDtoThenReservoirCapacityDuplicationExceptionWhenCreateReservoirCapacity() {

		Location location = Location.builder()
									.id(130L)
									.name("test-loc")
									.build();
		when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));

		ReservoirCapacity reservoirCapacity = ReservoirCapacity.builder()
															   .id(140L)
															   .location(location)
															   .date(dateTime)
															   .build();
		when(reservoirCapacityRepository.findByLocationIdAndDate(130L, dateTime)).thenReturn(reservoirCapacity);

		ReservoirCapacityCreateRequestDto request = ReservoirCapacityCreateRequestDto.builder()
																					 .locationId(130L)
																					 .date(dateTime)
																					 .build();

		assertThatThrownBy(() -> reservoirCapacityService.createReservoirCapacity(request)).isInstanceOf(
																							   ReservoirCapacityDuplicationException.class)
																						   .hasMessage(
																							   ReservoirCapacityErrorCode.RESERVOIR_CAPACITY_DUPLICATION.getMessage());
	}
}
