package com.profitera.services.system.document.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.MapCar;

public class DocumentRetriever {
  private static final String FRAGMENT_TEXT = "FRAGMENT_TEXT";
  private final long documentId;
  private final IReadOnlyDataProvider p;

  public DocumentRetriever(long documentId, IReadOnlyDataProvider p) {
    this.documentId = documentId;
    this.p = p;
  }
  class Frag implements Comparable {
    private final String content;
    private final int seq;

    Frag(String content, int seq){
      this.content = content;
      this.seq = seq;
    }

    public int compareTo(Object o) {
      return seq - ((Frag)o).seq;
    }
  }
  
  public String[] retrieveLines(long startId, int lineCount) throws SQLException {
    long endSequenceId = startId + lineCount - 1;
    Map args = new HashMap();
    args.put("DOCUMENT_ID", documentId);
    args.put("START_SEQUENCE_ID", startId);
    args.put("END_SEQUENCE_ID", endSequenceId);
    args.put("ROW_COUNT", new Long(lineCount));
    Iterator<Map> rows = p.query(IReadWriteDataProvider.LIST_RESULTS, "getDocumentFragmentsBetween", args);
    List<Frag> l = new ArrayList<Frag>();
    while(rows.hasNext()) {
      Map r = rows.next();
      String t = (String) r.get(FRAGMENT_TEXT);
      Number n = (Number) r.get("FRAGMENT_SEQUENCE");
      Frag f = new Frag(t, n.intValue());
      l.add(f);
    }
    Collections.sort(l);
    List result = MapCar.map(new MapCar(){
      @Override
      public Object map(Object o) {
        return ((Frag)o).content;
      }}, l);
    return (String[]) result.toArray(new String[0]);
  }
}
