package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.financial.PostingScheduleParseConfig;
import com.profitera.financial.PostingScheduleParseConfig.EntryParseConfig;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.IScheduleManager;
import com.profitera.services.system.loan.impl.PostingSchedule;
import com.profitera.services.system.loan.impl.PostingScheduleParser;
import com.profitera.services.system.loan.impl.PostingSchedule.PostingType;
import com.profitera.services.system.lookup.LookupManager;

public class PostingScheduleTreatmentProcessActivity extends
    ConditionalTreatmentProcessActivity {
  private static final String STARTDATE = "startdate";
  private static final String ENDDATE = "enddate";
  private static final String SCHEDULETYPE = "scheduletype";
  private static final String SCHEDULELIST = "schedulelist";
  private static final String ENTRYDATE = "entrydate";
  private static final String ENTRYPOSTINGTYPE = "entrypostingtype";
  private static final String ENTRYRATE = "entryrate";
  private static final String ENTRYCYCLE = "entrycycle";
  //
  private PostingScheduleParseConfig conf;

  public void setProperties(Map conditions) {
    super.setProperties(conditions);
    EntryParseConfig e = new EntryParseConfig(getRequiredProperty(ENTRYDATE), 
        getRequiredProperty(ENTRYRATE), 
        getRequiredProperty(ENTRYPOSTINGTYPE), getProperty(ENTRYCYCLE));
    conf = new PostingScheduleParseConfig(getProperty(STARTDATE), getProperty(ENDDATE), 
        getRequiredProperty(SCHEDULETYPE), getRequiredProperty(SCHEDULELIST), e);
  }

  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p)
      throws SQLException, AbortTransactionException {
    PostingScheduleParser parser = new PostingScheduleParser(conf);
    PostingType pType = parser.getScheduleType(target);
    PostingSchedule s = parser.parse(target);
    LoanAccount loanAccount = getLoanAccountService().getLoanAccount(accountId);
    IScheduleManager m = loanAccount.getPostingScheduleManager(pType, p, t);
    Long id = m.addPostingSchedule(s, p, t);
    String idfield = getProperty("scheduleid");
    if (idfield != null) {
      target.put(idfield, id);
    }
  }    
    
  protected ILoanAccountService getLoanAccountService(){
    final ILoanAccountService provider = (ILoanAccountService) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "LoanAccountService");
    return provider;
  }
}
