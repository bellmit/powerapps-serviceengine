/*
 * Created on Aug 7, 2003
 */
package com.profitera.rpm.expression;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * @author jamison
 *
 */
public class Renderer {

	public static String transform(Document document){
		//Use a Transformer for String output
		// This is straight out of the tutorial!
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StringWriter sw = new StringWriter(500);
			StreamResult result = new StreamResult(sw);
			transformer.transform(source, result);
			return sw.toString();
		} catch (TransformerException e) {
			throw new RuntimeException("Failed to perform identity transformation on XML document", e);
		}
	
	}
}
