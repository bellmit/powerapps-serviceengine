package com.profitera.services.business.template;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ObjectLevelReadQuery;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;
import oracle.toplink.sessions.Session;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.deployment.rmi.TemplateServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.reference.ReferenceBeanConverter;
import com.profitera.descriptor.business.template.TemplateBusinessBean;
import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.db.contact.AddressDetails;
import com.profitera.descriptor.db.reference.ContactTypeRef;
import com.profitera.descriptor.db.reference.CountryRef;
import com.profitera.descriptor.db.reference.SalutationTypeRef;
import com.profitera.descriptor.db.reference.StateRef;
import com.profitera.descriptor.db.reference.TemplateTypeRef;
import com.profitera.descriptor.db.treatment.Template;
import com.profitera.persistence.SessionManager;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.report.IReportGenerationService;
import com.profitera.services.system.report.ReportCompilationException;
import com.profitera.services.system.report.ReportGenerationException;
import com.profitera.util.PrimitiveValue;
import com.profitera.util.TopLinkQuery;
import com.profitera.util.io.FileUtil;

public final class TemplateService extends ProviderDrivenService implements TemplateServiceIntf {
    private static TemplateBusinessBean createTemplateBusinessBean(final Template templateDao) {
        final TemplateBusinessBean templateBean = new TemplateBusinessBean();
        templateBean.setContent(templateDao.getContent());
        templateBean.setDescription(templateDao.getDescription());
        templateBean.setName(templateDao.getTemplateName());
        templateBean.setTemplateId(templateDao.getTemplateId());
        final TemplateTypeRef templateTypeRef = templateDao.getTypeRef();
        if (templateTypeRef != null) {
            templateBean.setType(ReferenceBeanConverter.convertToBusinessBean(templateTypeRef));
        } else {
            templateBean.setType(null);
        }
        return templateBean;
    }

    public final TransferObject getAllTemplates() {
        return getTemplates(null);
    }

    /**
     * get Template
     */
    public final TransferObject getTemplates(final Double type) {
        final Session session = SessionManager.getClientSession();
        final Vector returnObject = new Vector();
        Expression exp = null;
        final ExpressionBuilder builder = new ExpressionBuilder();
        if (type != null) {
            exp = builder.get(Template.TYPE_REF).get(TemplateTypeRef.TEMPLATE_TYPE_ID).equal(type);
            exp = TopLinkQuery.andExpressions(exp, builder.get(Template.DISABLE).equal(Boolean.FALSE));
        } else {
            exp = builder.get(Template.DISABLE).equal(Boolean.FALSE);
        }
        ReadAllQuery readAllQuery = new ReadAllQuery(Template.class, exp);
        readAllQuery.setCacheUsage(ObjectLevelReadQuery.DoNotCheckCache);
        final Vector templates = (Vector) session.executeQuery(readAllQuery);
        for (int i = 0; i < templates.size(); i++) {
            final Template t = (Template) templates.get(i);
            returnObject.add(createTemplateBusinessBean(t));
        }
        return new TransferObject(returnObject);
    }

    public final TransferObject addTemplate(final Map templateBusinessBean) {
    	return updateTemplate(templateBusinessBean);
    }

