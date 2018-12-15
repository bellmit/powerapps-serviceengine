package com.profitera.services.system.loan.impl;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;

public class LoanAccountSetLog implements ILogClient {

  public static final String LOAN_LEDGER_FIRST_MISSING = "LOAN_LEDGER_FIRST_MISSING";
  public static final String LOAN_LEDGER_FIRST_NO_SPLIT = "LOAN_LEDGER_FIRST_NO_SPLIT";
  public String getName() {
    return "Loan Account Set";
  }

  public void registerMessages(ILogProvider provider) {
    provider.registerMessage(this, LOAN_LEDGER_FIRST_MISSING, Level.W, 
        "Loan ledger first transaction ID of {1} not found for {0}", 
        "Indicates that the first transaction that is expect as a cut off was not found, the next" +
        "highest transaction id will be used as the cutoff for the ledger start.");
    provider.registerMessage(this, LOAN_LEDGER_FIRST_NO_SPLIT, Level.W,
        "Loan ledger first split not found on or after {1}, no summary transaction will be produced for {0}",
        "Indicates that the first transaction is set and no splits are found aftwards so a summary transaction will not be produced.");
  }
}
