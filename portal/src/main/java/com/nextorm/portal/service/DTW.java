package com.nextorm.portal.service;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DTW: Dynamic Time Warping
 * 시계열 데이터 간의 유사성을 비교하기 위한 알고리즘이다.
 */
@AllArgsConstructor
public class DTW {
	private final double[] series1;
	private final double[] series2;

	public double[] getWarpingData() {
		DTWResult result = dtw(series1, series2);

		double[] warped = new double[series1.length];
		// NaN으로 초기화
		Arrays.fill(warped, Double.NaN);

		for (Point p : result.path) {
			warped[p.x] = series2[p.y];
		}

		return warped;
	}

	private DTWResult dtw(
		double[] s1,
		double[] s2
	) {
		int n = s1.length;
		int m = s2.length;
		double[][] dtwMatrix = new double[n + 1][m + 1];

		// DTW 매트릭스 초기화
		for (int i = 0; i <= n; i++) {
			for (int j = 0; j <= m; j++) {
				dtwMatrix[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		dtwMatrix[0][0] = 0;

		// DTW 매트릭스 계산
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				double cost = Math.abs(s1[i - 1] - s2[j - 1]);
				dtwMatrix[i][j] = cost + Math.min(Math.min(dtwMatrix[i - 1][j],     // insertion
						dtwMatrix[i][j - 1]),     // deletion
					dtwMatrix[i - 1][j - 1]   // match
				);
			}
		}

		// 워핑 경로 역추적
		List<Point> path = new ArrayList<>();
		int i = n;
		int j = m;

		while (i > 0 && j > 0) {
			path.add(new Point(i - 1, j - 1));

			double diagCost = dtwMatrix[i - 1][j - 1];
			double upCost = dtwMatrix[i - 1][j];
			double leftCost = dtwMatrix[i][j - 1];

			if (diagCost <= upCost && diagCost <= leftCost) {
				i--;
				j--;
			} else if (upCost <= leftCost) {
				i--;
			} else {
				j--;
			}
		}

		Collections.reverse(path);
		return new DTWResult(dtwMatrix[n][m], path);
	}

	record DTWResult(double distance, List<Point> path) {
	}

	record Point(int x, int y) {
	}
}

