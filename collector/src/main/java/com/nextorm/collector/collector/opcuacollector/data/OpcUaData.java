package com.nextorm.collector.collector.opcuacollector.data;

import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;

public class OpcUaData {
	public UaMonitoredItem uaMonitoredItem = new UaMonitoredItem();
	public DataValue dataValue = new DataValue();
	public ParamOrder paramOrder = new ParamOrder();
	public DataOrder dataOrder = new DataOrder();

	public class UaMonitoredItem {
		private String nodeId = "";
		private int nameSpaceIndex = 0;
		private String accessLevel = "";
		private String userAccessLevel = "";
		private String browseName = "";
		private String displayName = "";
		private String dataType = "";
		private String typeDefinition = "";
		private String referenceType = "";
		private String referenceTragetNodeType = "";
		private String timestampsToReturn = "";
		private String nodeClass = "";
		private String monitoringMode = "";

		/**
		 * @return the nodeId
		 */
		public String getNodeId() {
			return nodeId;
		}

		/**
		 * @param nodeId the nodeId to set
		 */
		public void setNodeId(String nodeId) {
			this.nodeId = nodeId;
		}

		/**
		 * @return the nameSpaceIndex
		 */
		public int getNameSpaceIndex() {
			return nameSpaceIndex;
		}

		/**
		 * @param nameSpaceIndex the nameSpaceIndex to set
		 */
		public void setNameSpaceIndex(String nameSpaceIndex) {
			this.nameSpaceIndex = Integer.parseInt(nameSpaceIndex);
		}

		/**
		 * @return the accessLevel
		 */
		public String getAccessLevel() {
			return accessLevel;
		}

		/**
		 * @param accessLevel the accessLevel to set
		 */
		public void setAccessLevel(String accessLevel) {
			this.accessLevel = accessLevel;
		}

		/**
		 * @return the userAccessLevel
		 */
		public String getUserAccessLevel() {
			return userAccessLevel;
		}

		/**
		 * @param userAccessLevel the userAccessLevel to set
		 */
		public void setUserAccessLevel(String userAccessLevel) {
			this.userAccessLevel = userAccessLevel;
		}

		/**
		 * @return the browseName
		 */
		public String getBrowseName() {
			return browseName;
		}

		/**
		 * @param browseName the browseName to set
		 */
		public void setBrowseName(String browseName) {
			this.browseName = browseName;
		}

		/**
		 * @return the displayName
		 */
		public String getDisplayName() {
			return displayName;
		}

		/**
		 * @param displayName the displayName to set
		 */
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		/**
		 * @return the dataType
		 */
		public String getDataType() {
			return dataType;
		}

		/**
		 * @param dataType the dataType to set
		 */
		public void setDataType(String dataType) {
			this.dataType = dataType;
		}

		/**
		 * @return the typeDefinition
		 */
		public String getTypeDefinition() {
			return typeDefinition;
		}

		/**
		 * @param typeDefinition the typeDefinition to set
		 */
		public void setTypeDefinition(String typeDefinition) {
			this.typeDefinition = typeDefinition;
		}

		/**
		 * @return the referenceType
		 */
		public String getReferenceType() {
			return referenceType;
		}

		/**
		 * @param referenceType the referenceType to set
		 */
		public void setReferenceType(String referenceType) {
			this.referenceType = referenceType;
		}

		/**
		 * @return the referenceTragetNodeType
		 */
		public String getReferenceTragetNodeType() {
			return referenceTragetNodeType;
		}

		/**
		 * @param referenceTragetNodeType the referenceTragetNodeType to set
		 */
		public void setReferenceTragetNodeType(String referenceTragetNodeType) {
			this.referenceTragetNodeType = referenceTragetNodeType;
		}

		/**
		 * @return the timestampsToReturn
		 */
		public TimestampsToReturn getTimestampsToReturn() {
			switch (this.monitoringMode) {
				case "Source":
					return TimestampsToReturn.Source;
				case "Server":
					return TimestampsToReturn.Server;
				case "Neither":
					return TimestampsToReturn.Neither;
				case "Both":
				default:
					return TimestampsToReturn.Both;
			}
		}

		/**
		 * @param timestampsToReturn the timestampsToReturn to set
		 */
		public void setTimestampsToReturn(String timestampsToReturn) {
			this.timestampsToReturn = timestampsToReturn;

		}

		/**
		 * @return the nodeClass
		 */
		public String getNodeClass() {
			return nodeClass;
		}

		/**
		 * @param nodeClass the nodeClass to set
		 */
		public void setNodeClass(String nodeClass) {
			this.nodeClass = nodeClass;
		}

		/**
		 * @return the monitoringMode
		 */
		public MonitoringMode getMonitoringMode() {
			switch (this.monitoringMode) {
				case "Disabled":
					return MonitoringMode.Disabled;
				case "Sampling":
					return MonitoringMode.Sampling;
				case "Reporting":
				default:
					return MonitoringMode.Reporting;
			}
		}

		/**
		 * @param monitoringMode the monitoringMode to set
		 */
		public void setMonitoringMode(String monitoringMode) {
			this.monitoringMode = monitoringMode;
		}
	}

	public class DataValue {
		private int seq = 0;
		private String nodeId = "";
		private String nameSpaceIndex = "";
		private String dataType = "";

		/**
		 * @return the seq
		 */
		public int getSeq() {
			return seq;
		}

		/**
		 * @param string the seq to set
		 */
		public void setSeq(String seq) {
			this.seq = Integer.parseInt(seq);
		}

		/**
		 * @return the nodeId
		 */
		public String getNodeId() {
			return nodeId;
		}

		/**
		 * @param nodeId the nodeId to set
		 */
		public void setNodeId(String nodeId) {
			this.nodeId = nodeId;
		}

		/**
		 * @return the nameSpaceIndex
		 */
		public String getNameSpaceIndex() {
			return nameSpaceIndex;
		}

		/**
		 * @param nameSpaceIndex the nameSpaceIndex to set
		 */
		public void setNameSpaceIndex(String nameSpaceIndex) {
			this.nameSpaceIndex = nameSpaceIndex;
		}

		/**
		 * @return the dataType
		 */
		public String getDataType() {
			return dataType;
		}

		/**
		 * @param dataType the dataType to set
		 */
		public void setDataType(String dataType) {
			this.dataType = dataType;
		}
	}

	public class ParamOrder {
		private String name = "";

		public String getParamOrderName() {
			return name;
		}

		public void setParamOrderName(String name) {
			this.name = name;
		}
	}

	public class DataOrder {
		private String name = "";
		private String type = "";

		public String getDataOrderName() {
			return name;
		}

		public void setDataOrderName(String name) {
			this.name = name;
		}

		public String getDataOrderType() {
			return type;
		}

		public void setDataOrderType(String type) {
			this.type = type;
		}
	}
}
