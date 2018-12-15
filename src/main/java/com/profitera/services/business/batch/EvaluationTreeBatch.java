package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.statusevaluation.impl.TreeDataEvaluator;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class EvaluationTreeBatch extends AbstractProviderBatch {

  private static final String QUERY = "query";
  private static final String TREE_ID_S_DOC = "Field from which to retrieve the tree's ID";

  public EvaluationTreeBatch(){
    addRequiredProperty(QUERY, String.class, "Query that retrieves relevant data", 
        "Query defined for this batch that will retrieve the data that will be processed " +
        "by the decision tree associated with that data.");
    addProperty("treeidfield", String.class, "TREE_ID", TREE_ID_S_DOC, TREE_ID_S_DOC);
    addThreadsProperty(1, this);
    addCommitSizeProperty(100, this);
  }

  protected String getBatchDocumentation() {
    return "This process executes the query associated with the batch "
    + "and then for each row in the result looks up an evaluation tree "
    + "and applies it to the data, storing the result in the database.";
  }

  protected String getBatchSummary() {
    return "Processes data based on evaluation tree associated with each row";
  }

  protected TransferObject invoke() {
    IReadWriteDataProvider p = getReadWriteProvider();
    int commitSize = getCommitSizeProperty(this);
    String query = (String) getPropertyValue(QUERY);
    TreeDataEvaluator tde = new TreeDataEvaluator(Long.MAX_VALUE, commitSize, getThreadsProperty(this), p, getLog(), true);
    Iterator i = null;
    HashMap args = new HashMap();
    args.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
    try {
      i = p.query(IReadOnlyDataProvider.STREAM_RESULTS, query, args);
    } catch (SQLException e) {
      getLog().error("Failed to execute query defined for batch " + getIdentifier(), e);
      return new TransferObject(TransferObject.ERROR, "QUERY_ERROR");
    }
    tde.process(i, getTreeField(), null);
    tde.waitForEmptyQueue();
    return new TransferObject();
  }

  private String getTreeField() {
    return (String) getPropertyValue("treeidfield");
  }

}
