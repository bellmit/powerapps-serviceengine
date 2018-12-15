package com.profitera.services.system.document.impl;

public class NoopDocumentLineListener implements IDocumentLineListener {

  public boolean line(String l) {
    return true;
  }

}
