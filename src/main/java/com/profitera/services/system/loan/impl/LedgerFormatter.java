package com.profitera.services.system.loan.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.profitera.log.ILogProvider;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.MapListUtil;

public class LedgerFormatter {
  private static final String TRANSACTION_ORDER = "TRANSACTION_ORDER";
  private static final String SPLIT_ORDER = "SPLIT_ORDER";
  private static final String CREDIT_AMOUNT = "CREDIT_AMOUNT";
  private static final String DEBIT_AMOUNT = "DEBIT_AMOUNT";
  private static final String TRANSACTION_CODE = "TRANSACTION_CODE";
  private static final String EXCHANGED_AMOUNT = "EXCHANGED_AMOUNT";
  private static final String ACCOUNT_CODE = "ACCOUNT_CODE";
  private static final String IS_GENERAL = "IS_GENERAL";
  private static final String TRANSACTION_ID = "TRANSACTION_ID";
  private boolean isNonSuspense;
  private boolean isSuspense;
  private boolean isAnyStatus;
  private boolean isEnteredStatus;
  private boolean isPostedStatus;
  private Object traceId;
  private final ILogProvider log;
  private final String firstTransactionArg;
  // We will need a feature to add a terminal trans too
  // or do we? You can just restrict the query itself, the start is harder 
  // because we need to run from the very start.
  //private final String lastTransactionArg;
  
  public LedgerFormatter(Object traceId, String mode, String status, String firstTransactionArg, ILogProvider log){
    this.firstTransactionArg = firstTransactionArg;
    this.log = log;
    if (status == null) {
      isAnyStatus = true;
    } else if (status.equals("P")){
      isPostedStatus = true;
    } else if (status.equals("E")){
      isEnteredStatus = true;
    } else {
      throw new IllegalArgumentException("Configured STATUS for ledger formatter illegal: " + status);
    }
    isNonSuspense = mode.equals("LEDGER");
    isSuspense = mode.equals("SUSPENSE");
    this.traceId = traceId;
  }
  
  private String getTrace() {
    return traceId + "";
  }
  
  private boolean isNonSuspenseProcessor() {
    return isNonSuspense;
  }

  private boolean isSuspenseProcessor() {
    return isSuspense;
  }

