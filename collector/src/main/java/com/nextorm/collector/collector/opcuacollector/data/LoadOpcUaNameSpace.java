package com.nextorm.collector.collector.opcuacollector.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;

@Slf4j
@Component
public class LoadOpcUaNameSpace {
	private String opcUaNameSpaceXmlFileName = "";

	public LoadOpcUaNameSpace() {
	}

	public void initilaize(String xmlFileName) {
		if (xmlFileName != null) {
			opcUaNameSpaceXmlFileName = xmlFileName;
		}
	}

	public ArrayList<OpcUaData> loadToolConnectorList(
		String strNode,
		String strNodeItem
	) {
		ArrayList<OpcUaData> retArray = new ArrayList<OpcUaData>();
		String fileParamOrder = readOpcUaNameSpaceFile();
		if (fileParamOrder != null && fileParamOrder.length() > 0) {
			XmlUtility xmlUtility;
			try {
				xmlUtility = new XmlUtility(fileParamOrder);
				Node node = xmlUtility.getNode(strNode);
				if (node != null) {
					parseNode(xmlUtility, strNodeItem, retArray);
				}
			} catch (Exception e) {
				log.error(String.valueOf(e));
			}
		}
		return retArray;
	}

	private String readOpcUaNameSpaceFile() {
		String opcUaContents = "";

		try {
			opcUaContents = FileUtility.readFileContents(opcUaNameSpaceXmlFileName);
			if (opcUaContents != null && opcUaContents != "") {
				opcUaContents = FileUtility.removeCRString(opcUaContents);
				opcUaContents = FileUtility.removeTABString(opcUaContents);
			} else {
				log.warn("OpcUa Xml File's Contents is not valid");
			}
		} catch (Throwable e) {
			log.error("ERROR", "OpcUa Xml File is not Exist : " + opcUaNameSpaceXmlFileName);
		}

		return opcUaContents;
	}

	public boolean checkLocalConfigFile() {
		boolean isExist = false;
		try {
			File file = new File(opcUaNameSpaceXmlFileName);
			isExist = file.exists();
			file = null;
		} catch (Exception ex) {
			log.error(String.valueOf(ex));
		}
		return isExist;
	}

	private void parseNode(
		XmlUtility p_xmlUtility,
		String p_namePath,
		ArrayList<OpcUaData> p_toolConnectorDataList
	) {
		try {
			NodeList nodeList = p_xmlUtility.getNodeList(p_namePath);
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					OpcUaData opcUaData = new OpcUaData();

					if (p_namePath.contains("UaMonitoredItem")) {
						opcUaData.uaMonitoredItem.setNodeId(XmlUtility.getAttribute(node, "NodeId", "NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setNameSpaceIndex(XmlUtility.getAttribute(node,
							"NameSpaceIndex",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setAccessLevel(XmlUtility.getAttribute(node,
							"AccessLevel",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setUserAccessLevel(XmlUtility.getAttribute(node,
							"UserAccessLevel",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setBrowseName(XmlUtility.getAttribute(node,
							"BrowseName",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setDisplayName(XmlUtility.getAttribute(node,
							"DisplayName",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setDataType(XmlUtility.getAttribute(node, "DataType", "NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setTypeDefinition(XmlUtility.getAttribute(node,
							"TypeDefinition",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setReferenceType(XmlUtility.getAttribute(node,
							"ReferenceType",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setReferenceTragetNodeType(XmlUtility.getAttribute(node,
							"ReferenceTragetNodeType",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setTimestampsToReturn(XmlUtility.getAttribute(node,
							"TimestampsToReturn",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setNodeClass(XmlUtility.getAttribute(node,
							"NodeClass",
							"NOT_DEFINED"));
						opcUaData.uaMonitoredItem.setMonitoringMode(XmlUtility.getAttribute(node,
							"MonitoringMode",
							"NOT_DEFINED"));
					} else if (p_namePath.contains("DataValue")) {
						opcUaData.dataValue.setSeq(XmlUtility.getAttribute(node, "Seq", "NOT_DEFINED"));
						opcUaData.dataValue.setNodeId(XmlUtility.getAttribute(node, "NodeId", "NOT_DEFINED"));
						opcUaData.dataValue.setNameSpaceIndex(XmlUtility.getAttribute(node,
							"NameSpaceIndex",
							"NOT_DEFINED"));
						opcUaData.dataValue.setDataType(XmlUtility.getAttribute(node, "DataType", "NOT_DEFINED"));
					} else if (p_namePath.contains("ParamOrder")) {
						opcUaData.paramOrder.setParamOrderName(XmlUtility.getAttribute(node, "Name", "NOT_DEFINED"));
					} else if (p_namePath.contains("DataOrder")) {
						opcUaData.dataOrder.setDataOrderName(XmlUtility.getAttribute(node, "Name", "NOT_DEFINED"));
						opcUaData.dataOrder.setDataOrderType(XmlUtility.getAttribute(node, "Type", "NOT_DEFINED"));
					}
					p_toolConnectorDataList.add(opcUaData);
				}
			}
		} catch (Exception e) {
			log.error(String.valueOf(e));
		}

	}
}
