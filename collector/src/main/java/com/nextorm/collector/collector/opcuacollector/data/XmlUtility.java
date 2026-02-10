package com.nextorm.collector.collector.opcuacollector.data;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class XmlUtility {
	private DocumentBuilder documentBuilder = null;
	private Document document = null;
	private Element root = null;
	private Node currentNode = null;
	private NodeList currentNodeList = null;
	private String encoding = null;
	private String header = null;
	private String loggerName = null;
	static DocumentBuilderFactory factory = null;

	static {
		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		factory.setIgnoringComments(false);
		factory.setIgnoringElementContentWhitespace(false);
		factory.setCoalescing(false);
		factory.setExpandEntityReferences(true);
	}

	private static class Attribute {
		private final String name;
		private final String value;

		private Attribute(
			String p_name,
			String p_value
		) {
			name = p_name;
			value = p_value;
		}

		private String getName() {
			return name;
		}

		private String getValue() {
			return value;
		}
	}

	private static class XmlNodeList implements NodeList {

		ArrayList<Node> list = new ArrayList<Node>();

		public void addItem(Node p_node) throws Exception {
			list.add(p_node);
		}

		public Node item(int index) {
			if (index < list.size()) {
				return list.get(index);
			} else {
				return null;
			}
		}

		public int getLength() {
			return list.size();
		}

	}

	public static String toString(Element p_element) throws Exception {
		StringBuilder l_result = new StringBuilder();
		Node l_node = null;
		String l_value = null;
		l_result.append("<")
				.append(p_element.getNodeName());

		if (p_element.hasAttributes()) {
			NamedNodeMap l_attrs = p_element.getAttributes();
			for (int i = 0; i < l_attrs.getLength(); i++) {
				l_node = l_attrs.item(i);
				l_result.append(" ")
						.append(l_node.getNodeName())
						.append("=\"");
				l_value = l_node.getNodeValue();
				l_value = l_value.replaceAll("&", "&amp;");
				l_value = l_value.replaceAll("<", "&lt;");
				l_value = l_value.replaceAll(">", "&gt;");
				l_value = l_value.replaceAll("\"", "&quot;");
				l_value = l_value.replaceAll("\'", "&apos;");
				l_result.append(l_value)
						.append("\"");
			}
			l_node = null;
		}
		if (p_element.hasChildNodes()) {
			l_result.append(">");
			NodeList l_list = p_element.getChildNodes();
			for (int i = 0; i < l_list.getLength(); i++) {
				l_node = l_list.item(i);
				switch (l_node.getNodeType()) {
					case Node.ELEMENT_NODE:
						l_result.append(toString((Element)l_node));
						break;
					case Node.CDATA_SECTION_NODE:
						l_result.append("<![CDATA[");
						l_result.append(l_node.getNodeValue());
						l_result.append("]]>");
						break;
					case Node.COMMENT_NODE:
						l_result.append("<!--");
						l_result.append(l_node.getNodeValue());
						l_result.append("-->");
						break;
					case Node.TEXT_NODE:
					default:
						l_value = l_node.getNodeValue();
						l_value = l_value.replaceAll("&", "&amp;");
						l_value = l_value.replaceAll("<", "&lt;");
						l_value = l_value.replaceAll(">", "&gt;");
						l_result.append(l_value);
						break;
				}
			}
			l_node = null;
			l_result.append("</")
					.append(p_element.getNodeName())
					.append(">");
		} else {
			l_result.append(" />");
		}
		return l_result.toString();
	}

	public XmlUtility() throws Exception {
		if (encoding == null) {
			setEncoding(System.getProperty("file.encoding"));
		}
		buildHeader();
		documentBuilder = factory.newDocumentBuilder();
	}

	public XmlUtility(String p_source) throws Exception {
		if (encoding == null) {
			setEncoding(System.getProperty("file.encoding"));
		}
		buildHeader();
		documentBuilder = factory.newDocumentBuilder();

		parse(p_source);
	}

	public XmlUtility(
		String p_source,
		String p_encoding
	) throws Exception {
		setEncoding(p_encoding);
		documentBuilder = factory.newDocumentBuilder();
		parse(p_source);
	}

	private void buildHeader() throws Exception {
		header = "<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\" ?>";
	}

	public boolean parse(String string) throws Exception {
		String l_encoding = getEncoding();
		if (string != null && !string.equals("")) {
			string = string.trim();
			if (string.startsWith("<")) {
				if (string.startsWith("<?xml")) {
					int l_start = -1;
					int l_end = -1;
					l_start = string.indexOf("encoding=\"");
					if (l_start > 0) {
						l_start += 10;
						l_end = string.indexOf("\"", l_start);
						l_encoding = string.substring(l_start, l_end);
					} else {
						l_start = string.indexOf("encoding='");
						if (l_start > 0) {
							l_start += 10;
							l_end = string.indexOf("'", l_start);
							l_encoding = string.substring(l_start, l_end);
						}
					}
					if (!Charset.isSupported(l_encoding)) {
						l_encoding = getEncoding();
					}
				} else {
					string = header + string;
				}
				ByteArrayInputStream in = new ByteArrayInputStream(string.getBytes(l_encoding));
				document = documentBuilder.parse(in);
			} else {
				File l_file = new File(string);
				if (l_file.exists()) {
					FileInputStream in = new FileInputStream(l_file);
					document = documentBuilder.parse(in);
				} else {
					return false;
				}
			}
			root = document.getDocumentElement();
		}
		return true;
	}

	public NodeList getNodeList(String name) throws Exception {
		if (name != null) {
			if (name.startsWith("/")) {
				return XmlUtility.selectNodeList(root, name);
			} else {
				return XmlUtility.selectNodeList(currentNode, name);
			}
		} else {
			return currentNodeList;
		}
	}

	public NodeList getNodeList(
		Node p_node,
		String name
	) throws Exception {
		currentNodeList = XmlUtility.selectNodeList(p_node, name);
		return currentNodeList;
	}

	public Node getNode(String namePath) throws Exception {
		Node node = null;

		if (this.currentNode == null || namePath.startsWith("/")) {
			node = this.document.getDocumentElement();
		} else {
			node = this.currentNode;
		}

		if (node != null) {
			node = getNode(node, namePath);
		}
		this.currentNode = node;
		return this.currentNode;
	}

	private static Node findNode(
		Node parentNode,
		String childName
	) throws Exception {
		for (Node child = parentNode.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName()
																 .compareToIgnoreCase(childName) == 0) {
				return child;
			}
		}
		return null;
	}

	private static Node findNode(
		Node node,
		String name,
		Attribute[] p_attributes
	) throws Exception {
		Node matchNode = null;
		boolean found = false;
		for (Node child = node.getFirstChild(); child != null && found == false; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName()
																 .compareToIgnoreCase(name) == 0) {
				if (child.hasAttributes()) {
					NamedNodeMap atts = child.getAttributes();
					found = true;
					if (p_attributes != null) {
						Node l_node = null;
						for (int i = 0; i < p_attributes.length; i++) {
							l_node = atts.getNamedItem(p_attributes[i].getName());
							if (l_node == null) {
								found = false;
								break;
							} else {
								if (!p_attributes[i].getValue()
													.equals(l_node.getNodeValue())) {
									found = false;
									break;
								}
							}
						}
						if (found) {
							matchNode = child;
						}
					}

				}
			}
		}
		return matchNode;
	}

	private static Attribute[] buildAttribute(String p_attributes) throws Exception {
		int index = -1;
		int start = -1;
		int end = -1;
		ArrayList<Attribute> l_array = new ArrayList<Attribute>();
		Attribute[] l_attribute = null;
		String l_name = null;
		String l_value = null;
		char l_delVal = '"';
		while (true) {
			index = p_attributes.indexOf("[@", end);
			if (index == -1) {
				break;
			}
			start = index + 2;
			end = p_attributes.indexOf("=", start);
			l_name = p_attributes.substring(start, end);
			start = end + 2;
			l_delVal = p_attributes.charAt(start - 1);
			end = p_attributes.indexOf(l_delVal, start);
			l_value = p_attributes.substring(start, end);
			l_array.add(new Attribute(l_name, l_value));
		}
		l_attribute = new Attribute[l_array.size()];
		l_array.toArray(l_attribute);
		return l_attribute;
	}

	public static Node selectNode(
		Node node,
		String namePath
	) throws Exception {
		Node child = null;
		Attribute[] l_attribute = null;
		if (node != null) {
			String lastTag = "";
			if (namePath.equals("/")) {
				Node l_node = node.getOwnerDocument()
								  .getFirstChild();

				while (l_node != null) {
					if (l_node.getNodeType() == Node.ELEMENT_NODE) {
						return l_node;
					} else {
						l_node = l_node.getNextSibling();
					}
				}
				return l_node;
			}

			int index = namePath.indexOf("[@");
			if (index > 0) {
				l_attribute = buildAttribute(namePath.substring(index));
				namePath = namePath.substring(0, index);
				index = namePath.lastIndexOf("/");
				lastTag = namePath.substring(index + 1);
				if (index != -1) {
					namePath = namePath.substring(0, index);
				} else {
					namePath = "";
				}
			}
			StringTokenizer st = new StringTokenizer(namePath, "/");
			if (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (node.getNodeName()
						.compareToIgnoreCase(token) != 0) {
					for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
						if (node.getNodeName()
								.compareToIgnoreCase(token) == 0) {
							child = node;
							while (st.hasMoreTokens()) {
								token = st.nextToken();
								child = findNode(child, token);
								if (child == null) {
									break;
								}
							}
							if (child != null) {
								break;
							}
						}
					}
				} else {
					if (node.getNodeName()
							.compareToIgnoreCase(token) == 0) {
						child = node;
						while (st.hasMoreTokens()) {
							token = st.nextToken();
							child = findNode(child, token);
							if (child == null) {
								break;
							}
						}
					}
				}
			}
			if (l_attribute != null) {
				if (child != null) {
					child = findNode(child, lastTag, l_attribute);
				} else {
					child = findNode(node, lastTag, l_attribute);
				}
			}
		}
		return child;
	}

	public static NodeList selectNodeList(
		Node p_node,
		String p_namePath
	) throws Exception {
		XmlNodeList l_result = new XmlNodeList();
		Node l_startNode = null;
		String l_namePath = null;
		if (p_namePath != null) {
			if (p_namePath.startsWith("/") == true) {
				l_startNode = p_node.getOwnerDocument();
				l_namePath = p_namePath.substring(1);
			} else {
				l_startNode = p_node;
				l_namePath = p_namePath;
			}
			selectNodeList(l_startNode, l_namePath, l_result);

		}
		return l_result;
	}

	private static void selectNodeList(
		Node p_node,
		String p_namePath,
		XmlNodeList p_list
	) throws Exception {
		int l_index = -1;
		String l_namePath = null;
		String l_findNodeName = null;
		Node l_node = null;
		NodeList l_nodeList = null;
		ArrayList<NamedValue> l_attributes = null;
		if (p_namePath != null) {
			l_index = p_namePath.indexOf("/");
			if (l_index != -1) {
				l_findNodeName = p_namePath.substring(0, l_index);
				l_namePath = p_namePath.substring(l_index + 1);
				l_index = l_findNodeName.indexOf("[@");
				if (l_index == -1) {
					if (p_node.hasChildNodes()) {
						l_nodeList = p_node.getChildNodes();
						for (int i = 0; i < l_nodeList.getLength(); i++) {
							l_node = l_nodeList.item(i);
							if (l_node.getNodeType() == Node.ELEMENT_NODE) {
								if (l_node.getNodeName()
										  .equals(l_findNodeName) == true) {
									selectNodeList(l_node, l_namePath, p_list);
								}
							}
						}
					}
				} else {
					int l_tmpIndex = p_namePath.indexOf("]/", l_index);
					if (l_tmpIndex == -1) {
						l_findNodeName = p_namePath;
						l_tmpIndex = l_findNodeName.length() - 1;
					} else {
						l_findNodeName = p_namePath.substring(0, l_tmpIndex + 1);
					}
					l_namePath = p_namePath.substring(l_tmpIndex + 1);
					l_attributes = retrieveAttributes(l_findNodeName);
					l_findNodeName = l_findNodeName.substring(0, l_index);
					boolean found = true;

					if (p_node.hasChildNodes()) {
						l_nodeList = p_node.getChildNodes();
						for (int i = 0; i < l_nodeList.getLength(); i++) {
							l_node = l_nodeList.item(i);
							if (l_node.getNodeType() == Node.ELEMENT_NODE) {
								if (l_node.getNodeName()
										  .equals(l_findNodeName) == true) {
									if (l_node.hasAttributes()) {
										NamedNodeMap l_map = l_node.getAttributes();
										NamedValue l_namedValue = null;
										Node l_attrNode = null;
										found = true;

										for (int j = 0; j < l_attributes.size(); j++) {

											l_namedValue = (NamedValue)l_attributes.get(j);
											l_attrNode = l_map.getNamedItem(l_namedValue.getName());
											if (l_namedValue.getValue()
															.equals(l_attrNode.getNodeValue()) == false) {
												found = false;
												break;
											}
										}
										if (found == true) {
											if (l_namePath.length() == 0) {
												p_list.addItem(l_node);
											} else {
												if (l_namePath.startsWith("/")) {
													l_namePath = l_namePath.substring(1);
												}
												selectNodeList(l_node, l_namePath, p_list);
											}
										}
									}

								}
							}
						}
					}
				}

			} else {
				l_index = p_namePath.indexOf("[@");
				if (l_index == -1) {
					l_findNodeName = p_namePath;
					if (p_node.hasChildNodes()) {
						l_nodeList = p_node.getChildNodes();
						for (int i = 0; i < l_nodeList.getLength(); i++) {
							l_node = l_nodeList.item(i);
							if (l_node.getNodeType() == Node.ELEMENT_NODE) {
								if (l_node.getNodeName()
										  .equals(l_findNodeName) == true) {
									p_list.addItem(l_node);
								}
							}
						}
					}
				} else {
					int l_tmpIndex = p_namePath.indexOf(']', l_index);
					l_findNodeName = p_namePath.substring(0, l_tmpIndex + 1);
					l_namePath = p_namePath.substring(l_tmpIndex + 1);
					l_findNodeName = p_namePath.substring(0, l_index);

					boolean found = true;
					if (p_node.hasChildNodes()) {
						l_attributes = retrieveAttributes(p_namePath);
						l_nodeList = p_node.getChildNodes();
						for (int i = 0; i < l_nodeList.getLength(); i++) {
							l_node = l_nodeList.item(i);
							if (l_node.getNodeType() == Node.ELEMENT_NODE) {
								if (l_node.getNodeName()
										  .equals(l_findNodeName) == true) {
									if (l_node.hasAttributes()) {
										NamedNodeMap l_map = l_node.getAttributes();
										NamedValue l_namedValue = null;
										Node l_attrNode = null;
										found = true;

										for (int j = 0; j < l_attributes.size(); j++) {

											l_namedValue = (NamedValue)l_attributes.get(j);
											l_attrNode = l_map.getNamedItem(l_namedValue.getName());
											if (l_namedValue.getValue()
															.equals(l_attrNode.getNodeValue()) == false) {
												found = false;
												break;
											}
										}
										if (found == true) {
											p_list.addItem(l_node);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private static class NamedValue {
		private final String name;
		private final String value;

		private NamedValue(
			String p_name,
			String p_value
		) {
			name = p_name;
			value = p_value;
		}

		private String getName() {
			return name;
		}

		private String getValue() {
			return value;
		}
	}

	private static ArrayList<NamedValue> retrieveAttributes(String p_namePath) throws Exception {
		ArrayList<NamedValue> l_result = new ArrayList<NamedValue>();
		int l_start = 0;
		int l_end = -1;
		String l_name = null;
		String l_value = null;
		String l_del = "\"";
		while (true) {
			l_start = p_namePath.indexOf("[@", l_start);
			if (l_start == -1) {
				break;
			} else {
				l_start += 2;
			}

			l_end = p_namePath.indexOf("=" + l_del, l_start);
			if (l_end == -1) {
				l_del = "'";
				l_end = p_namePath.indexOf("=" + l_del, l_start);
			}
			l_name = p_namePath.substring(l_start, l_end);
			l_start = l_end + 2;
			l_end = p_namePath.indexOf(l_del, l_start);
			l_value = p_namePath.substring(l_start, l_end);
			l_result.add(new NamedValue(l_name, l_value));
			l_start = l_end;
		}
		return l_result;
	}

	public Node getNode(
		Node node,
		String namePath
	) throws Exception {

		currentNode = XmlUtility.selectNode(node, namePath);
		return currentNode;
	}

	public Node getNode(
		String inputSource,
		String namePath
	) throws Exception {
		Node node = null;
		if (parse(inputSource)) {
			node = getNode(namePath);
		}

		this.currentNode = node;
		return this.currentNode;
	}

	public Node getNode(
		Node node,
		int index
	) throws Exception {
		Node child = null;
		int j = 0;
		if (node != null) {
			if (node.hasChildNodes()) {
				NodeList nodeList = node.getChildNodes();
				int count = nodeList.getLength();
				for (int i = 0; i < count; i++) {
					child = nodeList.item(i);
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						if (j++ == index) {
							break;
						}
					}
				}
			}
		}

		currentNode = child;
		return currentNode;
	}

	public String getNodeText(
		String inputSource,
		String namePath
	) throws Exception {
		return getNodeText(getNode(inputSource, namePath));
	}

	public String getNodeText(String namePath) throws Exception {
		return getNodeText(getNode(namePath));
	}

	public String getNodeText(
		Node node,
		String namePath
	) throws Exception {
		return getNodeText(getNode(node, namePath));
	}

	public String getNodeText(
		Node node,
		int index
	) throws Exception {
		return getNodeText(getNode(node, index));
	}

	public String getNodeText() throws Exception {
		return getNodeText(currentNode);
	}

	public String getNodeText(int index) throws Exception {
		String nodeText = "";
		if (currentNodeList != null) {
			if (index >= 0 && index < currentNodeList.getLength()) {
				nodeText = currentNodeList.item(index)
										  .getNodeValue();
			}
		}
		return nodeText;
	}

	//	public String getAttribute(int index) throws Exception{
	//		return getAttribute(this.currentNode, index);
	//	}

	//	public String getAttribute(Node node, int index) throws Exception
	//	{
	//		String value = "";
	//		if (node != null)
	//		{
	//			if (node.hasAttributes())
	//			{
	//				NamedNodeMap atts = node.getAttributes();
	//				if (index >= 0 && index < atts.getLength())
	//				{
	//					value = atts.item(index).getNodeValue();
	//				}
	//			}
	//		}
	//		return value;
	//	}

	public String getEncoding() throws Exception {
		if (encoding == null) {
			encoding = System.getProperty("file.encoding");
		} else {
			if (!Charset.isSupported(encoding)) {
				encoding = System.getProperty("file.encoding");
			}
		}
		return encoding;
	}

	public void setEncoding(String string) throws Exception {
		if (Charset.isSupported(string)) {
			encoding = string;
		}
	}

	public static String getNodeText(Node node) throws Exception {
		String nodeText = "";
		if (node != null) {
			for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
					nodeText = child.getNodeValue();
					nodeText = nodeText.replace('\t', ' ')
									   .replace('\n', ' ')
									   .replace('\r', ' ')
									   .replace('\b', ' ')
									   .replace('\f', ' ');
					if (!nodeText.trim()
								 .equals("")) {
						break;
					}
				}
			}
		}

		return nodeText;
	}

	public static String getAttribute(
		Node node,
		String name,
		String p_defaultValue
	) throws Exception {
		String value = p_defaultValue;
		if (node != null) {
			if (node.hasAttributes()) {
				NamedNodeMap atts = node.getAttributes();
				Node att = atts.getNamedItem(name);
				if (att != null) {
					value = att.getNodeValue();
				} else {
					value = p_defaultValue;
				}
			}
		}
		return value;
	}

	public static void setAttribute(
		Node node,
		String name,
		String value
	) throws Exception {
		if (node != null) {
			if (node.hasAttributes()) {
				NamedNodeMap atts = node.getAttributes();

				Node att = atts.getNamedItem(name);
				if (att == null) {
					att = node.getOwnerDocument()
							  .createAttribute(name);
					atts.setNamedItem(att);
				}
				att.setNodeValue(value);
			}
		}
	}

	public static List<String> getAttributeNameList(Node p_node) {
		List<String> nameList = new ArrayList<String>();
		if (p_node != null) {
			if (p_node.hasAttributes()) {
				NamedNodeMap atts = p_node.getAttributes();
				for (int i = 0; i < atts.getLength(); i++) {
					Node att = atts.item(i);
					if (att != null) {
						nameList.add(att.getNodeName());
					}
				}
			}
		}
		return nameList;
	}

	public static boolean getAttribute(
		Node node,
		String name,
		boolean p_defaultValue
	) throws Exception {
		return Boolean.parseBoolean(getAttribute(node, name, "FALSE"));
	}

	public static int getAttribute(
		Node node,
		String name,
		int p_defaultValue
	) throws Exception {
		int retVal = p_defaultValue;
		String tmp = getAttribute(node, name, "0");
		if (tmp != null && tmp.length() > 0) {
			try {
				retVal = Integer.parseInt(tmp);
			} catch (Exception ex) {
			}
		}
		return retVal;
	}

	public static long getAttribute(
		Node node,
		String name,
		long p_defaultValue
	) throws Exception {
		long retVal = p_defaultValue;
		String tmp = getAttribute(node, name, "0");
		if (tmp != null && tmp.length() > 0) {
			try {
				retVal = Long.parseLong(tmp);
			} catch (Exception ex) {
			}
		}
		return retVal;
	}

	public static String getAttrValue(
		String p_nodeString,
		String p_xPath,
		String p_name,
		String p_defaultValue
	) {
		String retVal = p_defaultValue;
		try {
			if (p_nodeString != null && !p_nodeString.equals("")) {
				XmlUtility xmlReader = new XmlUtility(p_nodeString);
				Node node = xmlReader.getNode(p_xPath);
				if (node != null) {
					retVal = XmlUtility.getAttribute(node, p_name, p_defaultValue);
				}
				xmlReader = null;
			}
		} catch (Exception ex) {

		}
		return retVal;
	}

	public static String getInnerXml(
		Node p_node,
		String p_default
	) throws Exception {
		StringBuilder l_result = new StringBuilder();

		if (p_node != null) {
			NodeList l_list = p_node.getChildNodes();
			Node l_node = null;
			for (int i = 0; i < l_list.getLength(); i++) {
				l_node = l_list.item(i);
				switch (l_node.getNodeType()) {
					case Node.ELEMENT_NODE:
						l_result.append(toString((Element)l_node));
						break;
					case Node.CDATA_SECTION_NODE:
						l_result.append("<![CDATA[");
						l_result.append(l_node.getNodeValue());
						l_result.append("]]>");
						break;
					case Node.COMMENT_NODE:
						l_result.append("<!--\n");
						l_result.append(l_node.getNodeValue());
						l_result.append("-->\n");
						break;
					case Node.TEXT_NODE:
					default:
						l_result.append(l_node.getNodeValue());
						break;
				}
			}

		} else {
			return p_default;
		}

		if (l_result.length() == 0) {
			return p_default;
		} else {
			return l_result.toString();
		}
	}

	public static String getOuterXml(Node p_node) throws Exception {
		if (p_node != null) {
			return toString(p_node.getOwnerDocument()
								  .getDocumentElement());
		} else {
			return "";
		}
	}

}
