package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.batch.financial.impl.GeneralAccountInitializer;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.ILoanAccountService;

public class InitializeGeneralAccountsBatch extends AbstractFinancialBatch {
  private static final String EXTENDEDACCOUNTTYPES = "extendedaccounttypes";
  private static final String[] ACCOUNT_TYPE_CODES = {
    IAccountTypes.PRINCIPAL, 
    //IAccountTypes.SRC_PINST - no general account for this, it is transferred from P 
    IAccountTypes.CHARGE, IAccountTypes.INTEREST, 
    IAccountTypes.PENALTY, IAccountTypes.CUMULATIVE_IIS,
    IAccountTypes.PAYMENT, IAccountTypes.IIS, 
    IAccountTypes.PIS, IAccountTypes.CIS,
    // Source Bank accounts
    IAccountTypes.SRC_C, IAccountTypes.SRC_CIS,
    IAccountTypes.SRC_I, IAccountTypes.SRC_IIS,
    IAccountTypes.SRC_PN, IAccountTypes.SRC_PIS,
    IAccountTypes.SRC_P, IAccountTypes.SRC_PINST 
  };
  
  {
    addRequiredProperty(CURRENCYCODE, String.class, 
        CURRENCYCODE_SHORT_DOC, CURRENCYCODE_LONG_DOC);
    addListProperty(EXTENDEDACCOUNTTYPES, String.class, null, 
        "Extended account types to add in addition to the default account " +
        "types as created by this batch",
        "This batch will add new general accounts as required based on the account " +
        "types specified by this property in addition to the account types that " +
        "are absolutely required by the system.");
  }

  protected String getBatchDocumentation() {
    return "Initalizes the general accounts by creating the appropriate accounts in the" +
    		" financial subsystem and storing their account identifiers in a reference table.";
  }

  protected String getBatchSummary() {
    return "Initializes general accounts";
  }

  protected TransferObject invoke() {
    final IReadWriteDataProvider p = getReadWriteProvider();
    final String commodity = getDefaultCommodityCode();
    List<String> types = new ArrayList(Arrays.asList(ACCOUNT_TYPE_CODES));
    String[] addedTypes = getExtendedAccountTypes();
    for (int i = 0; i < addedTypes.length; i++) {
      types.add(addedTypes[i]);
    }
    GeneralAccountInitializer generalAccountInitializer = new GeneralAccountInitializer();
    ILoanAccountService service = getLoanAccountService();
    try {
      generalAccountInitializer.create(types.toArray(new String[0]), commodity, getEffectiveDate(), service, p);
    } catch (AbortTransactionException e) {
      getLog().error(e.getMessage(), e);
      return new TransferObject(TransferObject.ERROR, "ACCOUNT_INIT_ABORTED");
    } catch (SQLException e) {
      getLog().error(e.getMessage(), e);
      return new TransferObject(TransferObject.EXCEPTION, "ACCOUNT_INIT_FAILED");
    }
    return new TransferObject();
  }
  
  private String[] getExtendedAccountTypes() {
    List types = (List) getPropertyValue(EXTENDEDACCOUNTTYPES);
    if (types == null) return new String[0];
    String[] returnTypes = new String[types.size()];
    for (int i = 0; i < returnTypes.length; i++) {
      returnTypes[i] = (String) types.get(i);
    }
    return returnTypes;
  }

  private String getDefaultCommodityCode() {
    return (String) getPropertyValue("currencycode");
  }
}
