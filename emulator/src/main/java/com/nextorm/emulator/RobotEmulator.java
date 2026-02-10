package com.nextorm.emulator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.common.db.entity.ReservoirLayout;
import com.nextorm.emulator.services.ReservoirLayoutService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// @Component
public class RobotEmulator {

	//    북위 33~43, 동경 124~132도
	//
	//    위도 - latitude 33 y
	//    경도 - longitude 124 x

	@Autowired
	ReservoirLayoutService reservoirLayoutService;

	public static Double _latitude = 0.0;
	public static Double _longitude = 0.0;

	public static int[][] dataMap;
	public static Double xMin;
	public static Double xMax;
	public static Double yMin;
	public static Double yMax;

	public static int gpsScale = 10000; //14.48m

	@PostConstruct
	public void init() throws JsonProcessingException {
		List<ReservoirLayout> reservoirLasts = reservoirLayoutService.getReservoirLayout(1l);

		ObjectMapper mapper = new ObjectMapper();

		Map data = mapper.readValue(reservoirLasts.get(0)
												  .getData(), Map.class);
		List<List<Double>> markers = (List<List<Double>>)data.get("markers");

		List<Point2D.Double> datas = markers.stream()
											.map(d -> {
												return new Point2D.Double(Double.valueOf(String.format("%.6f",
													d.get(1))) * gpsScale,
													Double.valueOf(String.format("%.6f", d.get(0))) * gpsScale);
											})
											.collect(Collectors.toList());
		getMinMax(datas);
		makeDataMap(datas);
		System.out.println("test");
	}

	//    public static Point2D.Double findNextPosition(Point2D.Double p){
	public static Point2D.Double findNextPosition(Boolean init) {
		int x = 0;
		if (_longitude != 0 || init) {
			x = (int)Math.ceil(_longitude * gpsScale - RobotEmulator.xMin);
		}
		int y = 0;
		if (_latitude != 0 || init) {
			y = (int)Math.ceil(_latitude * gpsScale - RobotEmulator.yMin);
		}

		//        int y=Integer.valueOf(String.valueOf(p.y)),x=Integer.valueOf(String.valueOf(p.x));
		//오른족 끝이면
		while (y < dataMap.length) {
			Point2D.Double nextPos = findRow(new Point2D.Double(x, y));
			if (nextPos != null) {
				RobotEmulator._longitude = nextPos.x / gpsScale;
				RobotEmulator._latitude = nextPos.y / gpsScale;
				System.out.println("Longitude:" + RobotEmulator._longitude + " Latitude:" + RobotEmulator._latitude);
				return nextPos;
			}
			y++;
			x = 0;
		}
		_longitude = RobotEmulator.xMin / gpsScale;
		_latitude = RobotEmulator.yMin / gpsScale;
		return findNextPosition(true);
	}

	private static Point2D.Double findRow(Point2D.Double p) {
		int x = (int)Math.ceil(p.x);
		int y = (int)Math.ceil(p.y);
		for (int i = x + 1; i < dataMap[y].length; i++) {
			if (dataMap[y][i] == 0) {
				//                return {x:i,y:pos.y}
				return new Point2D.Double(i + RobotEmulator.xMin, y + RobotEmulator.yMin);
			}
		}
		return null;
	}

	public static void makeDataMap(List<Point2D.Double> p) {
		double xMin = RobotEmulator.xMin;
		double xMax = RobotEmulator.xMax;
		double yMin = RobotEmulator.yMin;
		double yMax = RobotEmulator.yMax;

		int ySize = (int)Math.ceil(yMax - yMin + 1);
		int xSize = (int)Math.ceil(xMax - xMin + 1);

		// let dataMap = Array.from(Array(), (ySize) => new Array(xSize))
		//        let dataMap = new Array(ySize).fill(-1).map(()=>new Array(xSize))
		int[][] dataMap = new int[ySize][xSize];
		for (int i = 0; i < ySize; i++) {
			for (int j = 0; j < xSize; j++) {
				Point2D.Double currPos = new Point2D.Double(xMin + j, yMin + i);
				boolean isIn = isInside(currPos, p);
				if (isIn) {
					dataMap[i][j] = 0;
				} else {
					dataMap[i][j] = -1;
				}
			}
		}
		//        return dataMap;
		RobotEmulator.dataMap = dataMap;
	}

	private static void getMinMax(List<Point2D.Double> p) {
		double xMin = Collections.min(p.stream()
									   .map(d -> d.x)
									   .collect(Collectors.toList()));
		double xMax = Collections.max(p.stream()
									   .map(d -> d.x)
									   .collect(Collectors.toList()));
		double yMin = Collections.min(p.stream()
									   .map(d -> d.y)
									   .collect(Collectors.toList()));
		double yMax = Collections.max(p.stream()
									   .map(d -> d.y)
									   .collect(Collectors.toList()));
		//        return Arrays.asList(xMin,xMax,yMin,yMax);
		RobotEmulator.xMin = xMin;
		RobotEmulator.xMax = xMax;
		RobotEmulator.yMin = yMin;
		RobotEmulator.yMax = yMax;
	}

	private static boolean isInside(
		Point2D.Double B,
		List<Point2D.Double> p
	) {
		//crosses는 점q와 오른쪽 반직선과 다각형과의 교점의 개수
		int crosses = 0;
		for (int i = 0; i < p.size(); i++) {
			int j = (i + 1) % p.size();
			//점 B가 선분 (p[i], p[j])의 y좌표 사이에 있음
			if ((p.get(i).y > B.y) != (p.get(j).y > B.y)) {
				//atX는 점 B를 지나는 수평선과 선분 (p[i], p[j])의 교점
				double atX = (p.get(j).x - p.get(i).x) * (B.y - p.get(i).y) / (p.get(j).y - p.get(i).y) + p.get(i).x;
				//atX가 오른쪽 반직선과의 교점이 맞으면 교점의 개수를 증가시킨다.
				if (B.x < atX) {
					crosses++;
				}
			}
		}
		return crosses % 2 > 0;
	}

}
