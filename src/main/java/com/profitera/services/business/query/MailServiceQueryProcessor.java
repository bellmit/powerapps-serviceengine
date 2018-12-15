package com.profitera.services.business.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.email.IEmailProvider;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.lookup.LookupManager;

public class MailServiceQueryProcessor extends BaseListQueryProcessor {

  private boolean failOnError = false;	
  private static final String FAIL_ON_ERROR_KEYWORD = "FAIL_ON_ERROR";
  
  public MailServiceQueryProcessor() {
  	super();
  	super.addProperty(FAIL_ON_ERROR_KEYWORD, Boolean.class, new Boolean(failOnError), "Fail query if an error occurs", "When set to true causes the entire query to fail if an error occurs when sending an email, a reasonable choice if the sole purpose of the query is to send the email.");
  }
  protected void configureProcessor() {
    super.configureProcessor();
   	failOnError = ((Boolean)getProperty(FAIL_ON_ERROR_KEYWORD)).booleanValue();    
  }  
  
  public Map preprocessArguments(Map arguments, IQueryService qs) throws TransferObjectException {
    return arguments;
  }	

  public List postProcessResults(Map arguments, List result, IQueryService qs) throws TransferObjectException {
    for (Iterator i = result.iterator(); i.hasNext();) {
      final Map row = (Map) i.next();
      final Map mailDetails = new HashMap(arguments.size() + row.size());
      mailDetails.putAll(arguments);
      mailDetails.putAll(row);
  		try {
  		  getMailService().sendEmail(mailDetails, failOnError);
			} catch (AbortTransactionException e) {
        getLog().error("Failed to send email", e);
        throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, "FAILED_TO_SEND_EMAIL"), e);
			}
    }
    return result;
  }	

  protected IEmailProvider getMailService() {
	Object p = (IEmailProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "MailService");
	return (IEmailProvider) p;
  }	

  protected String getDocumentation() {
    return "This query processor relies on the MailService configuration so assign the field values appropriately to send the intended email." 
    + " Fields in the response are used to populate the message, to, cc, bcc field, etc. The resulting databse transaction generates a ID that"
    + " can be used to retrieve the email details, it is assigned to a result field as configured.";
  }
  protected String getSummary() {
    return "Sends an email for each row returned by the assigned query";
  }    
}