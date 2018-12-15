package com.profitera.services.system.financial;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.QuerySpec;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.CollectionUtil;

public class TransactionQuery {
  private static final  Class L = Long.class;
  private static QuerySpec TRANS_SPEC = new QuerySpec("getFinancialTransaction", 
      new String[]{"ID", "COMMODITY_ID", "STATUS", "TRANSACTION_DATE", "POSTING_DATE", "SPLIT_ID", "ACCOUNT_ID", "SPLIT_COMMODITY_ID", "AMOUNT", "EXCHANGED_AMOUNT"}, 
      new Class[]{L, L, String.class, Date.class, Date.class, L, L, L, BigDecimal.class, BigDecimal.class});
  static {
    TRANS_SPEC.allowNull("POSTING_DATE");
  }
  private static QuerySpec ATTACHED_TRANS_ID_SPEC = 
    new QuerySpec("getFinancialAttachedTransactionIds", 
      new String[]{"ID"}, new Class[]{L});
  public Transaction[] getAttachedTransactions(Long id, AccountAction aa, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException{
    if (id == null){
      throw new IllegalArgumentException("ID of Transaction attached-to cannot be null");
    }
    if (p == null){
      throw new IllegalArgumentException("Provider can not be null");
    }
    List ids = CollectionUtil.asList(p.query(IReadWriteDataProvider.LIST_RESULTS, ATTACHED_TRANS_ID_SPEC.getName(), id));
    Transaction[] trans = new Transaction[ids.size()];
    for (int i = 0; i < trans.length; i++) {
      Map r = ATTACHED_TRANS_ID_SPEC.verifyResultInstance((Map)ids.get(i));
      Long tid = (Long) r.get(ATTACHED_TRANS_ID_SPEC.getFields()[0]);
      trans[i] = getTransaction(tid, aa, p, t);
    }
    return trans;
  }
  
  public Transaction getTransaction(Long id, AccountAction aa, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException{
    if (id == null){
      throw new IllegalArgumentException("ID of Transaction to retrieve cannot be null");
    }
    if (aa == null){
      throw new IllegalArgumentException("AccountAction instance is required to fetch split accounts");
    }
    if (p == null){
      throw new IllegalArgumentException("Provider can not be null");
    }
    //Split: ID  TRANSACTION_ID  ACCOUNT_ID  COMMODITY_ID  AMOUNT  EXCHANGED_AMOUNT  STATUS
    // Transaction: ID  COMMODITY_ID  STATUS  TRANSACTION_DATE  POSTING_DATE
    
    List splitMaps = CollectionUtil.asList(p.query(IReadWriteDataProvider.LIST_RESULTS, TRANS_SPEC.getName(), id));
    if (splitMaps.size() == 0){
      throw new IllegalArgumentException("No such transaction: " + id);
    }
    Map first = TRANS_SPEC.verifyResultInstance((Map) splitMaps.get(0));
    int status = first.get("STATUS").equals("P") ? Transaction.POSTED : Transaction.ENTERED;
    Date transDate = (Date) first.get("TRANSACTION_DATE");
    Date postDate = (Date) first.get("POSTING_DATE");
    Long commodity = (Long) first.get("COMMODITY_ID");
    Split[] splits = new Split[splitMaps.size()];
    for (int i = 0; i < splits.length; i++) {
      Map s = (Map) splitMaps.get(i);
      Long splitId = (Long) s.get("SPLIT_ID");
      BigDecimal amount = (BigDecimal) s.get("AMOUNT");
      BigDecimal xAmount = (BigDecimal) s.get("EXCHANGED_AMOUNT");
      Long comm = (Long) s.get("SPLIT_COMMODITY_ID");
      Long accountId = (Long) s.get("ACCOUNT_ID");
      Account a = aa.getAccount(accountId, p, t);
      splits[i] = new Split(splitId, a, amount, new Commodity(comm), xAmount);
    }
    Transaction trans = new Transaction(id, status, transDate, postDate, new Commodity(commodity), splits);
    return trans;
  }

}
