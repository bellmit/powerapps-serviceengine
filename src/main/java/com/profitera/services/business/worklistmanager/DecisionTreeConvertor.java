package com.profitera.services.business.worklistmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.profitera.decisiontree.BucketSet;
import com.profitera.decisiontree.BucketSetAttachedDataMarshaller;
import com.profitera.decisiontree.DecisionNodeFactory;
import com.profitera.decisiontree.Expression;
import com.profitera.decisiontree.ExpressionFactory;
import com.profitera.decisiontree.IDecisionNode;
import com.profitera.decisiontree.TreeMarshaller;
import com.profitera.decisiontree.BucketSet.Bucket;
import com.profitera.decisiontree.BucketSet.Distribution;
import com.profitera.util.io.FileUtil;

public class DecisionTreeConvertor {
	
	private static DecisionNodeFactory dFactory = new DecisionNodeFactory();
	private static ExpressionFactory eFactory = new ExpressionFactory();
	
	private static boolean isEqual(Map m){
		if(isElse(m)) return false;
		Long ops = (Long)m.get(WorkListGenerationDecisionTreeExtractor.COMPARE_OPERATOR);
		if(ops==null) return false;
		return ops == com.profitera.rpm.expression.Expression.EQUAL;
	}
	
	private static boolean isGreater(Map m){
		Long ops = (Long)m.get(WorkListGenerationDecisionTreeExtractor.COMPARE_OPERATOR);
		if(ops==null) return false;
		return ops == com.profitera.rpm.expression.Expression.GREATER_THAN;
	}
	
	private static boolean isLesser(Map m){
		Long ops = (Long)m.get(WorkListGenerationDecisionTreeExtractor.COMPARE_OPERATOR);
		if(ops==null) return false;
		return ops == com.profitera.rpm.expression.Expression.LESS_THAN;
	}
	
	private static boolean isElse(Map m){
		return getColumnName(m)==null;
	}
	
	private static Long getColumnValue(Map m){
		return (Long)m.get(WorkListGenerationDecisionTreeExtractor.COLUMN_VALUE);
	}

	private static String getColumnName(Map m) {
		return (String)m.get(WorkListGenerationDecisionTreeExtractor.COLUMN_NAME);
	}
	
	private static BucketSet getWorkList(Map m){
		List l = DecisionTreeUtil.getNodeWorkLists(m);
		List distibutions = new ArrayList();
		for(int i=0;i<l.size();i++){
			Map wl = (Map)l.get(i);
			Long id = (Long)wl.get("WORK_LIST_ID");
			Bucket b = new Bucket(id);
			Distribution dist = new Distribution(b, null, null);
			distibutions.add(dist);
		}
		Distribution[] d = (Distribution[])distibutions.toArray(new Distribution[0]);
		return new BucketSet(null, d); 
	}
	
	private static IDecisionNode builcTree(Map root){
		IDecisionNode node = dFactory.root();
		List children = DecisionTreeUtil.getNodeChildren(root);
		buildLeaf(node, children);
		return node;
	}
	
	private static void buildLeaf(IDecisionNode parent, List children){
		for(int i=0;i<children.size();i++){
			Map m = (Map)children.get(i);
			Expression e = null;
			if(isEqual(m)){
				e = eFactory.equals(getColumnName(m), getColumnValue(m));
			}else if(isGreater(m)){
				e = eFactory.greaterThan(getColumnName(m),  getColumnValue(m));
			}else if(isLesser(m)){
				e = eFactory.lessThan(getColumnName(m),  getColumnValue(m));
			}else if(isElse(m)){
				e = eFactory.elseExpression();
			}else{
				throw new RuntimeException("Unsupport operation : " + m);
			}
			IDecisionNode child = dFactory.build(parent, e);
			child.setAttachedData(getWorkList(m));
			List grandChildren = DecisionTreeUtil.getNodeChildren(m);
			buildLeaf(child, grandChildren);
		}
	}
	
	public static void main(String[] args){
		if(args.length==0){
			System.out.println(".wgd file required.");
			System.exit(0);
		}
		String path = args[0];
		File file = new File(path);
		System.out.println("start...");
		try {
			byte[] source = null;
			source = FileUtil.readEntireFile(file, 100000);
			Map wgd = new WorkListGenerationDecisionTreeParser().getWorkListEntitiesFromXML(source);
			IDecisionNode root = builcTree(wgd);
			String xml = new TreeMarshaller().unmarshallToXML(root, new BucketSetAttachedDataMarshaller());
			String f = file.getAbsolutePath()+".dtxml";
			FileUtil.writeFile(new File(f), xml, "UTF8");			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end...");
	}
}
