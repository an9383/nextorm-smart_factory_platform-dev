package com.nextorm.portal.dto.weather.enums;

public enum WindDirection {
	N_NE, NE_E, E_SE, SE_S, S_SW, SW_W, W_NW, NW_N;

	public static WindDirection of(int value) {
		if (value >= 0 && value <= 45) {
			return N_NE;
		} else if (value > 45 && value <= 90) {
			return NE_E;
		} else if (value > 90 && value <= 135) {
			return E_SE;
		} else if (value > 135 && value <= 180) {
			return SE_S;
		} else if (value > 180 && value <= 225) {
			return S_SW;
		} else if (value > 225 && value <= 270) {
			return SW_W;
		} else if (value > 270 && value <= 315) {
			return W_NW;
		} else {
			return NW_N;
		}
	}
}