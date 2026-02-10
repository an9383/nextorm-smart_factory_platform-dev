package com.nextorm.collector.collector.opcuacollector.data;

import com.nextorm.collector.collector.opcuacollector.connectivity.DEFINE_OPCUA_COMMAND;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OpcUaNodes {
	Object Low_pressure_mould_close_position;
	Object Low_pressure_mould_close_pressure;
	Object Mould_closing_position_1;

	ArrayList<OpcUaData> sensorList = null;
	Object[] sensorObj = null;

	public OpcUaNodes(
		ArrayList<OpcUaData> list,
		Object[] obj
	) {
		sensorList = list;
		sensorObj = obj;
	}

	public OpcUaNodes(
		Object Low_pressure_mould_close_position,
		Object Low_pressure_mould_close_pressure,
		Object Mould_closing_position_1
	) {
		this.Low_pressure_mould_close_position = Low_pressure_mould_close_position;
		this.Low_pressure_mould_close_pressure = Low_pressure_mould_close_pressure;
		this.Mould_closing_position_1 = Mould_closing_position_1;

	}

	/**
	 * Priamus OPC  Server로부터 파라미터들의 값을 구해 패킷형식으로 구성한다.
	 *
	 * @Method Name    : getPacketData
	 * @ChangeDate : 2019. 12. 26.
	 * @Owner : jkkim
	 * ============================
	 * @Tool_Status,RUN;System_Time,2019-12-08T18:54:46.958Z;Cycle_Count,7351;OEE,14.1358;Ch1_Available,TRUE;Ch1_Max1,284.304;Ch1_Integral1,622.018;Ch1_MeasData,0/0/0/28.6574/0/0;Ch2_Available,TRUE;Ch2_Max1,281.309;Ch2_Integral1,625.018;Ch2_MeasData,0/0/0/26.6574/0/0;Ch3_Available,TRUE;Ch3_Max1,284.309;Ch3_Integral1,622.018;Ch3_MeasData,0/0/0/26.6574/0/0;Ch4_Available,TRUE;Ch4_Max1,281.309;Ch4_Integral1,627.015;Ch4_MeasData,0/0/0/28.9576/0/0;!
	 */
	public String getPacketData(List<OpcUaDataType> opcUaDataTypeList) {
		String retPackeData = "";
		int iSize = sensorList.size() + 1;

		for (int i = 0; i < iSize; i++) {
			retPackeData += addPacketData(opcUaDataTypeList, i);
		}

		return retPackeData;
	}

	@SuppressWarnings("unused")
	private String addPacketData(
		List<OpcUaDataType> opcUaDataTypeList,
		int dataSeq,
		Object adddata
	) {
		String strRet = null;

		log.debug("PressureSensorNodes",
			"addPacketData [" + opcUaDataTypeList.get(dataSeq)
												 .getName() + "] dataSeq = " + dataSeq);
		if (dataSeq == 7 || dataSeq == 11 || dataSeq == 15 || dataSeq == 19) {
			strRet = JsonUtility.ObjectToFormattedString(adddata);
			strRet = strRet.replace("[", "")
						   .replace("]", "")
						   .replace(",", "/")
						   .replace(" ", "");
		} else if (dataSeq == 5 || dataSeq == 6 || dataSeq == 9 || dataSeq == 10 || dataSeq == 13 || dataSeq == 14 || dataSeq == 17 || dataSeq == 18) {
			if (adddata == null) {
				strRet = "0.0";
			} else {
				strRet = String.valueOf(adddata);
			}
		} else if (dataSeq == 4 || dataSeq == 8 || dataSeq == 12 || dataSeq == 16) {
			boolean bVal = (boolean)adddata;
			if (bVal) {
				strRet = "True";
			} else {
				strRet = "False";
			}
		} else {
			strRet = String.valueOf(adddata);
		}

		strRet = opcUaDataTypeList.get(dataSeq)
								  .getName() + DEFINE_OPCUA_COMMAND.OPCUA_DATA.getRepSubDelimeterChar() + strRet + DEFINE_OPCUA_COMMAND.OPCUA_DATA.getRepMainDelimeterChar();

		log.debug("PressureSensorNodes", "addPacketData strRet = [" + strRet + "] ");

		return strRet;
	}

	private String addPacketData(
		List<OpcUaDataType> opcUaDataTypeList,
		int seq
	) {
		String strRet = null;
		String strDataType = null;

		if (seq == 0) {
			strDataType = "String";
		} else {
			strDataType = sensorList.get(seq - 1).dataValue.getDataType();
		}

		if (strDataType.equals("StringArray")) {
			strRet = JsonUtility.ObjectToFormattedString(sensorObj[seq]);
			strRet = strRet.replace("[", "")
						   .replace("]", "")
						   .replace(",", "/")
						   .replace(" ", "");
		} else if (strDataType.equals("Double") || strDataType.equals("Float") || strDataType.equals("Long")) {
			if (sensorObj[seq] == null) {
				strRet = "0.0";
			} else {
				strRet = String.valueOf(sensorObj[seq]);
			}
		} else if (strDataType.equals("Boolean")) {
			boolean bVal = (boolean)sensorObj[seq];
			if (bVal) {
				strRet = "True";
			} else {
				strRet = "False";
			}
		} else {
			strRet = String.valueOf(sensorObj[seq]);
		}

		strRet = opcUaDataTypeList.get(seq)
								  .getName() + DEFINE_OPCUA_COMMAND.OPCUA_DATA.getRepSubDelimeterChar() + strRet + DEFINE_OPCUA_COMMAND.OPCUA_DATA.getRepMainDelimeterChar();

		return strRet;
	}
}
