package com.profitera.services.business.rule;

import com.profitera.deployment.rmi.RuleBomServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.ra.RuleBomBean;
import com.profitera.descriptor.db.rule.RuleBom;
import com.profitera.persistence.SessionManager;
import com.profitera.services.business.BusinessService;
import com.profitera.util.MapCar;
import com.profitera.util.TopLinkQuery;
import java.sql.Timestamp;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

public class RuleBomService extends BusinessService implements RuleBomServiceIntf {
	private final String NO_SUCH_DICTIONARY = "NO_SUCH_DICTIONARY";
	private static final MapCar RULE_BOM_BEAN_MAPCAR = new MapCar(){
		public Object map(Object o) {
			return buildRuleBomBean((RuleBom) o);
		}
    };

    /*
     * @return A TransferObject which holds all business dictionary (RuleBomBean) objects.
     */
    public TransferObject getRuleDictionaries() {
        ReadAllQuery query = new ReadAllQuery(RuleBom.class);
        query.addAscendingOrdering(RuleBom.BOM_NAME);
        query.setName("Get Rule Dictionaries");
        return new TransferObject(MapCar.map(RULE_BOM_BEAN_MAPCAR,TopLinkQuery.asList(query, SessionManager.getClientSession())));        
    }

    /*
     * Builds the RuleBomBean object
     */
    private static RuleBomBean buildRuleBomBean(RuleBom ruleBom) {
    	RuleBomBean bean = new RuleBomBean();
        bean.setAgentCode(ruleBom.getAgentCode());
        bean.setBomId(ruleBom.getBomId());
        bean.setBomName(ruleBom.getBomName());
        bean.setContent(ruleBom.getContent());
        bean.setCreatedBy(ruleBom.getCreatedBy());
        bean.setCreatedDate(ruleBom.getCreatedDate());
        bean.setDescription(ruleBom.getDescription());
        bean.setUpdatedBy(ruleBom.getUpdatedBy());
        bean.setUpdatedDate(ruleBom.getUpdatedDate());
        return bean;
    }

    /*
     * Updates a RuleBom object by calling RuleBomQueryManager.update(..)
     * @return The newly update RuleBom object
     * @param ruleBom The RuleBom object to be updated
     * @param name The new name of the RuleBom
     * @param description The new description of the RuleBom
     * @param userId The user id that updates the RuleBom
     */
    public TransferObject update(Long ruleBomId, String name, String description, String userId) {
        RuleBom ruleBom = getRuleBom(ruleBomId, SessionManager.getClientSession());
        update(ruleBom, name, description, userId);
        return getRuleBomById(ruleBomId);
    }
    
    public RuleBom getRuleBom(Long ruleBomId, Session s) {
        ReadObjectQuery query = new ReadObjectQuery(RuleBom.class, new ExpressionBuilder().get(RuleBom.BOM_ID).equal(ruleBomId));
        return (RuleBom) s.executeQuery(query);
    }

    
    private void update(RuleBom ruleBom, String name, String description, String userId) {
        UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        RuleBom wcRuleBom = (RuleBom) handle.registerObject(ruleBom);
        wcRuleBom.setBomName(name);
        wcRuleBom.setDescription(description);
        wcRuleBom.setUpdatedBy(userId);
        wcRuleBom.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
        handle.commit();
    }

    /*
     * @return A TransferObject that holds a RuleBom object with a specific bom id
     * @param bomId The RuleBom id to be retrieved
     */
    public TransferObject getRuleBomById(Long bomId) {
        return new TransferObject(buildRuleBomBean(getRuleBom(bomId, SessionManager.getClientSession())));
    }

    /*
     * Writes the XML source content of a rule bom
     * @return A TransferObject that holds the status of the update
     * @param ruleBomId The rule bom id
     * @param content The XML source content
     */
    public TransferObject writeBomContent(Long ruleBomId, String content) {
    	UnitOfWork uow = SessionManager.getClientSession().acquireUnitOfWork();
    	RuleBom b = (RuleBom) uow.executeQuery(new ReadObjectQuery(RuleBom.class, new ExpressionBuilder().get(RuleBom.BOM_ID).equal(ruleBomId)));
    	if (b == null) return new TransferObject(TransferObject.ERROR, NO_SUCH_DICTIONARY);
    	b.setContent(content);
    	uow.commit();
    	return getRuleBomById(ruleBomId);
    }
}