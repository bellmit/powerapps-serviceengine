package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IAgencyContract;
import com.profitera.descriptor.business.meta.IAgencyInvoice;
import com.profitera.descriptor.business.meta.IAgencyInvoiceTransactionGroupRel;
import com.profitera.descriptor.business.meta.ILedgerTransaction;
import com.profitera.descriptor.business.meta.ILedgerTransactionGroup;
import com.profitera.descriptor.business.meta.ILedgerTransactionGroupRel;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.IRecordDispenser;
import com.profitera.util.IteratorRecordDispenser;
import com.profitera.util.MapListUtil;

public class AgencyInvoiceBatch extends AbstractProviderBatch {

  public static final String COMMIT_SIZE = "commitsize";
  public static final String THREADS = "threads";

  private int mCommitSize = 1;
  private int mNoOfThreads = 1;
  
  private Date mInvoiceDate = new Date();
  private Date mInvoiceDueDate = new Date();
  private Long mInvoiceNewStatusId = new Long(1);
  private String mInvoiceNewStatusUserId = "Admin";
  
  public AgencyInvoiceBatch(){
    addProperty(COMMIT_SIZE, Integer.class, mCommitSize+"", "Commit size", "Commit size for transactions");
    addProperty(THREADS, Integer.class, mNoOfThreads+"", "No. of threads", "Number of threads for concurrent processing");
  }

  public TransferObject invoke() {
    return invoke(getIdentifier(), getEffectiveDate(), getEffectiveDate());
  }

  public TransferObject invoke(String identifier, Date startDate, Date endDate) {
  	mCommitSize = ((Integer)getPropertyValue(COMMIT_SIZE)).intValue();
  	mNoOfThreads = ((Integer)getPropertyValue(THREADS)).intValue();
    return generateInvoice(startDate, endDate);
  }

