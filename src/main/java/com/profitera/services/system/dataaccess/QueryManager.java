package com.profitera.services.system.dataaccess;

import com.profitera.persistence.SessionManager;
import com.profitera.services.system.SystemService;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;
import oracle.toplink.queryframework.ReportQuery;
import oracle.toplink.queryframework.ReportQueryResult;
import oracle.toplink.sessions.UnitOfWork;

public class QueryManager extends SystemService {
    protected static final Double defaultvalue = new Double(0); // not disabled, default sort priority
    public final static Double DISABLED = new Double(1); // disabled
    public static final Character DELETED = new Character('D');

    public Vector get(Expression exp, Class cls) {
        return ((Vector) SessionManager.getClientSession().executeQuery(new ReadAllQuery(cls, exp)));
    }

    public Object getObject(Expression exp, Class cls) {
        return SessionManager.getClientSession().executeQuery(new ReadObjectQuery(cls, exp));
    }

    public Vector get(ReadAllQuery readAllQuery) {
        return (Vector) SessionManager.getClientSession().executeQuery(readAllQuery);
    }

    /**
     * DO NOT USE THIS METHOD TO RETRIEVE OBJECTS FOR UPDATING,
     * THE OBJECTS RETRIEVED WILL NOT NECCESSARILY BE PART OF
     * THE SAME SESSION. IT WON'T FUCKING WORK.
     * You will get this exception:<pre>
     * LOCAL EXCEPTION STACK:
     * EXCEPTION [TOPLINK-6004] (TopLink - 9.0.3 (Build 423)): oracle.toplink.exceptions.QueryException
     * EXCEPTION DESCRIPTION: The object [com.profitera.descriptor.db.reference.TreatmentStreamRef@15b1773], of class [class com.profitera.descriptor.db.reference.TreatmentStreamRef], with identity hashcode (System.identityHashCode()) [22,746,995],
     * is not from this UnitOfWork object space, but the parent session's.  The object was never registered in this UnitOfWork,
     * but read from the parent session and related to an object registered in the UnitOfWork.  Ensure that you are correctly
     * registering your objects.  If you are still having problems, you can use the UnitOfWork.validateObjectSpace() method to
     * help debug where the error occurred.  For more information, see the manual or FAQ.
     * at oracle.toplink.exceptions.QueryException.backupCloneIsOriginalFromParent(Unknown Source)
     * .. And so on...
     * </pre>
     *
     * @param readQuery
     * @return
     */
    public Object getObject(ReadObjectQuery readQuery) {
        return SessionManager.getClientSession().executeQuery(readQuery);
    }

    public Vector getAll(Class cls) {
        return SessionManager.getClientSession().readAllObjects(cls);
    }

    public Object getObject(Class cls) {
        return SessionManager.getClientSession().readObject(cls);
    }

    /*
     * @return A refreshed copy of the object
     * @param object The object to be refresh
     */
    public Object refreshObject(Object object) {
        return SessionManager.getClientSession().refreshObject(object);
    }

    public void deleteAllObjects(Collection objects) {
        UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        handle.deleteAllObjects(objects);
        handle.commit();
    }

    public Vector get(ReportQuery query) {
        return (Vector) SessionManager.getClientSession().executeQuery(query);
    }

    /**
     * This method can be used to compute mim, sum, count etc...
     * see Performance Manager's getAttemptedCalls() method for usage of this method.
     */
    public ReportQueryResult getReport(ReportQuery query) {
        Iterator i = ((Vector) SessionManager.getClientSession().executeQuery(query)).iterator();
        return (ReportQueryResult) i.next();
    }

    /**
     * This method is used to return a specific attribute of an object
     * see SearchService getTeamIds() for the usage of this method.
     */
    public Iterator getAllReports(ReportQuery query) {
        return ((Vector) SessionManager.getClientSession().executeQuery(query)).iterator();
    }

    public long getMaximum(Class clazz, String requiredAttribute, Expression exp) {
        ReportQuery query = new ReportQuery(clazz, exp);
        query.addMaximum("MAXVAL", new ExpressionBuilder().get(requiredAttribute));
        Vector report = (Vector) SessionManager.getClientSession().executeQuery(query);
        Iterator i = report.iterator();
        ReportQueryResult result = (ReportQueryResult) i.next();
        Double max = (Double) result.get("MAXVAL");
        return (max == null ? 0 : max.longValue());
    }

}