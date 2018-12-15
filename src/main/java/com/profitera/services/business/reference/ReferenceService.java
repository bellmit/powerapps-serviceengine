package com.profitera.services.business.reference;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.ReferenceServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.reference.PriorityRefComparator;
import com.profitera.descriptor.business.reference.ReferenceBeanConverter;
import com.profitera.descriptor.business.reference.ReferenceBusinessBean;
import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;
import com.profitera.descriptor.db.account.CustomerSegment;
import com.profitera.descriptor.db.reference.AccountSexRef;
import com.profitera.descriptor.db.reference.AccountStatusRef;
import com.profitera.descriptor.db.reference.AccountWorkListStatusRef;
import com.profitera.descriptor.db.reference.AgencyTypeRef;
import com.profitera.descriptor.db.reference.AutoPayStatusRef;
import com.profitera.descriptor.db.reference.BillingCycleRef;
import com.profitera.descriptor.db.reference.BlockCodeRef;
import com.profitera.descriptor.db.reference.ChannelCodeRef;
import com.profitera.descriptor.db.reference.ChargeOffReasonRef;
import com.profitera.descriptor.db.reference.ChargeOffStatusRef;
import com.profitera.descriptor.db.reference.CheckStatusRef;
import com.profitera.descriptor.db.reference.CitizenshipRef;
import com.profitera.descriptor.db.reference.ClarityMessagesRef;
import com.profitera.descriptor.db.reference.ClassOfServiceRef;
import com.profitera.descriptor.db.reference.ClientTypeRef;
import com.profitera.descriptor.db.reference.CollectionStatusRef;
import com.profitera.descriptor.db.reference.ContactNumberTypeRef;
import com.profitera.descriptor.db.reference.ContactTypeRef;
import com.profitera.descriptor.db.reference.CostingTypeRef;
import com.profitera.descriptor.db.reference.CostingUomRef;
import com.profitera.descriptor.db.reference.CountryRef;
import com.profitera.descriptor.db.reference.CreditCardStatusRef;
import com.profitera.descriptor.db.reference.DebtRecoveryStatusRef;
import com.profitera.descriptor.db.reference.DelinquencyTypeRef;
import com.profitera.descriptor.db.reference.DemandDraftStatusRef;
import com.profitera.descriptor.db.reference.DirectDebitStatusRef;
import com.profitera.descriptor.db.reference.DisputeReasonRef;
import com.profitera.descriptor.db.reference.EmployeeProfileRef;
import com.profitera.descriptor.db.reference.EmployeeReportToTypeRef;
import com.profitera.descriptor.db.reference.EmployeeTypeRef;
import com.profitera.descriptor.db.reference.InstallmentStatusRef;
import com.profitera.descriptor.db.reference.InvoiceSummaryTypeRef;
import com.profitera.descriptor.db.reference.InvoiceTypeRef;
import com.profitera.descriptor.db.reference.LegalReasonRef;
import com.profitera.descriptor.db.reference.LetterHouseKeepStatusRef;
import com.profitera.descriptor.db.reference.LoanTypeRef;
import com.profitera.descriptor.db.reference.MaritalStatusRef;
import com.profitera.descriptor.db.reference.MoneyOrderStatusRef;
import com.profitera.descriptor.db.reference.NotificationTypeRef;
import com.profitera.descriptor.db.reference.PaymentBehaviourRef;
import com.profitera.descriptor.db.reference.PaymentFrequencyRef;
import com.profitera.descriptor.db.reference.PaymentLocationRef;
import com.profitera.descriptor.db.reference.PaymentTypeRef;
import com.profitera.descriptor.db.reference.PremisesRef;
import com.profitera.descriptor.db.reference.PriorityRef;
import com.profitera.descriptor.db.reference.ProcessStatusRef;
import com.profitera.descriptor.db.reference.ProcessTypeRef;
import com.profitera.descriptor.db.reference.ProductTypeLevelRef;
import com.profitera.descriptor.db.reference.ProductTypeRef;
import com.profitera.descriptor.db.reference.ProfileSegmentRef;
import com.profitera.descriptor.db.reference.RaceTypeRef;
import com.profitera.descriptor.db.reference.RiskLevelRef;
import com.profitera.descriptor.db.reference.SensitiveStatusRef;
import com.profitera.descriptor.db.reference.ServiceStatusRef;
import com.profitera.descriptor.db.reference.StateRef;
import com.profitera.descriptor.db.reference.TemplateTypeRef;
import com.profitera.descriptor.db.reference.TransactionCodeRef;
import com.profitera.descriptor.db.reference.TreatProcessTypeStatusRef;
import com.profitera.descriptor.db.reference.TreatmentProcessStatusRef;
import com.profitera.descriptor.db.reference.TreatmentProcessTypeRef;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.descriptor.db.reference.TreatmentStreamRef;
import com.profitera.descriptor.db.reference.TreatprocSubtypeRef;
import com.profitera.descriptor.db.reference.UnbilledTypeRef;
import com.profitera.descriptor.db.reference.UomMeasureRef;
import com.profitera.descriptor.db.reference.UserRoleRef;
import com.profitera.descriptor.db.reference.WaiveReasonRef;
import com.profitera.descriptor.db.worklist.WorkList;
import com.profitera.persistence.SessionManager;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.FullTableCache;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.MapCar;
import com.profitera.util.TopLinkQuery;
import com.profitera.util.reflect.Reflect;

