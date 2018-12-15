package com.profitera.services.business.login.impl;

import java.security.Principal;
import java.util.Map;

import com.profitera.descriptor.business.meta.IUser;

public class PowerPrincipal implements Principal {
  private final String name;
  public PowerPrincipal(Map user) {
      this.name = (String) user.get(IUser.USER_ID);
  }

  public String getName() {
      return name;
  }
}
