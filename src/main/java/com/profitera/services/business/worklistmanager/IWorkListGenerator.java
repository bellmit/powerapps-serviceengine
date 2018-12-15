/**
 * 
 */
package com.profitera.services.business.worklistmanager;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public interface IWorkListGenerator {
  public void setGeneratorCode(String code);
  public void setDecisionTree(Map tree);
  public void setDate(Date date);
  public void setUseCustomerMode(boolean useCustomerMode);
  public void setTransferAtCustomerMode(boolean transferAtCustomerMode);
  public void setMultipleAccountWorklist(boolean multipleAccountWorklist);
  public void setDebugMode(boolean debugMode);
  public String getCustomerAccountQuery();
  public String getUserQuery();
  public String getWorkListQuery();
  public String getDecisionTreeOptionsQuery();
  public boolean isUseCustomerMode();
  public boolean isTransferAtCustomerMode();
  public boolean isMultipleAccountWorklist();
  public boolean isDebugMode();
  public TransferObject getWorkListGenerationDecisionTree(Long treeId, Map arguments, final IReadWriteDataProvider provider);  
  public IRunnableTransaction getBuildTree(Long treeId, Map oldRoot, final Map root, Map parameters, final IReadWriteDataProvider provider);
  public IRunnableTransaction getDeleteCurrentTree(final Map oldRoot, final IReadWriteDataProvider provider);
  public NodeRunnableTransaction process(List thisCustomerAccounts, IReadWriteDataProvider p);  
  public IRunnableTransaction getUserWorkListAssignmentTransaction(final String userId,final Object[] workListIds,final Date date, final IReadWriteDataProvider provider);
  public IRunnableTransaction processTransfer(List thisCustomerAccounts, List transferToList, Map args, IReadWriteDataProvider p);
  public IRunnableTransaction getSetAccountWorkListTransaction(final Map args, final Date assignmentDate, final IReadWriteDataProvider p);  
  public TransferObject extractWorkListGenerationDecisionTree(Map root) throws SAXException, IOException, TransformerConfigurationException;
  public TransferObject importWorkListGenerationDecisionTree(byte[] source) throws SAXException, IOException, ParserConfigurationException, Exception;
  public void setDataGroupingFields(String[] fields);
  public String[] getDataGroupingFields();
}