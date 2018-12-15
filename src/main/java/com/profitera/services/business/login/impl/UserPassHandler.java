package com.profitera.services.business.login.impl;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

public class UserPassHandler implements CallbackHandler {
  private final String userName;
  private final String passWord;

  public UserPassHandler(String userName, String passWord) {
      this.userName = userName;
      this.passWord = passWord;
  }

  public void handle(Callback[] callbacks) {
      for (int i = 0; i < callbacks.length; i++) {
          Callback c = callbacks[i];
          if (c instanceof NameCallback) {
              ((NameCallback) c).setName(userName);
          } else if (c instanceof PasswordCallback) {
              ((PasswordCallback) c).setPassword(passWord.toCharArray());
          } 
      }
  }

}
