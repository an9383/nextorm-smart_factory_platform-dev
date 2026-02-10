package com.nextorm.portal.client.eqms;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class EqmsProductsResponse {
	private boolean success;
	private String message;
	private List<Product> data;

	public static EqmsProductsResponse success(List<Product> products) {
		EqmsProductsResponse response = new EqmsProductsResponse();
		response.success = true;
		response.data = products;
		return response;
	}

	public static EqmsProductsResponse failure(String message) {
		EqmsProductsResponse response = new EqmsProductsResponse();
		response.success = false;
		response.message = message;
		return response;
	}

	@Getter
	@Builder
	public static class Product {
		private Long id;
		private String name;
		private String description;
		private Map<String, Object> ext;
	}
}
