/*
 * Created on Sep 3, 2003
 */
package com.profitera.services.system.dataaccess;

import com.profitera.descriptor.db.reference.NotifierCodeRef;
import com.profitera.descriptor.db.reference.TreatprocSubtypeRef;
import com.profitera.descriptor.db.treatment.Template;
import com.profitera.descriptor.db.treatment.TreatprocTemplate;
import com.profitera.persistence.SessionManager;
import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Vector;

/**
 * @author jamison
 */
public class TreatmentAdminDataManager {
    private final static Log log = LogFactory.getLog(TreatmentAdminDataManager.class);

    public TreatmentAdminDataManager() {
        super();
    }

    public static Session getSession() {
        return SessionManager.getClientSession();
    }

    public static TreatprocSubtypeRef getSubtype(Double id, Session session) {
        ExpressionBuilder eb = new ExpressionBuilder();
        Expression exp = eb.get("treatprocSubtypeId").equal(id);
        return (TreatprocSubtypeRef) session.executeQuery(new ReadObjectQuery(TreatprocSubtypeRef.class, exp));
    }

    public static TreatprocTemplate getTemplateForSubtype(Double subtypeId, Session session) {
        ExpressionBuilder eb = new ExpressionBuilder();
        Expression exp = eb.get(TreatprocTemplate.TREATPROC_SUBTYPE_ID).equal(subtypeId);
        ReadObjectQuery query = new ReadObjectQuery(TreatprocTemplate.class, exp);
		return (TreatprocTemplate) session.executeQuery(query);
    }

    public static Vector getAllSubtypes(Session session) {
        return (Vector) session.executeQuery(new ReadAllQuery(TreatprocSubtypeRef.class));
    }
    

	/**
	 * @param typeId
	 * @param session
	 * @return
	 */
	public static Vector getSubtypes(Double typeId, Session session) {
		ExpressionBuilder eb = new ExpressionBuilder();
		Expression exp = eb.get(TreatprocSubtypeRef.TREATPROC_TYPE_ID).equal(typeId);
		return (Vector) session.executeQuery(new ReadAllQuery(TreatprocSubtypeRef.class, exp));
	}

    public static TreatprocSubtypeRef updateSubtype(TreatprocSubtypeRef updatedSubtypeRef,
                                                    TreatprocTemplate updatedTemplate, Session session) {
        boolean isNew = false;
        TreatprocSubtypeRef subTypeRef;
        if (updatedSubtypeRef.getTreatprocSubtypeId() == null) {
            isNew = true;
            subTypeRef = new TreatprocSubtypeRef();
            log.debug("Creating new treatment subtype and template");
        } else {
            subTypeRef = TreatmentAdminDataManager.getSubtype(updatedSubtypeRef.getTreatprocSubtypeId(), session);
            log.debug(subTypeRef.getTreatprocSubtypeId() + ": updating subtype and template");
        }
        // Here we need to get the Process Template for this subtype, if it exists
        TreatprocTemplate template = null;
        if (!isNew) {
            template = TreatmentAdminDataManager.getTemplateForSubtype(subTypeRef.getTreatprocSubtypeId(), session);
        }
        // We might be creating new or there might be no existing template
        if (template == null) {
            template = new TreatprocTemplate(); // TODO: Set the subtype ID later b/c of mapping problem
            if (!isNew) {
                log.error(subTypeRef.getTreatprocSubtypeId() + ": had no template, will fix in this transaction.");
            }
        } else {
            log.debug(template.getTreatprocSubtypeId() + ": has an existing template ID(" + template.getTreatprocTemplateId() + ")");
        }
        UnitOfWork handle = session.acquireUnitOfWork();
        TreatprocSubtypeRef subTypeRefWCopy = (TreatprocSubtypeRef) handle.registerObject(subTypeRef);
        subTypeRefWCopy.setTreatprocTypeCode(updatedSubtypeRef.getTreatprocTypeCode());
        subTypeRefWCopy.setTreatprocTypeDesc(updatedSubtypeRef.getTreatprocTypeDesc());
        subTypeRefWCopy.setTreatprocTypeId(updatedSubtypeRef.getTreatprocTypeId());
        subTypeRefWCopy.setDisable(updatedSubtypeRef.getDisable());
        subTypeRefWCopy.setSortPriority(updatedSubtypeRef.getSortPriority());
        if (isNew) {
            handle.assignSequenceNumber(subTypeRefWCopy);
        }
        TreatprocTemplate templateWCopy;
        if (template.getTreatprocTemplateId() == null) {
            log.debug("This subtype has no template, adding a new template.");
            templateWCopy = (TreatprocTemplate) handle.registerNewObject(template);
            handle.assignSequenceNumber(templateWCopy);
        } else {
            templateWCopy = (TreatprocTemplate) handle.registerExistingObject(template);
        }
        templateWCopy.setDaysDuration(updatedTemplate.getDaysDuration());
        templateWCopy.setDocumentTemplate(
            (Template) handle.registerExistingObject(updatedTemplate.getDocumentTemplate()));
        templateWCopy.setNotifierProcess(updatedTemplate.getNotifierProcess());
        templateWCopy.setTreatprocSubtypeId(subTypeRefWCopy.getTreatprocSubtypeId());
        templateWCopy.setTreatprocTypeId(subTypeRefWCopy.getTreatprocTypeId());
        templateWCopy.setLeadTimeHours(updatedTemplate.getLeadTimeHours());
        templateWCopy.setCost(updatedTemplate.getCost());
        templateWCopy.setUpdateHost(updatedTemplate.getUpdateHost());
        if (updatedTemplate.getNotifierCodeRef() != null){
          templateWCopy.setNotifierProcess(new Boolean(true));
          NotifierCodeRef r = new NotifierCodeRef();
		  r.setId(updatedTemplate.getNotifierCodeRef().getId());
		  templateWCopy.setNotifierCodeRef((NotifierCodeRef) handle.registerExistingObject(r));
        } else {
          templateWCopy.setNotifierProcess(new Boolean(false));
          templateWCopy.setNotifierCodeRef(null);
        }
        handle.commit();
        session.refreshObject(templateWCopy);
        return subTypeRef;
    }

}
