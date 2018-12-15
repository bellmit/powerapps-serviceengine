package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.profitera.descriptor.business.meta.IAgencyContractTerm;
import com.profitera.descriptor.business.meta.IAgencyContractTermLedgerTransactionRel;
import com.profitera.descriptor.business.meta.IAmountPerActionTerm;
import com.profitera.descriptor.business.meta.IAmountPerLegalProcedureTerm;
import com.profitera.descriptor.business.meta.ILedgerTransaction;
import com.profitera.descriptor.business.meta.IPercentageOfPaymentTerm;
import com.profitera.descriptor.business.meta.IPercentageOfPaymentTermDetail;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.CollectionUtil;
import com.profitera.util.DateParser;
import com.profitera.util.IRecordDispenser;
import com.profitera.util.IteratorRecordDispenser;
import com.profitera.util.MapListUtil;

public class AgencyTransactionBatch extends AbstractProviderBatch {

  public static final String COMMIT_SIZE = "commitsize";
  public static final String THREADS = "threads";

  private int mCommitSize = 1;
  private int mNoOfThreads = 1;
  
  private Date mLedgerTransactionPostingDate = new Date();
  private Date mLedgerTransactionReversalTransactionDate = new Date();
  
  public AgencyTransactionBatch(){
    addProperty(COMMIT_SIZE, Integer.class, mCommitSize+"", "Commit size", "Commit size for transactions");
    addProperty(THREADS, Integer.class, mNoOfThreads+"", "No. of threads", "Number of threads for concurrent processing");
  }

  public TransferObject invoke() {
  	mCommitSize = ((Integer)getPropertyValue(COMMIT_SIZE)).intValue();
  	mNoOfThreads = ((Integer)getPropertyValue(THREADS)).intValue();
    return generateTransactions(getEffectiveDate());
  }

  public TransferObject generateTransactions(Date date) {
    final IReadWriteDataProvider readWriter = getReadWriteProvider();
    //get the posting date first
    try {
			Object ledgerTransactionPostingDate = readWriter.queryObject("getLedgerTransactionPostingDate");
			if (ledgerTransactionPostingDate != null)
				mLedgerTransactionPostingDate = (Date)ledgerTransactionPostingDate;
		} catch (SQLException e1) {
			mLedgerTransactionPostingDate = date;
			getLog().error("Error while quering for posting date: " +  e1.getSQLState() + " - " + e1.getErrorCode() + " - " + e1.getMessage(), e1);
			getLog().error("Unable to query posting date from database, will use generation date as posting date.");
		} catch (Exception e) {
			mLedgerTransactionPostingDate = date;
			getLog().error("Error while quering for posting date: " +  e.getMessage(), e);
			getLog().error("Unable to query posting date from database, will use generation date as posting date.");
		}

    Map parameter = new HashMap();
    parameter.put(IAgencyContract.EFFECTIVE_DATE, date);
    parameter.put(IAgencyContract.EXPIRY_DATE, date);
    Iterator iter = null;
    try {
    	iter = readWriter.query(IReadOnlyDataProvider.LIST_RESULTS, "getActiveContractsWithinEffectiveDate", parameter);
    } catch (SQLException e) {
			getLog().error("SQL Exception occured while retrieving valid contract  for " +  new SimpleDateFormat("yyyyMMdd").format(date));
			getLog().error("Error is:", e);
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    }
    Map groupped = MapListUtil.groupBy(iter, IAgencyContract.AGENCY_ID, IAgencyContract.ID);
    Iterator keys = groupped.keySet().iterator();
    while (keys.hasNext()) {
    	Long agencyId = (Long)keys.next();
    	List contracts = (List)groupped.get(agencyId);
    	getLog().info("Processing agency " + agencyId + " with contracts " + contracts);
    	try {
				processContracts(agencyId, contracts, date, readWriter);
			} catch (SQLException e) {
				getLog().error("SQL Exception occured while processing contract transactions for agency " + agencyId, e);
			}
    }
    return new TransferObject(groupped);
  }
  
