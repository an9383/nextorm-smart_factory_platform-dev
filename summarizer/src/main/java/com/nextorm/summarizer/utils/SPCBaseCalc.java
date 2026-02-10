package com.nextorm.summarizer.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class SPCBaseCalc {
	protected Double ewma = null;
	protected Double latitude = null;
	protected Double longitude = null;
	List<Double> dataList = new ArrayList<>();
	List<Double> sorted = new ArrayList<>();
	protected Double dMax = null;
	protected Double dMin = null;
	protected Double dAvg = 0.0;
	protected Double dSTD = null;
	protected Double dSum = 0.0;
	protected Double dMedian = null;
	protected Double d2_3Median = null;
	protected Double d1_3Median = null;
	protected Double range = 0.0;
	protected Double cp = null;
	protected Double cpk = null;
	protected Double cpu = null;
	protected Double cpl = null;

	public void addLatitude(Double latitude) {
		if (latitude == null) {
			throw new IllegalArgumentException("latitude is null");
		}
		this.latitude = latitude;
	}

	public void addLongitude(Double longitude) {
		if (longitude == null) {
			throw new IllegalArgumentException("longitude is null");
		}
		this.longitude = longitude;
	}

	public void addValue(Double value) {
		if (value == null) {
			throw new IllegalArgumentException("value is null");
		}

		if (dMax == null) {
			dMax = value;
		} else {
			dMax = Math.max(dMax, value);
		}

		if (dMin == null) {
			dMin = value;
		} else {
			dMin = Math.min(dMin, value);
		}

		dataList.add(value);
		sorted.add(value);
	}

	public int getDataCount() {
		return dataList.size();
	}

	public void calculate(
		Double usl,
		Double lsl
	) {
		calculate();
		calculateCP(usl, lsl);
	}

	public void calculate() {
		mean();
		dSTD = standardDeviation(1);
		calculateMedian();
		calculateRange();
		calculateEWMA(0.3);
	}

	private void calculateCP(
		Double usl,
		Double lsl
	) {
		if (dSTD != null && dSTD != 0.0) {
			cp = (usl - lsl) / (6 * this.dSTD);
			cpu = (usl - this.dAvg) / (3 * this.dSTD);
			cpl = (this.dAvg - lsl) / (3 * this.dSTD);
			cpk = Math.min(cpu, cpl);
		}
	}

	/*
		EWMA계산
		lamda: smoothing factor 0~1사이 값
	 */
	private void calculateEWMA(double lamda) {
		if (dataList.isEmpty()) {
			return;
		}
		//초기 EWMA 값 설정( 첫 번째ㅓ 데이터 포인트로 설정)
		ewma = dataList.get(0);

		//EWMA 계산
		for (int i = 1; i < dataList.size(); i++) {
			ewma = lamda * dataList.get(i) + (1 - lamda) * ewma;
		}
	}

	public void roundVal(int fraction) {
		if (dMax != null) {
			dMax = DoubleUtility.roundVal(dMax, fraction);
		}
		if (dMin != null) {
			dMin = DoubleUtility.roundVal(dMin, fraction);
		}
		if (dAvg != null) {
			dAvg = DoubleUtility.roundVal(dAvg, fraction);
		}
		if (dSTD != null) {
			dSTD = DoubleUtility.roundVal(dSTD, fraction);
		}
		if (dSum != null) {
			dSum = DoubleUtility.roundVal(dSum, fraction);
		}
		if (dMedian != null) {
			dMedian = DoubleUtility.roundVal(dMedian, fraction);
		}
		if (d2_3Median != null) {
			d2_3Median = DoubleUtility.roundVal(d2_3Median, fraction);
		}
		if (d1_3Median != null) {
			d1_3Median = DoubleUtility.roundVal(d1_3Median, fraction);
		}
		if (range != null) {
			range = DoubleUtility.roundVal(range, fraction);
		}
		if (cp != null) {
			cp = DoubleUtility.roundVal(cp, fraction);
		}
		if (cpk != null) {
			cpk = DoubleUtility.roundVal(cpk, fraction);
		}
		if (cpu != null) {
			cpu = DoubleUtility.roundVal(cpu, fraction);
		}
		if (cpl != null) {
			cpl = DoubleUtility.roundVal(cpl, fraction);
		}
		if (ewma != null) {
			ewma = DoubleUtility.roundVal(ewma, fraction);
		}
	}

	private void calculateRange() {
		if (dMax != null && dMin != null) {
			range = dMax - dMin;
		}
	}

	private void mean() {
		dSum = 0.0;
		for (Double aDouble : dataList) {
			dSum += aDouble;
		}
		dAvg = dSum / dataList.size();
	}

	private void calculateMedian() {
		if (sorted.isEmpty()) {
			return;
		}

		Double[] array = null;
		int tmpNo = 0;
		try {
			array = new Double[sorted.size()];
			//데이터 형이 Double이 아닐 수 있으므로, 아래처럼 처리한다.
			int i = 0;
			for (Number data : sorted) {
				array[i] = data.doubleValue();
				i++;
			}

			Arrays.sort(array);

			if (sorted.size() == 1) {
				dMedian = array[0];
				d1_3Median = array[0];
				d2_3Median = array[0];
			} else if (sorted.size() == 2) {
				dMedian = (array[0] + array[1]) / 2.0;
				d1_3Median = array[0];
				d2_3Median = array[1];
			} else if (sorted.size() == 3) {
				dMedian = array[1];
				d1_3Median = array[0];
				d2_3Median = array[2];
			} else {//Median 값 구하기
				tmpNo = array.length / 2; // 요소 개수의 절반값 구하기
				if (tmpNo > 0) {
					if (array.length % 2 == 1) {                // 요소 개수가 홀수면
						dMedian = array[tmpNo];                // 홀수 개수인 배열에서는 중간 요소를 그대로 반환
					} else {
						dMedian = (array[tmpNo] + array[tmpNo - 1]) / 2.0; // 짝수 개 요소는, 중간 두 수의 평균 반환
					}
				} else {
					dMedian = array[0];
				}

				//1/3 값 구하기
				double loc = 0;
				int intLoc = 0;
				double decimalLoc = 0;

				loc = (array.length - 1) * 0.25;
				intLoc = (int)loc;
				decimalLoc = loc - intLoc;

				if (loc > 0) {
					d1_3Median = array[intLoc] + ((array[intLoc + 1] - array[intLoc]) * decimalLoc);
				} else {
					d1_3Median = array[0];
				}

				loc = (array.length - 1) * 0.75;
				intLoc = (int)loc;
				decimalLoc = loc - intLoc;   // 요소 개수의 1/3 값 구하기        	    // 요소 개수의 1/3 값 구하기
				if (loc > 0) {

					d2_3Median = array[intLoc] + ((array[intLoc + 1] - array[intLoc]) * decimalLoc);

				} else {
					d2_3Median = array[0];
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Double standardDeviation(int option) {
		if (dataList.size() < 2) {
			return null;
		}

		double sum = 0.0;
		for (Double aDouble : dataList) {
			double diff = aDouble - dAvg;
			sum += diff * diff;
		}
		double sd = Math.sqrt(sum / (dataList.size() - option));
		sd = DoubleUtility.roundVal(sd, 5);
		return sd;
	}
}
