package com.profitera.services.business.worklistmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.profitera.rpm.expression.Expression;
import com.profitera.util.xml.DOMDocumentUtil;

public class WorkListGenerationDecisionTreeParser {

	private static final Log log = LogFactory.getLog(WorkListGenerationDecisionTreeParser.class);
	
	final public static String _AND = "AND";
	final public static String _OR = "OR";
	final public static String _EQUAL = "EQUAL";
	final public static String _NOT_EQUAL = "NOT_EQUAL";
	final public static String _LESS_THAN = "LESS_THAN";
	final public static String _GREATER_THAN = "GREATER_THAN";
	final public static String _LESS_THAN_OR_EQUAL = "LESS_THAN_OR_EQUAL";
	final public static String _GREATER_THAN_OR_EQUAL = "GREATER_THAN_OR_EQUAL";

	final public static String _ASSIGN = "ASSIGN";
	final public static String _MULTIPLY = "MULTIPLY";
	final public static String _DIVIDE = "DIVIDE";
	final public static String _ADD = "ADD";
	final public static String _SUBTRACT = "SUBSTRACT";
	
	public Map getWorkListEntitiesFromXML(byte[] input) throws IOException, SAXException, ParserConfigurationException, Exception{
		if(input == null) throw new IllegalArgumentException("Work list generation decision tree to import cannot be null");
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputSource is = new InputSource(new ByteArrayInputStream(input));
        is.setEncoding("UTF-8");
        Document doc = docBuilder.parse (is);
		
        Node node = doc.getFirstChild();
        if(node.getNodeName()!=WorkListGenerationDecisionTreeExtractor.ROOT)
        	throw new RuntimeException("Invalid root "+node.getNodeName()+" of work list generation decision tree source file");
        if(!hasElements(node)) 
        	log.warn("No children was found for "+node.getNodeName());
        Map allAccount = new HashMap();
        Node childNode = node.getChildNodes().item(0);
        while(childNode!=null){
        	if(isElementNode(childNode)){
        		Element e = (Element)childNode;
        		if(hasElements(e)){
        			List child = getChild(e);
        			allAccount.put(e.getNodeName(), child);
        		}else{
        			assignValue(allAccount, e);
        		}
        	}
        	childNode = childNode.getNextSibling();
        }
		return allAccount;
	}

	private boolean isElementNode(Node childNode) {
		return childNode.getNodeType()==Node.ELEMENT_NODE;
	}

	private void assignValue(Map map, Element e) throws Exception{
		String nodeChildText = DOMDocumentUtil.getNodeChildText(e);
		if(isCompareOperator(e)){
			if(!isLogicalOperator(getENUM(nodeChildText)))
				throw new Exception("Invalid compare operator detected : "+nodeChildText);
			map.put(e.getNodeName(), nodeChildText.length()==0 ? null : new Long(String.valueOf(getENUM(nodeChildText))) );
		}else if(isValueEntry(e) || isWorkListId(e)){
			map.put(e.getNodeName(), nodeChildText.length()==0 ? null : new Long(DOMDocumentUtil.getNodeChildText(e).toString()));
		}else if(isWorkListList(e)){
			map.put(e.getNodeName(), new ArrayList());
		}else if(isChildren(e)){
			map.put(e.getNodeName(), new ArrayList());
		}else if(isUserList(e)){
			map.put(e.getNodeName(), new ArrayList());
		}else if(e.getNodeName().equals("DISTRIBUTION_PERC")){
			map.put(e.getNodeName(), nodeChildText.length()==0 ? null : new Double(DOMDocumentUtil.getNodeChildText(e)));
		}else if(e.getNodeName().equals("DISTRIBUTION_TYPE_ID")){
			map.put(e.getNodeName(), nodeChildText.length()==0 ? null : new Long(DOMDocumentUtil.getNodeChildText(e)));
		}else{
			map.put(e.getNodeName(), nodeChildText.length()==0 ? null : DOMDocumentUtil.getNodeChildText(e));
		}
	}
	
	private boolean isLogicalOperator(int operator){
		int[] operators = Expression.LOGICAL_CONNECTIVES;
		for(int i=0;i<operators.length;i++){
			if(operator == operators[i]) return true;
		}
		return false;
	}
	
	private boolean isWorkListId(Element e) {
		return e.getNodeName() == WorkListGenerationDecisionTreeExtractor.WORK_LIST_ID;
	}
	
	private boolean isValueEntry(Element e) {
		return e.getNodeName()==WorkListGenerationDecisionTreeExtractor.COLUMN_VALUE;
	}
	
	private boolean isUserList(Element e) {
		return e.getNodeName()==WorkListGenerationDecisionTreeExtractor.USER_LIST;
	}

	private boolean isChildren(Element e) {
		return e.getNodeName()==WorkListGenerationDecisionTreeExtractor.CHILDREN;
	}

	private boolean isCompareOperator(Element e) {
		return e.getNodeName()==WorkListGenerationDecisionTreeExtractor.COMPARE_OPERATOR;
	}

	private boolean isWorkListList(Element e) {
		return e.getNodeName()==WorkListGenerationDecisionTreeExtractor.WORK_LIST_LIST;
	}

	private List getChild(Element element) throws Exception{		
		List list = new ArrayList();
		NodeList nodeList = element.getChildNodes();
		for(int i=0;i<nodeList.getLength();i++){
			Map map = new HashMap();
			Node node = nodeList.item(i);
			if(isElementNode(node)){
				Element e = (Element)node;
				NodeList nl = e.getChildNodes();
				for(int x=0;x<nl.getLength();x++){
					Node n = nl.item(x);
					if(isElementNode(n)){
						Element detail = (Element)n;
						if(hasElements(detail)){					
							map.put(detail.getNodeName(), getChild(detail));
						}else{
							assignValue(map, detail);
						}
					}
				}
			}
			if(map.size()!=0)
				list.add(map);
		}
		return list;
	}
	
	private boolean hasElements(Node node){
		NodeList l = node.getChildNodes();
		for(int y=0;y<l.getLength();y++){
			if(l.item(y).getNodeType()==Node.ELEMENT_NODE)
				return true;
		}
		return false;
	}

	private int getENUM(String key){
		if(key.equals(_AND))
			return Expression.AND;
		if(key.equals(_OR))
			return Expression.OR;
		if(key.equals(_EQUAL))
			return Expression.EQUAL;
		if(key.equals(_NOT_EQUAL))
			return Expression.NOT_EQUAL;
		if(key.equals(_LESS_THAN))
			return Expression.LESS_THAN;
		if(key.equals(_LESS_THAN_OR_EQUAL))
			return Expression.LESS_THAN_OR_EQUAL;
		if(key.equals(_GREATER_THAN))
			return Expression.GREATER_THAN;
		if(key.equals(_GREATER_THAN_OR_EQUAL))
			return Expression.GREATER_THAN_OR_EQUAL;
		if(key.equals(_ASSIGN))
			return Expression.ASSIGN;
		if(key.equals(_ADD))
			return Expression.ADD;
		if(key.equals(_SUBTRACT))
			return Expression.SUBTRACT;
		if(key.equals(_MULTIPLY))
			return Expression.MULTIPLY;
		if(key.equals(_DIVIDE))
			return Expression.DIVIDE;
		log.warn("Unregconize compare operator : "+key);
		return -1;
	}
	
}
