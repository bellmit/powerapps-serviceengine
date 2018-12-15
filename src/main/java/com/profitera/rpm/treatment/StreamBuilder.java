package com.profitera.rpm.treatment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.profitera.descriptor.business.treatment.admin.*;
import com.profitera.util.MapCar;
import com.profitera.util.Mapper;
import com.profitera.util.xml.DocumentLoader;
import com.profitera.util.xml.NodeLister;

/**
 * 
 *
 * @author jamison
 */
public class StreamBuilder {
	private Map actionNodes = new HashMap();
	private Map streamStarts = new HashMap();
	private Integer stageId;
	private DefaultTreatmentGraph graph = null;
	
	public StreamBuilder(File xmlFile) throws IOException{
		StringBuffer buffer = new StringBuffer();
		BufferedReader br = new BufferedReader(new FileReader(xmlFile));
		String line = br.readLine();
		while (line != null){
			buffer.append(line + "\n");
			line = br.readLine();
		}
		graph = buildActionGraph(buffer.toString());
	}

	/**
	 * @param empty_text
	 */
	public StreamBuilder(String xmlString) {
		graph = buildActionGraph(xmlString);
	}

	/**
	 * @param string
	 * @return
	 */
	private DefaultTreatmentGraph buildActionGraph(String string) {
		Document d = DocumentLoader.parseDocument(string);
		stageId = new Integer(getTextOfFirstChildElementWithName(d.getDocumentElement(), "Stage"));
		actionNodes = getActions(d.getDocumentElement());
		streamStarts = TreatmentGraphs.getStreamStarts(actionNodes);
		return new DefaultTreatmentGraph(stageId, streamStarts, actionNodes);
	}

	/**
	 * @param d
	 * @param checks
	 * @return
	 */
	private Map getActions(Element e) {
		MapCar m = new MapCar(){
			public Object map(Object o) {
				return buildAction((Element) o);
			}};
		return Mapper.asMap(MapCar.map(m, new NodeLister(e.getElementsByTagName("Action"))),new HashMap(), new Mapper(){
			public Object getKey(Object o, int index) {
				return ((ActionNode) o).getId();	
			}});
	}

	/**
	 * @param element
	 * @param checkPoints2
	 * @return
	 */
	private ActionNode buildAction(Element element) {
		ActionNode n = new ActionNode();
		n.setId(element.getAttribute("id"));
		n.setName(element.getAttribute("name"));
		String retryText = getTextOfFirstChildElementWithName(element, "Retries");
		if (retryText != null)
			n.setRetries(Integer.parseInt(retryText));
		Element type = (Element)element.getElementsByTagName("Type").item(0);
		n.setProcessSubtype(new Integer((type).getAttribute("subtypeId")));
		n.setProcessType(new Integer((type).getAttribute("typeId")));
		Element position = getFirstChildElementWithName(element, "Position");
		if (position != null){
			n.setPosition(Integer.parseInt(position.getAttribute("x")), Integer.parseInt(position.getAttribute("y")));
		}
		Element streamElem = getParentElement(element, "Stream");
		n.setStreamId(new Integer(streamElem.getAttribute("id")));
		Element transitions = getFirstChildElementWithName(element, "Transitions");
		// You can have no transitions, a dead-end state.
		if (transitions != null){
			Element deafultTransition = getFirstChildElementWithName(transitions, "DefaultTransition");
			n.setDefaultTransition(buildTransition(deafultTransition));
			MapCar m = new MapCar(){
				public Object map(Object o) {
					return buildTransition((Element) o);
				}};
			List tranistionsList = MapCar.map(m, new NodeLister(transitions.getElementsByTagName("Transition")));
			for (Iterator iter = tranistionsList.iterator(); iter.hasNext();)
				n.addTransition((Transition) iter.next());
		}
		return n;
	}

	/**
	 * <DefaultTransition>
	 *     <Weight>5</Weight>
	 *     <Destination type="Action">2</Destination>
	 * </DefaultTransition>
	 * <Transition>
	 *     <Key>50007</Key>
	 *     <Weight>1</Weight>
	 *     <Destination type="Stage">S2</Destination>
	 * </Transition>
	 * Empty dest will have null as the dest key
	 * @param deafultTransition
	 * @return
	 */
	private Transition buildTransition(Element e) {
		if (e == null)
			return null;
		Transition t = new Transition();
		String keyString = getTextOfFirstChildElementWithName(e, "Key");
		if (keyString!=null)
			t.setEntryStatus(new Integer(keyString));
		t.setDestinationKey(getTextOfFirstChildElementWithName(e, "Destination"));
		t.setName(getTextOfFirstChildElementWithName(e, "Name"));
		if ("".equals(t.getDestinationKey())) t.setDestinationKey(null);
		t.setWeight(Integer.parseInt(getTextOfFirstChildElementWithName(e, "Weight")));
		return t;
	}
	
	private Element getFirstChildElementWithName(Element e, String name){
		NodeList nl = e.getElementsByTagName(name);
		if (nl.getLength() == 0) return null;
		return (Element) nl.item(0);
	}
	
	private String getTextOfFirstChildElementWithName(Element e, String name){
		e = getFirstChildElementWithName(e, name);
		if (e == null) return null;
		for (Iterator i = new NodeLister(e.getChildNodes()).iterator();i.hasNext();){
			Node n = (Node) i.next();
			if (n.getNodeType() == Node.TEXT_NODE) return n.getNodeValue();
		}
		return null;
	}

	/**
	 * @param element
	 * @param string
	 * @return
	 */
	private Element getParentElement(Node element, String tagName) {
		if (element.getParentNode() == null || element.getParentNode() instanceof Element && element.getParentNode().getNodeName().equals(tagName))
			return (Element) element.getParentNode();
		return getParentElement(element.getParentNode(), tagName);
	}
	public DefaultTreatmentGraph getGraph() {
		return graph;
	}
}
