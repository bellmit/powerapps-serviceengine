package com.profitera.services.business.http.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPathExpressionException;

import com.profitera.docs.GenerateDocumentation;
import com.profitera.webclient.form.WebComponentRegistry;

public class DocumentationStreamer {
  public void renderDocumentation(HttpServletRequest req,
      HttpServletResponse resp) throws IOException {
    resp.addHeader("Content-Type", "text/html");
    try {
      if (req.getServletPath().equals("/docs")) {
        XMLStreamWriter writer = new WebComponentRegistry()
            .renderDocumentationStart("Documentation Index", "/yui",
                resp.getOutputStream());
        writer.writeStartElement("h1");
        writer.writeCharacters("Profitera PowerApps Reference Documentation");
        writer.writeEndElement();
        writer.writeStartElement("h2");
        writer.writeStartElement("a");
        writer.writeAttribute("href", "docs/webcomponents");
        writer.writeCharacters("Web Component Reference");
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeStartElement("h2");
        writer.writeStartElement("a");
        writer.writeAttribute("href", "docs/serveractions");
        writer.writeCharacters("Server Action Reference");
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();// body
        writer.writeEndElement();// html
      } else if (req.getServletPath().startsWith("/docs/webcomponents")) {
        new WebComponentRegistry().renderDocumentation("/yui",
            resp.getOutputStream());
      } else if (req.getServletPath().startsWith("/docs/serveractions")) {
        GenerateDocumentation.writeEventActionDocumentation(resp
            .getOutputStream());
      }
    } catch (XMLStreamException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (XPathExpressionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
