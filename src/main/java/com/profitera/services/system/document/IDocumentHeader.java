package com.profitera.services.system.document;

public interface IDocumentHeader {
  public long getId();
  public boolean isArchived();
  public boolean isEncoded();
  public long getType();
}
