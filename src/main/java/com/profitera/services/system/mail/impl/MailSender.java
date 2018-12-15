package com.profitera.services.system.mail.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.profitera.descriptor.business.meta.IEmail;
import com.profitera.email.IEmailProvider;
import com.profitera.io.StreamUtil;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;

public class MailSender {
  private ILogProvider log;
  public void composeEmail(Map<String, Object> emailDetails, Properties mailConfig) throws IOException, MessagingException {
    if (!emailDetails.containsKey(IEmail.CREATED_DATE)){
      emailDetails.put(IEmail.CREATED_DATE, new Date());
    }
    if (!emailDetails.containsKey(IEmail.MIME_TYPE)) {
      emailDetails.put(IEmail.MIME_TYPE, IEmailProvider.MIME_TYPE_PLAIN_TEXT);
    }
    String subject = emailDetails.get(IEmail.SUBJECT).toString();
    String from = (String) emailDetails.get(IEmail.SENDER);
    List toList = extractToList(emailDetails, IEmail.TO_LIST);
    List ccList = extractToList(emailDetails, IEmail.CC_LIST);
    List bccList = extractToList(emailDetails, IEmail.BCC_LIST);
    Date date = (Date) emailDetails.get(IEmail.CREATED_DATE);
    String content = (String) emailDetails.get(IEmail.CONTENT);
    List attachmentList = (List) emailDetails.get(IEmail.FILE_ATTACHMENT);
    String mimeType = emailDetails.get(IEmail.MIME_TYPE).toString();

    final Properties props = new Properties();
    for (Iterator<Map.Entry<Object, Object>> i = mailConfig.entrySet().iterator(); i.hasNext();) {
      Map.Entry<Object, Object> object = i.next();
      if (object.getKey().toString().startsWith("mail.")) {
        props.put(object.getKey(), object.getValue());
      }
    }
    String mailServer = props.getProperty("mail.smtp.host");
    if (mailServer == null) {
      mailServer = props.getProperty("mail.host");
    }
    if (mailServer == null) {
      throw new IllegalArgumentException("Mail server host address required 'mail.host' or 'mail.smtp.host'");
    }
    Session session = Session.getInstance(props, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"));
      }
    });
    MimeMessage msg = new MimeMessage(session);
    msg.setSubject(subject);
    msg.setSentDate(date);

    try {
      if (from == null) {
        from = "";
      }
      msg.setFrom(new InternetAddress(from));
    } catch (AddressException ex) {
      getLog().emit(MailLogClient.NO_SENDER, "");
      msg.setFrom(new InternetAddress(mailServer));
    }

    if (toList != null && toList.size() != 0)
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(listToString(toList), false));

    if (ccList != null && ccList.size() != 0)
      msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(listToString(ccList), true));

    if (bccList != null && bccList.size() != 0)
      msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(listToString(bccList), true));

    Multipart mp = new MimeMultipart();
    MimeBodyPart p1 = new MimeBodyPart();
    p1.setContent(content, mimeType);
    mp.addBodyPart(p1);
    msg.setContent(mp);
    FileOutputStream fos = null;
    List<File> tempFiles = new ArrayList<File>();
    try {
      if (attachmentList != null && attachmentList.size() != 0) {
        MimeBodyPart[] attachment = new MimeBodyPart[attachmentList.size()];
        for (int i = 0; i < attachment.length; i++) {
          attachment[i] = new MimeBodyPart();
          File file = File.createTempFile("temp", ".email");
          tempFiles.add(file);
          fos = new FileOutputStream(file);
          fos.write((byte[]) ((Map) attachmentList.get(i)).get(IEmail.IEmailAttachment.FILE_CONTENT));
          attachment[i].setDataHandler(new DataHandler(new FileDataSource(file)));
          attachment[i].setFileName(((Map) attachmentList.get(i)).get(IEmail.IEmailAttachment.FILE_NAME).toString());
          mp.addBodyPart(attachment[i]);
        }
      }
      msg.saveChanges();
      Transport.send(msg);
      getLog().emit(MailLogClient.SENT, mailServer);
    } finally {
      StreamUtil.closeFinally(fos);
    }
    for (Iterator<File> i = tempFiles.iterator(); i.hasNext();) {
      File f = i.next();
      f.delete();
    }
  }
  
  private List<?> extractToList(Map emailDetails, String key) {
    Object value = emailDetails.get(key);
    if (value == null || value instanceof List) {
      return (List) value;
    } else {
      String text = value.toString();
      String[] values = text.split("[;,]");
      List<String> list = new ArrayList<>();
      for (int i = 0; i < values.length; i++) {
        list.add(values[i].trim());
      }
      return list;
    }
  }

  private String listToString(List<?> list) {
    String emailAddress = "";
    if (list.size() != 0) {
      if (list.size() > 1)
        for (int i = 0; i < list.size(); i++)
          if (i != list.size() - 1)
            emailAddress = emailAddress + list.get(i) + ",";
          else
            emailAddress = emailAddress + list.get(i);
      else
        emailAddress = list.get(0).toString();
      return emailAddress;
    }
    return null;
  }
  private ILogProvider getLog() {
    if (log == null) {
      DefaultLogProvider l = new DefaultLogProvider();
      l.register(new MailLogClient());
      log = l;
    }
    return log;
  }
}
