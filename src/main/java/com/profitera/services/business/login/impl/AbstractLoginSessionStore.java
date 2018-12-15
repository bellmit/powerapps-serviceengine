package com.profitera.services.business.login.impl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;

public abstract class AbstractLoginSessionStore implements ILoginSessionStore {
  private Long sessionTimeout;
  private Timer sessionTimeoutTimer;
  private final Map<Long, Date> lastActive = new ConcurrentHashMap<Long, Date>();
  private Random random;
  private static DefaultLogProvider log;
  protected Random getRandom() {
    if (random == null) {
      Random r = null;
      try {
        r = SecureRandom.getInstance("SHA1PRNG");
      } catch (NoSuchAlgorithmException e) {
        r = new SecureRandom();
      }
      random = r;
    }
    return random;
  }
  protected void removeSession(long session) {
    lastActive.remove(session);
  }
  @Override
  public void updateSessionLastActive(long session, Date requestTime) {
    lastActive.put(session, requestTime);
  }

  protected long[] getSessionsWithLastActiveTimes() {
    Long[] array = lastActive.keySet().toArray(new Long[0]);
    long[] result = new long[array.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = array[i];
    }
    return result;
  }
  protected ILogProvider getLog() {
    if (log == null) {
      log = new DefaultLogProvider();
      log.register(new LoginSessionLogClient());
    }
    return log;
  }
  @Override
  public void setSessionTimeout(long milliseconds) {
    sessionTimeout = milliseconds;
    if (sessionTimeoutTimer != null) {
      sessionTimeoutTimer.cancel();
    }
    sessionTimeoutTimer = new Timer(true);
    sessionTimeoutTimer.schedule(new TimerTask() {
      long count = 0;
      @Override
      public void run() {
        count++;
        // The first call will now be 1, so even are checks
        if (count % 2 == 0) {
          checkLastActiveTimesForExpirations(new HashMap<Long, Date>(lastActive));
        } else { // Odds are persists
          persistLastActiveTimes(new HashMap<Long, Date>(lastActive));
        }
        
      }
    }, milliseconds/2, milliseconds/2);
  }
  
  public Long getCurrentSessionTimeout() {
    return sessionTimeout;
  }
  protected abstract void persistLastActiveTimes(Map<Long,Date> times);
  protected abstract void checkLastActiveTimesForExpirations(Map<Long,Date> times);
}
