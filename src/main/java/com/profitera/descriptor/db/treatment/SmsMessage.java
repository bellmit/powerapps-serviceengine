// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.treatment;

public class SmsMessage
	extends com.profitera.descriptor.db.treatment.TreatmentProcess
	implements java.io.Serializable
{
	// Generated constants
	public static final String RECIPIENT_MOBILE_NO = "recipientMobileNo";
	public static final String SMS_MESG_CONTENT = "smsMesgContent";
	// End of generated constants
	
	private java.lang.String recipientMobileNo;

	private java.lang.String smsMesgContent;

	public  SmsMessage()
	{
		// Fill in method body here.
	}

	public java.lang.String getRecipientMobileNo()
	{
		return recipientMobileNo;
	}

	public java.lang.String getSmsMesgContent()
	{
		return smsMesgContent;
	}

	public void setRecipientMobileNo(java.lang.String recipientMobileNo)
	{
		this.recipientMobileNo = recipientMobileNo;
	}

	public void setSmsMesgContent(java.lang.String smsMesgContent)
	{
		this.smsMesgContent = smsMesgContent;
	}
}
