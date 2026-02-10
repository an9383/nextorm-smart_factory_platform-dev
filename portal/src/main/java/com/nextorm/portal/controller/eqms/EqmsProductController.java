package com.nextorm.portal.controller.eqms;

import com.nextorm.portal.dto.eqms.EqmsMoldProductResponseDto;
import com.nextorm.portal.service.eqms.EqmsProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/eqms")
public class EqmsProductController {
	private final EqmsProductService eqmsProductService;

	@GetMapping("/mold-products")
	public List<EqmsMoldProductResponseDto> getEqmsProducts() {
		return eqmsProductService.getProducts();
	}
}
