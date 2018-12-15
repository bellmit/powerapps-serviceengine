package com.profitera.rpm;

public interface ICustomerProfiler {

  public abstract void buildCacheTables();

  public abstract void dropCacheTables();

  public abstract void profileCustomers(boolean useCache, String startingId, String endingId);

}