  public TransferObject reverseTransactions(final Long contractId) {
    final IReadWriteDataProvider readWriter = getReadWriteProvider();

    try {
			Object ledgerTransactionPostingDate = readWriter.queryObject("getLedgerTransactionPostingDate");
			if (ledgerTransactionPostingDate != null)
				mLedgerTransactionPostingDate = (Date)ledgerTransactionPostingDate;
		} catch (SQLException e1) {
			getLog().error("Error while quering for posting date: " +  e1.getSQLState() + " - " + e1.getErrorCode() + " - " + e1.getMessage(), e1);
			getLog().error("Unable to query posting date from database, will use current system date as posting date.");
		} catch (Exception e) {
			getLog().error("Error while quering for posting date: " +  e.getMessage(), e);
			getLog().error("Unable to query posting date from database, will use current system date as posting date.");
		}

    try {
			Object ledgerTransactionReversalTransactionDate = readWriter.queryObject("getLedgerTransactionReversalTransactionDate");
			if (ledgerTransactionReversalTransactionDate != null)
				mLedgerTransactionReversalTransactionDate = (Date)ledgerTransactionReversalTransactionDate;
		} catch (SQLException e1) {
			getLog().error("Error while quering for reversal transaction date: " +  e1.getSQLState() + " - " + e1.getErrorCode() + " - " + e1.getMessage(), e1);
			getLog().error("Unable to query reversal transaction date from database, will use current system date as transaction date.");
		} catch (Exception e) {
			getLog().error("Error while quering for reversal transaction date: " +  e.getMessage(), e);
			getLog().error("Unable to queryreversal transaction date from database, will use current system date as transaction date.");
		}

		try {
			generateReversalTransactions(contractId, readWriter);
		} catch (SQLException e) {
			getLog().error("SQL exception while trying to reverse transactions for contract " + contractId, e);
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
    } catch (Exception e) {
			getLog().error("Exception while trying to reverse transactions for contract " + contractId, e);
      return new TransferObject(TransferObject.EXCEPTION, e.getMessage());
		}

  	return new TransferObject();
  }

  private void generateReversalTransactions(Long contractId, final IReadWriteDataProvider readWriter) throws SQLException, Exception {
		final List popTransactions = new ArrayList();
		final List popTransactionInfos = new ArrayList();
  	Iterator pop = readWriter.query(IReadOnlyDataProvider.STREAM_RESULTS, "getPOPTermTransactionInfoForContract", contractId);
  	Long popTransactionTypeId = (Long)readWriter.queryObject("getPercentageOfPaymentTermLedgerReversalTransactionTypeId");
  	while (pop.hasNext()) {
    	Map tranInfo = (Map)pop.next();
    	Map reversed = getReversedTransaction(tranInfo, popTransactionTypeId);
    	popTransactions.add(reversed);
    	popTransactionInfos.add(tranInfo);
  	}

		final List lglTransactions = new ArrayList();
		final List lglTransactionInfos = new ArrayList();
    Iterator lgl = readWriter.query(IReadOnlyDataProvider.STREAM_RESULTS, "getLglProcTermTransactionInfoForContract", contractId);
    Long lglTransactionTypeId = (Long)readWriter.queryObject("getLegalProcedureTermLedgerReversalTransactionTypeId");
  	while (lgl.hasNext()) {
    	Map tranInfo = (Map)lgl.next();
    	Map reversed = getReversedTransaction(tranInfo, lglTransactionTypeId);
    	lglTransactions.add(reversed);
    	lglTransactionInfos.add(tranInfo);
  	}

		final List apaTransactions = new ArrayList();
		final List apaTransactionInfos = new ArrayList();
    Iterator apa = readWriter.query(IReadOnlyDataProvider.STREAM_RESULTS, "getApaTermTransactionInfoForContract", contractId);
    Long apaTransactionTypeId = (Long)readWriter.queryObject("getAmountPerActionTermLedgerReversalTransactionTypeId");
  	while (apa.hasNext()) {
    	Map tranInfo = (Map)apa.next();
    	Map reversed = getReversedTransaction(tranInfo, apaTransactionTypeId);
    	apaTransactions.add(reversed);
    	apaTransactionInfos.add(tranInfo);
  	}

		IRunnableTransaction tran = new IRunnableTransaction() {
			public void execute(ITransaction t) throws SQLException, AbortTransactionException {
				for (int i = 0; i < popTransactions.size(); i++) {
					Map reversed = (Map)popTransactions.get(i);
					Map reversedInfo = (Map)popTransactionInfos.get(i);
					Long reversedTransactionId = insertLedgerTransaction(reversed, readWriter, t);
					Long termLedgerTransactionRelId = insertContractTermLedgerTransactionRel(reversedTransactionId, (Long)reversedInfo.get(IAmountPerActionTerm.TERM_ID), readWriter, t);
					reversedInfo.put("LEDGER_TRANSACTION_ID", reversedTransactionId);
					Long popTermReversedTransactionInfoId = insertPercentageOfPaymentTermTransactionInfo(reversedInfo, readWriter, t);
				}
				for (int i = 0; i < lglTransactions.size(); i++) {
					Map reversed = (Map)lglTransactions.get(i);
					Map reversedInfo = (Map)lglTransactionInfos.get(i);
					Long reversedTransactionId = insertLedgerTransaction(reversed, readWriter, t);
					Long termLedgerTransactionRelId = insertContractTermLedgerTransactionRel(reversedTransactionId, (Long)reversedInfo.get(IAmountPerActionTerm.TERM_ID), readWriter, t);
					reversedInfo.put("LEDGER_TRANSACTION_ID", reversedTransactionId);
					Long lglTermReversedTransactionInfoId = insertLegalProcedureTermTransactionInfo(reversedInfo, readWriter, t);
				}
				for (int i = 0; i < apaTransactions.size(); i++) {
					Map reversed = (Map)apaTransactions.get(i);
					Map reversedInfo = (Map)apaTransactionInfos.get(i);
					Long reversedTransactionId = insertLedgerTransaction(reversed, readWriter, t);
					Long termLedgerTransactionRelId = insertContractTermLedgerTransactionRel(reversedTransactionId, (Long)reversedInfo.get(IAmountPerActionTerm.TERM_ID), readWriter, t);
					reversedInfo.put("LEDGER_TRANSACTION_ID", reversedTransactionId);
					Long apaTermReversedTransactionInfoId = insertAmtPerActionTermTransactionInfo(reversedInfo, readWriter, t);
				}
			}
		};
		readWriter.execute(tran);
  }

