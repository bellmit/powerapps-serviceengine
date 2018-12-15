// ### TopLink Mapping Workbench 9.0.3 generated source code ###

package com.profitera.descriptor.db.history;

public class PasswordHistory
	implements java.io.Serializable
{
	// Generated constants
	public static final String PASSWORD = "password";
	public static final String PASSWORD_DATE = "passwordDate";
	public static final String PASSWORD_ID = "passwordId";
	public static final String USER = "user";
	// End of generated constants
	
	private java.lang.String password;

	private java.sql.Timestamp passwordDate;

	private java.lang.Double passwordId;

	private java.lang.String user;

	public  PasswordHistory()
	{
		// Fill in method body here.
	}

	public java.lang.String getPassword()
	{
		return password;
	}

	public java.sql.Timestamp getPasswordDate()
	{
		return passwordDate;
	}

	public java.lang.Double getPasswordId()
	{
		return passwordId;
	}

	public java.lang.String getUser()
	{
		return user;
	}

	public void setPassword(java.lang.String password)
	{
		this.password = password;
	}

	public void setPasswordDate(java.sql.Timestamp passwordDate)
	{
		this.passwordDate = passwordDate;
	}

	public void setPasswordId(java.lang.Double passwordId)
	{
		this.passwordId = passwordId;
	}

	public void setUser(java.lang.String user)
	{
		this.user = user;
	}
}
