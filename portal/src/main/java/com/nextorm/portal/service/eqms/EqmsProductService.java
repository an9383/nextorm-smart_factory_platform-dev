package com.nextorm.portal.service.eqms;

import com.nextorm.portal.client.eqms.EqmsApiClient;
import com.nextorm.portal.client.eqms.EqmsProductsResponse;
import com.nextorm.portal.dto.eqms.EqmsMoldProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EqmsProductService {
	private final EqmsApiClient eqmsApiClient;

	public List<EqmsMoldProductResponseDto> getProducts() {
		EqmsProductsResponse response = eqmsApiClient.getProducts();
		if (!response.isSuccess()) {
			return List.of();
		}

		return response.getData()
					   .stream()
					   .filter(it -> it.getExt() != null)
					   .filter(it -> it.getExt()
									   .containsKey("cavity"))
					   .filter(it -> it.getExt()
									   .containsKey("averageProductionCount"))
					   .map(it -> EqmsMoldProductResponseDto.builder()
															.id(it.getId())
															.name(it.getName())
															.code(it.getDescription())
															.cavity(Integer.parseInt(it.getExt()
																					   .get("cavity")
																					   .toString()))
															.cycleCount(Integer.parseInt(it.getExt()
																						   .getOrDefault("cycleCount",
																							   "0")
																						   .toString()))
															.averageProductionCount(Integer.parseInt(it.getExt()
																									   .get(
																										   "averageProductionCount")
																									   .toString()))
															.build())
					   .toList();
	}

}