  private Map getReversedTransaction(Map tranInfo, Long transactionTypeId) {
  	Map reversed = new HashMap();
  	reversed.put(ILedgerTransaction.AMOUNT, new Double(((Double)tranInfo.get(ILedgerTransaction.AMOUNT)).doubleValue() * -1));
  	reversed.put(ILedgerTransaction.TRANSACTION_DATE, mLedgerTransactionReversalTransactionDate);
  	reversed.put(ILedgerTransaction.POSTING_DATE, mLedgerTransactionPostingDate);
  	reversed.put(ILedgerTransaction.TRANSACTION_TYPE_ID, transactionTypeId);
  	reversed.put(ILedgerTransaction.ACCOUNT_ID, tranInfo.get(ILedgerTransaction.ACCOUNT_ID));
  	return reversed;
  }

  private void processContracts(final Long agencyId, final List contracts, final Date date, final IReadWriteDataProvider readWriter) throws SQLException {
  	final Iterator accountRecords = readWriter.query(IReadOnlyDataProvider.STREAM_RESULTS, "getAgencyAccountIdsAsList", agencyId);
  	if (!accountRecords.hasNext())
  		return;
		final IteratorRecordDispenser dispenser = new IteratorRecordDispenser(accountRecords, mCommitSize, getIdentifier());
  	Thread[] threads = new Thread[mNoOfThreads];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(new Runnable() {
        public void run()  {
        	try {
						processContract(agencyId, contracts, dispenser, date, readWriter);
					} catch (SQLException e) {
          	getLog().fatal("Database related failure, please refer to error below.", e);
					} catch (Exception e) {
            getLog().fatal("Uknown exception, please refer to error below.", e);
					}
        }
      });
      threads[i].setName(getIdentifier() +  "-agency-" + agencyId +  "-thread-" + (i+1));
      threads[i].start();
    }
    for (int i = 0; i < threads.length; i++) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  private void processContract(Long agencyId, List contracts, IRecordDispenser dispenser, Date date, IReadWriteDataProvider readWriter) throws Exception{
		while (true) {
			List accountList = new ArrayList();
			dispenser.dispenseRecords(accountList);
			if (accountList.size() > 0) {
				List accountsTransactions = new ArrayList();
				for (int i = 0; i < contracts.size(); i++) {
					Long contractId = (Long)contracts.get(i);
					//this is where all the various term processing for transaction generation should happen
	  			accountsTransactions.addAll(processPercentageOfPaymentTerm(agencyId, contractId, accountList, date, readWriter));
	  			accountsTransactions.addAll(processLegalProcedureTerm(agencyId, contractId, accountList, date, readWriter));
	  			accountsTransactions.addAll(processAmountPerActionTerm(agencyId, contractId, accountList, date, readWriter));
	  			//after all the terms processed, we can execute the transactions
	  			commitTransactions(accountsTransactions, readWriter);
				}
			}
			else
				break;
		}
  }

  private void commitTransactions(List transactions, IReadWriteDataProvider readWriter) throws AbortTransactionException, SQLException {
   	readWriter.execute(new RunnableTransactionSet((IRunnableTransaction[]) transactions.toArray(new IRunnableTransaction[0])));
  }

