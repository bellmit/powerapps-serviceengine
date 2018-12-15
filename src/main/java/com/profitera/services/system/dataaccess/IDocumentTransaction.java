/**
 * 
 */
package com.profitera.services.system.dataaccess;

import com.profitera.dataaccess.IRunnableTransaction;

public interface IDocumentTransaction extends IRunnableTransaction {
  public Long getId();
}