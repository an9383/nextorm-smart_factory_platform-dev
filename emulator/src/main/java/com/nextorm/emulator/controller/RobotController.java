package com.nextorm.emulator.controller;

import com.nextorm.emulator.RobotEmulator;
import com.nextorm.emulator.utils.RandomValueGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/robot-mgt")
public class RobotController {
	private Map<String, Object> PREV_STATUS = new HashMap<>();
	private Map<String, Object> PREV_QUALITY = new HashMap<>();

	@GetMapping(path = "/status")
	public ResponseEntity<Object> getStatus() {
		Double depth_data = 30.0;
		Double ctr_bat_soc = 65.0;
		Double ctr_bat_v = 25.5;
		if (!PREV_STATUS.isEmpty()) {
			depth_data = (Double)PREV_STATUS.get("depth_data");
			ctr_bat_soc = (Double)PREV_STATUS.get("ctr_bat_soc");
			ctr_bat_v = (Double)PREV_STATUS.get("ctr_bat_v");
			depth_data = RandomValueGenerator.generate(depth_data, 20, 43);
			ctr_bat_soc = RandomValueGenerator.generate(ctr_bat_soc, 57, 84);
			ctr_bat_v = RandomValueGenerator.generate(ctr_bat_v, 25, 27);
		}
		PREV_STATUS.put("depth_data", depth_data);
		PREV_STATUS.put("ctr_bat_soc", ctr_bat_soc);
		PREV_STATUS.put("ctr_bat_v", ctr_bat_v);

		Map<String, Object> datas = new HashMap<>();
		datas.put("timestamp", new Date().getTime());
		datas.put("latitude", RobotEmulator._latitude);
		datas.put("longitude", RobotEmulator._longitude);
		datas.put("depth_data", depth_data);
		datas.put("velocity", RandomValueGenerator.getRandomValue(Arrays.asList(0.04, 0.06)));
		datas.put("ctr_bat_soc", ctr_bat_soc);
		datas.put("ctr_bat_v", ctr_bat_v);
		datas.put("ctr_charging_status", 1);
		datas.put("robot_id", "robot01");

		return ResponseEntity.ok(datas);
	}

	@GetMapping(path = "/water_quality")
	public ResponseEntity<Object> getWater_quality() {

		Double temp_deg_c = 9.0;
		Double ph_units = 7.5;
		Double depth_m = 0.2;
		Double spcond_us_cm = 500.0;
		Double turb_ntu = 50.0;
		Double hdo_sat = 90.0;
		Double hdo_mg_l = 11.0;
		Double chl_ug_l = 30.0;
		Double bg_ppb = 10.0;
		Double ph_mv = -40.0;

		if (!PREV_QUALITY.isEmpty()) {
			temp_deg_c = RandomValueGenerator.generate(temp_deg_c, 8.07, 13.25);
			ph_units = RandomValueGenerator.generate(ph_units, 7.36, 7.86);
			depth_m = RandomValueGenerator.generate(depth_m, 0.17, 0.23);
			spcond_us_cm = RandomValueGenerator.generate(spcond_us_cm, 449.4, 579.3);
			turb_ntu = RandomValueGenerator.generate(turb_ntu, 9.53, 2493.0);
			hdo_sat = RandomValueGenerator.generate(hdo_sat, 85.5, 107.0);
			hdo_mg_l = RandomValueGenerator.generate(hdo_mg_l, 10.04, 11.93);
			chl_ug_l = RandomValueGenerator.generate(chl_ug_l, 6.87, 68.52);
			bg_ppb = RandomValueGenerator.generate(bg_ppb, 2.8, 64.55);
			ph_mv = RandomValueGenerator.generate(ph_mv, -63.8, -36.9);

		}
		PREV_QUALITY.put("temp_deg_c", temp_deg_c);
		PREV_QUALITY.put("ph_units", ph_units);
		PREV_QUALITY.put("depth_m", depth_m);
		PREV_QUALITY.put("spcond_us_cm", spcond_us_cm);
		PREV_QUALITY.put("turb_ntu", turb_ntu);
		PREV_QUALITY.put("hdo_sat", hdo_sat);
		PREV_QUALITY.put("hdo_mg_l", hdo_mg_l);
		PREV_QUALITY.put("chl_ug_l", chl_ug_l);
		PREV_QUALITY.put("bg_ppb", bg_ppb);
		PREV_QUALITY.put("ph_mv", ph_mv);

		Map<String, Object> datas = new HashMap<>();
		datas.put("timestamp", new Date().getTime());
		datas.put("latitude", RobotEmulator._latitude);
		datas.put("longitude", RobotEmulator._longitude);
		datas.put("temp_deg_c", temp_deg_c);
		datas.put("ph_units", ph_units);
		datas.put("depth_m", depth_m);
		datas.put("spcond_us_cm", spcond_us_cm);
		datas.put("turb_ntu", turb_ntu);
		datas.put("hdo_sat", hdo_sat);
		datas.put("hdo_mg_l", hdo_mg_l);
		datas.put("chl_ug_l", chl_ug_l);
		datas.put("bg_ppb", bg_ppb);
		datas.put("ph_mv", ph_mv);
		datas.put("robot_id", "robot01");

		return ResponseEntity.ok(datas);
	}

	// @Scheduled(fixedDelay = 1000)
	// private void getCordi() {
	// 	RobotEmulator.findNextPosition(false);
	// }
}