  private List processLegalProcedureTerm(Long agencyId, Long contractId, List accounts, Date date, IReadWriteDataProvider readWriter) throws SQLException {
  	List transactions = new ArrayList();
  	List legalTerms = CollectionUtil.asList(readWriter.query(IReadOnlyDataProvider.LIST_RESULTS, "getAgencyContractLegalProcedureTermInfo", contractId));
  	if (legalTerms.size() == 0)
  		return transactions;
  	Map qArg1 = new HashMap();
  	qArg1.put("ACCOUNT_ID_LIST", accounts);
  	qArg1.put("PROCESSING_DATE", date);
  	Iterator accountLegalProcedures = readWriter.query(IReadOnlyDataProvider.STREAM_RESULTS, "getLegalProceduresForAccounts", qArg1);
  	while (accountLegalProcedures.hasNext()) {
  		Map accountLegalProcedure = (Map)accountLegalProcedures.next();
  		IRunnableTransaction transaction = applyLegalProcedureTermsToAccount(legalTerms, accountLegalProcedure, date, readWriter);
  		if (transaction != null)
  			transactions.add(transaction);
  	}
  	return transactions;
  }

  private List processAmountPerActionTerm(Long agencyId, Long contractId, List accounts, Date date, IReadWriteDataProvider readWriter) throws SQLException {
  	List transactions = new ArrayList();
  	List amtPerActionTerms = CollectionUtil.asList(readWriter.query(IReadOnlyDataProvider.LIST_RESULTS, "getAgencyContractAmountPerActionProcedureTermInfo", contractId));
  	if (amtPerActionTerms.size() == 0)
  		return transactions;
  	Map qArg1 = new HashMap();
  	qArg1.put("ACCOUNT_ID_LIST", accounts);
  	qArg1.put("PROCESSING_DATE", date);
  	Iterator accountActionExpenses = readWriter.query(IReadOnlyDataProvider.STREAM_RESULTS, "getActionsForAccounts", qArg1);
  	while (accountActionExpenses.hasNext()) {
  		Map accountActionExpense = (Map)accountActionExpenses.next();
  		IRunnableTransaction transaction = applyAmountPerActionTermsToAccount(amtPerActionTerms, accountActionExpense, date, readWriter);
  		if (transaction != null)
  			transactions.add(transaction);
  	}
  	return transactions;
  }

