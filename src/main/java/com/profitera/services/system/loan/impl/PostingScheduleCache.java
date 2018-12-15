package com.profitera.services.system.loan.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PostingScheduleCache implements IPostingScheduleCache {
  private List<PostingSchedule> list = new ArrayList<PostingSchedule>();
  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IPostingScheduleCache#get(java.util.Date)
   */
  public PostingSchedule get(Date date) {
    for (Iterator<PostingSchedule> i = list.iterator(); i.hasNext();) {
      PostingSchedule s = i.next();
      if (s.getStartDate().compareTo(date) <= 0 
          && s.getEndDate().compareTo(date) >= 0){
        return s;
      }
      
    }
    return null;
  }

  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IPostingScheduleCache#put(com.profitera.services.system.loan.impl.PostingSchedule)
   */
  public void put(PostingSchedule postingSchedule) {
    if (postingSchedule != null) {
      list.add(postingSchedule);
    }
  }

  /* (non-Javadoc)
   * @see com.profitera.services.system.loan.impl.IPostingScheduleCache#clear()
   */
  public void clear() {
    list.clear();
  }
}
