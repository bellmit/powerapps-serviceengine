package com.profitera.services.business.worklistmanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.profitera.descriptor.business.meta.IDecisionTreeNode;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.descriptor.business.meta.IWorkList;
import com.profitera.rpm.expression.Expression;

public class WorkListGenerationDecisionTreeExtractor {

	private static final Log log = LogFactory.getLog(WorkListGenerationDecisionTreeExtractor.class);
	
	//ROOT
	public static final String ROOT = "WORK_LIST_ENTITIES";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String CODE = "CODE";
	
	//CONDITION
	public static final String CONDITION = "CONDITION";
	
	//use by both ROOT and CONDITION
	public static final String CHILDREN = "CHILDREN";
	public static final String WORK_LIST_LIST = "WORK_LIST_LIST";
	public static final String COLUMN_NAME = IDecisionTreeNode.COLUMN_NAME;
	public static final String COLUMN_NAME_DESCRIPTION = IDecisionTreeNode.COLUMN_NAME_DESCRIPTION;
	public static final String COLUMN_VALUE_NAME = IDecisionTreeNode.COLUMN_VALUE_NAME;
	public static final String COLUMN_VALUE = IDecisionTreeNode.COLUMN_VALUE;
	public static final String COMPARE_OPERATOR = IDecisionTreeNode.COMPARE_OPERATOR;
	
	//WORKLIST
	public static final String WORK_LIST = "WORK_LIST";
	public static final String WORK_LIST_ID = IWorkList.WORK_LIST_ID;
	public static final String USER_LIST = "USER_LIST";
	
	//USER
	public static final String USER = "USER";
	public static final String USER_ID = IUser.USER_ID;
	
	// use by both WORKLIST and USER
	public static final String NAME = "NAME";
	
	public byte[] exportToXML(Map input) throws IOException, TransformerConfigurationException, SAXException {
		if(input == null) throw new IllegalArgumentException("Work list generation decision tree to extract cannot be null");
		ByteArrayOutputStream out = new ByteArrayOutputStream();		
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		StreamResult streamResult = new StreamResult(writer);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

		TransformerHandler hd = tf.newTransformerHandler();
		Transformer serializer = hd.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		hd.setResult(streamResult);
		
		hd.startDocument();
		hd.startElement("", "", ROOT, getEmptyAttributes());

		for (Iterator i = input.entrySet().iterator(); i.hasNext();) {
			Map.Entry e = (Map.Entry)i.next();
			if(isRootElement(e.getKey().toString())){
				if(e.getValue() instanceof List){
					hd.startElement("", "", e.getKey().toString(), getEmptyAttributes());
					if(e.getKey().equals(WORK_LIST_LIST)){
						hd = addWorkListElement(hd, (List)e.getValue());
					}
					if(e.getKey().equals(CHILDREN)){
						hd = addChildrenElement(hd, (List)e.getValue());
					}
					hd.endElement("", "", e.getKey().toString());
				}else{
					if(e.getKey().equals(COMPARE_OPERATOR)){
						Object o = e.getValue();
						String value = null;
						if(o!=null){
							value = convertToText(Integer.parseInt(o.toString()));
						}
						hd = addElement(hd, e.getKey().toString(), value);
					}else{
						String value = e.getValue() == null? null : e.getValue().toString();
						hd = addElement(hd, e.getKey().toString(), value);
					}
				}
			}
		}

		hd.endElement("", "", ROOT);
		hd.endDocument();

		return out.toByteArray();
	}

	private TransformerHandler addWorkListElement(TransformerHandler handler, List list) throws SAXException{
		for(Iterator i = list.iterator();i.hasNext();){
			handler.startElement("", "", WORK_LIST, getEmptyAttributes());
			Map map = (Map)i.next();
			for(Iterator iter = map.entrySet().iterator();iter.hasNext();){
				Map.Entry e = (Map.Entry)iter.next();
				if(e.getValue() instanceof List){
					if(e.getKey().equals(USER_LIST)){
						handler.startElement("", "", USER_LIST, getEmptyAttributes());
						handler = addUserElement(handler, (List)e.getValue());
						handler.endElement("", "", USER_LIST);
					}
				}else{
					String value = e.getValue() == null? null : e.getValue().toString();
					handler = addElement(handler, e.getKey().toString(), value);
				}
			}
			handler.endElement("", "", WORK_LIST);
		}
		return handler;
	}
	