  public List<Map<String, Object>> format(Map arguments, List result) {
    Map runningBalances = new HashMap();
    TreeMap transactions = extractTransactions(result);
    List orderedIds = getTransOrder(transactions);
    Map sourceSplit = null;
    BigDecimal sourceDRAmount = BigDecimalUtil.ZERO;
    Set sourceSet = new HashSet();
    int orderedId = 0;
    for (Iterator i = orderedIds.iterator(); i.hasNext();) {
      Long t = (Long) i.next();
      orderedId++;
      List splits = (List) transactions.get(t);
      for (Iterator iter = splits.iterator(); iter.hasNext();) {
        Map s = (Map) iter.next();
        applyDebitOrCredit(s);
        updateRunningBalances(s, runningBalances);
        s.put(TRANSACTION_ORDER, new Integer(orderedId));
      }
      int tNo = 0;
      // if there is a general account it should be the*first split
      Map g = getGeneralSplit(splits);
      if (g != null){
        tNo = addSplitOrder(tNo, g);
        g.put(TRANSACTION_CODE, getGeneralTransactionCode(g));
      }
      Map o = getOverpaymentAdditionToPayment(splits);
      if (o != null){
        tNo = addSplitOrder(tNo, o);
        o.put(TRANSACTION_CODE, getOverpaymentAdditionTransactionCode(o, g));
      }
      Map source = g;
      if (source == null){
        source = o;
      }
      if (source != null){
        source.putAll(runningBalances);
      } else if (splits.size() > 0){
        for (Iterator iter = splits.iterator(); iter.hasNext();) {
          Map otherSplit = (Map) iter.next();
          if (isIncludedSplit(otherSplit)){
            otherSplit.putAll(runningBalances); 
            break;
          }
        }
      }
      if (source != null) {
        BigDecimal newSourceAmount = BigDecimalUtil.ZERO;
        for (Iterator iter = splits.iterator(); iter.hasNext();) {
          Map split = (Map) iter.next();
          if (split == source) continue;
          if (isIncludedSplit(split)){
            newSourceAmount = newSourceAmount.add(getSplitAmount(split));
          }
        }
        source.put(EXCHANGED_AMOUNT, newSourceAmount.multiply(BigDecimalUtil.NEG_ONE));
        applyDebitOrCredit(source);
      }
      for (Iterator iter = splits.iterator(); iter.hasNext();) {
        Map split = (Map) iter.next();
        if (!hasSplitOrder(split) && isIncludedSplit(split)){
          tNo = addSplitOrder(tNo, split);
          split.put(TRANSACTION_CODE, getStandardTransactionCode(split, source));
        }
      }
      if (source != null && getAccountCode(source).startsWith("9") && isIncludedTransaction(getTransactionId(source), transactions)){
        sourceSplit = source;
        sourceDRAmount = sourceDRAmount.add(getSplitAmount(sourceSplit));
        sourceSet.add(getTransactionId(source));
      }
    }
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map split = (Map) i.next();
      String trans = (String) split.get(TRANSACTION_CODE);
      String dc = split.get(DEBIT_AMOUNT) == null ? "C" : "D";
      split.put("T_CODE", dc + trans);
    }
    if (sourceSplit != null) {
      for (Iterator i = result.iterator(); i.hasNext();) {
        Map split = (Map) i.next();
        if (sourceSet.contains(getTransactionId(split))){
          i.remove();
        }
      }
      Map m = new HashMap();
      m.putAll(sourceSplit);
      m.put(TRANSACTION_ID, new Long(-1));
      m.put("T_CODE", "D" + "000900");
      m.put(TRANSACTION_CODE, "000900");
      m.put(EXCHANGED_AMOUNT, sourceDRAmount);
      m.put(SPLIT_ORDER, new Integer(1));
      applyDebitOrCredit(m);
      //m.put(DEBIT_AMOUNT, sourceDRAmount.multiply(BigDecimalUtil.NEG_ONE));
      result.add(0, m);
    }
    Long firstId = getFirstTransactionId(arguments);
    if (firstId != null){
      cutOffAtFirstTransaction(firstId, runningBalances, transactions, result);
    }
    if (!isAnyStatus()){
      for (Iterator i = result.iterator(); i.hasNext();) {
        Map s = (Map) i.next();
        String status = (String) getManditory("STATUS", String.class, s);
        if (isPostedStatus() && !status.equals("P")){
          i.remove();
        } else if (isEnteredStatus() && !status.equals("E")){
          i.remove();
        }
      }
    }
    return result;
  }

  private boolean isAnyStatus() {
    return isAnyStatus;
  }
  
  private boolean isPostedStatus() {
    return isPostedStatus;
  }
  private boolean isEnteredStatus() {
    return isEnteredStatus;
  }

  private List getTransOrder(final TreeMap transactions) {
    List ids = new ArrayList();
    ids.addAll(transactions.keySet());
    Collections.sort(ids, new Comparator(){
      public int compare(Object a, Object b) {
        List tA = (List) transactions.get(a);
        List tB = (List) transactions.get(b);
        Map sA = (Map) tA.get(0);
        Map sB = (Map) tB.get(0);
        Date postedA = getPostingDate(sA);
        Date postedB = getPostingDate(sB);
        if (postedA != null && postedB != null){
          return postedA.compareTo(postedB);
        } else if (postedA == null && postedB == null){
          return getTransactionId(sA).compareTo(getTransactionId(sB));
        } else {
          return postedA == null ? 1 : -1;
        }
      }});
    return ids;
  }
  
  private ILogProvider getLog() {
    return log;
  }

  private void cutOffAtFirstTransaction(Long firstId, Map runningBalances,
      TreeMap transactions, List result) {
    removeTransactionsUpToIncluding(firstId, result);
    List splits = (List) transactions.get(firstId);
    // If the firstId is not present we then assume that the following transaction
    // is the one we want, but I will issue a warning first:
    if (splits == null) {
      getLog().emit(LoanAccountSetLog.LOAN_LEDGER_FIRST_MISSING, getTrace(), firstId);
      Set s = transactions.keySet();
      Iterator iter = s.iterator();
      while (iter.hasNext()) {
        Long id = (Long) iter.next();
        if (id.compareTo(firstId) > 0) {
          firstId = id;
        }
      }
      splits = (List) transactions.get(firstId);
    }
    if (splits == null) {
      getLog().emit(LoanAccountSetLog.LOAN_LEDGER_FIRST_NO_SPLIT, getTrace(), firstId);
      return;
    }
    int firstSplit = MapListUtil.firstIndexOf(SPLIT_ORDER, new Integer(1), splits);
    Map runningSplit = (Map) splits.get(firstSplit);
    Collection accounts = runningBalances.keySet();
    BigDecimal total = getRunningBalanceTotalForSplit(runningSplit, accounts);
    if (!BigDecimalUtil.isEqual(total, BigDecimalUtil.ZERO)) {
      Map m = new HashMap();
      m.putAll(runningSplit);
      m.put(TRANSACTION_ID, new Long(-1));
      m.put("T_CODE", "D" + "000800");
      m.put(TRANSACTION_CODE, "000800");
      if (total.compareTo(BigDecimalUtil.ZERO) > 0){
        m.put(CREDIT_AMOUNT, null);
        m.put(DEBIT_AMOUNT, total);
      } else {
        m.put(CREDIT_AMOUNT, total.multiply(BigDecimalUtil.NEG_ONE));
        m.put(DEBIT_AMOUNT, null);
      }
      m.put(SPLIT_ORDER, new Integer(1));
      result.add(0, m);
    }
  }

  private BigDecimal getRunningBalanceTotalForSplit(Map runningSplit,
      Collection accounts) {
    BigDecimal total = BigDecimalUtil.ZERO;
    for (Iterator i = accounts.iterator(); i.hasNext();) {
      String account = (String) i.next();
      BigDecimal amount = null;
      if (isSuspenseProcessor()){
        if (isSuspenseCode(account)){
          amount = (BigDecimal) runningSplit.get(account);   
        }
      } else if (isNonSuspenseProcessor()){
        if (!isSuspenseCode(account)){
          amount = (BigDecimal) runningSplit.get(account);
        }
      } else { // ALL, means include everything
        amount = (BigDecimal) runningSplit.get(account);
      }
      if (amount != null){
        total = total.add(amount);
      }
    }
    return total;
  }

  private void removeTransactionsUpToIncluding(Long firstId, List result) {
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map s = (Map) i.next();
      Long tId = getTransactionId(s);
      if (tId.compareTo(firstId) <= 0){
        i.remove();
      }
    }
  }

  private Long getFirstTransactionId(Map arguments) {
    Object o = arguments.get(firstTransactionArg);
    if (o != null){
      if (!(o instanceof Long)){
        throw new IllegalArgumentException(firstTransactionArg + " argument must be of type " + Long.class.getName());
      }
    }
    return (Long) o;
  }

  private void applyDebitOrCredit(Map split) {
    BigDecimal splitAmount = getSplitAmount(split);
    if (isGeneral(split)){
      // Reverse the D/C for general split
      splitAmount = splitAmount.multiply(BigDecimalUtil.NEG_ONE);
    }
    if (splitAmount.compareTo(BigDecimalUtil.ZERO) > 0){
      split.put(DEBIT_AMOUNT, splitAmount);
      split.remove(CREDIT_AMOUNT);
    } else {
      split.remove(DEBIT_AMOUNT);
      split.put(CREDIT_AMOUNT, splitAmount.multiply(BigDecimalUtil.NEG_ONE));
    }
  }
  
  private void updateRunningBalances(Map split, Map runningBalances) {
    BigDecimal splitAmount = getSplitAmount(split);
    if (!isGeneral(split)){
      String code = getAccountCode(split);
      BigDecimal balance = getRunningBalance(code, runningBalances);
      BigDecimal currentBalance = balance.add(splitAmount);
      setRunningBalance(code, currentBalance, runningBalances);
    }
  }

  private void setRunningBalance(String code, BigDecimal newBalance, Map balances) {
    balances.put(code, newBalance);
  }

  private BigDecimal getRunningBalance(String code, Map runningBalances) {
    BigDecimal b = (BigDecimal) runningBalances.get(code);
    return b == null ? BigDecimalUtil.ZERO : b;
  }

  private Object getStandardTransactionCode(Map split, Map source) {
    String firstHalf = source == null ? "000" : getAccountCode(source);
    return firstHalf + getAccountCode(split);
  }

  private String getOverpaymentAdditionTransactionCode(Map over, Map general) {
    String oCode = getAccountCode(over);
    if (general == null){
      return "000" + oCode;
    } else {
      return oCode + getAccountCode(general);
    }
  }

  private Map getOverpaymentAdditionToPayment(List splits) {
    for (Iterator u = splits.iterator(); u.hasNext();) {
      Map s = (Map) u.next();
      String code = getAccountCode(s);
      if (code.equals(IAccountTypes.OVERPAY)){
        BigDecimal amt = getSplitAmount(s);
        if (amt.compareTo(BigDecimalUtil.ZERO) > 0){
          return s;
        }
      }
    }
    return null;
  }
  
  private Object getManditory(String field, Class type, Map split){
    Object value = getOptional(field, type, split);
    if (value == null){
      throw getMissingRequired(field);
    } else {
      return value;
    }
  }
  
  private Object getOptional(String field, Class type, Map split){
    Object value = split.get(field);
    if (value == null){
      return null;
    }
    if (type.isAssignableFrom(value.getClass())) {
      return value;
    }
    throw getWrongType(field, type, value);
  
  }

  private BigDecimal getSplitAmount(Map s) {
    return (BigDecimal) getManditory(EXCHANGED_AMOUNT, BigDecimal.class, s);
  }

  private IllegalArgumentException getWrongType(String field, Class expected, Object value) {
    return new IllegalArgumentException("Wrong type for " + field + ", expected " + expected.getName() + " but was " + value.getClass().getName());
  }

  private String getGeneralTransactionCode(Map g) {
    return "000" + getAccountCode(g);
  }
  
  private IllegalArgumentException getMissingRequired(String field){
    return new IllegalArgumentException("Missing required field from query " + field);
  }
  
  private String getAccountCode(Map split){
    return (String) getManditory(ACCOUNT_CODE, String.class, split);
  }

  private int addSplitOrder(int no, Map split) {
    split.put(SPLIT_ORDER, new Integer(no + 1));
    if (no == 0){
      Date postingDate = getPostingDate(split);
      split.put("DISPLAY_POSTING_DATE", postingDate);
      Date tDate = getTransactionDate(split);
      split.put("DISPLAY_TRANSACTION_DATE", tDate);
    }
    return no + 1;
  }
  
  private boolean hasSplitOrder(Map split) {
    return split.get(SPLIT_ORDER) != null;
  }


  private Date getPostingDate(Map split) {
    return (Date) getOptional("POSTING_DATE", Date.class, split);
  }
  
  private Date getTransactionDate(Map split) {
    return (Date) getManditory("TRANSACTION_DATE", Date.class, split);
  }

  private boolean isGeneral(Map split){
    Object isG = getManditory(IS_GENERAL, Boolean.class, split);
    return isG.equals(Boolean.TRUE);
  }
  
  private int getGeneralSplitIndex(List splits){
    for (int i = 0; i < splits.size(); i++) {
      if (isGeneral((Map) splits.get(i))){
        return i;
      }
    }
    return -1;
  }
  
  private Map getGeneralSplit(List splits) {
    int i = getGeneralSplitIndex(splits);
    return (Map) (i == -1 ? null : splits.get(i));
  }

  private TreeMap extractTransactions(List result) {
    TreeMap trans = new TreeMap();
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map split = (Map) i.next();
      Long transactionId = getTransactionId(split);
      List t = (List) trans.get(transactionId);
      if (t == null){
        t = new ArrayList();
        trans.put(transactionId, t);
      }
      t.add(split);
    }
    for (Iterator i = result.iterator(); i.hasNext();) {
      Map split = (Map) i.next();
      Long id = getTransactionId(split);
      if (!isIncludedTransaction(id, trans)){
        i.remove();
      } else if (!isIncludedSplit(split)){
        i.remove();
      }
    }
    return trans;
  }

  private boolean isIncludedSplit(Map split) {
    if (!isSuspenseProcessor() && !isNonSuspenseProcessor()) return true;
    // The question here is IF this trans is included, is this split included?
    // For that reason we can say that the general is always included, if
    // the whole transaction was not included then the general split would of course also be toast
    if (isGeneral(split)) return true;
    boolean isSuspense = isSuspenseSplit(split);
    if (isSuspenseProcessor()){
      return isSuspense;
    } else {
      return !isSuspense;
    }
  }

  private boolean isIncludedTransaction(Long id, TreeMap trans) {
    List splits = (List) trans.get(id);
    // If it is an "ALL" processor nothing is excluded
    if (!isSuspenseProcessor() && !isNonSuspenseProcessor()) return true;
    if (isSuspenseProcessor()){
      return isSusupenseTransaction(splits);
    } else {
      return isNonSusupenseTransaction(splits);
    }
  }
  
  private boolean isSuspenseSplit(Map s){
    String code = getAccountCode(s);
    return isSuspenseCode(code);
  }
  
  private boolean isSusupenseTransaction(List splits){
    for (Iterator i = splits.iterator(); i.hasNext();) {
      Map s = (Map) i.next();
      // If only the general transaction is non-suspense it
      // is still a suspense transaction
      if (isGeneral(s)) continue;
      if (isSuspenseSplit(s)) return true;
    }
    return false;
  }

  private boolean isSuspenseCode(String code) {
    return code.startsWith("7") || code.startsWith("97");
  }
  
  private boolean isNonSusupenseTransaction(List splits){
    for (Iterator i = splits.iterator(); i.hasNext();) {
      Map s = (Map) i.next();
      if (isGeneral(s)) continue;
      String code = getAccountCode(s);
      if (!isSuspenseCode(code)) return true;
    }
    return false;
  }

  private Long getTransactionId(Map split) {
    Long transactionId = (Long) getManditory(TRANSACTION_ID, Long.class, split);
    return transactionId;
  }

  protected String getDocumentation() {    
    return "";
  }

  protected String getSummary() {
    return "";
  }
}