    public final TransferObject updateTemplate(final Map templateBusinessBean) {
    	// update subreports
    	final IReadWriteDataProvider p = getReadWriteProvider();
      String reportDesignContent = (String) templateBusinessBean.get(TemplateBusinessBean.DESIGN_CONTENT);
    	if (reportDesignContent != null && reportDesignContent.trim().length() != 0) {
    		try {
    		  resolveReportDesignSubreports(templateBusinessBean, p);
  			} catch (TransferObjectException e) {
  				return e.getTransferObject();
  			} catch (Exception ex) {
  			  log.error("Error updating design template", ex);
  				return new TransferObject(TransferObject.EXCEPTION, "ERROR_SAVING_TEMPLATE");
  			}
    	}
        
      if (templateBusinessBean instanceof TemplateBusinessBean){
        TemplateBusinessBean temp = (TemplateBusinessBean) templateBusinessBean;
        templateBusinessBean.put(TemplateBusinessBean.TEMPLATE_TYPE_ID, temp.getType() == null ? null : new Long(temp.getType().getId().longValue()));
      }
      if (templateBusinessBean.get(TemplateBusinessBean.TEMPLATE_TYPE_ID) == null) {
          return new TransferObject(TransferObject.ERROR, "Template has no type assigned");
      }
      Number id = (Number) templateBusinessBean.get(TemplateBusinessBean.TEMPLATE_ID);
      if (id != null){
        templateBusinessBean.put(TemplateBusinessBean.TEMPLATE_ID, new Long(id.longValue()));
      }
      
      final Long docId = (Long) templateBusinessBean.get(TemplateBusinessBean.DOCUMENT_ID);
      final String designFile = (String) templateBusinessBean.get(TemplateBusinessBean.DESIGN_FILE_NAME);
      final IDocumentTransaction docTrans = getDocumentTransaction(docId, designFile, (CharSequence)templateBusinessBean.get(TemplateBusinessBean.DESIGN_CONTENT), p);
      // Kill the content if there is there is an associated document, 
      // we don't want anything in the CLOB if we are using the doc service. 
      if (docId != null){
        templateBusinessBean.put(TemplateBusinessBean.CONTENT, null);
      }
      
      if (id == null){
        try {
          p.execute(new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException, AbortTransactionException {
              if (docTrans != null){
                docTrans.execute(t);
                templateBusinessBean.put(TemplateBusinessBean.DOCUMENT_ID, docTrans.getId());
              }
              Long newId = (Long) p.insert("insertTemplate", templateBusinessBean, t);
              templateBusinessBean.put(TemplateBusinessBean.TEMPLATE_ID, newId);
            }});
        } catch (AbortTransactionException e) {
          return sqlFailure("insert", "insertTemplate", templateBusinessBean, e);
        } catch (SQLException e) {
          return sqlFailure("insert", "insertTemplate", templateBusinessBean, e);
        }
      } else {
        try {
          p.execute(new IRunnableTransaction(){
            public void execute(ITransaction t) throws SQLException, AbortTransactionException {
              if (docTrans != null){
                docTrans.execute(t);
                templateBusinessBean.put(TemplateBusinessBean.DOCUMENT_ID, docTrans.getId());
              }
              p.update("updateTemplate", templateBusinessBean, t);
            }});
        } catch (AbortTransactionException e) {
          return sqlFailure("update", "updateTemplate", templateBusinessBean, e);
        } catch (SQLException e) {
          return sqlFailure("update", "updateTemplate", templateBusinessBean, e);
        }
        if (docTrans == null){
          Template t = (Template) TopLinkQuery.getObject(Template.class, new String[]{Template.TEMPLATE_ID}, id, SessionManager.getClientSession());
          SessionManager.getClientSession().refreshObject(t);
          
        }
      }
      return new TransferObject();
    }

    private void resolveReportDesignSubreports(final Map templateBusinessBean,
        final IReadWriteDataProvider p) throws TransferObjectException,
        SQLException, AbortTransactionException {
      String content = (String) templateBusinessBean.get(TemplateBusinessBean.DESIGN_CONTENT);
      Map subreport = (Map) templateBusinessBean.get(TemplateBusinessBean.TEMPLATE_SUB_REPORT);
        if (subreport == null) {
        subreport = new HashMap();
      }
        Map subreportIds = new HashMap();
        for (Iterator i = subreport.entrySet().iterator(); i.hasNext();) {
        Map.Entry r = (Map.Entry) i.next();
        subreportIds.put(r.getKey(), ((Map) r.getValue()).get(TemplateBusinessBean.TEMPLATE_ID));
      }
      final String newContent = getReportGenerationService().replaceDesignSubreportLocations(content, subreportIds);
      templateBusinessBean.put(TemplateBusinessBean.DESIGN_CONTENT, newContent);
      
      p.execute(new IRunnableTransaction() {
      	public void execute(ITransaction t) throws SQLException, AbortTransactionException {
      		if (newContent != null) {
      			IDocumentService docService = getDocumentService();
      			IDocumentTransaction transaction = docService.createDocument(
      					IDocumentService.REPORT_DESIGN_DOCUMENT_TYPE_ID,
      					"Template for " + templateBusinessBean.get(TemplateBusinessBean.NAME),
      					newContent, p);
      			transaction.execute(t);
      			templateBusinessBean.put(TemplateBusinessBean.DOCUMENT_ID, transaction.getId());
      		}
      	}});
    }

    private IDocumentTransaction getDocumentTransaction(Long docId, String designFile, CharSequence object, IReadWriteDataProvider p) {
      if (designFile != null && docId == null){
        // This is also a file upload for a template design.
        return getDocumentService().createDocument(IDocumentService.TEMPLATE_DOCUMENT_TYPE_ID, designFile, object, p);
      } else if (designFile != null){
        return getDocumentService().updateDocument(docId, IDocumentService.TEMPLATE_DOCUMENT_TYPE_ID, designFile, object, p);
      }
      return null;
    }

    public final TransferObject deleteTemplate(final Number id) {
      final Long templateId = new Long(id.longValue());
      final IReadWriteDataProvider p = getReadWriteProvider();
      try {
        p.execute(new IRunnableTransaction() {
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            p.update("disableTemplate", templateId, t);
          }
        });
        return new TransferObject(null);
      } catch (AbortTransactionException e) {
        return sqlFailure("update", "disableTemplate", templateId, e);
      } catch (SQLException e) {
        return sqlFailure("update", "disableTemplate", templateId, e);
      }
    }

    /**
     * TransferObject contains a LinkedHashMap, the keys are the tab names
     * and the vals are String arrays
     *
     * @see com.profitera.deployment.rmi.TemplateServiceIntf#getTemplateTags()
     */
    public final TransferObject getTemplateTags() {
        final LinkedHashMap map = new LinkedHashMap();
        map.put("Account", REPORT_BEAN_DATA_LIST_ACCOUNT);
        map.put("Contact", REPORT_BEAN_DATA_LIST_CONTACT);
//        map.put("Bills", REPORT_BEAN_DATA_LIST_BILLS);
        return new TransferObject(map);
    }

    /**
     * All valid tags should never return null, null returns are how the HTML passes
     * through the parser unscathed.
     *
     * @see com.profitera.deployment.rmi.TemplateServiceIntf#getTemplateTagValues(Double)
     */
    public final TransferObject getTemplateTagValues(final Double accountId) {
        final HashMap valueMap = new HashMap();
        final Session s = SessionManager.getClientSession();
        final Account a = getAccount(accountId, s);
        valueMap.put(ACCOUNT_NUMBER, a.getAccountNumber());
        valueMap.put(TOTAL_DELQ_AMOUNT, a.getTotalDelqAmt());
        valueMap.put(MINIMUM_PAYMENT_DUE, a.getCurrDueAmt());
        valueMap.put(OUTSTANDING_AMOUNT, a.getOutstandingAmt());
        final AddressDetails addressDetails = a.getAccountOwnerDet().getAddressDetails();
        if (addressDetails != null) {
            final String accountName = PrimitiveValue.stringValue(addressDetails.getContactFirstName()) + " " + PrimitiveValue.stringValue(
                addressDetails.getContactMiddleName()) + " " + PrimitiveValue.stringValue(
                    addressDetails.getContactLastName());
            valueMap.put(ACCOUNT_NAME, accountName);
            final SalutationTypeRef salutationTypeRef = addressDetails.getSalutationTypeRef();
            if (salutationTypeRef != null) {
                valueMap.put(SALUTATION, salutationTypeRef.getSalutationCode());
            } else {
                valueMap.put(SALUTATION, "");
            }
            // Now contact stuff:
            valueMap.put(PRIMARY_CONTACT_ADDRESS_1, PrimitiveValue.stringValue(addressDetails.getAddress1()));
            valueMap.put(PRIMARY_CONTACT_ADDRESS_2, PrimitiveValue.stringValue(addressDetails.getAddress2()));
            valueMap.put(PRIMARY_CONTACT_CITY, PrimitiveValue.stringValue(addressDetails.getCity()));
            final CountryRef countryRef = addressDetails.getCountryRef();
            if (countryRef != null) {
                valueMap.put(PRIMARY_CONTACT_COUNTRY, countryRef.getCountryDesc());
            } else {
                valueMap.put(PRIMARY_CONTACT_COUNTRY, "");
            }
            valueMap.put(PRIMARY_CONTACT_FIRST_NAME, PrimitiveValue.stringValue(addressDetails.getContactFirstName()));
            valueMap.put(PRIMARY_CONTACT_LAST_NAME, PrimitiveValue.stringValue(addressDetails.getContactLastName()));
            valueMap.put(PRIMARY_CONTACT_MIDDLE_NAME, PrimitiveValue.stringValue(addressDetails.getContactMiddleName()));
            valueMap.put(PRIMARY_CONTACT_PERSON_NAME, PrimitiveValue.stringValue(addressDetails.getContactFirstName()) +
                " " + PrimitiveValue.stringValue(addressDetails.getContactMiddleName()) + " " + PrimitiveValue.stringValue(
                    addressDetails.getContactLastName()));
            final SalutationTypeRef salutationRef = addressDetails.getSalutationTypeRef();
            if (salutationRef != null) {
                valueMap.put(PRIMARY_CONTACT_PERSON_SALUTATION, salutationRef.getSalutationDesc());
            } else {
                valueMap.put(PRIMARY_CONTACT_PERSON_SALUTATION, "");
            }
            valueMap.put(PRIMARY_CONTACT_SECTION, PrimitiveValue.stringValue(addressDetails.getSection()));
            final StateRef stateRef = addressDetails.getStateRef();
            if (stateRef != null) {
                valueMap.put(PRIMARY_CONTACT_STATE, stateRef.getStateDesc());
            } else {
                valueMap.put(PRIMARY_CONTACT_STATE, "");
            }
            valueMap.put(PRIMARY_CONTACT_TIME_AFTER, addressDetails.getContactTimeAfter());
            valueMap.put(PRIMARY_CONTACT_TIME_BEFORE, addressDetails.getContactTimeBefore());
            final ContactTypeRef contactTypeRef = addressDetails.getContactTypeRef();
            if (contactTypeRef != null) valueMap.put(PRIMARY_CONTACT_TYPE, contactTypeRef.getContactTypeCode());
            valueMap.put(PRIMARY_CONTACT_ZIP_CODE, PrimitiveValue.stringValue(addressDetails.getZipCode()));
/*
            valueMap.put(BILLS_ACCOUNT_NO, a.getAccountNumber());
            final Invoice invoice = ReconciliationDataManager.getInvoiceBefore(a, null, s);
            if (invoice != null) {
                valueMap.put(BILLS_BILLING_DATE, invoice.getInvoiceDate());
                valueMap.put(BILLS_CREDIT_ADJUSTMENT, invoice.getCurrentAdjustmentAmount());
                valueMap.put(BILLS_PAYMENT_BEFORE_DATE, invoice.getPaymentDueDate());
                valueMap.put(BILLS_PREVIOUS_BALANCE, invoice.getPreviousAmountBalance());
                valueMap.put(BILLS_TOTAL_AMMOUNT_DUE, invoice.getInvoiceDueAmount());
            }
*/
        }
        return new TransferObject(valueMap);
    }
    
    private IDocumentService getDocumentService() {
      final IDocumentService docService = (IDocumentService) LookupManager
          .getInstance().getLookupItem(LookupManager.SYSTEM, "DocumentService");
      return docService;
    }


    /**
     * @param accountId
     * @param s
     * @return
     */
    private static Account getAccount(final Double accountId, final Session s) {
        return (Account) s.executeQuery(
            new ReadObjectQuery(Account.class, new ExpressionBuilder().get(Account.ACCOUNT_ID).equal(accountId)));
    }

    //ACCOUNT INFO
    private static final String ACCOUNT_NAME = "ACCOUNT NAME";
    private static final String ACCOUNT_NUMBER = "ACCOUNT NO.";
    private static final String SALUTATION = "SALUTATION";
    private static final String TOTAL_DELQ_AMOUNT = "TOTAL DELINQUENT AMOUNT";
    private static final String MINIMUM_PAYMENT_DUE = "MINIMUM PAYMENT DUE";
    private static final String OUTSTANDING_AMOUNT = "OUTSTANDING AMOUNT";
    private static final String CREDIT_LIMIT = "CREDIT LIMIT";


    //CONTACT INFO
    private static final String PRIMARY_CONTACT_TYPE = "CONTACT TYPE";
    private static final String PRIMARY_CONTACT_PERSON_NAME = "CONTACT PERSON NAME";
    private static final String PRIMARY_CONTACT_PERSON_SALUTATION = "CONTACT PERSON SALUTATION";
    private static final String PRIMARY_CONTACT_FIRST_NAME = "CONTACT PERSON FIRST NAME";
    private static final String PRIMARY_CONTACT_MIDDLE_NAME = "CONTACT PERSON MIDDLE NAME";
    private static final String PRIMARY_CONTACT_LAST_NAME = "CONTACT PERSON LAST NAME";
