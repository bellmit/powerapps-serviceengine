package com.profitera.services.business.query;

import java.util.List;
import java.util.Map;

import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.loan.impl.LedgerFormatter;
import com.profitera.services.system.loan.impl.LoanAccountSetLog;

public class LedgerFormattingQueryProcessor extends BaseListQueryProcessor {
  private static final String FIRST_TRANSACTION_ID = "FIRST_TRANSACTION_ID";
  private ILogProvider logProvider;
  
  public LedgerFormattingQueryProcessor(){
    addProperty("MODE", String.class, "ALL", 
        "Sets the mode of this processor's display of transactions", 
        "Controls whether the query processor displays all transactions (ALL), " +
        "regular account transactions (LEDGER), or suspense account transactions (SUSPENSE).");
    addProperty("STATUS", String.class, null, 
        "Status of transactions to display", "P for Posted or E for entered (pending).");
  }
  
  private ILogProvider getLogProvider() {
    if (logProvider == null) {
      logProvider = new DefaultLogProvider();
      logProvider.register(new LoanAccountSetLog());
    }
    return logProvider;
  }

  public List postProcessResults(Map arguments, List result, IQueryService qs)
      throws TransferObjectException {
    LedgerFormatter ledgerFormatter = new LedgerFormatter(qs.getId(), (String)getProperty("MODE"), (String)getProperty("STATUS"), FIRST_TRANSACTION_ID, getLogProvider());
    return ledgerFormatter.format(arguments, result);
  }
  
  protected String getDocumentation() {    
    return "See the detailed documentation for the loan ledger formatting server action.";
  }

  protected String getSummary() {
    return "Formats a loan ledger for client table display.";
  }
}
