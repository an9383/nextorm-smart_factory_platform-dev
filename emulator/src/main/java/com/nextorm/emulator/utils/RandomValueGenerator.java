package com.nextorm.emulator.utils;

import java.util.List;

public class RandomValueGenerator {

	public static double generate(
		double previousValue,
		double from,
		double to
	) {
		double gap = to - from;
		double random = getRandomDouble(0.0, gap) * 0.1 * getRandomDouble(-1, 1);
		double y = previousValue + random;
		return y;
	}

	private static double getRandomDouble(
		double min,
		double max
	) {
		return (double)((Math.random() * (max - min)) + min);
	}

	public static Object getRandomValue(List<Object> datas) {
		int length = datas.size() - 1;
		int index = getRandomNumber(0, length);
		return datas.get(index);
	}

	private static int getRandomNumber(
		int min,
		int max
	) {
		return (int)((Math.random() * (max - min)) + min);
	}
}
