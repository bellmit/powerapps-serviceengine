package com.profitera.services.system.document.impl;

import java.util.ArrayList;
import java.util.List;

public class LineRangeListener implements IDocumentLineListener {
  private final long start;
  private final long end;
  private int count = -1; // To start at 0, we increment first
  private List<String> lines = new ArrayList<String>();
  public LineRangeListener(long start, int size) {
    this.start = start;
    this.end = start + size - 1;
  }
  public boolean line(String l) {
    count++;
    if (count > end) {
      return false;
    } else if (count >= start) {
      lines.add(l);
      return true;
    } else {
      return true;
    }
  }
  
  public String[] getLines() {
    return lines.toArray(new String[lines.size()]);
  }

}
