package com.profitera.services.system.loan;

import com.profitera.server.ServiceEngine;
import com.profitera.services.system.loan.impl.GeneralAccountService;

public class LoanAccountService extends GeneralAccountService implements ILoanAccountService {
  private Boolean isNeverWitholdingPrincipal = null;
  public LoanAccount getLoanAccount(Long loanAccountId){
    return new LoanAccount(loanAccountId, this, isNeverWitholdingPrincipal());
  }
  private boolean isNeverWitholdingPrincipal() {
    if (isNeverWitholdingPrincipal == null) {
      String prop = ServiceEngine.getProp("loanservice.withholdPrincipal", "false");
      isNeverWitholdingPrincipal = prop.equals("true");
    }
    return isNeverWitholdingPrincipal;
  }  
}
