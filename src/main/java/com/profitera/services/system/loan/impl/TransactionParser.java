package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.financial.TransactionParserConfig;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Commodity;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.SplitReducer;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.financial.TransactionAction;
import com.profitera.services.system.loan.IAccountSet;
import com.profitera.services.system.loan.IGeneralAccountService;
import com.profitera.services.system.loan.SuspenseSplitRedirector;
import com.profitera.util.BigDecimalUtil;

public class TransactionParser {
  private final TransactionParserConfig parser;
  private final IAccountTypeProvider typeProvider;

  public TransactionParser(TransactionParserConfig conf, IAccountTypeProvider typeProvider) {
    this.parser = conf;
    this.typeProvider = typeProvider;
  }
  
  public Transaction parseTransaction(IGeneralAccountService loanAccountService, IAccountSet loan, Map target, Date date, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    Date transactionDate = getTransactionDate(target, date);
    return buildTransaction(loanAccountService, loan, target, transactionDate, date, p, t);
  }
  
  private Transaction buildTransaction(IGeneralAccountService loanAccountService, IAccountSet loan, Map target, Date transactionDate, Date today, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    List splits = (List) target.get(parser.getSplitKey());
    if (splits == null || splits.size() == 0){
      throw new AbortTransactionException("No splits specified for transaction to process in " + parser.getSplitKey());
    }
    String generalAccountType = getAccountCode("general account code", parser.getGeneralAccountTypeField(), target);
    boolean doRedirect = isRedirectingSuspense(target);
    return buildMultileggedTransaction(loanAccountService, loan, generalAccountType, transactionDate, 
        doRedirect, splits, p, t);
  }

  private Transaction buildMultileggedTransaction(IGeneralAccountService las, IAccountSet loan, String generalAccountType, 
      Date transactionDate, boolean redirectSplits, List splits, 
      IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    List transSplits = new ArrayList();
    BigDecimal total = BigDecimalUtil.ZERO;
    Commodity commodity = null;
    for (Iterator i = splits.iterator(); i.hasNext();) {
      Map split = (Map) i.next();
      String accountTypeCode = getAccountCode("account type code", parser.getAccountTypeKey(), split);
      AccountType splitType = typeProvider.get(accountTypeCode, p, t);
      Account a = loan.getSetFinancialAccount(splitType, p, t);
      commodity = a.getCommodity();
      BigDecimal amount = getAmount(parser.getAmountField(), split.get(parser.getAmountField()));
      Split s = new Split(a, amount, null, null);
      transSplits.add(s);
      total = total.add(amount);
    }
    if (!BigDecimalUtil.isEqual(total, BigDecimalUtil.ZERO)){
      AccountType genType = typeProvider.get(generalAccountType, p, t);      
      BigDecimal amount = total.multiply(BigDecimalUtil.NEG_ONE);
      Account generalAccount = las.getGeneralAccount(genType, p, t);
      Split s = new Split(generalAccount, amount, null, null);
      transSplits.add(s);
    }
    Split[] temp = (Split[]) transSplits.toArray(new Split[0]);
    if (redirectSplits){
      SplitReducer reducer = new SplitReducer();
      SuspenseSplitRedirector redir = new SuspenseSplitRedirector(loan, reducer.reduceSplits(temp));
      Transaction redirectTrans = redir.getRedirectionTransaction(p, t);
      if (redirectTrans != null){        
        temp = redir.getRevisedSplits(p, t);
        redirectTrans = new TransactionAction(redirectTrans, transactionDate).enter(p, t);
        return new Transaction(commodity ,reducer.reduceSplits(temp), new Transaction[]{redirectTrans});
      }
    }
    return new Transaction(commodity , temp);
  }


  private Date getTransactionDate(Map target, Date date) throws AbortTransactionException {
    String transDateKey = parser.getTransactionDateKey();
    return getTransactionDate(target, transDateKey, date);
  }

  public static Date getTransactionDate(Map target, String transDateKey, Date date)
      throws AbortTransactionException {
    Object tDate = target.get(transDateKey);
    if (tDate == null) return date;
    try {
      Date d = (Date) tDate;
      return d;
    } catch (ClassCastException e){
      throw getWrongTypeAbort("transaction date", transDateKey, tDate, Date.class);
    }
  }

  public static String getAccountCode(String message, String debitAccountKey, Map target) throws AbortTransactionException {
    Object o = target.get(debitAccountKey);
    if (o == null) {
      throw new AbortTransactionException("Required field value for " + message + " in " + debitAccountKey + " was not supplied");
    }
    try {
      return (String) o;
    } catch (ClassCastException e){
      throw getWrongTypeAbort(message, debitAccountKey, o, String.class);
    }
  }

  protected static AbortTransactionException getWrongTypeAbort(String name, String key, Object value, Class expectedType) {
    return new AbortTransactionException("Field value for " + name + " in " + key + " was not a " + expectedType.getName() + ", found " + value.getClass().getName());
  }
  
  public static BigDecimal getAmount(String amountKey, Object amt) throws AbortTransactionException {
    if (amt == null) {
      throw new AbortTransactionException("All transaction amount values are required, missing amount for " + amountKey);
    }
    try {
      return (BigDecimal) amt;
    } catch (ClassCastException e){
      throw getWrongTypeAbort("transaction amount", amountKey, amt, BigDecimal.class);
    }
  }
  
  private boolean isRedirectingSuspense(Map target) throws AbortTransactionException {
    if (parser.getRedirectKey() == null) return false;
    Object o = target.get(parser.getRedirectKey());
    if (o == null) {
      return false;
    }
    try {
      return ((Boolean) o).booleanValue();
    } catch (ClassCastException e){
      throw getWrongTypeAbort("suspense redirection", parser.getRedirectKey(), o, Boolean.class);
    }
  }

  public static boolean isPosting(Map target, String actionField) throws AbortTransactionException {
    Object id = target.get(actionField);
    if (id == null) {
      throw new AbortTransactionException("No action key defined in " + actionField + ", transactions must have post (P) or enter (E) actions indicated");
    }
    try {
      String action = (String) id;
      if (action.equals("P")){
        return true;
      } else if (action.equals("E")){
        return false;
      } else {
        throw new AbortTransactionException("Transaction action value of '" + action + "' found in " + actionField + " is illegal, permitted values are E and P");
      }
    } catch (ClassCastException e){
      throw getWrongTypeAbort("transaction action", actionField, id, String.class);
    }
  }
}