  private IRunnableTransaction applyAmountPerActionTermsToAccount(List amountPerActionTerms, Map accountActionExpense, Date date, final IReadWriteDataProvider readWriter) throws SQLException {
  	Long ledgerTransactionTypeId = (Long)readWriter.queryObject("getAmountPerActionTermLedgerTransactionTypeId");
  	Long accountId = (Long)accountActionExpense.get("ACCOUNT_ID");
  	final List ledgerTransactionList = new ArrayList();
  	final List ledgerTransactionInfoList = new ArrayList();
  	for (int i = 0; i < amountPerActionTerms.size(); i++) {
  		Map amountPerActionTerm = (Map)amountPerActionTerms.get(i);
  		Long termId = (Long)amountPerActionTerm.get(IAmountPerActionTerm.TERM_ID);
  		Double fixedCharge = (Double)amountPerActionTerm.get(IAmountPerActionTerm.FIXED_CHARGE);
  		Double maxCharge = (Double)amountPerActionTerm.get(IAmountPerActionTerm.MAXIMUM_EXPENSE_CHARGE);
  		Long actionSubtypeTypeId = (Long)amountPerActionTerm.get(IAmountPerActionTerm.ACTION_SUBTYPE_ID);
  		Long actionStatusTypeId = (Long)amountPerActionTerm.get(IAmountPerActionTerm.ACTION_STATUS_ID);
  		Double percentageOfExpenses = (Double)amountPerActionTerm.get(IAmountPerActionTerm.PERCENTAGE_OF_EXPENSE);
  		if (((Long)accountActionExpense.get(ITreatmentProcess.PROCESS_SUBTYPE_ID)).equals(actionSubtypeTypeId) &&
  				((Long)accountActionExpense.get(ITreatmentProcess.PROCESS_STATUS_ID)).equals(actionStatusTypeId))
  			{
	  			Map ledgerTransaction = new HashMap();
	  			double tmpAmount = 0;
	  			tmpAmount += fixedCharge != null ? fixedCharge.doubleValue() : 0;
	  			if (accountActionExpense.get("AMOUNT") != null && percentageOfExpenses != null)
		  			tmpAmount +=  ((Double)accountActionExpense.get("AMOUNT")).doubleValue() + percentageOfExpenses.doubleValue();
	  			
	  			Double amount = new Double(tmpAmount);
	  			if (maxCharge != null && maxCharge.doubleValue() >0 && amount.doubleValue() > maxCharge.doubleValue())
	  				amount = new Double(maxCharge.doubleValue());
	  			ledgerTransaction.put(ILedgerTransaction.ACCOUNT_ID, accountId);
	  			ledgerTransaction.put(ILedgerTransaction.AMOUNT, amount);
	  			Date transactionDate = date;
	  			ledgerTransaction.put(ILedgerTransaction.TRANSACTION_DATE, transactionDate);
	  			ledgerTransaction.put(ILedgerTransaction.POSTING_DATE, mLedgerTransactionPostingDate);
	  			ledgerTransaction.put(ILedgerTransaction.TRANSACTION_TYPE_ID, ledgerTransactionTypeId);
	  			ledgerTransaction.put(IAmountPerActionTerm.TERM_ID, amountPerActionTerm.get(IAmountPerActionTerm.TERM_ID));
	  			ledgerTransactionList.add(ledgerTransaction);
	  			Map legalProcedureTermTransactionInfo = new HashMap();
	  			legalProcedureTermTransactionInfo.put(IAmountPerActionTerm.TERM_ID,  amountPerActionTerm.get(IAmountPerActionTerm.TERM_ID));
	  			legalProcedureTermTransactionInfo.put("ACTION_EXPENSE_ID", accountActionExpense.get("ACTION_EXPENSE_ID"));
	  			legalProcedureTermTransactionInfo.put(IAmountPerActionTerm.FIXED_CHARGE, amountPerActionTerm.get(IAmountPerActionTerm.FIXED_CHARGE));
	  			legalProcedureTermTransactionInfo.put(IAmountPerActionTerm.PERCENTAGE_OF_EXPENSE, amountPerActionTerm.get(IAmountPerActionTerm.PERCENTAGE_OF_EXPENSE));
	  			legalProcedureTermTransactionInfo.put(IAmountPerActionTerm.MAXIMUM_EXPENSE_CHARGE, amountPerActionTerm.get(IAmountPerActionTerm.MAXIMUM_EXPENSE_CHARGE));
	  			legalProcedureTermTransactionInfo.put(IAmountPerActionTerm.ACTION_SUBTYPE_ID, actionSubtypeTypeId);
	  			legalProcedureTermTransactionInfo.put(IAmountPerActionTerm.ACTION_STATUS_ID,  actionStatusTypeId);
	  			legalProcedureTermTransactionInfo.put("DESCRIPTION", amountPerActionTerm.get("DESCRIPTION"));
	  			ledgerTransactionInfoList.add(legalProcedureTermTransactionInfo);
  			}
  	}
  	if (ledgerTransactionList.size() == 0)
  		return null;
  	IRunnableTransaction tran = new IRunnableTransaction() {
			public void execute(ITransaction t) throws SQLException, AbortTransactionException {
				for (int i = 0; i < ledgerTransactionList.size(); i++) {
					Map ledgerTransaction = (Map)ledgerTransactionList.get(i);
					Long ledgerTransactionId = insertLedgerTransaction(ledgerTransaction, readWriter, t);
					Long termLedgerTransactionRelId = insertContractTermLedgerTransactionRel(ledgerTransactionId, (Long)ledgerTransaction.get(IAmountPerActionTerm.TERM_ID), readWriter, t);
					Map amountPerActionTermTransactionInfo = (Map)ledgerTransactionInfoList.get(i);
					amountPerActionTermTransactionInfo.put("LEDGER_TRANSACTION_ID", ledgerTransactionId);
					Long amtPerActionTermTransactionInfoId = insertAmtPerActionTermTransactionInfo(amountPerActionTermTransactionInfo, readWriter, t);
				}
			}
  	};
  	return tran;
  }
  
  private boolean checkWithinReturnDays(Integer returnDays, Date eDate, Date cDate) {
  	if (returnDays == null || returnDays.intValue() <= 0)
  		return true;
  	Calendar cal1 = Calendar.getInstance();
  	Calendar cal2 = Calendar.getInstance();
  	cal1.setTime(eDate);
  	cal2.setTime(cDate);
  	return (DateParser.getDaysDifference(eDate, cDate) <= returnDays.intValue());
  }

