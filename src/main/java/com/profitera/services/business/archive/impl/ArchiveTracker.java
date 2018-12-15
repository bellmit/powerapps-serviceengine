package com.profitera.services.business.archive.impl;

public class ArchiveTracker {

  private Archive archive;
  
  public void setRunning(Archive a) {
    archive = a;
  }

  public Archive getRunning() {
    return archive;
  }

}
