package com.profitera.services.business.worklistmanager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IDecisionTreeNode;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class SubtreeWorkListGenerator extends AllAccountWorklistGenerator {  
  private static final String ROOT_DIVIDER_PROPERTY_NAME = "subtreecolumn";
  private String rootColumn;

  public TransferObject getWorkListGenerationDecisionTree(Long treeId,
      Map parameters, IReadWriteDataProvider provider) {
    Long wlId = getParameterWorkList(parameters);
    TransferObject to = super.getWorkListGenerationDecisionTree(treeId, parameters, provider);
    if (to.isFailed()) return to;
    Map fullRoot = (Map) to.getBeanHolder();
    Object rootColumnName = fullRoot.get(IDecisionTreeNode.COLUMN_NAME);
    // If the retrieval already has the root at the
    // WL there is nothing left to do
    if (rootColumnName != null 
        && rootColumnName.equals(getSubtreeDecisionColumn())){
      return to;
    }
    List nodeChildren = DecisionTreeUtil.getNodeChildren(fullRoot);
    for (Iterator i = nodeChildren.iterator(); i.hasNext();) {
      Map child = (Map) i.next();
      Long workListId = (Long) child.get(IDecisionTreeNode.COLUMN_VALUE);
      if (workListId != null && workListId.equals(wlId)){
        return new TransferObject(child);
      }
    }
    Map root = new HashMap();
    return new TransferObject(root);
  }

  private String getSubtreeDecisionColumn() {
    if (rootColumn == null){
      rootColumn = getGeneratorProperty(ROOT_DIVIDER_PROPERTY_NAME);
      if (rootColumn == null || rootColumn.equals("")){
        throw new IllegalArgumentException("Missing required generator property '" + ROOT_DIVIDER_PROPERTY_NAME + "'");
      }
    }
    return rootColumn;
  }

  public IRunnableTransaction getBuildTree(final Long treeId, final Map oldRoot,
      final Map root, final Map parameters, final IReadWriteDataProvider provider) {
    Long savingWorkListId = getParameterWorkList(parameters);
    Long rootWorkListId = (Long) oldRoot.get(IDecisionTreeNode.COLUMN_VALUE);
    if (rootWorkListId == null){
      oldRoot.put(IDecisionTreeNode.PARENT_NODE, getTreeRootNodeId(treeId, provider));
      oldRoot.put(IDecisionTreeNode.COLUMN_NAME, getSubtreeDecisionColumn());
      oldRoot.put(IDecisionTreeNode.COLUMN_VALUE, savingWorkListId);
      oldRoot.put(IDecisionTreeNode.COMPARE_OPERATOR, new Long(com.profitera.rpm.expression.Expression.EQUAL));
      return new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          Long id = (Long) provider.insert(INSERT_DECISION_TREE_NODE, oldRoot, t);
          oldRoot.put("ID", id);
          SubtreeWorkListGenerator.super.getBuildTree(treeId, oldRoot, root, parameters, provider).execute(t);
        }};
    }
    return super.getBuildTree(treeId, oldRoot, root, parameters, provider);
  }

  private Object getTreeRootNodeId(final Long treeId,
      final IReadWriteDataProvider provider) {
    Map args = new HashMap();
    args.put("TREE_ID", treeId);
    Map tree = new HashMap();
    try {
      tree = (Map) provider.queryObject(getDecisionTreeRootQuery(), args);
    } catch (SQLException e) {
      throw new RuntimeException("Error occurred retrieving root for subtree construction", e);
    }
    return tree.get("ROOT_NODE_ID");
  }

  private Long getParameterWorkList(Map parameters) {
    Long savingWorkListId = (Long) parameters.get(getSubtreeDecisionColumn());
    if (savingWorkListId == null) {
      throw new IllegalArgumentException("Parameter missing required value for " + getSubtreeDecisionColumn());
    }
    return savingWorkListId;
  }

}