  private IRunnableTransaction applyLegalProcedureTermsToAccount(List legalProcedureTerms, Map accountLegalProcedure, Date date, final IReadWriteDataProvider readWriter) throws SQLException {
  	Long ledgerTransactionTypeId = (Long)readWriter.queryObject("getLegalProcedureTermLedgerTransactionTypeId");
  	Long accountId = (Long)accountLegalProcedure.get("ACCOUNT_ID");
  	final List ledgerTransactionList = new ArrayList();
  	final List ledgerTransactionInfoList = new ArrayList();
  	for (int i = 0; i < legalProcedureTerms.size(); i++) {
  		Map legalTerm = (Map)legalProcedureTerms.get(i);
  		Long termId = (Long)legalTerm.get(IAmountPerLegalProcedureTerm.TERM_ID);
  		Long legalProcedureTypeId = (Long)legalTerm.get(IAmountPerLegalProcedureTerm.LEGAL_PROCEDURE_TYPE_ID);
  		Long fixedCharge = (Long)legalTerm.get(IAmountPerLegalProcedureTerm.FIXED_CHARGE);
  		Long maxCharge = (Long)legalTerm.get(IAmountPerLegalProcedureTerm.MAX_CHARGE);
  		Integer returnInDays = (Integer)legalTerm.get(IAmountPerLegalProcedureTerm.RETURN_IN_DAYS);
  		Double percentageOfExpenses = (Double)legalTerm.get(IAmountPerLegalProcedureTerm.PERCENTAGE_OF_EXPENSE);
  		if (((Long)accountLegalProcedure.get(IAmountPerLegalProcedureTerm.LEGAL_PROCEDURE_TYPE_ID)).equals(legalProcedureTypeId)
  				&& checkWithinReturnDays(returnInDays, (Date)accountLegalProcedure.get("ELIGIBLE_DATE"), (Date)accountLegalProcedure.get("COMPLETION_DATE")))
  		{
  			Map ledgerTransaction = new HashMap();
  			Double amount = new Double(fixedCharge.doubleValue() + (((Double)accountLegalProcedure.get("AMOUNT")).doubleValue() * percentageOfExpenses.doubleValue()));
  			if (amount.doubleValue() > maxCharge.doubleValue())
  				amount = new Double(maxCharge.doubleValue());
  			ledgerTransaction.put(ILedgerTransaction.ACCOUNT_ID, accountId);
  			ledgerTransaction.put(ILedgerTransaction.AMOUNT, amount);
  			Date transactionDate = (Date)accountLegalProcedure.get("UPDATE_DATE");
  			ledgerTransaction.put(ILedgerTransaction.TRANSACTION_DATE, transactionDate);
  			ledgerTransaction.put(ILedgerTransaction.POSTING_DATE, mLedgerTransactionPostingDate);
  			ledgerTransaction.put(ILedgerTransaction.TRANSACTION_TYPE_ID, ledgerTransactionTypeId);
  			ledgerTransaction.put(IAmountPerLegalProcedureTerm.TERM_ID, legalTerm.get(IAmountPerLegalProcedureTerm.TERM_ID));
  			ledgerTransactionList.add(ledgerTransaction);
  			Map legalProcedureTermTransactionInfo = new HashMap();
  			legalProcedureTermTransactionInfo.put(IAmountPerLegalProcedureTerm.TERM_ID, termId);
  			legalProcedureTermTransactionInfo.put("LEGAL_PROCEDURE_EXPENSE_ID", accountLegalProcedure.get("LEGAL_PROCEDURE_EXPENSE_ID"));
  			legalProcedureTermTransactionInfo.put("FIXED_CHARGE", legalTerm.get("FIXED_CHARGE"));
  			legalProcedureTermTransactionInfo.put("PERCENTAGE_OF_EXPENSE", legalTerm.get("PERCENTAGE_OF_EXPENSE"));
  			legalProcedureTermTransactionInfo.put("MAX_CHARGE", legalTerm.get("MAX_CHARGE"));
  			legalProcedureTermTransactionInfo.put("RETURN_IN_DAYS", legalTerm.get("RETURN_IN_DAYS"));
  			legalProcedureTermTransactionInfo.put("DESCRIPTION", legalTerm.get("DESCRIPTION"));
  			ledgerTransactionInfoList.add(legalProcedureTermTransactionInfo);
  		}
  	}
  	if (ledgerTransactionList.size() == 0)
  		return null;
  	IRunnableTransaction tran = new IRunnableTransaction() {
			public void execute(ITransaction t) throws SQLException, AbortTransactionException {
				for (int i = 0; i < ledgerTransactionList.size(); i++) {
					Map ledgerTransaction = (Map)ledgerTransactionList.get(i);
					Long ledgerTransactionId = insertLedgerTransaction(ledgerTransaction, readWriter, t);
					Long termLedgerTransactionRelId = insertContractTermLedgerTransactionRel(ledgerTransactionId, (Long)ledgerTransaction.get(IAmountPerLegalProcedureTerm.TERM_ID), readWriter, t);
					Map legalProcedureTermTransactionInfo = (Map)ledgerTransactionInfoList.get(i);
					legalProcedureTermTransactionInfo.put("LEDGER_TRANSACTION_ID", ledgerTransactionId);
					Long legalProcedureTermTransactionInfoId = insertLegalProcedureTermTransactionInfo(legalProcedureTermTransactionInfo, readWriter, t);
				}
			}
  	};
  	return tran;
  }