/*
    Block Id
    Unit Id
    Building Name
    Floor Id
    Plot Id
    Street Type
    Street Name
    postal region Code
*/
//    private static final String PRIMARY_CONTACT_BLOCK_ID = "BLOCK ID";
//    private static final String PRIMARY_CONTACT_UNIT_ID = "UNIT ID";
//    private static final String PRIMARY_CONTACT_BUIDLING_NAME = "BUILDING NAME";
//    private static final String PRIMARY_CONTACT_FLOOR_ID = "FLOOR ID";
//    private static final String PRIMARY_CONTACT_PLOT_ID = "PLOT ID";
    private static final String PRIMARY_CONTACT_SECTION = "SECTION";
    private static final String PRIMARY_CONTACT_CITY = "CITY";
    private static final String PRIMARY_CONTACT_STATE = "STATE";
//    private static final String PRIMARY_CONTACT_STREET_TYPE = "STREET TYPE";
    private static final String PRIMARY_CONTACT_STREET_NAME = "STREET NAME";
    private static final String PRIMARY_CONTACT_ZIP_CODE = "ZIP CODE";
//    private static final String PRIMARY_CONTACT_POSTAL_REGION_CODE = "POSTAL REGION CODE";
    private static final String PRIMARY_CONTACT_COUNTRY = "COUNTRY";
    private static final String PRIMARY_CONTACT_TIME_BEFORE = "CONTACT TIME BEFORE";
    private static final String PRIMARY_CONTACT_TIME_AFTER = "CONTACT TIME AFTER";
    private static final String PRIMARY_CONTACT_ADDRESS_1 = "ADDRESS 1";
    private static final String PRIMARY_CONTACT_ADDRESS_2 = "ADDRESS 2";
    //bills
