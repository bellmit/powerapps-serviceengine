package com.profitera.services.system.loan.impl;

import java.util.Date;

public interface IPostingScheduleCache {

  public abstract PostingSchedule get(Date date);

  public abstract void put(PostingSchedule postingSchedule);

  public abstract void clear();

}