public class ReferenceService extends ProviderDrivenService implements ReferenceServiceIntf {
    private final static Class[] EMPTYCLASS = new Class[0];
    private final static Object[] EMPTYOBJECT = new Object[0];
    private final static Class[] VECTORCLASSARRAY = {Vector.class};
    private final static HashMap SPECIAL_ID_METHODS = new HashMap();
    private final static HashMap SPECIAL_CODE_METHODS = new HashMap();
    private FullTableCache codeCache = new FullTableCache();
    private FullTableCache idCache = new FullTableCache();
    private MapCar treatmentProcessTypeRefMapCar = new MapCar(){
      public Object map(Object o) {
        TreatmentProcessTypeRef r = (TreatmentProcessTypeRef) o;
        ReferenceBusinessBean b = new ReferenceBusinessBean(r.getTreatprocTypeId(), r.getTreatprocTypeCode(),r.getTreatprocTypeDesc());
        b.setSortPriority(r.getSortPriority() == null ? 0 : r.getSortPriority().intValue());
        b.setDisabled(r.getDisable() == null ? false : r.getDisable().intValue() == 1);
        return b;
      }};
    private MapCar treatmentProcessStatusRefMapCar = new MapCar(){
      public Object map(Object o) {
        TreatmentProcessStatusRef r = (TreatmentProcessStatusRef) o;
        ReferenceBusinessBean b = new ReferenceBusinessBean(r.getTreatprocStatusId(), r.getTreatprocStatusCode(),r.getTreatprocStatusDesc());
        b.setSortPriority(r.getSortPriority() == null ? 0 : r.getSortPriority().intValue());
        b.setDisabled(r.getDisable() == null ? false : r.getDisable().intValue() == 1);
        return b;
      }};
      private MapCar treatmentStreamRefMapCar = new MapCar(){
		public Object map(Object o) {
			return ReferenceBeanConverter.convertToBusinessBean((TreatmentStreamRef)o);
		}};

    static {
        SPECIAL_CODE_METHODS.put(UomMeasureRef.class, "getUomType");
    }

    /**
     * @author Jamison Masse
     */
    public class ReferenceUpdateException extends Exception {
        ReferenceUpdateException(String msg) {
            super(msg);
        }
    }

    public TransactionCodeRef getTransactionCodeByTc(String tcInd) {
        return (TransactionCodeRef) getReference(TransactionCodeRef.class, tcInd, "getTranCodeId",
            "getTcCodeInd");
    }
    
    public TransactionCodeRef getTransactionCodeById(Double id) {
        return (TransactionCodeRef) getReference(TransactionCodeRef.class, id, "getTranCodeId",
            "getTcCodeInd");
    }

    public TransferObject getCardLevelRef() {
        return getServiceReference(ProductTypeLevelRef.class);
    }

    public TransferObject getAccountSexRef() {
        return getServiceReference(AccountSexRef.class);
    }

    public TransferObject getAccountStatusRef() {
        return getServiceReference(AccountStatusRef.class);
    }

    public TransferObject getAccountWorklistStatusRef() {
        return getServiceReference(AccountWorkListStatusRef.class);
    }

    public TransferObject getAgencyTypeRef() {
        return getServiceReference(AgencyTypeRef.class);
    }

    public TransferObject getBillingCycleRef() {
        return getServiceReference(BillingCycleRef.class);
    }

