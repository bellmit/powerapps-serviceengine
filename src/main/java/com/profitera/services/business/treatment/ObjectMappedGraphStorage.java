package com.profitera.services.business.treatment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.treatment.admin.ActionNode;
import com.profitera.descriptor.business.treatment.admin.DefaultTreatmentGraph;
import com.profitera.descriptor.business.treatment.admin.Transition;
import com.profitera.descriptor.business.treatment.admin.TreatmentGraph;
import com.profitera.descriptor.business.treatment.admin.TreatmentGraphs;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.RPMDataManager;
import com.profitera.util.Filter;
import com.profitera.util.MapCar;

/**
 * 
 *
 * @author jamison
 */
public class ObjectMappedGraphStorage implements TreatmentGraphSaver, TreatmentGraphLoader {

	/**
	 * @see com.profitera.services.business.treatment.TreatmentGraphSaver#saveTreatmentGraph(com.profitera.descriptor.business.treatment.admin.DefaultTreatmentGraph, com.profitera.descriptor.db.reference.TreatmentStageRef, oracle.toplink.sessions.UnitOfWork)
	 */
	public void saveTreatmentGraph(TreatmentGraph graph, final TreatmentStageRef stage, final IReadWriteDataProvider provider, ITransaction t) {
		List oldDBNodes = getStageNodesInStage(stage,provider);
		final List oldNodes = MapCar.map(new MapCar(){
			public Object map(Object o) {
				return buildActionNode((Map)o, provider);
			}},oldDBNodes);
		Map allUpdatedNodes = TreatmentGraphs.getAllNodes(graph);
		// Old nodes not in updated nodes are for deletion 
		final List toDelete = new Filter.AbsentFilter(allUpdatedNodes.values()).filterItems(oldNodes);
		// New nodes are nodes not in the set of old nodes.
		final List toAdd = new Filter.AbsentFilter(oldNodes).filterItems(allUpdatedNodes.values());
		// Nodes that will stick around but might need changing
		List newAndtoBeDeleted = new ArrayList();
		newAndtoBeDeleted.addAll(toDelete); newAndtoBeDeleted.addAll(toAdd);
		final List toUpdate = new Filter.AbsentFilter(newAndtoBeDeleted).filterItems(allUpdatedNodes.values());
		final List allGraphNodes = new ArrayList();
		allGraphNodes.addAll(toAdd); allGraphNodes.addAll(toUpdate);  
		try {
					for(Iterator i = oldNodes.iterator(); i.hasNext();){
						ActionNode a = (ActionNode) i.next();
						provider.delete("deleteActionTransition",a.getId(),t);
					}
					for(Iterator i = toDelete.iterator(); i.hasNext();){
						ActionNode a = (ActionNode) i.next();
						provider.update("disableActionNode",a.getId(),t);
					}
					
					for(Iterator i = toAdd.iterator(); i.hasNext();){
						ActionNode a = (ActionNode) i.next();
						if (a.getId() == null)
							a.setId(a.getName() + " " + new Random(System.currentTimeMillis()).nextInt());
						
						Map args = new HashMap(10);
						args.put("ACTION_ID",a.getId());
						args.put("NAME",a.getName());
						args.put("TREATMENT_STAGE_ID",stage.getTreatmentStageId());
						args.put("TREATMENT_STREAM_ID",a.getStreamId());
						if(a.getProcessSubtype()==null||a.getProcessSubtype().intValue()==0)
							args.put("TREATPROC_SUBTYPE_ID",new Integer(RPMDataManager.TREATMENT_PROCESS_TYPE_NONE));
						else
							args.put("TREATPROC_SUBTYPE_ID",a.getProcessSubtype());
						
						args.put("X_POSITION",new Integer(a.getPosition().x));
						args.put("Y_POSITION",new Integer(a.getPosition().y));
						args.put("RETRIES",new Integer(a.getRetries()));
						args.put("MANDATORY",a.isManditory()?new Integer(1):new Integer(0));
						args.put("DELETED",new Integer(0));
						provider.insert("insertActionNode",args,t);
					}
					
					for(Iterator i = toUpdate.iterator(); i.hasNext();){
						ActionNode a = (ActionNode) i.next();
						if (a.getId() == null)
							a.setId(a.getName() + " " + new Random(System.currentTimeMillis()).nextInt());
						Map args = new HashMap(10);
						args.put("ACTION_ID",a.getId());
						args.put("NAME",a.getName());
						args.put("TREATMENT_STAGE_ID",stage.getTreatmentStageId());
						args.put("TREATMENT_STREAM_ID",a.getStreamId());
						if(a.getProcessSubtype()==null||a.getProcessSubtype().intValue()==0)
							args.put("TREATPROC_SUBTYPE_ID",new Integer(RPMDataManager.TREATMENT_PROCESS_TYPE_NONE));
						else
							args.put("TREATPROC_SUBTYPE_ID",a.getProcessSubtype());
						args.put("X_POSITION",new Integer(a.getPosition().x));
						args.put("Y_POSITION",new Integer(a.getPosition().y));
						args.put("RETRIES",new Integer(a.getRetries()));
						args.put("MANDATORY",a.isManditory()?new Integer(1):new Integer(0));
						args.put("DELETED",new Integer(0));
						provider.update("updateActionNode",args,t);
					}
					
					for (Iterator iterator = allGraphNodes.iterator(); iterator.hasNext();) {
						ActionNode actionNode = (ActionNode) iterator.next();
						for(Iterator i = actionNode.getTransitions().iterator();i.hasNext();){
							Transition transBean = (Transition) i.next();
							Map args = new HashMap(5);
							args.put("SOURCE_ACTION_ID",actionNode.getId());
							args.put("DESTINATION_ACTION_ID","STAGE".equalsIgnoreCase(transBean.getDestinationKey().toString())?null:transBean.getDestinationKey());
							args.put("NAME",transBean.getName());
							args.put("WEIGHT",new Integer(transBean.getWeight()));
							args.put("TREATPROC_STATUS_ID",transBean.getEntryStatus());
							provider.insert("insertActionTransition",args,t);
						}
					}
					
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		
	
		
	}
	/**
	 * @return <code>newNode</code> instance is actually returned, modified.
	 */
	/**
	 * @see com.profitera.services.business.treatment.TreatmentGraphLoader#loadGraph(com.profitera.descriptor.db.reference.TreatmentStageRef, oracle.toplink.sessions.Session)
	 */
	public DefaultTreatmentGraph loadGraph(Map stage, final IReadWriteDataProvider p) {
		List actionNodes = MapCar.map(new MapCar(){
			public Object map(Object o) {
				return buildActionNode((Map)o, p);
			}},getStageNodesInStage(stage,p));
		Map allNodes = new HashMap();
		for(Iterator i = actionNodes.iterator();i.hasNext();){
			ActionNode a = (ActionNode)i.next();
			allNodes.put(a.getId(), a);
		}
		Map streamStarts = TreatmentGraphs.getStreamStarts(allNodes);
		DefaultTreatmentGraph g = new DefaultTreatmentGraph(new Integer(((Number) stage.get("TREATMENT_STAGE_ID")).intValue()), streamStarts, allNodes);
		return g;
	}

	private List getStageNodesInStage(TreatmentStageRef stage,  IReadWriteDataProvider p){
		Map args = new HashMap(1);
		args.put("TREATMENT_STAGE_ID",stage.getTreatmentStageId());
		return getStageNodesInStage(args,p);
	}
	
	private List getStageNodesInStage(Map stage,  IReadWriteDataProvider p){
		try {
			Iterator i = p.query(IReadWriteDataProvider.LIST_RESULTS,"getStageNodes",stage);
			List l = new ArrayList();
			while(i.hasNext())
				l.add(i.next());
			return l;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ActionNode buildActionNode(Map node, IReadWriteDataProvider p) {
		ActionNode an = new ActionNode();
		an.setId((String) node.get("ACTION_ID"));
		an.setName((String) node.get("NAME"));
		an.setPosition(((Number) node.get("X_POSITION")).intValue(), ((Number) node.get("Y_POSITION")).intValue());
		int subtypeId = ((Number) node.get("TREATPROC_SUBTYPE_ID")).intValue();
		int typeId = ((Number) node.get("TREATPROC_TYPE_ID")).intValue();
		if (subtypeId == RPMDataManager.TREATMENT_PROCESS_TYPE_NONE){
			subtypeId = 0;
			typeId = 0; 
		}
		an.setProcessSubtype(new Integer(subtypeId));
		an.setProcessType(new Integer(typeId));
		an.setRetries(((Number) node.get("RETRIES")).intValue());
		an.setStreamId(new Integer(((Long) node.get("TREATMENT_STREAM_ID")).intValue()));
		Iterator i;
		try {
			i = p.query(IReadWriteDataProvider.LIST_RESULTS,"getTreatmentActionTransition",node);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		while(i.hasNext()){
			Map t = (Map) i.next();
			if (t.get("TREATPROC_STATUS_ID") == null)
				an.setDefaultTransition(buildTransition(t));
			else
				an.addTransition(buildTransition(t));
		}
		an.setManditory(((Boolean) node.get("MANDATORY")).booleanValue());
		return an;
	}
	
	private Transition buildTransition(Map t) {
		Transition tran = new Transition();
		if (t.get("DESTINATION_ACTION_ID") == null)
			tran.setDestinationKey(ActionNode.STAGE_KEY);
		else
			tran.setDestinationKey(t.get("DESTINATION_ACTION_ID"));
		if (t.get("TREATPROC_STATUS_ID") == null)
			tran.setEntryStatus(null);
		else
			tran.setEntryStatus(new Integer(((Long) t.get("TREATPROC_STATUS_ID")).intValue()));
		tran.setName((String) t.get("NAME"));
		tran.setWeight(((Number) t.get("WEIGHT")).intValue());
		return tran;
	}
	
	

}