/*
    private static final String BILLS_SERVICE_NO = "BILLS SERVICE NO";
    private static final String BILLS_ACCOUNT_NO = "BILLS ACCOUNT NO";
    private static final String BILLS_BILLING_DATE = "BILLS BILLING DATE";
    private static final String BILLS_PAYMENT_BEFORE_DATE = "BILLS PAYMENT BEFORE DATE";
    private static final String BILLS_PREVIOUS_BALANCE = "BILLS PREVIOUS BALANCE";
    private static final String BILLS_CREDIT_ADJUSTMENT = "BILLS CREDIT ADJUSTMENT";
    private static final String BILLS_RENTAL = "BILLS RENTAL";
    private static final String BILLS_CALLS = "BILLS CALLS";
    private static final String BILLS_SERVICE_TAX = "BILLS SERVICE TAX";
    private static final String BILLS_REBATE = "BILLS REBATE";
    private static final String BILLS_MESSAGE = "BILLS MESSAGE";
    private static final String BILLS_TOTAL_AMMOUNT_DUE = "TOTAL BILL AMMOUNT";
    private static final String BILLS_TOTAL_MONTH_CHARGES = "TOTAL MONTH BILL";
*/

    private static final String[] REPORT_BEAN_DATA_LIST_CONTACT = {
        PRIMARY_CONTACT_TYPE, PRIMARY_CONTACT_PERSON_NAME, PRIMARY_CONTACT_PERSON_SALUTATION,
        PRIMARY_CONTACT_FIRST_NAME, PRIMARY_CONTACT_MIDDLE_NAME, PRIMARY_CONTACT_LAST_NAME, /* PRIMARY_CONTACT_BLOCK_ID,
        PRIMARY_CONTACT_UNIT_ID, PRIMARY_CONTACT_BUIDLING_NAME, PRIMARY_CONTACT_FLOOR_ID, PRIMARY_CONTACT_PLOT_ID,
        PRIMARY_CONTACT_SECTION, PRIMARY_CONTACT_CITY, PRIMARY_CONTACT_STATE, PRIMARY_CONTACT_STREET_TYPE,
        PRIMARY_CONTACT_STREET_NAME, PRIMARY_CONTACT_ZIP_CODE, PRIMARY_CONTACT_POSTAL_REGION_CODE, */
        PRIMARY_CONTACT_COUNTRY, PRIMARY_CONTACT_TIME_BEFORE, PRIMARY_CONTACT_TIME_AFTER, PRIMARY_CONTACT_ADDRESS_1,
        PRIMARY_CONTACT_ADDRESS_2,PRIMARY_CONTACT_STREET_NAME
    };
    private static final String[] REPORT_BEAN_DATA_LIST_ACCOUNT = {
        ACCOUNT_NAME, ACCOUNT_NUMBER, SALUTATION, TOTAL_DELQ_AMOUNT, MINIMUM_PAYMENT_DUE, OUTSTANDING_AMOUNT,CREDIT_LIMIT
    };

