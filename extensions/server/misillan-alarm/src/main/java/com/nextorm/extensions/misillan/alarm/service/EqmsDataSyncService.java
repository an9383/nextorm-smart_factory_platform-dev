package com.nextorm.extensions.misillan.alarm.service;

import com.nextorm.extensions.misillan.alarm.client.EqmsPortalClient;
import com.nextorm.extensions.misillan.alarm.entity.EqmsProduct;
import com.nextorm.extensions.misillan.alarm.entity.EqmsTool;
import com.nextorm.extensions.misillan.alarm.repository.EqmsProductRepository;
import com.nextorm.extensions.misillan.alarm.repository.EqmsToolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EqmsDataSyncService {
	private final EqmsPortalClient eqmsPortalClient;
	private final EqmsToolRepository eqmsToolRepository;
	private final EqmsProductRepository eqmsProductRepository;

	@Transactional
	public void syncTool() {
		List<EqmsPortalClient.ToolDto> eqmsTools = eqmsPortalClient.getTools();
		Map<Long, EqmsTool> tools = eqmsToolRepository.findAll()
													  .stream()
													  .collect(Collectors.toMap(EqmsTool::getId, param -> param));

		// 기존에 저장된 것이 있는지 확인
		// 있으면 업데이트 시간을 보고 변경된 것 같으면 갱신처리
		eqmsTools.forEach(toolDto -> {
			EqmsTool tool = tools.get(toolDto.getId());

			EqmsTool convertedParam = EqmsTool.builder()
											  .id(toolDto.getId())
											  .name(toolDto.getName())
											  .updatedAt(toolDto.getUpdatedAt())
											  .build();

			if (tool == null) {
				eqmsToolRepository.save(convertedParam);
			} else if (tool.getUpdatedAt()
						   .isBefore(toolDto.getUpdatedAt())) {
				tool.modify(convertedParam);
			}
		});
	}

	@Transactional
	public void syncProduct() {
		List<EqmsPortalClient.ProductDto> eqmsProducts = eqmsPortalClient.getProducts();
		Map<Long, EqmsProduct> products = eqmsProductRepository.findAll()
															   .stream()
															   .collect(Collectors.toMap(EqmsProduct::getId,
																   param -> param));

		// 기존에 저장된 것이 있는지 확인
		// 있으면 업데이트 시간을 보고 변경된 것 같으면 갱신처리
		eqmsProducts.forEach(productDto -> {
			EqmsProduct product = products.get(productDto.getId());

			EqmsProduct convertedParam = EqmsProduct.builder()
													.id(productDto.getId())
													.name(productDto.getName())
													.updatedAt(productDto.getUpdatedAt())
													.build();

			if (product == null) {
				eqmsProductRepository.save(convertedParam);
			} else if (product.getUpdatedAt()
							  .isBefore(productDto.getUpdatedAt())) {
				product.modify(convertedParam);
			}
		});
	}
}