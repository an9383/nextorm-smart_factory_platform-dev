package com.nextorm.collector.collector.opcuacollector.connectivity;

public enum DEFINE_OPCUA_COMMAND {
	OPCUA_DATA("@", ";", ",", "!");
	private String repStartChar = "";
	private String repMainDelimeterChar = "";
	private String repSubDelimeterChar = "";
	private String repEndChar = "";

	private DEFINE_OPCUA_COMMAND(
		String repStartChar,
		String repMainDelimeterChar,
		String repSubDelimeterChar,
		String repEndChar
	) {
		this.repStartChar = repStartChar;
		this.repMainDelimeterChar = repMainDelimeterChar;
		this.repSubDelimeterChar = repSubDelimeterChar;
		this.repEndChar = repEndChar;
	}

	public String getRepStartChar() {
		return repStartChar;
	}

	public String getRepMainDelimeterChar() {
		return repMainDelimeterChar;
	}

	public String getRepSubDelimeterChar() {
		return repSubDelimeterChar;
	}

	public String getRepEndChar() {
		return repEndChar;
	}
}
