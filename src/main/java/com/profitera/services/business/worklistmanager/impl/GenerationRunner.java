package com.profitera.services.business.worklistmanager.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.map.GroupingMapIterator;
import com.profitera.services.business.worklistmanager.IWorkListGenerator;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class GenerationRunner {
  private static Log LOG;
  private int accountCount = 0;
  private int customerCount = 0;
  
  public void executeGeneration(String[] keys, Iterator accounts, IWorkListGenerator generator,
      TransactionSetRunner exe, int transactionSize, IReadWriteDataProvider provider) {
    if (accountCount > 0) {
      throw new IllegalStateException("Generation runner has already been executed");
    }
    List transactions = new ArrayList(4);
    GroupingMapIterator iter = new GroupingMapIterator(keys, accounts);
    while(iter.hasNext()) {
      List thisCustomer = iter.next();
      if (thisCustomer.size() > 0){
        // This transaction might actually be a Transaction set, i.e. account-based generation
        // actually creates one for each account and puts them in a set that is returned.
        IRunnableTransaction customerTrans = generator.process(thisCustomer, provider); 
        transactions.add(customerTrans);
        customerCount++;
        accountCount = accountCount + thisCustomer.size();
      }
      
      if (customerCount % transactionSize == 0){
        exe.executeUpdates(provider, transactions);
      }
    }
    exe.executeUpdates(provider, transactions);
    long waitStart = System.currentTimeMillis();
    exe.waitForUpdates();
    getLog().info(accountCount + " rows processed");
    getLog().info(customerCount + " groups processed");
    getLog().info((System.currentTimeMillis() - waitStart) + " ms spent waiting for transactions");
  }
  
  private Log getLog() {
    if (LOG == null) {
      LOG = LogFactory.getLog(getClass());
    }
    return LOG;
  }

  public int getAccountCount() {
    return accountCount;
  }

  public int getCustomerCount() {
    return customerCount;
  }
}
