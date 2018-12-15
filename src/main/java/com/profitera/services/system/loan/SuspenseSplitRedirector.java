/**
 * 
 */
package com.profitera.services.system.loan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.Transaction;
import com.profitera.util.BigDecimalUtil;

public class SuspenseSplitRedirector {
  private final Split[] original;
  private final IAccountSet loanAccount;
  private Split[] revised;
  private Transaction transaction;

  public SuspenseSplitRedirector(IAccountSet a, Split[] original) {
    this.loanAccount = a;
    this.original = original;
  }
  
  public Split[] getRevisedSplits(IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException{
    if (revised == null){
      Split[] replaced = new Split[original.length];
      Map suspenseAmounts = new HashMap();
      Map suspenseLedgerAccounts = new HashMap();
      for (int i = 0; i < replaced.length; i++) {
        if (loanAccount.isSuspense(original[i], p, t)){
          Account suspAccount = original[i].getAccount();
          BigDecimal d = original[i].getAmount();
          Account ledgerAccount = loanAccount.getLedgerAccount(suspAccount, p, t);
          suspenseLedgerAccounts.put(suspAccount, ledgerAccount);
          replaced[i] = new Split(ledgerAccount, d, null, null);
          BigDecimal current = (BigDecimal) suspenseAmounts.get(suspAccount);
          suspenseAmounts.put(suspAccount, current == null ? d : current.add(d));
        } else {
          replaced[i] = original[i];
        }
      }
      if (suspenseLedgerAccounts.size() > 0) {
        List splits = new ArrayList();
        for (Iterator i = suspenseLedgerAccounts.entrySet().iterator(); i.hasNext();) {
          Map.Entry e = (Map.Entry) i.next();
          Account susp = (Account) e.getKey();
          Account ledger = (Account) e.getValue();
          BigDecimal amount = (BigDecimal) suspenseAmounts.get(susp);
          splits.add(new Split(susp, amount, null, null));
          splits.add(new Split(ledger, amount.multiply(BigDecimalUtil.NEG_ONE), null, null));
        }
        Split[] redirection = (Split[]) splits.toArray(new Split[0]);
        Transaction trans = new Transaction(redirection[0].getCommodity(), redirection);
        this.transaction = trans;
      }
      revised = replaced;
    }
    return revised;
  }
  
  public Transaction getRedirectionTransaction(IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException{
    getRevisedSplits(p, t);
    return transaction;
  }
  
  
}