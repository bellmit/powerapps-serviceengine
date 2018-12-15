package com.profitera.services.business.batch;

import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.lookup.LookupManager;

public abstract class AbstractProviderBatch extends AbstractBatchProcess {
  private static final String COMMIT_SIZE = "commitsize";
  private static final String THREADS = "threads";
  
  protected static void addCommitSizeProperty(int defaultCommitSize, AbstractProviderBatch b){
    b.addProperty(COMMIT_SIZE, Integer.class, defaultCommitSize+"", "Commit size", "Commit size for transactions");
  }
  
  protected static int getCommitSizeProperty(AbstractProviderBatch b){
    Integer i = (Integer) b.getPropertyValue(COMMIT_SIZE);
    return i.intValue();
  }
  
  protected static void addThreadsProperty(int defaultThreads, AbstractProviderBatch b){
    b.addProperty(THREADS, Integer.class, defaultThreads+"", "Number of threads", "Number of threads for concurrent processing");
  }
  
  protected static int getThreadsProperty(AbstractProviderBatch b){
    Integer i = (Integer) b.getPropertyValue(THREADS);
    return i.intValue();
  }
  
  protected IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }
  
  protected IReadOnlyDataProvider getReadOnlyProvider() {
    return getReadWriteProvider();
  }

}
