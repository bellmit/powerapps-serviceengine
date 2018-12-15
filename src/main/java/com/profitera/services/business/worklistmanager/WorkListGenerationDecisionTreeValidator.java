package com.profitera.services.business.worklistmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.profitera.descriptor.business.meta.IDecisionTreeNode;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.descriptor.business.meta.IWorkList;
import com.profitera.rpm.expression.Expression;
import com.profitera.util.ArrayUtil;
import com.profitera.util.MapListUtil;


/**
 * @param users users
 * @param workLists Work Lists
 * @param workListGenerationOptions Work List Generation Options
 */
public class WorkListGenerationDecisionTreeValidator {

	public static final String CONFLICT = "CONFLICT";
  public static final String REASON = "REASON";
  public static final String NOT_FOUND = "NOT_FOUND";
  public static final String NOT_MATCH = "NOT_MATCH";
	
	private final List availableUsers;
	private final List availableWorkListGenerationOptions;
	private final List availableWorkLists;
	
	public WorkListGenerationDecisionTreeValidator(List users, List workLists, List workListGenerationOptions){
		availableUsers = users;
		availableWorkLists = workLists;
		availableWorkListGenerationOptions = workListGenerationOptions;
	}
	
	public Map checkConflict(Map root){
		if(root == null) return root;
		for(Iterator i = root.entrySet().iterator();i.hasNext();){
			Map.Entry e = (Map.Entry)i.next();
			String key = e.getKey().toString();
			Object value = e.getValue();
			if(value!=null){
				if(value instanceof List){
					if(key.toString().equals(WorkListGenerationDecisionTreeExtractor.USER_LIST)){
						List userList = new ArrayList((List)value);
						userList = checkUser(userList);
						root.put(e.getKey(), userList);
					}
					if(key.toString().equals(WorkListGenerationDecisionTreeExtractor.WORK_LIST_LIST)){
						List workListList = new ArrayList((List)value);
						workListList = checkWorkList(workListList);
						root.put(e.getKey(), workListList);
					}
					if(key.toString().equals(WorkListGenerationDecisionTreeExtractor.CHILDREN)){
						List children = new ArrayList((List)value);
						children = checkChildren(children);
						root.put(e.getKey(), children);
					}
				}
			}else{
				
			}
		}
		return root;
	}
	
	private List checkUser(List users){
		for(int x=0;x<users.size();x++){	
			Map newUser = (Map)users.get(x);
			if(newUser.containsKey(CONFLICT)) newUser.remove(CONFLICT);
			String id = newUser.get(IUser.USER_ID).toString(); 
      String newName = newUser.get(IUser.NAME).toString();
			boolean idFound = checkId(id, getAllUserId());
			Map conflict = new HashMap();
			if(!idFound){
        conflict.put(REASON, NOT_FOUND);
			}else{
				boolean nameMatch = checkUserName(newName, id, availableUsers);
				if(!nameMatch){
          conflict.put(REASON, NOT_MATCH);
				}
			}
			if(conflict.size()>0){
        conflict.put(IUser.USER_ID, id);
        conflict.put(IUser.NAME, newName);
				newUser.put(CONFLICT, conflict);
			}
		}
		return users;
	}

	private List getAllUserId(){
		List idList = new ArrayList();
		for(Iterator i = availableUsers.iterator();i.hasNext();){
			Map user = (Map)i.next();
			String id = user.get(IUser.USER_ID).toString();
			idList.add(id);
		}
		return idList;
	}
	
	private boolean checkId(String newId, List idList) {
		for(Iterator i = idList.iterator();i.hasNext();){
			String id = i.next().toString();
			if(id.equals(newId)){
				return true;
			}
		}
		return false;
	}
		
	private boolean checkUserName(String newName, String id, List users){
		for(Iterator i = users.iterator();i.hasNext();){
			Map m = (Map)i.next();
			if(m.get(IUser.USER_ID).toString().equals(id)){
				String name = m.get(IUser.NAME).toString();
				if(name.equals(newName)) return true;
			}
		}
		return false;
	}
	
	private List checkWorkList(List workLists){
		for(int x=0;x<workLists.size();x++){	
			Map workList = (Map)workLists.get(x);
			if(workList.containsKey(CONFLICT)) workList.remove(CONFLICT);
			String id = workList.get(IWorkList.WORK_LIST_ID).toString(); 
      String wlName = workList.get(IWorkList.NAME).toString();
			boolean idFound = checkId(id, getAllWorkListId());
			Map conflict = new HashMap();
			if(!idFound){
				conflict.put(REASON,NOT_FOUND);
			}else{
				boolean matchName = checkWorkListName(wlName, id, availableWorkLists);
				if(!matchName){
          conflict.put(REASON,NOT_MATCH);
				}
			}
			if(conflict.size()>0){
        conflict.put(IWorkList.WORK_LIST_ID, id);
        conflict.put(IWorkList.NAME, wlName);
				workList.put(CONFLICT, conflict);
			}
			if(workList.get(WorkListGenerationDecisionTreeExtractor.USER_LIST) != null){
				List userList = (List)workList.get(WorkListGenerationDecisionTreeExtractor.USER_LIST);
				userList = checkUser(userList);
				workList.put(WorkListGenerationDecisionTreeExtractor.USER_LIST, userList);
			}
			
		}
		return workLists;
	}