/*
    private static final String[] REPORT_BEAN_DATA_LIST_BILLS = {
        BILLS_SERVICE_NO, BILLS_ACCOUNT_NO, BILLS_BILLING_DATE, BILLS_PAYMENT_BEFORE_DATE, BILLS_PREVIOUS_BALANCE,
        BILLS_CREDIT_ADJUSTMENT, BILLS_RENTAL, BILLS_CALLS, BILLS_SERVICE_TAX, BILLS_REBATE, BILLS_MESSAGE,
        BILLS_TOTAL_AMMOUNT_DUE, BILLS_TOTAL_MONTH_CHARGES,
    };
*/
    public static final String[] REPORT_BEAN_DATA_LIST = {
        PRIMARY_CONTACT_TYPE, PRIMARY_CONTACT_PERSON_NAME, PRIMARY_CONTACT_PERSON_SALUTATION,
        PRIMARY_CONTACT_FIRST_NAME, PRIMARY_CONTACT_MIDDLE_NAME, PRIMARY_CONTACT_LAST_NAME, /* PRIMARY_CONTACT_BLOCK_ID,
        PRIMARY_CONTACT_UNIT_ID, PRIMARY_CONTACT_BUIDLING_NAME, PRIMARY_CONTACT_FLOOR_ID, PRIMARY_CONTACT_PLOT_ID, */
        PRIMARY_CONTACT_SECTION, PRIMARY_CONTACT_CITY, PRIMARY_CONTACT_STATE, /* PRIMARY_CONTACT_STREET_TYPE,
        PRIMARY_CONTACT_STREET_NAME, */ PRIMARY_CONTACT_ZIP_CODE, /* PRIMARY_CONTACT_POSTAL_REGION_CODE, */
        PRIMARY_CONTACT_COUNTRY, PRIMARY_CONTACT_TIME_BEFORE, PRIMARY_CONTACT_TIME_AFTER, PRIMARY_CONTACT_ADDRESS_1,
        PRIMARY_CONTACT_ADDRESS_2, ACCOUNT_NAME, ACCOUNT_NUMBER, SALUTATION, TOTAL_DELQ_AMOUNT /*, BILLS_SERVICE_NO,
        BILLS_ACCOUNT_NO, BILLS_BILLING_DATE, BILLS_PAYMENT_BEFORE_DATE, BILLS_PREVIOUS_BALANCE,
        BILLS_CREDIT_ADJUSTMENT, BILLS_RENTAL, BILLS_CALLS, BILLS_SERVICE_TAX, BILLS_REBATE, BILLS_MESSAGE,
        BILLS_TOTAL_AMMOUNT_DUE, BILLS_TOTAL_MONTH_CHARGES */
    };
    
    public TransferObject getTemplateDesign(long templateId) {
      try {
        Map template = (Map) getReadOnlyProvider().queryObject("getTemplate", templateId);
        if (template == null) {
          return new TransferObject(TransferObject.ERROR, "MISSING_TEMPLATE");
        }
        Long doc = (Long) template.get(TemplateBusinessBean.DOCUMENT_ID);
        if (doc == null && template.get(TemplateBusinessBean.CONTENT) != null){
          return new TransferObject(TransferObject.ERROR, "CONTENT_TEMPLATE");
        } else if (doc == null){
          return new TransferObject(TransferObject.ERROR, "NO_TEMPLATE_DOCUMENT");
        }
        IDocumentService d = getDocumentService();
        String designText = d.getCharacterDocumentContent(doc, getReadOnlyProvider()).toString();
        return new TransferObject(designText);
      } catch (SQLException e) {
        log.error(e.getMessage(), e);
        return sqlFailure("Object", "getTemplate", templateId, e);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
        return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
      }
    }

    public TransferObject generateDocument(Long templateId, Map values) {
      try {
        IDataSourceConfiguration defaultDataSource = getDataSourceConfigurations().getDefaultDataSource();
        IReportGenerationService s = getReportGenerationService();
        TransferObject to = getTemplateDesign(templateId);
        if (to.isFailed()) {
          return to;
        }
        String designText = (String) to.getBeanHolder();
        String path = s.generateTemplate(templateId, designText, values, defaultDataSource);
        File f = new File(path);
        TransferObject transferObject = new TransferObject(FileUtil.readEntireTextFile(f, "UTF-8").toString());
        f.delete();
        return transferObject;
      } catch (SQLException e) {
        log.error(e.getMessage(), e);
        return sqlFailure("Object", "getTemplate", templateId, e);
      } catch (ReportCompilationException e) {
        log.error(e.getMessage(), e);
        return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
      } catch (ReportGenerationException e) {
        log.error(e.getMessage(), e);
        return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
      } catch (IOException e) {
        log.error(e.getMessage(), e);
        return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
      } catch (TransferObjectException e) {
        //
        return e.getTransferObject();
      }
    }

    private IReportGenerationService getReportGenerationService() {
      final IReportGenerationService repGenService = (IReportGenerationService) LookupManager
          .getInstance().getLookupItem(LookupManager.SYSTEM,
              "ReportGenerationService");
      return repGenService;
    }

    public TransferObject getDocument(Long documentId) {
      try {
        IDocumentService d = getDocumentService();
        StringBuffer sb = d.getCharacterDocumentContent(documentId, getReadWriteProvider());
        String content = sb.toString();
        return new TransferObject(content);
      } catch (Exception e) {
        return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
      }
    }
}
