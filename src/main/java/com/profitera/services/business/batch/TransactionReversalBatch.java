package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.QuerySpec;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.financial.TransactionAction;
import com.profitera.services.system.financial.TransactionQuery;

public class TransactionReversalBatch extends AbstractFinancialBatch {
  protected static final String TRANSACTION_ID = "TRANSACTION_ID";
  private static final String TRANSACTIONQUERY = "transactionquery";
  {
    addRequiredProperty(TRANSACTIONQUERY, String.class, 
        "Query that retrieves transactions to reverse", 
        "Query that with retrieve the transactions which are to be reveresed by this batch, the query has to return:  "
        + "<variablelist>"
        + "<varlistentry><term>" + TRANSACTION_ID + " (required)</term><listitem><para>The ID from PTRFIN_TRANSACTION where the transaction to be reversed already exists.</para></listitem></varlistentry>"
        + "</variablelist>");
  }

  protected String getBatchDocumentation() {
    return "Reverses existing transactions.";
  }

  protected String getBatchSummary() {
    return "Reverses existing transactions";
  }

  protected TransferObject invoke() {
    final IReadWriteDataProvider p = getReadWriteProvider();
    QuerySpec spec = new QuerySpec((String) getPropertyValue(TRANSACTIONQUERY), 
        new String[]{TRANSACTION_ID}, 
        new Class[]{Long.class});
    Map args = new HashMap();
    args.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
    try {
      Iterator i = p.query(IReadWriteDataProvider.STREAM_RESULTS, spec.getName(), args);
      final TransactionQuery transQuery = new TransactionQuery();
      while(i.hasNext()){
        final Map payment = spec.verifyResultInstance((Map) i.next());
        final Long transId = (Long) payment.get(TRANSACTION_ID);
        p.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            Transaction transaction = transQuery.getTransaction(transId, getAccountFetcher(), p, t);
            TransactionAction ta = new TransactionAction(transaction, getEffectiveDate());
            ta.reverse(p, t);
          }});
      }
    } catch (SQLException e) {
      getLog().error("Error querying transactions to process: " + e.getMessage(), e);
      return new TransferObject(new Object[]{spec.getName(), e.getMessage()}, TransferObject.EXCEPTION, "PAYMENT_QUERY_FAILED");
    } catch (AbortTransactionException e) {
      getLog().error("Error in query for transactions to process: " + e.getMessage(), e);
      return new TransferObject(new Object[]{spec.getName(), e.getMessage()}, TransferObject.EXCEPTION, "PAYMENT_QUERY_FAILED");
    }
    return new TransferObject();
  }

}
