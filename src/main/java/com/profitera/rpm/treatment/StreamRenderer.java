package com.profitera.rpm.treatment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.profitera.descriptor.business.treatment.admin.ActionNode;
import com.profitera.descriptor.business.treatment.admin.Transition;
import com.profitera.descriptor.business.treatment.admin.TreatmentGraph;
import com.profitera.descriptor.business.treatment.admin.TreatmentGraphs;
import com.profitera.util.xml.DocumentLoader;
import com.profitera.util.xml.DocumentRenderer;

/**
 * 
 *
 * @author jamison
 */
public class StreamRenderer {
	
	public static String getXMLString(String stageId, TreatmentGraph g){
		Document d = DocumentLoader.getNewDocument();
		Element docElement = d.createElement("Treatment");
		d.appendChild(docElement);
		docElement.appendChild(createElementWithText(d, "Stage", stageId));
		ActionNode[] starts = g.getAllStreamStarts();
		for (int i = 0; i < starts.length; i++) 
			docElement.appendChild(buildStream(starts[i], g, d));			
		return DocumentRenderer.transform(d);
	}

	private static Node buildStream(ActionNode node, TreatmentGraph g, Document d) {
		Element streamElem = d.createElement("Stream");
		streamElem.setAttribute("id", node.getStreamId().toString());
		Map streamNodes = new HashMap();
		TreatmentGraphs.getStreamNodes(node.getStreamId(), node, g, streamNodes);
		for(Iterator i = streamNodes.values().iterator();i.hasNext();)
			streamElem.appendChild(renderActionNode((ActionNode)i.next(), d));
		return streamElem;
	}

	/**
	 * @param node
	 * @return
	 */
	private static Node renderActionNode(ActionNode node, Document d) {
		Element action = d.createElement("Action");
		action.setAttribute("id", node.getId().toString());
		action.setAttribute("name", node.getName().toString());
		Element type = d.createElement("Type");
		action.appendChild(type);
		type.setAttribute("typeId", node.getProcessType().toString());
		type.setAttribute("subtypeId", node.getProcessSubtype().toString());
		action.appendChild(createElementWithText(d, "Retries", node.getRetries() + ""));
		Element pos = d.createElement("Position");
		action.appendChild(pos);
		pos.setAttribute("x", node.getPosition().x+"");
		pos.setAttribute("y", node.getPosition().y+"");
		Element trans = d.createElement("Transitions");
		action.appendChild(trans);
		Transition defaultTrans = node.getDefaultTransition();
		if (defaultTrans != null){
			Element t = d.createElement("DefaultTransition");
			trans.appendChild(t);
			t.appendChild(createElementWithText(d, "Weight", defaultTrans.getWeight()+""));
			t.appendChild(createElementWithText(d, "Destination", defaultTrans.getDestinationKey().toString()));			
		}
		for(Iterator i = node.getTransitions().iterator();i.hasNext();){
			Transition tran = (Transition) i.next();
			if (tran.equals(defaultTrans)) continue;
			Element t = d.createElement("Transition");
			trans.appendChild(t);
			t.appendChild(createElementWithText(d, "Key", tran.getEntryStatus().toString()));
			t.appendChild(createElementWithText(d, "Weight", tran.getWeight()+""));
			t.appendChild(createElementWithText(d, "Destination", tran.getDestinationKey().toString()));
			t.appendChild(createElementWithText(d, "Name", tran.getName().toString()));
		}
		return action;
	}

	/**
	 * @param d
	 * @param string
	 * @param i
	 * @return
	 */
	private static Node createElementWithText(Document d, String name, String text) {
		Element e = d.createElement(name);
		e.appendChild(d.createTextNode(text));
		return e;
	}

}
