package com.profitera.services.system.financial;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class TransactionAction {
  private static final String UPDATE_PENDING_BALANCE = "updateFinancialAccountPendingBalance";
  private static final String UPDATE_POSTED_BALANCE = "updateFinancialAccountPostedBalance";
  private static final String UPDATE_POSTED_BALANCE_NEW_TRANS = "updateFinancialAccountNewPostedBalance";
  
  private static final String EFFECTIVE_DATE_ARG_NAME = "EFFECTIVE_DATE";
  private static final String POSTING_DATE_ARG_NAME = "POSTING_DATE";
  private static final String COMMODITY_ID_ARG_NAME = "COMMODITY_ID";
  private static final String TRANS_ID_ARG_NAME = "TRANSACTION_ID";
  private static final String TRANS_STATUS_ARG_NAME = "STATUS";
  private static final String INSERT_SPLIT = "insertFinancialSplit";
  private static final String UPDATE_SPLIT = "updateFinancialSplit";
  private static final String INSERT_TRANS = "insertFinancialTransaction";
  private static final String UPDATE_TRANS = "updateFinancialTransaction";
  
  private final Transaction transaction;
  private final Date effectiveDate;

  public TransactionAction(Transaction transaction, Date effectiveDate){
    if (transaction == null){
      throw new IllegalArgumentException("Target transaction can not be null");
    }
    this.transaction = transaction;
    if (effectiveDate == null){
      throw new IllegalArgumentException("Transaction update effective date can not be null");
    }
    this.effectiveDate = effectiveDate;
  }
  public Transaction post(IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    checkProvider(p);
    return postTransaction(this.transaction, p, t);
  }
  private Transaction postTransaction(Transaction trans,
      IReadWriteDataProvider p, ITransaction t) throws SQLException,
      AbortTransactionException {
    boolean isNew = isNewTransaction(trans);
    if (isNew){
      return insertTransaction(trans, Transaction.POSTED, p, t);
    } else {
      if (isPosted(trans, p)){
        throw new AbortTransactionException("Attempting to POST a posted transaction");
      } else {
        return updateTransaction(trans, Transaction.POSTED, p, t);  
      }
    }
  }
  private boolean isPosted(Transaction trans, IReadWriteDataProvider p) {
    //TODO: Should check the database here to really be sure
    return trans.getStatus() == Transaction.POSTED;
  }

  private boolean isNewTransaction(Transaction trans) {
    return trans.getId() == null;
  }
  
  public Transaction enter(IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    checkProvider(p);
    if (!isNewTransaction(this.transaction)){
      throw new AbortTransactionException("Attempted to enter an existing transaction");
    }
    return insertTransaction(this.transaction, Transaction.ENTERED, p, t);
  }

  public Transaction reverse(IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    checkProvider(p);
    if (this.transaction.getId() == null){
      throw new AbortTransactionException("Transactions must exist in database to be reversed");
    }
    Transaction original = this.transaction;
    if (!isPosted(transaction, p)){
      original = postTransaction(this.transaction, p, t);
    }
    // My original transaction is now posted, so I know any attached transactions
    // are also posted, now I ask attached reversal to be entered:
    Transaction[] reversalAttached = enterAttachedReversals(transaction, p, t);
    Transaction reversal = transaction.getReversal(reversalAttached);
    Transaction result = postTransaction(reversal, p, t);
    createReversalLinks(original, result, p, t);
    return result;
  }
  
  private Transaction[] enterAttachedReversals(Transaction original, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException{
    Transaction[] attached = getRelatedTransactionsFromDatabase(original, p, t);
    Transaction[] enteredAttachedReversal = new Transaction[attached.length];
    for(int i = 0; attached != null && i < attached.length; i++){
      Transaction[] attachedReversedAttached = enterAttachedReversals(attached[i], p, t);
      enteredAttachedReversal[i] = new TransactionAction(attached[i].getReversal(attachedReversedAttached), effectiveDate).enter(p, t);
      createReversalLinks(attached[i], enteredAttachedReversal[i], p, t);
    }
    return enteredAttachedReversal;
  }
    
  private void createReversalLinks(Transaction original, Transaction result, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put(EFFECTIVE_DATE_ARG_NAME, effectiveDate);
    args.put("ORIGINAL_TRANSACTION_ID", original.getId());
    args.put("REVERSAL_TRANSACTION_ID", result.getId());
    Split[] originalSplits = original.getSplits();
    for (int i = 0; i < originalSplits.length; i++) {
      Split o = originalSplits[i];
      Split r = result.getSplits()[i];
      Map<String, Object> splitArgs = new HashMap<String, Object>(args);
      splitArgs.put("ORIGINAL_SPLIT_ID", o.getId());
      splitArgs.put("REVERSAL_SPLIT_ID", r.getId());
      p.insert("insertFinancialReverseSplit", splitArgs, t);
    }
    p.insert("insertFinancialReverseTransaction", args, t);
    
  }
  private void checkProvider(IReadWriteDataProvider p) {
    if (p == null){
      throw new IllegalArgumentException("Transaction provider can not be null");
    }
  }
  private Transaction insertTransaction(Transaction transaction, int status, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    // If we are posting, post related, otherwise they would be already be entered
    if (status == Transaction.POSTED){
      Transaction[] related = transaction.getRelatedTransactions();
      if (related != null){
        for (int i = 0; i < related.length; i++) {
          updateTransaction(related[i], status, p, t);
        }
      }
    }
    String statement = status == Transaction.POSTED ? UPDATE_POSTED_BALANCE_NEW_TRANS : UPDATE_PENDING_BALANCE;
    String statusCharacter = status == Transaction.POSTED ? "P" : "E";
    Date postedDate = status == Transaction.POSTED ? effectiveDate : null;
    Split[] splits = transaction.getSplits();
    Long transactionId = insertTransactionRow(transaction, statusCharacter, postedDate, p, t);
    for (int i = 0; i < splits.length; i++) {
      Split split = splits[i];
      Long splitId = insertSplitRow(split, transactionId, statusCharacter, postedDate, statement, p, t);
      //TODO: This might not be the best strategy, querying to splits
      // back out would perform worse but ensure round-trip fidelity
      splits[i] = new Split(splitId, split.getAccount(), split.getAmount(), split.getCommodity(), split.getExchangedAmount());
    }
    // Now create the relationship links
    Transaction[] related = transaction.getRelatedTransactions();
    if (related != null){
      for (int i = 0; i < related.length; i++) {
        insertTransactionRelationship(transactionId, related[i], p, t);
      }
    }
    // I know the entry date is the effective date because I am creating it now
    return new Transaction(transactionId, status, effectiveDate, postedDate, transaction.getCommodity(), splits);
  }
  
  private Long insertSplitRow(Split split, Long transactionId,
      String statusCharacter, Date post, String updateAccountstatement, IReadWriteDataProvider p,
      ITransaction t) throws SQLException, AbortTransactionException {
    Object temp = null;
    Long splitId = null;
    try {
      Map<String, Object> args = getSplitArguments(transactionId, statusCharacter, post, split);
      temp = p.insert(INSERT_SPLIT, args, t);
      splitId = (Long) temp;
    } catch (ClassCastException e){
      throw ExceptionUtil.getClassCastAbort(INSERT_SPLIT, Long.class, temp, e);
    }
    Map<String, Object> updateArgs = new HashMap<String, Object>();
    updateArgs.put("SPLIT_ID", splitId);
    updateArgs.put("AMOUNT", split.getAmount());
    updateArgs.put("EXCHANGED_AMOUNT", split.getExchangedAmount());
    updateArgs.put("ACCOUNT_ID", split.getAccount().getId());
    p.update(updateAccountstatement, updateArgs, t);
    return splitId;
  }
  
  private void updateSplitRow(Split split, IReadWriteDataProvider p,
      ITransaction t) throws SQLException, AbortTransactionException {
    Map<String, Object> args = getSplitArguments(null, "P", effectiveDate, split);
    p.update(UPDATE_SPLIT, args, t);
    Map<String, Object> updateArgs = new HashMap<String, Object>();
    updateArgs.put("SPLIT_ID", split.getId());
    p.update(UPDATE_POSTED_BALANCE, updateArgs, t);
  }
  
  private Map<String, Object> getSplitArguments(Long transactionId, String statusCharacter,
      Date postingDate, Split split) {
    Map<String, Object> args = new HashMap<String, Object>();
    // Parent Transaction
    if (transactionId != null){
      args.put(TRANS_ID_ARG_NAME, transactionId);
    }
    args.put(TRANS_STATUS_ARG_NAME, statusCharacter);
    // Split
    args.put(COMMODITY_ID_ARG_NAME, split.getCommodity().getId());
    args.put("AMOUNT", split.getAmount());
    args.put("EXCHANGED_AMOUNT", split.getExchangedAmount());
    args.put("ACCOUNT_ID", split.getAccount().getId());
    // Date
    args.put(EFFECTIVE_DATE_ARG_NAME, effectiveDate);
    args.put(POSTING_DATE_ARG_NAME, postingDate);
    if (split.getId() != null){
      args.put("ID", split.getId());
    }
    return args;
  }
  private Long insertTransactionRow(Transaction transaction,
      String statusCharacter, Date postingDate, IReadWriteDataProvider p, ITransaction t)
      throws SQLException, AbortTransactionException {
    Object tempTransId = null;
    Long transactionId = null;
    try {
      Map<String, Object> args = new HashMap<String, Object>();
      args.put(TRANS_STATUS_ARG_NAME, statusCharacter);
      args.put(COMMODITY_ID_ARG_NAME, transaction.getCommodity().getId());
      args.put(EFFECTIVE_DATE_ARG_NAME, effectiveDate);
      args.put(POSTING_DATE_ARG_NAME, postingDate);
      tempTransId = p.insert(INSERT_TRANS, args, t);
      transactionId = (Long) tempTransId;
    } catch (ClassCastException e){
      throw ExceptionUtil.getClassCastAbort(INSERT_TRANS, Long.class, tempTransId, e);
    }
    return transactionId;
  }
  
  private Transaction updateTransaction(Transaction transaction, int status, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    // If we are posting then we post all related transactions
    // but if we are entering a transaction then there is nothing to
    // do, related transactions are always entered if the transaction
    // is already entered itself.
    if (status == Transaction.POSTED){
      Transaction[] related = getRelatedTransactionsFromDatabase(transaction, p, t);
      if (related != null){
        for (int i = 0; i < related.length; i++) {
          updateTransaction(related[i], status, p, t);
        }
      }
    }
    Split[] splits = transaction.getSplits();
    updateTransactionRow(transaction, p, t);
    for (int i = 0; i < splits.length; i++) {
      Split split = splits[i];
      // We don't need to rebuild the split because
      // the changes are only in the DB, it is really only
      // the transaction that changes
      updateSplitRow(split, p, t);
    }
    return new Transaction(transaction.getId(), status, transaction.getTransactionDate(), effectiveDate, transaction.getCommodity(), splits);
  }
    
  /**
   * @param trans
   * @param p
   * @param t
   * @return Transaction array that is never null
   * @throws AbortTransactionException
   * @throws SQLException
   */
  private Transaction[] getRelatedTransactionsFromDatabase(
      Transaction trans, IReadWriteDataProvider p, ITransaction t) 
      throws AbortTransactionException, SQLException {
    TransactionQuery q = new TransactionQuery();
    AccountAction aa = new AccountAction();
    return q.getAttachedTransactions(trans.getId(), aa, p, t);
  }
  private void insertTransactionRelationship(Long transactionId,
      Transaction toAttach, IReadWriteDataProvider p, ITransaction t) throws SQLException {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("TRANSACTION_ID", transactionId);
    args.put("ATTACHED_TRANSACTION_ID", toAttach.getId());
    p.insert("insertFinancialAttachedTransaction", args, t);
  }

  private void updateTransactionRow(Transaction transaction,
      IReadWriteDataProvider p, ITransaction t)
      throws SQLException, AbortTransactionException {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("ID", transaction.getId());
    args.put(TRANS_STATUS_ARG_NAME, "P");
    args.put(EFFECTIVE_DATE_ARG_NAME, effectiveDate);
    p.update(UPDATE_TRANS, args, t);
  }
}
