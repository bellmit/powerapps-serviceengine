package com.profitera.services.system.financial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SplitReducer {

  public Split[] reduceSplits(Split[] allSplits) {
    Set commodities = new HashSet();
    for (int i = 0; i < allSplits.length; i++) {
      if (!commodities.contains(allSplits[i].getCommodity())){
        commodities.add(allSplits[i].getCommodity());
      }
    }
    List all = new ArrayList();
    for (Iterator i = commodities.iterator(); i.hasNext();) {
      Commodity c = (Commodity) i.next();
      all.addAll(reduceSplits(c, allSplits));
    }
    return (Split[]) all.toArray(new Split[all.size()]);
  }
  private Collection reduceSplits(Commodity c, Split[] allSplits) {
    Map reduced = new HashMap();
    for (int i = 0; i < allSplits.length; i++) {
      Split s = allSplits[i];
      if (!s.getCommodity().equals(c)) continue;
      Split rSplit = (Split) reduced.get(s.getAccount());
      if (rSplit == null){
        rSplit = s;
      } else {
        rSplit = new Split(s.getAccount(), rSplit.getAmount().add(s.getAmount()), c, rSplit.getExchangedAmount().add(s.getExchangedAmount()));
      }
      reduced.put(rSplit.getAccount(), rSplit);
    }
    return reduced.values();
  }
}
