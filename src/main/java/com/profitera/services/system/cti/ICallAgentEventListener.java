package com.profitera.services.system.cti;

import java.util.Collection;
import java.util.Map;

public interface ICallAgentEventListener {
 enum Command {Ring, Connect, Disconnect}
 enum Action {CreatedAgent, CreatedCall, ModifiedCall, IncompatibleRequest}
 public class CallAction {
   private Action a;
   private String id;
   private String description;

    public CallAction(Action a, String id, String description) {
      this.a = a;
      this.id = id;
      this.description = description;
       
     }

    public Action getAction() {
      return a;
    }

    public String getId() {
      return id;
    }

    public String getDescription() {
      return description;
    }
 }
 Collection<CallAction> send(Command receivedCommand, Map<String, Object> finalParameters);
}
