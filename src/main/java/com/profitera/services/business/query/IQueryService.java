package com.profitera.services.business.query;

import java.util.Map;

import com.profitera.descriptor.business.TransferObject;

public interface IQueryService {
  public TransferObject getQueryList(String name, Map<String, Object> arguments);
  public long getId();
}
