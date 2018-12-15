package com.profitera.services.system.lookup.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.profitera.client.SessionlessClientSession;
import com.profitera.util.interceptor.Interceptor;
import com.profitera.util.reflect.Reflect;
import com.profitera.util.reflect.ReflectionException;

public class ServerToServerSessionBuilder {
  public Interceptor getInterceptor(String user, Object loginService) {
    // Now if I really don't have a user I am going to fall back to
    // the 'Admin' user, I have no other option
    if (user == null) {
      user = "Admin";
    }
    try {
      Method getUserForLogin =loginService.getClass().getDeclaredMethod("getUserForLogin", String.class);
      getUserForLogin.setAccessible(true);
      Map userData = (Map) Reflect.invokeMethod(getUserForLogin, loginService, new Object[]{user});
      String password = userData == null ? null : (String)userData.get("PASSWORD");
      return new SessionlessClientSession(user, password, "server");
    } catch (ReflectionException e) {
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
