package com.profitera.services.system.mail;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.email.IEmailProvider;
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.SystemService;
import com.profitera.services.system.mail.impl.MailSender;

public class MailService extends SystemService implements IEmailProvider {
  private static final String errorMsg = "Exception while sending email";
  public boolean sendEmail(final Map<String, Object> emailDetails, final boolean failOnError) throws AbortTransactionException {
    AbortTransactionException ate = null;
    boolean mailSent = false;
    try {
      // call method to compose and actually sent the mail
      new MailSender().composeEmail(emailDetails, ServiceEngine.getConfig(false));
    } catch (IOException ex) {
      ate = new AbortTransactionException(errorMsg, ex);
    } catch (MessagingException ex) {
      ate = new AbortTransactionException(errorMsg, ex);
    }
    if (ate == null) {
      mailSent = true;
    }
    if (!mailSent && failOnError) {
      throw ate;
    }
    return mailSent;
  }
}
