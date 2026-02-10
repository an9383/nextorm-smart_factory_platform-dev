package com.nextorm.portal.util;

public class GeoUtil {

	public static final double EARTH_RADIUS_KM = 6371; // 지구 반지름

	/**
	 * 두 좌표간의 거리를 haversine을 이용하여 계산
	 *
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return 계산된 거리(m)
	 */
	public static double calculateDistanceByHaversine(
		double lat1,
		double lon1,
		double lat2,
		double lon2
	) {
		final int R = 6371; // 지구 반경 (km)
		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(
			Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS_KM * c * 1000;
	}
}