    public TransferObject getChargeOffReasonRef() {
        return getServiceReference(ChargeOffReasonRef.class);
    }

    public TransferObject getChargeOffStatusRef() {
        return getServiceReference(ChargeOffStatusRef.class);
    }

    public TransferObject getCheckStatusRef() {
        return getServiceReference(CheckStatusRef.class);
    }

    public TransferObject getCheckTypeRef() {
        return new TransferObject(new Vector());
    }

    public TransferObject getClassOfServiceRef() {
        return getServiceReference(ClassOfServiceRef.class);
    }
    public TransferObject getClientTypeRef() {
        return getServiceReference(ClientTypeRef.class);
    }

    public TransferObject getCollectionStatusRef() {
        return getServiceReference(CollectionStatusRef.class);
    }

    public TransferObject getContactNumberTypeRef() {
        return getServiceReference(ContactNumberTypeRef.class);
    }

    public TransferObject getContactTypeRef() {
        return getServiceReference(ContactTypeRef.class);
    }

    public TransferObject getCostingTypeRef() {
        return getServiceReference(CostingTypeRef.class);
    }

    public TransferObject getCostingUomRef() {
        return getServiceReference(CostingUomRef.class);
    }

    public TransferObject getDebtRecoveryStatusRef() {
        return getServiceReference(DebtRecoveryStatusRef.class);
    }

    public TransferObject getEmployeeReportToTypeRef() {
        return getServiceReference(EmployeeReportToTypeRef.class);
    }

    public TransferObject getEmployeeTypeRef() {
        return getServiceReference(EmployeeTypeRef.class);
    }

    public TransferObject getInstallmentStatusRef() {
        return getServiceReference(InstallmentStatusRef.class);
    }

    public TransferObject getInvoiceSummaryRef() {
        return getServiceReference(InvoiceSummaryTypeRef.class);
    }

    public TransferObject getInvoiceTypeRef() {
        return getServiceReference(InvoiceTypeRef.class);
    }

    public TransferObject getLoanTypeRef() {
        return getServiceReference(LoanTypeRef.class);
    }

    public TransferObject getMaritalStatusRef() {
        return getServiceReference(MaritalStatusRef.class);
    }

    public TransferObject getNotifyTypeRef() {
        return getServiceReference(NotificationTypeRef.class);
    }

    public TransferObject getPaymentTypeRef() {
        return getServiceReference(PaymentTypeRef.class);
    }

    public TransferObject getChannelCodeRef() {
        return getServiceReference(ChannelCodeRef.class);
    }

    public TransferObject getPriorityRef() {
        return getServiceReference(PriorityRef.class);
    }

    public TransferObject getProcessStatusRef() {
        return getServiceReference(ProcessStatusRef.class);
    }

    public TransferObject getProcessTypeRef() {
        return getServiceReference(ProcessTypeRef.class);
    }

    public TransferObject getSensitiveStatusRef() {
        return getServiceReference(SensitiveStatusRef.class);
    }

    public TransferObject getServiceStatusRef() {
        return getServiceReference(ServiceStatusRef.class);
    }

    public TransferObject getTemplateTypeRef() {
        return getServiceReference(TemplateTypeRef.class);
    }


    public TransferObject getEmployeeProfileRef() {
        return getServiceReference(EmployeeProfileRef.class);
    }

    public TransferObject getUnbilledTypeRef() {
        return getServiceReference(UnbilledTypeRef.class);
    }

    public TransferObject getCreditCardStatusRef() {
        return getServiceReference(CreditCardStatusRef.class);
    }

    public TransferObject getDisputeReasonRef() {
        return getServiceReference(DisputeReasonRef.class);
    }

    public TransferObject getLegalReasonRef() {
        return getServiceReference(LegalReasonRef.class);
    }

    public TransferObject getLetterHouseKeepStatusRef() {
        return getServiceReference(LetterHouseKeepStatusRef.class);
    }

    public TransferObject getWaiveReasonRef() {
        return getServiceReference(WaiveReasonRef.class);
    }

    public TransferObject getAutoPayStatusRef() {
        return getServiceReference(AutoPayStatusRef.class);
    }

    public TransferObject getMoneyOrderStatusRef() {
        return getServiceReference(MoneyOrderStatusRef.class);
    }

    public TransferObject getDemandDraftStatusRef() {
        return getServiceReference(DemandDraftStatusRef.class);
    }