  private List processPercentageOfPaymentTerm(Long agencyId, Long contractId, List accounts, Date date, IReadWriteDataProvider readWriter) throws SQLException {
  	List transactions = new ArrayList();
  	List popTerms = CollectionUtil.asList(readWriter.query(IReadOnlyDataProvider.LIST_RESULTS, "getAgencyContractPercentageOfPaymentTermInfo", contractId));
  	if (popTerms.size() == 0)
  		return transactions;
  	Map qArg1 = new HashMap();
  	qArg1.put("ACCOUNT_ID_LIST", accounts);
  	qArg1.put("PROCESSING_DATE", date);
  	Iterator accountPayments = readWriter.query(IReadOnlyDataProvider.STREAM_RESULTS, "getPaymentsForAccounts", qArg1);
  	Map lastAccountRecord = null;
  	while (accountPayments.hasNext()) {
  		Map accountPayment = (Map)accountPayments.next();
  		if (lastAccountRecord == null || !lastAccountRecord.get("ACCOUNT_ID").equals(accountPayment.get("ACCOUNT_ID"))) {
  	  	Map qArg2 = new HashMap();
  	  	qArg2.put("ACCOUNT_ID", accountPayment.get("ACCOUNT_ID"));
  	  	qArg2.put("PROCESSING_DATE", date);
  	  	lastAccountRecord = (Map)readWriter.queryObject("getPercentageOfPaymentTermFieldsForAccount", qArg2);
  		}
  		IRunnableTransaction transaction = applyPOPTermsToAccount(popTerms, accountPayment, lastAccountRecord, date, readWriter);
  		if (transaction != null)
  			transactions.add(transaction);
  	}
  	return transactions;
  }
  
  private IRunnableTransaction applyPOPTermsToAccount(List popTerms, Map accountPayment, Map account, Date date, final IReadWriteDataProvider readWriter) throws SQLException {
  	Long ledgerTransactionTypeId = (Long)readWriter.queryObject("getPercentageOfPaymentTermLedgerTransactionTypeId");
  	Long accountId = (Long)account.get("ACCOUNT_ID");
  	final List ledgerTransactionList = new ArrayList();
  	final List ledgerTransactionInfoList = new ArrayList();
  	for (int i = 0; i < popTerms.size(); i++) {
  		Map popTerm = (Map)popTerms.get(i);
  		String termName = (String)popTerm.get(IAgencyContractTerm.NAME);
  		String fieldName = (String)popTerm.get(IPercentageOfPaymentTerm.FIELD_NAME);
  		String valueFieldName = (String)popTerm.get(IPercentageOfPaymentTerm.VALUE_FIELD_NAME);
  		Long startValue = (Long)popTerm.get(IPercentageOfPaymentTermDetail.START_VALUE);
  		Long endValue = (Long)popTerm.get(IPercentageOfPaymentTermDetail.END_VALUE);
  		Integer operator = (Integer)popTerm.get(IPercentageOfPaymentTermDetail.OPERATOR);
  		Double percentage = (Double)popTerm.get(IPercentageOfPaymentTermDetail.PERCENTAGE_OF_AMOUNT);
  		Long fieldValue = (Long)account.get(fieldName);
  		if (valueMeetsCondition(fieldValue, startValue, endValue, operator)) {
  			Map ledgerTransaction = new HashMap();
  			Double amount = new Double(((Double)accountPayment.get("AMOUNT")).doubleValue() * percentage.doubleValue());
  			Date transactionDate = (Date)accountPayment.get("TRANSACTION_DATE");
  			ledgerTransaction.put(ILedgerTransaction.ACCOUNT_ID, accountId);
  			ledgerTransaction.put(ILedgerTransaction.AMOUNT, amount);
  			ledgerTransaction.put(ILedgerTransaction.TRANSACTION_DATE, transactionDate);
  			ledgerTransaction.put(ILedgerTransaction.POSTING_DATE, mLedgerTransactionPostingDate);
  			ledgerTransaction.put(ILedgerTransaction.TRANSACTION_TYPE_ID, ledgerTransactionTypeId);
  			ledgerTransaction.put(IPercentageOfPaymentTerm.TERM_ID, popTerm.get(IPercentageOfPaymentTerm.TERM_ID));
  			ledgerTransactionList.add(ledgerTransaction);
  			Map popTermTransactionInfo = new HashMap();
  			popTermTransactionInfo.put("TRANSACTION_ID", accountPayment.get("TRANSACTION_ID"));
  			popTermTransactionInfo.putAll(popTerm);
  			popTermTransactionInfo.put("FIELD_VALUE", fieldValue);
  			ledgerTransactionInfoList.add(popTermTransactionInfo);
  		}
  	}
  	if (ledgerTransactionList.size() == 0)
  		return null;
  	IRunnableTransaction tran = new IRunnableTransaction() {
			public void execute(ITransaction t) throws SQLException, AbortTransactionException {
				for (int i = 0; i < ledgerTransactionList.size(); i++) {
					Map ledgerTransaction = (Map)ledgerTransactionList.get(i);
					Long ledgerTransactionId = insertLedgerTransaction(ledgerTransaction, readWriter, t);
					Long termLedgerTransactionRelId = insertContractTermLedgerTransactionRel(ledgerTransactionId, (Long)ledgerTransaction.get(IPercentageOfPaymentTerm.TERM_ID), readWriter, t);
					Map popTermTransactionInfo = (Map)ledgerTransactionInfoList.get(i);
					popTermTransactionInfo.put("LEDGER_TRANSACTION_ID", ledgerTransactionId);
					Long popTermTransactionInfoId = insertPercentageOfPaymentTermTransactionInfo(popTermTransactionInfo, readWriter, t);
				}
			}
  	};
  	return tran;
  }
  
