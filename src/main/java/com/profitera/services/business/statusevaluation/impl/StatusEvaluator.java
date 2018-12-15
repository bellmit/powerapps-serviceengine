package com.profitera.services.business.statusevaluation.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.decisiontree.BucketSet;
import com.profitera.decisiontree.BucketSetAttachedDataMarshaller;
import com.profitera.decisiontree.Evaluator;
import com.profitera.decisiontree.IDecisionNode;
import com.profitera.decisiontree.IEvaluationListener;
import com.profitera.decisiontree.TreeMarshaller;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class StatusEvaluator {

  public static final IEvaluationListener NOOP_LISTENER = new IEvaluationListener(){
    public void nodeEvaluated(boolean value, Object nodeData, Map data,
        IDecisionNode n) {}};
  private final IDecisionNode tree;
  private final String statusField;
  private final String statusDateField;
  private final String updateQueryName;
  private final String insertQueryName;
  private boolean readOnly;

  public StatusEvaluator(String treeXML, String statusField, String statusDateField){
    this(treeXML, statusField, statusDateField, null, null);
  }
  public StatusEvaluator(String treeXML, String statusField, String statusDateField, String insert, String update){
    this.statusField = statusField;
    this.statusDateField = statusDateField;
    insertQueryName = insert;
    updateQueryName = update;
    TreeMarshaller m = new TreeMarshaller();
    tree = m.marshallTree(treeXML, new BucketSetAttachedDataMarshaller());
    readOnly = insert == null && update == null;
  }
  
  public IRunnableTransaction evaluate(final Map data, Date effectiveDate, IEvaluationListener l, final IReadWriteDataProvider p) {
    Evaluator evaluator = new Evaluator(tree);
    if (l == null){
      l = NOOP_LISTENER;
    }
    IDecisionNode node = evaluator.evaluate(data, l);
    BucketSet attached = (BucketSet) (node != null ? node.getAttachedData() : null);
    // We have a result of some kind, we might have to do something
    if (attached != null) {
      Object newValue = attached.allocate(BigDecimal.ONE);
      if (newValue != null){
        Long currentValue = (Long) data.get(statusField);
        if (currentValue == null || !currentValue.equals(newValue)){
          data.put(statusField, newValue);
          data.put(statusDateField, effectiveDate);
          if (!readOnly){
          return new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException,
                AbortTransactionException {
              int rows = p.update(updateQueryName, data, t);
              if (rows == 0){
                p.insert(insertQueryName, data, t);
              }
            }};
          }
        }
      }
    }
    return null;
  }

}