    public TransferObject getDirectDebitStatusRef() {
        return getServiceReference(DirectDebitStatusRef.class);
    }

    public TransferObject getCardTypeRef() {
        return getServiceReference(ProductTypeRef.class);
    }

    public TransferObject getPremisesRef() {
        return getServiceReference(PremisesRef.class);
    }

    public TransferObject getStateCodeRef() {
        return getServiceReference(StateRef.class);
    }

    public TransferObject getMaritalStatusReference() {
        return getServiceReference(MaritalStatusRef.class);
    }

    public TransferObject getSexReference() {
        return getServiceReference(AccountSexRef.class);
    }

    public TransferObject getRaceTypeRef() {
        return getServiceReference(RaceTypeRef.class);
    }

    public TransferObject getAccountSegment() {
        return getServiceReference(CustomerSegment.class);
    }

    public TransferObject getCitizenshipRef() {
        return getServiceReference(CitizenshipRef.class);
    }

    public TransferObject getSystemStatusRef() {
        return getServiceReference(AccountStatusRef.class);
    }

    public TransferObject getBlockCodeRef() {
        return getServiceReference(BlockCodeRef.class);
    }

    public TransferObject getTreatmentStageRef() {
        return getServiceReference(TreatmentStageRef.class);
    }

    public TransferObject getDelinquencyTypeRef() {
        return getServiceReference(DelinquencyTypeRef.class);
    }

    public TransferObject getTreatmentProcessStatusRef() {
      return mapReference("getTreatmentProcessStatusRef", treatmentProcessStatusRefMapCar, TreatmentProcessStatusRef.class);
    }

    public TransferObject getTreatmentProcessTypeRef() {
      return mapReference("getTreatmentProcessTypeRef", treatmentProcessTypeRefMapCar, TreatmentProcessTypeRef.class);
    }

    private TransferObject mapReference(String callName, MapCar mapCar, Class c) {
      String str = getClass().getName() + "." + callName;
      try {
        log.debug(str);
        ReadAllQuery q = new ReadAllQuery(c);
        q.setCacheUsage(ReadAllQuery.DoNotCheckCache);
        List l = TopLinkQuery.asList(q, SessionManager.getClientSession());
        List rbbs = MapCar.map(mapCar, l);
        return new TransferObject(rbbs);
      } catch (Exception e) {
        log.error(str, e);
        return new TransferObject(TransferObject.EXCEPTION, str + " " + e.getMessage());
      }
    }

    public TransferObject getTreatmentStreamRef() {
      return mapReference("getTreatmentStreamRef", treatmentStreamRefMapCar, TreatmentStreamRef.class);
    }

    public TransferObject getPaymentBehaviourRef() {
        return getServiceReference(PaymentBehaviourRef.class);
    }

    public TransferObject getPaymentFrequencyRef() {
        return getServiceReference(PaymentFrequencyRef.class);
    }

    /**
     * @return contains vector of Reference Beans
     */
    public TransferObject getServiceReference(Class clazz) {
        try {
            return new TransferObject(convertToReferenceBeans(getCachedReference(clazz)));
        } catch (Exception e) {
            log.error("Exception while loading " + clazz.getName(), e);
            return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        }
    }


    private Vector createUomMeasureRef(Vector refs) {
        Vector beans = new Vector();
        for (int i = 0; i < refs.size(); i++) {
            beans.add(ReferenceBeanConverter.convertToBusinessBean((UomMeasureRef) refs.get(i)));
        }
        return beans;
    }

    /**
     * I'm special, I require custom conversion!
     *
     * @see com.profitera.deployment.rmi.ReferenceServiceIntf#getProfileSegment()
     */
    public TransferObject getProfileSegment() {
        try {
            Vector refs = new Vector();
            Iterator i = getCachedReference(ProfileSegmentRef.class);
            while (i.hasNext()) {
                refs.add(
                    ReferenceBeanConverter.convertToBusinessBean((ProfileSegmentRef) i.next()));
            }
            return new TransferObject(refs);
        } catch (Exception e) {
            return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        }
    }

    public TransferObject getUserRoleRef() {
        try {
            Vector refs = new Vector();
            Iterator i = getCachedReference(UserRoleRef.class, "getRoleId", "getRoleName");
            while (i.hasNext()) {
                refs.add(ReferenceBeanConverter.convertToBusinessBean((UserRoleRef) i.next()));
            }
            return new TransferObject(refs);
        } catch (Exception e) {
            return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        }
    }

