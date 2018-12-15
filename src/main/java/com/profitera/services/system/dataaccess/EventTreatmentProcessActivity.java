package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.event.ProcessingException;
import com.profitera.services.business.http.IMessageHandler;
import com.profitera.services.business.login.ServerSession;
import com.profitera.services.system.lookup.LookupManager;

class EventTreatmentProcessActivity extends
    ConditionalTreatmentProcessActivity {

  protected void executeActivity(Long accountId, Map process, Map target,
      Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
    String eventName = getStatement();
    IMessageHandler h = (IMessageHandler) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "MessageHandler");
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("transaction", t);
    context.put("session", ServerSession.THREAD_SESSION.get());
    Object handleMessage = h.handleMessage(null, "EventService", "sendEvent", new Class[]{String.class, Map.class}, 
        new Object[]{eventName, target}, context);
    TransferObject to = (TransferObject) handleMessage;
    if (to.isFailed()) {
      throw new ProcessingException(to.getMessage(), (Object[]) to.getBeanHolder());
    }
  }
}
