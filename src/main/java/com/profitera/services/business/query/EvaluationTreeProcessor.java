package com.profitera.services.business.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.business.statusevaluation.impl.TreeDataEvaluator;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.lookup.LookupManager;


public class EvaluationTreeProcessor extends BaseListQueryProcessor {
  private static final String TREE_FIELD = "TREE_FIELD";
  private static final String UPDATE_DATABASE = "UPDATE_DATABASE";
  private static final long CACHE_TIME = 60 * 1000;
  private Boolean updateDB = null;
  private String treeFieldName = null;
  private TreeDataEvaluator treeDataEvaluator;
  public EvaluationTreeProcessor(){
    addProperty(UPDATE_DATABASE, Boolean.class, Boolean.FALSE, 
        "Whether or not to execute updates against the database when evaluating", 
        "Then 'true' the results of the tree processing is updated against the database anychronously after a request is processed.");
    addProperty(TREE_FIELD, String.class, "TREE_ID", "Field name for tree to use to evaluate data", "Field which should contain the name of the field that will have the ID of the tree to use to evaluate the data in that row");
    addProperty("THREADS", Integer.class, new Integer(1), "Number of threads to execute updates with", "Controls the amount of concurrency in database updates, lower values will spread database load over a longer period.");
    addProperty("COMMIT_SIZE", Integer.class, new Integer(100), "Number of records to update at a time", "Controls the size of the transactions performed on the database, larger transactions require more locking but will execute faster in low-contention environments.");
  }
  protected String getDocumentation() {
    return "";
  }

  protected String getSummary() {
    return "";
  }
  
  private boolean isUpdatingDB(){
    if (updateDB == null){
      updateDB = (Boolean) getProperty(UPDATE_DATABASE);
    }
    return updateDB.booleanValue();
  }
  
  private int getThreads(){
    Integer i = (Integer) getProperty("THREADS");
    return i.intValue();
  }
  
  private int getCommitSize(){
    Integer i = (Integer) getProperty("COMMIT_SIZE");
    return i.intValue();
  }
  
  
  protected void configureProcessor() {
    super.configureProcessor();
    treeDataEvaluator = new TreeDataEvaluator(CACHE_TIME, getCommitSize(), getThreads(), getReadWriteProvider(), getLog(), isUpdatingDB());
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs)
      throws TransferObjectException {
    List newResult = new ArrayList();
    treeDataEvaluator.process(result.iterator(), getTreeIdField(), newResult);
    
    return newResult;
  }

  protected IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }
  
  private String getTreeIdField(){
    if (treeFieldName == null) {
      treeFieldName = (String) getProperty(TREE_FIELD);
    }
    return treeFieldName;
  }
  
  

}
