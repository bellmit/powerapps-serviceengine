package com.profitera.services.business.login.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.services.business.login.LdapServerNotFoundException;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.business.login.MapLoginService.HashMode;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.net.DNSCheck;
import com.sun.jndi.ldap.LdapCtxFactory;

public class PowerLoginModule implements LoginModule {
		
		public static final int DEFAULT = 0;
		public static final int LDAP = 1;
	
		private static final String PASSWORD = "PASSWORD";
		private static Log LOG = LogFactory.getLog(PersistentLoginSessionStore.class);
    private Subject s;
    protected CallbackHandler handler;
    protected Map user;
    private Map options= new HashMap();
    private MapLoginService provider;
    private final int moduleType;
    
    public PowerLoginModule(){
    	moduleType = DEFAULT;
    }
    
    public PowerLoginModule(int type){
    	moduleType = type;
    }
    
    protected MapLoginService getProvider(){
      if (provider == null){
        provider = (MapLoginService) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "LoginService");
      }
      return provider;
    }
  
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        s = subject;
        handler = callbackHandler;
        this.options = options;
    }
  
    public boolean abort() throws LoginException {
      try {
        getProvider().updateUserLogonStatus(user, false);
      } catch (AbortTransactionException e) {
        throw new LoginException("Login transaction aborted");
      } catch (SQLException e) {
        throw new LoginException("Login transaction failed");
      }
      return true;
    }
  
    public boolean commit() throws LoginException {
        s.getPrincipals().add(new PowerPrincipal(user));
        return true;
    }
  
    public boolean login() throws LoginException {
    	NameCallback n = new NameCallback("userId");
      PasswordCallback p = new PasswordCallback("password", false);
    	switch (moduleType) {
			case LDAP:
				try {
					String domains = (String)options.get("domain");
					if(domains==null){
						throw new LdapServerNotFoundException("domain not provided.");
					}
		    	String[] domain = domains.split(";");
          handler.handle(new Callback[]{n, p});
          String name = n.getName();
          Map user = (Map) getProvider().getPrivateProvider().queryObject("getUser", name);
          if (user!=null){
            char[] password = p.getPassword();
            String passwordCleartext = new String(password);
            Properties props = new Properties();
            for(int i=0;i<domain.length;i++){
            	String dns ="";
            	try{
            		dns = DNSCheck.obtainLDAPServer(domain[i].trim());
            		if(dns==null) continue;
            	}catch(NamingException e){
            		LOG.error(e.getMessage(), e);
            		continue; // test next domain
            	}catch(RuntimeException e){
            		LOG.error(e.getMessage(), e);
            		continue;
            	}
            	String principalName = name + '@' + domain[i].trim();
        			props.put(Context.SECURITY_PRINCIPAL, principalName);
        			props.put(Context.SECURITY_CREDENTIALS,passwordCleartext);
        			props.put(Context.REFERRAL, "follow");
        			LdapCtxFactory.getLdapCtxInstance("ldap://"+dns, props);
        			this.user = user;
              return true;
            } 		
            // No ldap server can be located
            throw new LdapServerNotFoundException("Failed to locate LDAP server");
          }
				} catch (LdapServerNotFoundException e){
      		throw e;
      	} catch (AuthenticationException e) {
      		throw new LoginException("LDAP authentication failed");
      	} catch (Exception e){
      		LOG.error("Login failed", e);
      	}
				break;
				
			default:
				try {
          handler.handle(new Callback[]{n, p});
          final String name = n.getName();
          Map user = (Map) getProvider().getPrivateProvider().queryObject("getUser", name);
          if (user!=null){
            boolean isMatching = getProvider().isUserPassword(user, n.getName(), new String(p.getPassword()));
            if (isMatching) {
              this.user = user;
              return true;
            }
          }
				} catch (Exception e) {
					LOG.error("Login failed", e);
      	}
				break;
			}
      return false;
    }

    public boolean logout() throws LoginException {
      try {
        getProvider().updateUserLogonStatus(user, false);
      } catch (AbortTransactionException e) {
        throw new LoginException("Login transaction aborted");
      } catch (SQLException e) {
        throw new LoginException("Login transaction failed");
      }
      return true;
    }
}
