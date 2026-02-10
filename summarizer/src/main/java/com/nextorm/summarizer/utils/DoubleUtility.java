package com.nextorm.summarizer.utils;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DoubleUtility {
	static final double[][] roundMap = new double[][] {{0, 1.0}, {1, 10.0}, {2, 100.0}, {3, 1000.0}, {4, 10000.0},
		{5, 100000.0}, {6, 1000000.0}, {7, 10000000.0}, {8, 100000000.0}, {9, 1000000000.0}};

	//특정 자리 숫자만큼 표시한다.
	public static double roundVal(
		Double value,
		int fraction
	) {
		return Math.round(value * roundMap[fraction][1]) / roundMap[fraction][1];
	}

	/*
		특정 자리 숫자만큼만 표시하고 버린다.
	 */
	public static Double floorVal(
		Double value,
		int fraction
	) {
		if (value == null) {
			return null;
		}
		return Math.floor(value * roundMap[fraction][1]) / roundMap[fraction][1];
	}
}
