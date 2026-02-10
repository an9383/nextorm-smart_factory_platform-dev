package com.nextorm.collector.collector.opcuacollector.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OpcUaDataType {
	String name;
	String type;

	public OpcUaDataType(
		String name,
		String type
	) {
		this.name = name;
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	public Object castData(String data) {
		Object retVal = null;
		if (data != null) {
			try {
				switch (type.toUpperCase()) {
					case "STRING":
						retVal = data;
						break;
					case "BOOLEAN":
						retVal = Boolean.parseBoolean(data);
						break;
					case "INTEGER":
						retVal = Integer.parseInt(data);
						break;
					case "DOUBLE":
						retVal = Double.parseDouble(data);
						break;
					case "LONG":
						retVal = Long.parseLong(data);
						break;
					case "TIMESTRING":
						retVal = getDateTime(data, "yyyy-MM-dd HH:mm:SS").getTime();
						retVal = Long.parseLong(data);
						break;
					case "TOOLSTATUS":
						retVal = data;
						break;
					default:
						retVal = data;
						break;
				}
			} catch (Exception ex) {

			}
		}
		return retVal;
	}

	public static Date getDateTime(
		String p_timeString,
		String p_timeFormat
	) {
		DateFormat l_dateFormat = new SimpleDateFormat(p_timeFormat);
		Date retMessage = null;
		try {
			retMessage = l_dateFormat.parse(p_timeString);
		} catch (Throwable ex) {
		}
		l_dateFormat = null;
		return retMessage;
	}
}