    /**
     * I'm special...
     *
     * @see com.profitera.deployment.rmi.ReferenceServiceIntf#getPaymentLocationRef()
     */
    public TransferObject getPaymentLocationRef() {
        try {
            Iterator i = getCachedReference(PaymentLocationRef.class, "getPaymentLocationId",
                "getLocationCategory");
            Vector beans = new Vector();
            while (i.hasNext()) {
                beans.add(
                    ReferenceBeanConverter.convertToBusinessBean((PaymentLocationRef) i.next()));
            }
            return new TransferObject(beans);
        } catch (Exception e) {
            return new TransferObject(TransferObject.ERROR, e.getMessage());
        }
    }

    /**
     * I'm special! Yay!
     */
    public TransferObject getTreatmentProcessTypeStatusRef() {
        try {
            Iterator i = getCachedReference(TreatProcessTypeStatusRef.class,
                "getTreatprocTypeStatusId", "getTreatprocTypeStatusCode");
            Vector beans = new Vector();
            while (i.hasNext()) {
                beans.add(
                    ReferenceBeanConverter.createReferenceBusinessBean(
                        (TreatProcessTypeStatusRef) i.next()));
            }
            return new TransferObject(beans);
        } catch (Exception e) {
            e.printStackTrace();
            return new TransferObject(TransferObject.ERROR, e.getMessage());
        }
    }

    /**
     * I'm special!
     *
     * @see com.profitera.deployment.rmi.ReferenceServiceIntf#getRiskLevelRef()
     */
    public TransferObject getRiskLevelRef() {
        Vector refs = new Vector();
        try {
            Iterator i = getCachedReference(RiskLevelRef.class);
            while (i.hasNext()) {
                refs.add(ReferenceBeanConverter.convertToBusinessBean((RiskLevelRef) i.next()));
            }
        } catch (Exception e) {
            return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        }
        return new TransferObject(refs);
    }

    /**
     * I'm special!
     *
     * @see com.profitera.deployment.rmi.ReferenceServiceIntf#getTreatmentProcessSubtypeRef()
     */
    public TransferObject getTreatmentProcessSubtypeRef() {
        try {
            Vector beans = new Vector();
            Iterator i = getCachedReference(TreatprocSubtypeRef.class, "getTreatprocSubtypeId",
                "getTreatprocTypeCode");
            while (i.hasNext()) {
                beans.add(
                    ReferenceBeanConverter.convertToBusinessBean((TreatprocSubtypeRef) i.next()));
            }
            return new TransferObject(beans);
        } catch (Exception e) {
            return new TransferObject(TransferObject.ERROR, e.getMessage());
        }
    }

    public TransferObject getReference(String refName) {
        Method m;
        Exception thrown;
        try {
            m = this.getClass().getMethod(refName.trim(), EMPTYCLASS);
        } catch (NoSuchMethodException e) {
            return new TransferObject(TransferObject.EXCEPTION,
                "No such Method Found (" + refName + ")");
        } catch (SecurityException e) {
            return new TransferObject(TransferObject.EXCEPTION,
                "Security Exception raised when trying (" + refName + ")");
        }
        try {
            return (TransferObject) m.invoke(this, EMPTYOBJECT);
        } catch (IllegalAccessException e) {
            thrown = e;
        } catch (IllegalArgumentException e) {
            thrown = e;
        } catch (InvocationTargetException e) {
            thrown = e;
        }
        throw new RuntimeException("Failed to get ref " + refName + " from service.", thrown);
    }

    public TransferObject updateReference(String refName, List references) {
        // The update methods should all have a single param, a list of beans to update
        // Here we do a stupid little trick, the refname above should be the getter, so we replace
        // the 'get' with 'update' and use reflection.
        String methodName = "update" + refName.trim().substring(3);
        Method m;
        Exception thrown;
        try {
            m = this.getClass().getMethod(methodName, VECTORCLASSARRAY);
            Object[] arg = {references};
            return (TransferObject) m.invoke(this, arg);
        } catch (IllegalAccessException e) {
            thrown = e;
        } catch (IllegalArgumentException e) {
            thrown = e;
        } catch (InvocationTargetException e) {
            thrown = e;
        } catch (NoSuchMethodException e) {
            thrown = e;
        } catch (SecurityException e) {
            thrown = e;
        }
        log.error("Failed to update " + refName, thrown);
      	return new TransferObject(TransferObject.ERROR, "Updating this reference is unsupported.");
    }

