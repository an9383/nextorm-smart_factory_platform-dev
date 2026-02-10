package com.nextorm.collector.collector.opcua;

import lombok.Getter;

@Getter
public enum OpcUaDataType {
	BOOLEAN(1, "Boolean"),
	SBYTE(2, "SByte"),
	BYTE(3, "Byte"),
	INT16(4, "Int16"),
	UINT16(5, "UInt16"),
	INT32(6, "Int32"),
	UINT32(7, "UInt32"),
	INT64(8, "Int64"),
	UINT64(9, "UInt64"),
	FLOAT(10, "Float"),
	DOUBLE(11, "Double"),
	STRING(12, "String"),
	DATE_TIME(13, "DateTime"),
	GUID(14, "Guid"),
	BYTE_STRING(15, "ByteString"),
	XML_ELEMENT(16, "XmlElement"),
	NODE_ID(17, "NodeId"),
	EXPANDED_NODE_ID(18, "ExpandedNodeId"),
	STATUS_CODE(19, "StatusCode"),
	QUALIFIED_NAME(20, "QualifiedName"),
	LOCALIZED_TEXT(21, "LocalizedText"),
	EXTENSION_OBJECT(22, "ExtensionObject"),
	DATA_VALUE(23, "DataValue"),
	VARIANT(24, "Variant"),
	DIAGNOSTIC_INFO(25, "DiagnosticInfo"),
	UNKNOWN(-1, "Unknown"),
	CUSTOM(-2, "CustomType");

	private final int typeId;
	private final String displayName;

	OpcUaDataType(
		int typeId,
		String displayName
	) {
		this.typeId = typeId;
		this.displayName = displayName;
	}

	/**
	 * 타입 ID로 OpcUaDataType을 찾는다
	 */
	public static OpcUaDataType fromTypeId(int typeId) {
		for (OpcUaDataType type : values()) {
			if (type.typeId == typeId) {
				return type;
			}
		}
		return CUSTOM;
	}

	/**
	 * 커스텀 타입의 표시 이름을 생성한다
	 */
	public static String getCustomTypeName(int typeId) {
		return "CustomType[" + typeId + "]";
	}
}