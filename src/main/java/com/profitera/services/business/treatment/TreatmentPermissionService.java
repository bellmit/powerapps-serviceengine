package com.profitera.services.business.treatment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.profitera.deployment.rmi.TreatmentPermissionServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.BusinessService;
import com.profitera.util.io.ForEachLine;
import com.profitera.util.xml.DocumentLoader;
import com.profitera.util.xml.XMLConfigUtil;

/**
 * @author cmlow
 * 
 */
public class TreatmentPermissionService extends BusinessService implements TreatmentPermissionServiceIntf {
  private static final String TREATMENT_PERMISSIONS_CONFIG_FILE_PATH = "treatmentpermissionservice.treatmentpermissionsfilepath";
  private static final String DEFAULT_TREATMENT_PERMISSIONS_CONFIG_FILE_NAME = "TreatmentWorkpadPermissions.xml";
  private HashMap<String, Object> permissions;
  private final Log log = LogFactory.getLog(TreatmentPermissionService.class);
  private long lastModified;

  public TransferObject getTreatmentPermissions() {
    String path = ServiceEngine.getProp(TREATMENT_PERMISSIONS_CONFIG_FILE_PATH,
        DEFAULT_TREATMENT_PERMISSIONS_CONFIG_FILE_NAME);
    try {
      // check for modification, reload if any
      File f = new File(path);
      if (f.lastModified() > lastModified) {
        lastModified = f.lastModified();
        permissions = null;
        log.debug("File '" + path + "' has been modified. Treatment permissions configuration will be reloaded.");
      }
    } catch (Exception e) {
      log.debug("Error occurred while checking the file '" + path + "' for last modification", e);
      return new TransferObject(TransferObject.EXCEPTION,
          "Error occurred while checking the file for last modification");
    }
    return getPermissionsMap();
  }

  private TransferObject getPermissionsMap() {
    if (permissions == null) {
      permissions = new HashMap<String, Object>();
      try {
        String path = ServiceEngine.getProp(TREATMENT_PERMISSIONS_CONFIG_FILE_PATH,
            DEFAULT_TREATMENT_PERMISSIONS_CONFIG_FILE_NAME);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuffer buffer = new StringBuffer();
        try {
          new ForEachLine(r) {
            protected void process(String line) {
              buffer.append(line + "\n");
            }
          }.process();
          Document d = DocumentLoader.parseDocument(buffer.toString());
          Element root = d.getDocumentElement();
          NodeList nl = root.getChildNodes();
          Map<Properties, Double> accounts = new HashMap<Properties, Double>();
          Map<Long, Double> processes = new HashMap<Long, Double>();
          for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("process")) {
              Element q = (Element) n;
              String typeId = q.getAttribute("type");
              String authorizationId = q.getAttribute("authorization");
              try {
                Long typeIdNum = null;
                Double authorizationIdNum = null;
                try {
                  typeIdNum = new Long(typeId);
                } catch (NumberFormatException e) {
                  throw new RuntimeException("Invalid process type id ('" + typeId
                      + "') supplied for treatment permissions");
                }
                try {
                  authorizationIdNum = new Double(authorizationId);
                } catch (NumberFormatException e) {
                  throw new RuntimeException("Invalid authorization id ('" + authorizationId
                      + "') supplied for treatment permissions");
                }
                processes.put(typeIdNum, authorizationIdNum);
              } catch (RuntimeException rte) {
                log.error(rte.getMessage());
                return new TransferObject(TransferObject.EXCEPTION, rte.getMessage());
              }
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("account")) {
              Element q = (Element) n;
              String authorizationId = q.getAttribute("authorization");
              Properties p = XMLConfigUtil.getProperties(q);
              try {
                accounts.put(p, new Double(authorizationId));
              } catch (NumberFormatException e) {
                log.error("Invalid authorization id ('" + authorizationId + "') supplied for treatment permissions");
                return new TransferObject(TransferObject.EXCEPTION, "Invalid authorization id ('" + authorizationId
                    + "') supplied for treatment permissions");
              }
            }
          }
          permissions.put("process", processes);
          permissions.put("account", accounts);
        } catch (Exception e) {
          log.fatal("Failed to load treatment permissions", e);
          throw new MissingResourceException("File '" + path
              + "' not found, was empty or malformed, refusing to proceed with missing configuration information.",
              this.getClass().getName(), TREATMENT_PERMISSIONS_CONFIG_FILE_PATH);
        }
      } catch (Throwable t) {
        log.error("Failed to load treatment permissions", t);
        return new TransferObject(TransferObject.EXCEPTION, "Failed to load treatment permissions.");
      }
    }
    return new TransferObject(permissions);
  }
}