  public TransferObject generateInvoice(final Date startDate, final Date endDate) {
  	final IReadWriteDataProvider readWriter = getReadWriteProvider();
    //get the invoice date first
    try {
			Object invoiceDate = readWriter.queryObject("getInvoiceDate");
			if (invoiceDate != null)
				mInvoiceDate = (Date)invoiceDate;
		} catch (SQLException e1) {
			getLog().error("Error while quering for invoice date: " +  e1.getSQLState() + " - " + e1.getErrorCode() + " - " + e1.getMessage(), e1);
			getLog().error("Unable to query invoice date from database, will use current system date as invoice date.");
		} catch (Exception e) {
			getLog().error("Error while quering for invoice date: " +  e.getMessage(), e);
			getLog().error("Unable to query invoice date from database, will use current system date as invoice date.");
		}
		try {
			Object invoiceDueDate = readWriter.queryObject("getInvoiceDueDate");
			if (invoiceDueDate != null)
				mInvoiceDueDate = (Date)invoiceDueDate;
			} catch (SQLException e1) {
				getLog().error("Error while quering for invoice due date: " +  e1.getSQLState() + " - " + e1.getErrorCode() + " - " + e1.getMessage(), e1);
				getLog().error("Unable to query invoice due date from database, will use current system date as invoice date.");
			} catch (Exception e) {
				getLog().error("Error while quering for invoice due date: " +  e.getMessage(), e);
				getLog().error("Unable to query invoice due date from database, will use current system date as invoice date.");
			}
			try {
				Object invoiceNewStatusId = readWriter.queryObject("getInvoiceNewStatusId");
				if (invoiceNewStatusId != null)
					mInvoiceNewStatusId = (Long)invoiceNewStatusId;
			} catch (SQLException e1) {
				getLog().error("Error while quering for new invoice status id: " +  e1.getSQLState() + " - " + e1.getErrorCode() + " - " + e1.getMessage(), e1);
				getLog().error("Unable to query new invoice status id from database, will use 1 (presumed NEW) as invoice status id.");
			} catch (Exception e) {
				getLog().error("Error while quering for new invoice status id: " +  e.getMessage(), e);
				getLog().error("Unable to query new invoice status id from database, will use 1 (presumed NEW) as invoice status id.");
			}
			try {
				Object invoiceNewStatusUserId = readWriter.queryObject("getInvoiceNewStatusUserId");
				if (invoiceNewStatusUserId != null)
					mInvoiceNewStatusUserId = (String)invoiceNewStatusUserId;
			} catch (SQLException e1) {
				getLog().error("Error while quering for new invoice status user id: " +  e1.getSQLState() + " - " + e1.getErrorCode() + " - " + e1.getMessage(), e1);
				getLog().error("Unable to query new invoice status user id from database, will use Admin) as invoice status user id.");
			} catch (Exception e) {
				getLog().error("Error while quering for new invoice status user id: " +  e.getMessage(), e);
				getLog().error("Unable to query new invoice status user id from database, will use Admin as invoice status user id.");
			}

		Map parameter = new HashMap();
    parameter.put(IAgencyInvoice.START_DATE, startDate);
    parameter.put(IAgencyInvoice.END_DATE, endDate);
    Iterator iter = null;
    try {
    	iter = readWriter.query(IReadOnlyDataProvider.STREAM_RESULTS, "getUnbilledTransactions", parameter);
    } catch (SQLException e) {
			getLog().error("SQL Exception occured while retrieving eligible transaction up to " +  new SimpleDateFormat("yyyyMMdd").format(startDate));
			getLog().error("Error is:", e);
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    }
    Map grouppedByClient = MapListUtil.groupBy(iter, IAgencyContract.CLIENT_ID, null);
    Iterator clients = grouppedByClient.keySet().iterator();
    while (clients.hasNext()) {
    	final Long clientId = (Long)clients.next();
    	List byClientRecord = (List)grouppedByClient.get(clientId);
    	final Map grouppedByAgency = MapListUtil.groupBy(byClientRecord.iterator(), IAgencyInvoice.AGENCY_ID, null);
    	Iterator agencies = grouppedByAgency.keySet().iterator();
  		final IteratorRecordDispenser dispenser = new IteratorRecordDispenser(agencies, mCommitSize, getIdentifier());
    	Thread[] threads = new Thread[mNoOfThreads];
      for (int i = 0; i < threads.length; i++) {
        threads[i] = new Thread(new Runnable() {
          public void run() {
						try {
							generateInvoice(clientId, dispenser, grouppedByAgency, startDate, endDate, readWriter);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
          }
        });
        threads[i].setName(getIdentifier() +  "-client-" + clientId +  "-thread-" + (i+1));
        threads[i].start();
      }
      for (int i = 0; i < threads.length; i++) {
        try {
          threads[i].join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    	grouppedByClient.put(clientId, grouppedByAgency);
    }
    return null;
  }
    
    public void generateInvoice(Long clientId, IRecordDispenser dispenser, Map dataGrouppedByAgency, Date startDate, Date endDate, IReadWriteDataProvider readWriter) throws Exception {
    	List agencyIds = new ArrayList();
    	List runnableTrans = new ArrayList();
    	dispenser.dispenseRecords(agencyIds);
	    for (int i = 0; i < agencyIds.size(); i++) {
	  		Long agencyId = (Long)agencyIds.get(i);
	  		List byClientAndAgencyRecord = (List)dataGrouppedByAgency.get(agencyId);
	  		Map grouppedByDescription = MapListUtil.groupBy(byClientAndAgencyRecord.iterator(), "DESCRIPTION", null);
	  		IRunnableTransaction irt = getInvoiceGenerationTransaction(grouppedByDescription, startDate, endDate, readWriter);
	  		runnableTrans.add(irt);
	  	}
	    commitTransactions(runnableTrans, readWriter);
    }
    
    public IRunnableTransaction getInvoiceGenerationTransaction(Map grouppedByDescription, Date startDate, Date endDate, final IReadWriteDataProvider readWriter) throws SQLException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyDDD");
    	Iterator descriptions = grouppedByDescription.keySet().iterator();
    	final List ledgerTransactionGroups = new ArrayList();
    	final List ledgerTransactionGroupRelLists = new ArrayList();
    	Map tmpTransaction = null;
    	double amount = 0;
    	while (descriptions.hasNext()) {
    		String description = (String) descriptions.next();
    		Map aGroup = new HashMap();
    		aGroup.put(ILedgerTransactionGroup.DESCRIPTION, description);
    		ledgerTransactionGroups.add(aGroup);
    		List transactions = (List)grouppedByDescription.get(description);
    		List ledgerTransactionGroupRelsForDescription = new ArrayList();
    		for (int i = 0; i < transactions.size(); i++) {
    			Map transaction = (Map)transactions.get(i);
    			Map ledgerTransactionGroupRel = new HashMap();
    			ledgerTransactionGroupRel.put(ILedgerTransactionGroupRel.LEDGER_TRANSACTION_ID, transaction.get(ILedgerTransactionGroupRel.LEDGER_TRANSACTION_ID));
    			ledgerTransactionGroupRelsForDescription.add(ledgerTransactionGroupRel);
    			amount += ((Double)transaction.get(ILedgerTransaction.AMOUNT)).doubleValue();
    		}
    		ledgerTransactionGroupRelLists.add(ledgerTransactionGroupRelsForDescription);
    		if (tmpTransaction == null)
    			tmpTransaction = (Map)transactions.get(0);
    	}
    	final Map agencyInvoice = new HashMap();
    	agencyInvoice.put(IAgencyInvoice.AGENCY_INVOICE_NUMBER,  sdf.format(startDate) + sdf.format(endDate));
    	agencyInvoice.put(IAgencyInvoice.AGENCY_ID, tmpTransaction.get(IAgencyInvoice.AGENCY_ID));
    	agencyInvoice.put(IAgencyInvoice.CLIENT_ID, tmpTransaction.get(IAgencyInvoice.CLIENT_ID));
    	agencyInvoice.put(IAgencyInvoice.STATUS_ID, mInvoiceNewStatusId);
    	agencyInvoice.put(IAgencyInvoice.AGENCY_INVOICE_DATE, mInvoiceDate);
    	agencyInvoice.put(IAgencyInvoice.AGENCY_INVOICE_DUE_DATE, mInvoiceDueDate);
    	agencyInvoice.put(IAgencyInvoice.START_DATE, startDate);
    	agencyInvoice.put(IAgencyInvoice.END_DATE, endDate);
    	agencyInvoice.put(IAgencyInvoice.AMOUNT, new Double(amount));
    	agencyInvoice.put(IAgencyInvoice.NEW_STATUS_USER_ID, mInvoiceNewStatusUserId);
    	IRunnableTransaction tran = new IRunnableTransaction() {
  			public void execute(ITransaction t) throws SQLException, AbortTransactionException {
  				for (int i = 0; i < ledgerTransactionGroups.size(); i++) {
  					Long agencyInvoiceId = insertAgencyInvoice(agencyInvoice, readWriter, t);
  					Map ledgerTransactionGroup = (Map)ledgerTransactionGroups.get(i);
  					Long ledgerTransactionGroupId = insertLedgerTransactionGroup(ledgerTransactionGroup, readWriter, t);
  					List ledgerTransactionGroupRels = (List)ledgerTransactionGroupRelLists.get(i);
  					for (int j = 0; j < ledgerTransactionGroupRels.size(); j++) {
  						Map ledgerTransactionGroupRel = (Map) ledgerTransactionGroupRels.get(j);
  						ledgerTransactionGroupRel.put(ILedgerTransactionGroupRel.LEDGER_TRANSACTION_GROUP_ID, ledgerTransactionGroupId);
  						Long ledgerTransactionGroupRelId = insertLedgerTransactionGroupRel(ledgerTransactionGroupRel, readWriter, t);
  					}
  					Map agencyInvoiceTransactionGroupRel = new HashMap();
  					agencyInvoiceTransactionGroupRel.put(IAgencyInvoiceTransactionGroupRel.AGENCY_INVOICE_ID, agencyInvoiceId);
  					agencyInvoiceTransactionGroupRel.put(IAgencyInvoiceTransactionGroupRel.TRANSACTION_GROUP_ID, ledgerTransactionGroupId);
  					Long agencyInvoiceTransactionGroupRelId = insertAgencyInvoiceTransactionGroupRel(agencyInvoiceTransactionGroupRel, readWriter, t);
  				}
  			}
    	};
    	return tran;
    }
  
    private Long insertAgencyInvoice(Map invoice, IReadWriteDataProvider provider,	ITransaction t) throws AbortTransactionException, SQLException {
  		Long invoiceId = (Long) provider.insert("insertAgencyInvoice", invoice, t);
  		return invoiceId;
  	}

    private Long insertLedgerTransactionGroup(Map ledgerTransactionGroup, IReadWriteDataProvider provider,	ITransaction t) throws AbortTransactionException, SQLException {
  		Long ledgerTransactionGroupId = (Long) provider.insert("insertLedgerTransactionGroup", ledgerTransactionGroup, t);
  		return ledgerTransactionGroupId;
  	}
    
    private Long insertLedgerTransactionGroupRel(Map ledgerTransactionGroupRel, IReadWriteDataProvider provider,	ITransaction t) throws AbortTransactionException, SQLException {
  		Long ledgerTransactionGroupRelId = (Long) provider.insert("insertLedgerTransactionGroupRel", ledgerTransactionGroupRel, t);
  		return ledgerTransactionGroupRelId;
  	}
    
    private Long insertAgencyInvoiceTransactionGroupRel(Map agencyInvoiceTransactionGroupRel, IReadWriteDataProvider provider,	ITransaction t) throws AbortTransactionException, SQLException {
  		Long agencyInvoiceTransactionGroupRelId = (Long) provider.insert("insertAgencyInvoiceTransactionGroupRel", agencyInvoiceTransactionGroupRel, t);
  		return agencyInvoiceTransactionGroupRelId;
  	}

  public TransferObject generateInvoice(Long contractId) {
    return generateInvoice(contractId, getReadWriteProvider());
  }

  public TransferObject generateInvoice(Long contractId, IReadWriteDataProvider readWriter) {
		return new TransferObject();
  }
  
  private void commitTransactions(List transactions, IReadWriteDataProvider readWriter) throws AbortTransactionException, SQLException {
   	readWriter.execute(new RunnableTransactionSet((IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0])));
  }

	protected String getBatchDocumentation() {
		return "Batch program to generate invoices for agencies";
	}

	protected String getBatchSummary() {
		// TODO Auto-generated method stub
		return "Reads all the transactions between 2 given dates and groups them and associates the to an invoice which is newly created.";
	}
}