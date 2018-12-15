package com.profitera.services.business.login;

import javax.security.auth.login.LoginException;

public class LdapServerNotFoundException extends LoginException{
	
	public static final long serialVersionUID = 1l;
	
	public LdapServerNotFoundException(){
		super();
	}
	
	public LdapServerNotFoundException(String msg){
		super(msg);
	}
}
