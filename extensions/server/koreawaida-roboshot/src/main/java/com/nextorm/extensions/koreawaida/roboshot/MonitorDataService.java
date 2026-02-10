package com.nextorm.extensions.koreawaida.roboshot;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Service
public class MonitorDataService {

	/**
	 * 현재 모니터링 데이터를 가져옵니다.
	 *
	 * @param machineId 모니터링할 머신 ID
	 * @param exePath   실행 파일 경로
	 * @return 모니터링 데이터 리스트
	 */
	public List<String> getCurrMonitorDataById(
		int machineId,
		String exePath
	) {
		String result = getMonitorData(machineId, exePath);

		if (result.startsWith("ERROR :")) {
			return List.of();
		}

		return Arrays.stream(result.split(","))
					 .map(String::trim)
					 .toList();
	}

	private String getMonitorData(
		int machineId,
		String exePath
	) {
		try {
			File exeFile = new File(exePath);
			ProcessBuilder pb = new ProcessBuilder(exePath, String.valueOf(machineId));
			pb.directory(exeFile.getParentFile());
			Process process = pb.start();

			// 결과 읽기
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS949"));

			String line = reader.readLine();
			if (line != null) {
				return line;
			}

			process.waitFor();
			return "ERROR:No result found";

		} catch (Exception e) {
			return "ERROR:" + e.getMessage();
		}
	}
}
