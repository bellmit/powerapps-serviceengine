package com.profitera.services.business.worklistmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DecisionTreeUtil {
  public static final List getNodeChildren(Map treeRoot) {
    List kids = (List) treeRoot.get("CHILDREN");
    return kids;
  }
  
  public static final List getNodeWorkLists(Map node) {
    List l = (List) node.get("WORK_LIST_LIST");
    if (l == null){
      l = new ArrayList();
      node.put("WORK_LIST_LIST", l);
    } 
    return l;
  }
  
  public static final List getNodeUsers(Map node) {
    List l = (List) node.get("USER_LIST");
    if (l == null){
      l = new ArrayList();
      node.put("USER_LIST", l);
    } 
    return l;
  }

}