  private boolean valueMeetsCondition(Long value, Long start, Long end, Integer operator) {
  	if (value == null)
  		return false;
  	if (end != null)
  		return (value.longValue() >= start.longValue() && value.longValue() <= end.longValue());
  	if (operator.equals(IPercentageOfPaymentTermDetail.GREATER_THAN_OPERATOR))
  		return value.longValue() > start.longValue();
  	if (operator.equals(IPercentageOfPaymentTermDetail.LESS_THAN_OPERATOR))
  		return value.longValue() < start.longValue();
  	if (operator.equals(IPercentageOfPaymentTermDetail.EQUALS_TO_OPERATOR))
  		return value.longValue() == start.longValue();
  	return false;
  }

  private Long insertLedgerTransaction(Map tranDetails, IReadWriteDataProvider provider,	ITransaction t) throws AbortTransactionException, SQLException {
		Long transactionId = (Long) provider.insert("insertLedgerTransaction", tranDetails, t);
		return transactionId;
	}
  
  private Long insertContractTermLedgerTransactionRel(Long transactionId, Long termId, IReadWriteDataProvider provider,	ITransaction t) throws AbortTransactionException, SQLException {
  	Map args = new HashMap();
  	args.put(IAgencyContractTermLedgerTransactionRel.CONTRACT_TERM_ID, termId);
  	args.put(IAgencyContractTermLedgerTransactionRel.LEDGER_TRANSACTION_ID, transactionId);
  	Long relId = (Long) provider.insert("insertTermLedgerTransactionRel", args, t);
  	return relId;
  }

  private Long insertPercentageOfPaymentTermTransactionInfo(Map info, IReadWriteDataProvider provider,	ITransaction t) throws AbortTransactionException, SQLException {
		Long infoId = (Long) provider.insert("insertPOPTermLedgerTransactionInfo", info, t);
		return infoId;
	}
  
  private Long insertLegalProcedureTermTransactionInfo(Map info, IReadWriteDataProvider provider,	ITransaction t) throws AbortTransactionException, SQLException {
		Long infoId = (Long) provider.insert("insertLegalProcTermLedgerTransactionInfo", info, t);
		return infoId;
	}

  private Long insertAmtPerActionTermTransactionInfo(Map info, IReadWriteDataProvider provider,	ITransaction t) throws AbortTransactionException, SQLException {
		Long infoId = (Long) provider.insert("insertAmtPerActionTermTransactionInfo", info, t);
		return infoId;
	}

	protected String getBatchDocumentation() {
		return "Batch program to generate transactions for agencies";
	}

	protected String getBatchSummary() {
		return "This batch program generates transaction entries (PTRTRANSACTION) based on the contract for any 2 given dates OR for 1 given date (on the day).";
	}

}