	private TransformerHandler addUserElement(TransformerHandler handler, List list) throws SAXException{
		for(Iterator i = list.iterator();i.hasNext();){
			handler.startElement("", "", USER, getEmptyAttributes());
			Map map = (Map)i.next();
			for(Iterator iter = map.entrySet().iterator();iter.hasNext();){
				Map.Entry e = (Map.Entry)iter.next();
				String value = e.getValue() == null? null : e.getValue().toString();
				handler = addElement(handler, e.getKey().toString(), value);
			}
			handler.endElement("", "", USER);
		}
		return handler;
	}
	
	private TransformerHandler addChildrenElement(TransformerHandler handler, List list) throws SAXException{
		for(Iterator i = list.iterator();i.hasNext();){
			handler.startElement("", "", CONDITION, getEmptyAttributes());
			Map map = (Map)i.next();
			for(Iterator iter = map.entrySet().iterator();iter.hasNext();){
				Map.Entry e = (Map.Entry)iter.next();
				if(e.getValue() instanceof List){
					handler.startElement("", "", e.getKey().toString(), getEmptyAttributes());
					if(e.getKey().equals(WORK_LIST_LIST)){
						handler = addWorkListElement(handler, (List)e.getValue());
					}
					if(e.getKey().equals(CHILDREN)){
						handler = addChildrenElement(handler, (List)e.getValue());
					}
					handler.endElement("", "", e.getKey().toString());
				}else{
					if(e.getKey().equals(COMPARE_OPERATOR)){
						Object o = e.getValue();
						String value = null;
						if(o!=null){
							value = convertToText(Integer.parseInt(o.toString()));
						}
						handler = addElement(handler, e.getKey().toString(), value);
					}else{
						String value = e.getValue() == null? null : e.getValue().toString();
						handler = addElement(handler, e.getKey().toString(), value);
					}
				}
			}
			handler.endElement("", "", CONDITION);
		}		
		return handler;
	}
	
	private TransformerHandler addElement(TransformerHandler handler, String key, String value) throws SAXException{
		handler.startElement("","",key,getEmptyAttributes());
		if(value!=null){		
			handler.characters(value.toCharArray(),0,value.length());		
		}
		handler.endElement("","",key);
		return handler;
	}

	private boolean isRootElement(String key){
		if(	
				key.equals(WorkListGenerationDecisionTreeExtractor.COLUMN_NAME) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.COLUMN_VALUE) ||
				key.equals(IDecisionTreeNode.COLUMN_VALUE_NAME) ||
				key.equals(IDecisionTreeNode.COLUMN_NAME_DESCRIPTION) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.COMPARE_OPERATOR) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.DESCRIPTION) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.CODE) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.WORK_LIST_LIST) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.CHILDREN)
		) return true;
		return false;
	}
	
	private boolean isConditionElement(String key){
		if(	
				key.equals(WorkListGenerationDecisionTreeExtractor.COLUMN_NAME) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.COLUMN_VALUE) ||
				key.equals(IDecisionTreeNode.COLUMN_VALUE_NAME) ||
				key.equals(IDecisionTreeNode.COLUMN_NAME_DESCRIPTION) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.COMPARE_OPERATOR) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.WORK_LIST_LIST) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.CHILDREN)
		) return true;
		return false;
	}
	
	private boolean isWorkListElement(String key){
		if(	
				key.equals(WorkListGenerationDecisionTreeExtractor.NAME) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.WORK_LIST_ID) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.USER_LIST)
		) return true;
		return false;
	}
	
	private boolean isUserElement(String key){
		if(	
				key.equals(WorkListGenerationDecisionTreeExtractor.NAME) ||
				key.equals(WorkListGenerationDecisionTreeExtractor.USER_ID) 
		) return true;
		return false;
	}
	
	private String convertToText(int operator){
		switch(operator){
		case Expression.AND:
			return "AND";
		case Expression.OR:
			return "OR";
		case Expression.EQUAL:
			return "EQUAL";
		case Expression.NOT_EQUAL:
			return "NOT_EQUAL";
		case Expression.LESS_THAN:
			return "LESS_THAN";
		case Expression.GREATER_THAN:
			return "GREATER_THAN";
		case Expression.LESS_THAN_OR_EQUAL:
			return "LESS_THAN_OR_EQUAL";
		case Expression.GREATER_THAN_OR_EQUAL:
			return "GREATER_THAN_OR_EQUAL";
		case Expression.ASSIGN:
			return "ASSIGN";
		case Expression.MULTIPLY:
			return "MULTIPLY";
		case Expression.DIVIDE:
			return "DIVIDE";
		case Expression.ADD:
			return "ADD";
		case Expression.SUBTRACT:
			return "SUBTRACT";
			default:
				log.warn("Unrecognize operator ENUM :" + operator);
				return String.valueOf(operator);
		}
	}
	
	private Attributes getEmptyAttributes(){
		return new AttributesImpl();
	}
	
}
