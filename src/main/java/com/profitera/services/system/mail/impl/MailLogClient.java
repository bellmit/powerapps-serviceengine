package com.profitera.services.system.mail.impl;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;

public class MailLogClient implements ILogClient {
  public static final String NO_SENDER = "NO_EMAIL_SENDER";
  public static final String SENT = "EMAIL_SENT";
  @Override
  public void registerMessages(ILogProvider provider) {
    provider.registerMessage(this, NO_SENDER, Level.W, "Invalid or missing sender address may not receive failure report",
        "An email is being sent without specifying the sender field, the sender server may populate this field for you, leave it blank, or a sending error might also occur. In some cases a sender that does not match the specified account email address will result in a sending failure.");
    provider.registerMessage(this, SENT, Level.I, "Email sent via server {0}",
        "An email was sent to the specified server, delivery of the send email is of course not confirmed.");
  
  }

  @Override
  public String getName() {
    return "Email";
  }

}
