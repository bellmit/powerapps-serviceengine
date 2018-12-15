package com.profitera.services.business.batch.financial.impl;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;

public class LedgerPenaltyLogClient implements ILogClient {

  public static final String NO_PERIOD = "LEDGER_PEN_NO_PERIOD";
  public static final String FORGIVE_INIT = "LEDGER_PEN_FORGIVE_INIT";
  public static final String ALREADY_POSTED = "LEDGER_PEN_ALREADY_POSTED";
  public static final String PAID_WITHIN_GRACE = "LEDGER_PEN_PAID_GRACE";
  public static final String NOT_DUE = "LEDGER_PEN_NOT_DUE";

  @Override
  public void registerMessages(ILogProvider p) {
    p.registerMessage(this, NO_PERIOD, Level.D, "Penalty computation not required, no billing period before {0" + DATE_FORMAT + "} for ledger {1" + LONG_FORMAT + "}",
        "Processing found no penalty posting schedule period in the time period under consideration.");
    p.registerMessage(this, FORGIVE_INIT, Level.D, "Penalty forgiven in intial partial billing period on {0" + DATE_FORMAT + "} for ledger {1" + LONG_FORMAT + "}",
        "The first penalty period for a new ledger's processing is always forgiven.");
    p.registerMessage(this, ALREADY_POSTED, Level.D, "Penalty already posted for billing period on {0" + DATE_FORMAT + "} for ledger {1" + LONG_FORMAT + "}",
        "The penalty period under consideration was already posted and will not be re-posted.");
    p.registerMessage(this, PAID_WITHIN_GRACE, Level.I, "Penalty forgiven for payment on or before grace date of {0} for billing period ending on {1" + DATE_FORMAT + "} for ledger {2" + LONG_FORMAT + "}",
        "The penalty-applicable accounts were paid in full before the grace date passed in the penalty period.");
    p.registerMessage(this, NOT_DUE, Level.D, "Penalty computation not required for ledger {0" + LONG_FORMAT + "} for {1" + DATE_FORMAT + "}, next due on {2" + DATE_FORMAT + "}",
        "Penalty period does terminate on effective date and is not due to be posted.");
  }

  @Override
  public String getName() {
    return "Ledger Penalty Interest Processing";
  }
}