	private List getAllWorkListId(){
		List idList = new ArrayList();
		for(Iterator i = availableWorkLists.iterator();i.hasNext();){
			Map workList = (Map)i.next();
			String id = workList.get(IWorkList.WORK_LIST_ID).toString();
			idList.add(id);
		}
		return idList;
	}
	
	private boolean checkWorkListName(String newName, String id, List workLists){
		for(Iterator i = workLists.iterator();i.hasNext();){
			Map m = (Map)i.next();
			if(m.get(IWorkList.WORK_LIST_ID).toString().equals(id)){
				String name = m.get(IWorkList.NAME).toString();
				if(name.equals(newName)) return true;
			}
		}
		return false;
	}
	
	private List checkChildren(List children){
		List checkedChildren = new ArrayList();
		for(Iterator i = children.iterator();i.hasNext();){
			Map child = (Map)i.next();
			if(child.get(WorkListGenerationDecisionTreeExtractor.CHILDREN) != null){
				List grandChildren = (List)child.get(WorkListGenerationDecisionTreeExtractor.CHILDREN);
				grandChildren = checkChildren(grandChildren);
				child.put(WorkListGenerationDecisionTreeExtractor.CHILDREN, grandChildren);
			}
			if(child.get(WorkListGenerationDecisionTreeExtractor.WORK_LIST_LIST) != null){
				List workLists = (List)child.get(WorkListGenerationDecisionTreeExtractor.WORK_LIST_LIST);
				workLists = checkWorkList(workLists);
				child.put(WorkListGenerationDecisionTreeExtractor.WORK_LIST_LIST, workLists);
			}
			child = checkCondition(child);
			checkedChildren.add(child);
		}
		return checkedChildren;
	}

	private Map checkCondition(Map child) {
		if(child.containsKey(CONFLICT)) child.remove(CONFLICT);
		Map conflict = new HashMap();
		if(child.get(IDecisionTreeNode.COLUMN_NAME)==null && child.get(IDecisionTreeNode.COLUMN_VALUE)==null) 
			return child;  // is an "ELSE" condition
		String columnName = child.get(IDecisionTreeNode.COLUMN_NAME).toString();
		String columnValue = child.get(IDecisionTreeNode.COLUMN_VALUE).toString();
		String compareOperator = child.get(IDecisionTreeNode.COMPARE_OPERATOR).toString();
		int valid = checkColumnName(columnName);
		// if valid == -1, means column name not found 
		if(valid==-1){
			conflict.put(IDecisionTreeNode.COLUMN_NAME, columnName);
			conflict.put(IDecisionTreeNode.COLUMN_NAME_DESCRIPTION, child.get(IDecisionTreeNode.COLUMN_NAME_DESCRIPTION));
		}else{
			List matchOption = (List)availableWorkListGenerationOptions.get(valid);
			if(isDiscrete((Map)matchOption.get(0))){
				if(Integer.parseInt(compareOperator)!=Expression.EQUAL){
					child.put(IDecisionTreeNode.COMPARE_OPERATOR, new Long(Expression.EQUAL));
				} // Auto correct it to equal if it is something else, but should never happen
				if(!isValidColumnValue(columnValue, matchOption)){
					conflict.put(IDecisionTreeNode.COLUMN_VALUE, columnValue);
					conflict.put(IDecisionTreeNode.COLUMN_VALUE_NAME, child.get(IDecisionTreeNode.COLUMN_VALUE_NAME));
				}
			}else{
				int compareOperatorENUM = Integer.parseInt(compareOperator);
				if(!isValidCompareOperator(compareOperatorENUM)){			
					conflict.put(IDecisionTreeNode.COMPARE_OPERATOR, compareOperator);	
				}
			}
		}		
		if(conflict.size()>0){
      conflict.put(REASON, NOT_FOUND);
			child.put(CONFLICT, conflict);
		}
		return child;
	}
	
	private int checkColumnName(String name){
		for(int i=0;i<availableWorkListGenerationOptions.size();i++){
			List options = (List)availableWorkListGenerationOptions.get(i);
			if(options.size()!=0){
				Map option = (Map)options.get(0);
				if(option.get(IDecisionTreeNode.COLUMN_NAME)!= null){
					String columnName = option.get(IDecisionTreeNode.COLUMN_NAME).toString();
					if(name.equals(columnName)) return i;
				}
			}
		}
		return -1;
	}
	
	private boolean isDiscrete(Map optionMap){
	    return Boolean.TRUE.equals(optionMap.get(IDecisionTreeNode.IS_DISCRETE));   
	}
	
	public static boolean isValidCompareOperator(int operator){
		int[] operators = new int[]{
				Expression.EQUAL,
				Expression.LESS_THAN,
				Expression.GREATER_THAN
		};
		for(int i=0;i<operators.length;i++){
			if(operator == operators[i]) return true;
		}
		return false;
	}

	private boolean isValidColumnValue(String value, List options){
		Set validColumnValues = (Set)getValidColumnValue(options);
		if(validColumnValues.contains(value)) return true;
		return false;
	}
	
	private Set getValidColumnValue(List options){
		Set validValues = new HashSet();
		for(int i=0;i<options.size();i++){
			Map option = (Map)options.get(i);
			if(option.get(IDecisionTreeNode.COLUMN_VALUE)!=null){
				validValues.add(option.get(IDecisionTreeNode.COLUMN_VALUE).toString());
			}
		}
		return validValues;
	}
}