    public TransferObject updateTreatmentStreamRef(Vector beans) {
        try {
            updateReference(beans, TreatmentStreamRef.class);
        } catch (ReferenceUpdateException e) {
            e.printStackTrace();
            return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        }
        return getServiceReference(TreatmentStreamRef.class);
    }

    /**
     * Uses toplink to update the class passed in assuming it has only 3 required
     * fields, an ID, code, and description. If uses the following regex to pull
     * the methods using reflection:<pre>
     * set.*Id
     * set.*Code
     * set.*Desc
     * </pre>
     * THe Vector is assumed to be full of ReferenceBusinessBeans.
     */
    private void updateReference(Vector refBeans, Class refDBClass) throws ReferenceUpdateException {
        Method idSetter = Reflect.getMethodMatching(refDBClass, "set.*Id$");
        Method codeSetter = Reflect.getMethodMatching(refDBClass, "set.*Code$");
        Method descSetter = Reflect.getMethodMatching(refDBClass, "set.*Desc$");
        try {
            if (idSetter == null || codeSetter == null || descSetter == null) {
              	throw new NoSuchMethodException("Id, Code, or Description setter could not be found.");
            }
            Object[] arg = new Object[1];
            UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
            for (int i = 0; i < refBeans.size(); i++) {
                ReferenceBusinessBean bean = (ReferenceBusinessBean) refBeans.get(i);
                Object dbRef;
                dbRef = refDBClass.newInstance();
                if (bean.getId() == null) {
                    dbRef = handle.registerNewObject(dbRef);
                } else {
                    dbRef = handle.registerExistingObject(dbRef);
                    arg[0] = bean.getId();
                    idSetter.invoke(dbRef, arg);
                }
                arg[0] = bean.getCode();
                codeSetter.invoke(dbRef, arg);
                arg[0] = bean.getDesc();
                descSetter.invoke(dbRef, arg);
            }
            handle.commit();
            clearCache(refDBClass);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // if we are down here an exception occurred...
        throw new ReferenceUpdateException("Failed to update reference.");
    }

    public TransferObject updateTreatmentProcessTypeStatusRef(Vector beansToUpdate) {
        try {
            Iterator i = beansToUpdate.iterator();
            UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
            while (i.hasNext()) {
                TreatProcessTypeStatusRef status = new TreatProcessTypeStatusRef();
                TreatmentProcessTypeStatusRefBusinessBean bean = (TreatmentProcessTypeStatusRefBusinessBean) i.next();
                if (bean.getId() == null) {
                    status = (TreatProcessTypeStatusRef) handle.registerNewObject(status);
                } else {
                    status.setTreatprocTypeStatusId(bean.getId());
                    status = (TreatProcessTypeStatusRef) handle.registerExistingObject(status);
                }
                ReferenceBeanConverter.convertToDBDescriptor(bean, status);
            }
            handle.commit();
        } catch (DatabaseException e) {
        	return new TransferObject(TransferObject.ERROR, e.getMessage());
        }
        clearCache(TreatProcessTypeStatusRef.class);
        return getTreatmentProcessTypeStatusRef();
    }

    /**
     * I'm special!
     *
     * @see com.profitera.deployment.rmi.ReferenceServiceIntf#getWorklistRef()
     */
    public TransferObject getWorklistRef() {
        try {
            Iterator i = getCachedReference(WorkList.class, "getWorkListId", "getWorkListName");
            Vector beans = new Vector();
            for (WorkList wl = (WorkList) i.next(); i.hasNext(); wl = (WorkList) i.next()) {
                beans.add(ReferenceBeanConverter.convertToBusinessBean(wl));
            }
            return new TransferObject(beans);
        } catch (Exception e) {
            return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
        }
    }

    private synchronized void clearCache(Class clazz) {
        codeCache.clearCache(clazz);
        idCache.clearCache(clazz);
    }

    private void ensureCached(Class clazz, FullTableCache classCache, String idMethod, String codeMethod) {
        if (classCache.isCached(clazz)) {
            return;
        }
        Session s = SessionManager.getClientSession();
        ReadAllQuery q = new ReadAllQuery(clazz);
        q.setCacheUsage(ReadAllQuery.DoNotCheckCache);
        Vector v = (Vector) TopLinkQuery.asList(q, s);
        populateCache(clazz, idMethod, codeMethod, v);
    }

    /**
     * Retrieves the reference object from the cache. Careful, this code is brittle
     *
     * @param clazz the Reference class
     * @param code  the Reference object's code field
     * @return the Reference object matching the class and code
     */
    public Object getReference(Class clazz, String code) {
        return getReference(clazz, code, null, null);
    }

    public Object getReference(Class clazz, String code, String idMethod, String codeMethod) {
        ensureCached(clazz, codeCache, idMethod, codeMethod);
        return codeCache.getObject(clazz, code);
    }

    /**
     * Convenience method that auto-registers a reference with a Unit of Work
     *
     * @return a registered reference for the given UOW, or null if no match
     */
    public Object getReference(Class clazz, String code, UnitOfWork uow) {
        Object ref = getReference(clazz, code);
        if (ref != null) ref = uow.registerExistingObject(ref);
        return ref;
    }

    /**
     * Retrieves the reference object from the cache. Careful, this code is brittle
     *
     * @param clazz the Reference class
     * @param id    the Reference object's id field
     * @return the Reference object matching the class and id
     */
    public Object getReference(Class clazz, Double id) {
        return getReference(clazz, id, (String) SPECIAL_ID_METHODS.get(clazz),
            (String) SPECIAL_CODE_METHODS.get(clazz));
    }

    public Object getReference(Class clazz, Double id, String idMethodName, String codeMethodName) {
        ensureCached(clazz, idCache, idMethodName, codeMethodName);
        return idCache.getObject(clazz, id);
    }

    /**
     * Convenience method that auto-registers a reference with a Unit of Work
     *
     * @return a registered reference for the given UOW, or null if no match
     */
    public Object getReference(Class clazz, Double id, UnitOfWork uow) {
        Object ref = getReference(clazz, id);
        if (ref != null) ref = uow.registerExistingObject(ref);
        return ref;
    }

    /**
     * Convenience method that auto-registers a reference with a Unit of Work
     * Handles case where bean is null, it cleanly returns null
     *
     * @return a registered reference for the given UOW, or null if no match
     */
    public Object getReference(Class clazz, ReferenceBusinessBean bean, UnitOfWork uow) {
        if (bean == null) return null;
        final Double id = bean.getId();
        Object ref = getReference(clazz, id);
        if (uow != null) ref = uow.registerExistingObject(ref);
        return ref;
    }

    private Iterator getCachedReference(Class clazz) {
        return getCachedReference(clazz, (String) SPECIAL_ID_METHODS.get(clazz),
            (String) SPECIAL_CODE_METHODS.get(clazz));
    }

    private Iterator getCachedReference(Class clazz, String idMethodName, String codeMethodName) {
        ensureCached(clazz, idCache, idMethodName, codeMethodName);
        return idCache.getObjects(clazz);
    }

    /* Uses reflection to get the Id and Code getters, works for the standard case where
     * [SomeClass]Ref.get[SomeClass]Id() or [SomeClass]Ref.get[SomeClass]Code()
     * method.  An example is provided below.
     */
    private synchronized void populateCache(Class clazz, String idMethod, String codeMethod, Vector v) {
        if (idMethod == null) {
            Method getId = Reflect.getMethodMatching(clazz, "get.*Id$");
            if (getId == null) {
                throw new RuntimeException("Id method could not be found for " + clazz.getName());
            }
            idMethod = getId.getName();
        }
        if (codeMethod == null) {
            Method getCode = Reflect.getMethodMatching(clazz, "get.*Code$");
            if (getCode == null) {
                throw new RuntimeException("Code method could not be found for " + clazz.getName());
            }
            codeMethod = getCode.getName();
        }
        if (v == null) {
            idCache.addCachedClass(clazz, idMethod);
            codeCache.addCachedClass(clazz, codeMethod);
        } else {
            idCache.addCachedClass(clazz, idMethod, v);
            codeCache.addCachedClass(clazz, codeMethod, v);
        }
    }

    /**
     * WARNING! use of this method may be slow
     *
     * @param clazz the Reference class
     * @param code  the value of code field
     */
    public ReferenceBusinessBean getReferenceBean(Class clazz, String code) {
        final Object obj = getReference(clazz, code);
        if (obj == null) {
            return null;
        }
        return convertToReferenceBean(obj);
    }

    public ReferenceBusinessBean getReferenceBean(Class clazz, Double id) {
        final Object obj = getReference(clazz, id);
        if (obj == null) {
            return null;
        }
        return convertToReferenceBean(obj);
    }

    public Vector getReferenceBeans(Class clazz) {
        Iterator i = getCachedReference(clazz);
        return convertToReferenceBeans(i);
    }

    public static ReferenceBusinessBean convertToReferenceBean(final Object obj) {
        if (obj == null) return null;
        ReferenceBusinessBean result;
        Method[] methods = obj.getClass().getMethods();
        Method idGetter = Reflect.getMethodMatching(methods, "get.*Id$");
        Method codeGetter = Reflect.getMethodMatching(methods, "get.*Code$");
        Method descGetter = Reflect.getMethodMatching(methods, "get.*Desc$");
        Method disabledGetter = Reflect.getMethodMatching(methods, "getDisable.*");
        Method prioGetter = Reflect.getMethodMatching(methods, "getSort.*");
        try {
            Double id = (Double) idGetter.invoke(obj, EMPTYOBJECT);
            String code1 = (String) codeGetter.invoke(obj, EMPTYOBJECT);
            String desc = (String) descGetter.invoke(obj, EMPTYOBJECT);
            Double disabled = (Double) disabledGetter.invoke(obj, EMPTYOBJECT);
            Double prio = (Double) prioGetter.invoke(obj, EMPTYOBJECT);
            result = new ReferenceBusinessBean(id, code1, desc);
            if (disabled == null || disabled.intValue() == 0) {
                result.setDisabled(false);
            } else {
                result.setDisabled(true);
            }
            if (prio != null) {
                result.setSortPriority(prio.intValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    public static Vector convertToReferenceBeans(Iterator i) {
        Vector beans = new Vector();
        while (i.hasNext()) {
            beans.add(convertToReferenceBean(i.next()));
        }
        Collections.sort((List)beans,new PriorityRefComparator());
        
        return beans;
    }

    public TransferObject getCountryRefs() {
        return getServiceReference(CountryRef.class);
    }


    private Class getColumnClass(String methodConstant,Class clazz,String classForColumn) 
                                 throws NoSuchMethodException, ClassNotFoundException, InstantiationException,
                                        NoSuchFieldException, IllegalAccessException{ //Used by reference manager
    
        Method method = null; 
        if (classForColumn==null) {
              Object obj = clazz.newInstance();          
              Field field = obj.getClass().getField(methodConstant);
              final String constValue = (String)field.get(null);
              final String methodName = "get" + constValue.substring(0, 1).toUpperCase() + constValue.substring(1);
              method = obj.getClass().getMethod(methodName,EMPTYCLASS);
        }
        return method == null ? Object.class : method.getReturnType();
    }
    
    private String getColumnName(String methodConstant,Class clazz) 
                                 throws InstantiationException, NoSuchFieldException, 
                                        IllegalAccessException{//Used by reference manager
      String constValue =null;
      
      Object obj = clazz.newInstance();          
      Field field = obj.getClass().getField(methodConstant);
      constValue = (String)field.get(null);
      return constValue;
    }
    
    private void classSetter(Object object,String methodName,Class[] classParameter,Object value) throws Exception{
       String methodNameToInvoke = "set" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
       Method method = object.getClass().getMethod(methodNameToInvoke,classParameter);
       method.invoke(object,new Object[]{value});
       
    }
    
    public ClarityMessagesRef getClarityMessagesRefByCode(String code) {
    	return (ClarityMessagesRef) getReference(ClarityMessagesRef.class, code, "getId", "getCode");
    }

    public TransferObject updateReferenceData(List data, final String insert, final String update) {
      final IReadWriteDataProvider p = getReadWriteProvider();
      IRunnableTransaction[] trans = new IRunnableTransaction[data.size()];
      for (int i = 0; i < trans.length; i++) {
        final Map ref = (Map) data.get(i);        
        trans[i] = new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            if (ref.get("ID") == null){
              p.insert(insert, ref, t);
            } else {
              p.update(update, ref, t);
            }
          }};
      }
      try {
        p.execute(new RunnableTransactionSet(trans));
      } catch (AbortTransactionException e) {
        return sqlFailure(null, insert + "/" + update, data, e);
      } catch (SQLException e) {
        return sqlFailure(null, insert + "/" + update, data, e);
      }
      return new TransferObject();
    }
}