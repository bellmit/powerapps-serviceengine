package com.profitera.services.business.agency;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.AgencyInvoiceServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IAgencyInvoice;
import com.profitera.descriptor.business.meta.IAgencyInvoiceAdjustment;
import com.profitera.descriptor.business.meta.IAgencyInvoiceRemark;
import com.profitera.descriptor.business.meta.IAgencyInvoiceTransactionGroupRel;
import com.profitera.descriptor.business.meta.ILedgerTransactionGroup;
import com.profitera.descriptor.business.meta.ILedgerTransactionGroupRel;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class AgencyInvoiceService extends ProviderDrivenService implements AgencyInvoiceServiceIntf {
	
	protected String INSERT_AGENCY_INVOICE_QUERY = "insertAgencyInvoice";
	protected String INSERT_AGENCY_INVOICE_ADJUSTMENT_QUERY = "insertAgencyInvoiceAdjustment";
	protected String INSERT_AGENCY_INVOICE_REMARK_QUERY = "insertAgencyInvoiceRemark";
	protected String UPDATE_AGENCY_INVOICE_QUERY = "updateAgencyInvoice";
	protected String GET_TRANSACTIONS_WITHIN_DATE_RANGE = "getTransactionsWithinDateRange";
	protected String INSERT_LEDGER_TRANSACTION_GROUP = "insertLedgerTransactionGroup";
	protected String INSERT_LEDGER_TRANSACTION_GROUP_REL = "insertLedgerTransactionGroupRel";
	protected String INSERT_AGENCY_INVOICE_TRANSACTION_GROUP_REL = "insertAgencyInvoiceTransactionGroupRel";
	protected String TRANSACTION_TERM_DESCRIPTION = "TRANSACTION_TERM_DESCRIPTION";
	protected String TRANSACTION_ID = "TRANSACTION_ID";
	
  private static final String AGENCY_INVOICE_UPDATE_FAILED = "AGENCY_INVOICE_UPDATE_FAILED";
  private static final String AGENCY_INVOICE_INSERT_FAILED = "AGENCY_INVOICE_INSERT_FAILED";

	public TransferObject updateInvoice(Map agencyInvoice, String userId) {
    List adjustments = (List) agencyInvoice.get(IAgencyInvoice.ADJUSTMENT_LIST);
    if (adjustments == null)
      adjustments = new ArrayList();
    IReadWriteDataProvider rwp = getReadWriteProvider();
    Long id = (Long) agencyInvoice.get(IAgencyInvoice.ID);
    if (id == null){
			try {
				id = insertNewInvoice(agencyInvoice, userId, rwp);
			} catch (AbortTransactionException e) {
			  return returnFailWithTrace(e.getMessage(), null, null, agencyInvoice, e);
			} catch (SQLException e) {
			  return returnFailWithTrace(AGENCY_INVOICE_INSERT_FAILED, null, null, agencyInvoice, e);
			}
    }
		else {
			try {
				updateInvoice(agencyInvoice, userId, rwp);
			} catch (AbortTransactionException e) {
			  return returnFailWithTrace(e.getMessage(), null, null, agencyInvoice, e);
			} catch (SQLException e) {
			  return returnFailWithTrace(AGENCY_INVOICE_UPDATE_FAILED, null, null, agencyInvoice, e);
			}
		}
    return new TransferObject(id);
	}

	private void updateInvoice(final Map agencyInvoice, String userId, final IReadWriteDataProvider rwp) throws AbortTransactionException, SQLException{
		agencyInvoice.put(getStatusUserIdToSet((Long)agencyInvoice.get(IAgencyInvoice.STATUS_ID)), userId);
		agencyInvoice.put(IAgencyInvoice.UPDATE_DATE, new Date());
		rwp.execute(new IRunnableTransaction(){
			public void execute(ITransaction t) throws SQLException, AbortTransactionException {
				Long agencyInvoiceId = (Long) agencyInvoice.get(IAgencyInvoice.ID);
				if (agencyInvoice.containsKey(IAgencyInvoice.ADJUSTMENT_LIST)) {
					List adjustments = (List)agencyInvoice.get(IAgencyInvoice.ADJUSTMENT_LIST);
					for (int i = 0; i < adjustments.size(); i++){
						Map adjustment = (Map)adjustments.get(i);
						if (adjustment.get(IAgencyInvoiceAdjustment.ID) == null) {
							adjustment.put(IAgencyInvoiceAdjustment.AGENCY_INVOICE_ID, agencyInvoiceId);
							rwp.insert(INSERT_AGENCY_INVOICE_ADJUSTMENT_QUERY, adjustment, t);
						}
					}
				}
				if (agencyInvoice.containsKey(IAgencyInvoice.REMARK_LIST)) {
					List remarks = (List)agencyInvoice.get(IAgencyInvoice.REMARK_LIST);
					for (int i = 0; i < remarks.size(); i++){
						Map remark = (Map)remarks.get(i);
						if (remark.get(IAgencyInvoiceRemark.ID) == null){
							remark.put(IAgencyInvoiceRemark.AGENCY_INVOICE_ID, agencyInvoiceId);
							remark.put(IAgencyInvoiceRemark.CREATION_DATE, new Date());
							rwp.insert(INSERT_AGENCY_INVOICE_REMARK_QUERY, remark, t);
						}
					}
				}
				rwp.update(UPDATE_AGENCY_INVOICE_QUERY, agencyInvoice, t);
			}
		});
	}

	//TODO: Check the transactions to be added to see whether they already belong to an
	//			alread active invoice/transaction group. If yes, they shouldn't be added with the new
	//			invoice. Guess for demo readiness this week, this can go out without that check.
	//			Must do it later.
	private Long insertNewInvoice(final Map agencyInvoice, String userId, final IReadWriteDataProvider rwp) throws AbortTransactionException, SQLException{
		if (agencyInvoice.get(IAgencyInvoice.STATUS_ID) == null)
			agencyInvoice.put(IAgencyInvoice.STATUS_ID, IAgencyInvoice.NEW_STATUS);
		agencyInvoice.put(getStatusUserIdToSet((Long)agencyInvoice.get(IAgencyInvoice.STATUS_ID)), userId);
		agencyInvoice.put(IAgencyInvoice.CREATION_DATE, new Date());
		Iterator transactions = rwp.query(IReadWriteDataProvider.LIST_RESULTS, GET_TRANSACTIONS_WITHIN_DATE_RANGE, agencyInvoice);
		final Map grouppedTrans = groupTransactions(transactions);
		final List ledgerTransactionGroups = buildLedgerTransactionGroups(grouppedTrans.keySet().iterator());
		rwp.execute(new IRunnableTransaction(){
			public void execute(ITransaction t) throws SQLException, AbortTransactionException {
				Long agencyInvoiceId = (Long) rwp.insert(INSERT_AGENCY_INVOICE_QUERY, agencyInvoice, t);
				agencyInvoice.put(IAgencyInvoice.ID, agencyInvoiceId);
				for (int i = 0; i < ledgerTransactionGroups.size(); i++) {
					Map ledgerTransactionGroup = (Map)ledgerTransactionGroups.get(i);
					String ltgDescription = (String)ledgerTransactionGroup.get(ILedgerTransactionGroup.DESCRIPTION);
					Long ltgId = (Long)rwp.insert(INSERT_LEDGER_TRANSACTION_GROUP, ledgerTransactionGroup, t);
					List ledgerTransactionGroupRels = buildLedgerTransactionGroupRels(ltgId, ltgDescription, grouppedTrans);
					for (int j = 0; j < ledgerTransactionGroupRels.size(); j++){
						rwp.insert(INSERT_LEDGER_TRANSACTION_GROUP_REL, (Map)ledgerTransactionGroupRels.get(j), t);
					}
					Map agencyInvoiceLedgerTransactionGroupRel = buildAgencyInvoiceLedgerTransactionGroupRel(agencyInvoiceId, ltgId);
					rwp.insert(INSERT_AGENCY_INVOICE_TRANSACTION_GROUP_REL, agencyInvoiceLedgerTransactionGroupRel, t);
				}
				if (agencyInvoice.containsKey(IAgencyInvoice.ADJUSTMENT_LIST)) {
					List adjustments = (List)agencyInvoice.get(IAgencyInvoice.ADJUSTMENT_LIST);
					for (int i = 0; i < adjustments.size(); i++){
						Map adjustment = (Map)adjustments.get(i);
						adjustment.put(IAgencyInvoiceAdjustment.AGENCY_INVOICE_ID, agencyInvoiceId);
						rwp.insert(INSERT_AGENCY_INVOICE_ADJUSTMENT_QUERY, adjustment, t);
					}
				}
				if (agencyInvoice.containsKey(IAgencyInvoice.REMARK_LIST)) {
					List remarks = (List)agencyInvoice.get(IAgencyInvoice.REMARK_LIST);
					for (int i = 0; i < remarks.size(); i++){
						Map remark = (Map)remarks.get(i);
						remark.put(IAgencyInvoiceRemark.AGENCY_INVOICE_ID, agencyInvoiceId);
						remark.put(IAgencyInvoiceRemark.CREATION_DATE, new Date());
						rwp.insert(INSERT_AGENCY_INVOICE_REMARK_QUERY, remark, t);
					}
				}
			}
		});
		return (Long)agencyInvoice.get(IAgencyInvoice.ID);
	}
	
	private String getStatusUserIdToSet(Long statusId){
		if (statusId.equals(IAgencyInvoice.RELEASED_STATUS))
			return IAgencyInvoice.RELEASED_STATUS_USER_ID;
		if (statusId.equals(IAgencyInvoice.REJECTED_STATUS))
			return IAgencyInvoice.REJECTED_STATUS_USER_ID;
		if (statusId.equals(IAgencyInvoice.APPROVED_STATUS))
			return IAgencyInvoice.APPROVED_STATUS_USER_ID;
		if (statusId.equals(IAgencyInvoice.PROCESSED_STATUS))
			return IAgencyInvoice.PROCESSED_STATUS_USER_ID;
		if (statusId.equals(IAgencyInvoice.PAYMENT_ACKNOWLEDGED_STATUS))
			return IAgencyInvoice.PAYMENT_ACKNOWLEDGED_STATUS_USER_ID;
		if (statusId.equals(IAgencyInvoice.RETRACTED_STATUS))
			return IAgencyInvoice.RETRACTED_STATUS_USER_ID;
		return IAgencyInvoice.NEW_STATUS_USER_ID;
	}
	
	private Map groupTransactions(Iterator transactions){
		Map grouppedTransactions = new HashMap();
		while (transactions.hasNext()) {
			Map transaction = (Map)transactions.next();
			String description = (String)transaction.get(TRANSACTION_TERM_DESCRIPTION);
			List transactionIds = (grouppedTransactions.containsKey(description) ? (List)grouppedTransactions.get(description) : new ArrayList());
			transactionIds.add(transaction.get(TRANSACTION_ID));
			grouppedTransactions.put(description, transactionIds);
		}
		return grouppedTransactions;
	}
	
	private List buildLedgerTransactionGroups(Iterator groupDescriptions){
		List ledgerTransactionGroups = new ArrayList();
		while (groupDescriptions.hasNext()){
			Map ledgerTransactionGroup = new HashMap();
			ledgerTransactionGroup.put(ILedgerTransactionGroup.DESCRIPTION, (String)groupDescriptions.next());
			ledgerTransactionGroup.put(ILedgerTransactionGroup.CREATION_DATE, new Date());
			ledgerTransactionGroups.add(ledgerTransactionGroup);
		}
		return ledgerTransactionGroups;
	}
	
	private List buildLedgerTransactionGroupRels(Long ltgId, String ltgDescription, Map grouppedTransactions){
		List ltgRels = new ArrayList();
		List transactionIds = (List)grouppedTransactions.get(ltgDescription);
		for (int i = 0; i < transactionIds.size(); i++) {
			Long transactionId = (Long)transactionIds.get(i);
			Map ltgRel = new HashMap();
			ltgRel.put(ILedgerTransactionGroupRel.LEDGER_TRANSACTION_ID, transactionId);
			ltgRel.put(ILedgerTransactionGroupRel.LEDGER_TRANSACTION_GROUP_ID, ltgId);
			ltgRels.add(ltgRel);
		}
		return ltgRels;
	}
	
	private Map buildAgencyInvoiceLedgerTransactionGroupRel(Long agencyInvoiceId, Long ledgerTransactionGroupId) {
		Map agencyInvoiceLedgerTransactionGroupRel = new HashMap();
		agencyInvoiceLedgerTransactionGroupRel.put(IAgencyInvoiceTransactionGroupRel.AGENCY_INVOICE_ID, agencyInvoiceId);
		agencyInvoiceLedgerTransactionGroupRel.put(IAgencyInvoiceTransactionGroupRel.TRANSACTION_GROUP_ID, ledgerTransactionGroupId);
		agencyInvoiceLedgerTransactionGroupRel.put(IAgencyInvoiceTransactionGroupRel.CREATION_DATE, new Date());
		return agencyInvoiceLedgerTransactionGroupRel;
	}
}