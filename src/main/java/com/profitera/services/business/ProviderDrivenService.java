package com.profitera.services.business;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.ibatis.common.jdbc.exception.NestedSQLException;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.InvalidQueryResultException;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.LimitingIterator;
import com.profitera.util.MapCar;
import com.profitera.util.Strings;

/**
 * @author jamison
 */
public class ProviderDrivenService extends BusinessService {
  private Random traceGenerator = new Random();
  
  public static class TransferObjectException extends Exception {
    private static final long serialVersionUID = 1L;
    private final TransferObject transferObject;
    
    public TransferObjectException(TransferObject t){
      this(null, t, null);
    }
    
    public TransferObjectException(String message, TransferObject t){
      this(message, t, null);
    }

    public TransferObjectException(TransferObject t, Throwable cause){
      this(null, t, cause);
    }
    
    public TransferObjectException(String message, TransferObject t, Throwable cause){
      super(message, cause);
      this.transferObject = t;
    }
    
    public TransferObject getTransferObject(){
      return transferObject;
    }
  }
  
  protected TransferObject returnFailWithTrace(String message, Object method, String selectionType, Object args, Exception e) {
    long trace = Math.abs(traceGenerator.nextLong());
    String argString = "" + args;
    if (args != null && args.getClass().isArray()){
      argString = Strings.getListString((Object[])args, "', '");
    }
    log.error(trace + " " + selectionType + ": '" + argString + "' via " + method);
    log.error(trace + " " + selectionType + ": " + e.getMessage(), e);
    return new TransferObject(new Object[]{selectionType}, TransferObject.EXCEPTION, message + " (Trace: " + trace + ")");
  }
  
  protected TransferObject sqlFailure(Object method, String sqlStatementId, Object args, Exception e) {
    if (e instanceof NestedSQLException && e.getCause() != null && e.getCause() instanceof Exception){
      e = (Exception) e.getCause();
    }
    return returnFailWithTrace("Unable to execute query to satisfy request.", method, sqlStatementId, args, e);
  }
  
  protected TransferObject invalidQueryFailure(Object method, String selectionType, Object args, Exception e) {
    return returnFailWithTrace("Configured query for this request does not return required columns.", method, selectionType, args, e);
  }
  
  protected TransferObject executeQuery(String qName, Object args, MapCar car, IReadOnlyDataProvider p) {
    Object method = IReadOnlyDataProvider.LIST_RESULTS;
    return executeQuery(method, qName, args, car, p);
  }
  
  protected TransferObject executeQuery(Object method, String qName, Object args, MapCar car, IReadOnlyDataProvider p) {
    return executeQuery(method, qName, args, car, -1, p);
  }
  
  protected TransferObject executeQuery(Object method, String qName, Object args, MapCar car, int maxRows, IReadOnlyDataProvider p) {
    try {
      Iterator i = p.query(method, qName, args);
      if (maxRows > 0){
        i = new LimitingIterator(i, maxRows);
      }
      List l = new ArrayList(100);
      MapCar.map(car, i, l);
      return new TransferObject(l);
    } catch (SQLException e) {
      return sqlFailure(method, qName, args, e);
    } catch (InvalidQueryResultException e){
      return invalidQueryFailure(method, qName, args, e);
    } catch (RuntimeException e){
      return invalidQueryFailure(method, qName, args, e);
    }
  }
  
  protected List executeListQuery(Object method, String qName, Object args, MapCar car, IReadOnlyDataProvider p) throws TransferObjectException{
    try {
      List l = new ArrayList(100);
      MapCar.map(car, p.query(method, qName, args), l);
      return l;
    } catch (SQLException e) {
      throw new TransferObjectException(sqlFailure(method, qName, args, e), e);
    } catch (InvalidQueryResultException e){
      throw new TransferObjectException(invalidQueryFailure(method, qName, args, e), e);
    } catch (RuntimeException e){
      throw new TransferObjectException(invalidQueryFailure(method, qName, args, e), e);
    }
  }
  
  protected Iterator executeIteratorQuery(Object method, String qName, Object args, IReadOnlyDataProvider p) throws TransferObjectException{
	try {
	  return p.query(method, qName, args);
	} catch (SQLException e) {
	  throw new TransferObjectException(sqlFailure(method, qName, args, e), e);
	} catch (InvalidQueryResultException e){
	   throw new TransferObjectException(invalidQueryFailure(method, qName, args, e), e);
	} catch (RuntimeException e){
	  throw new TransferObjectException(invalidQueryFailure(method, qName, args, e), e);
	}
  }
  
  protected IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }
  
  protected IReadOnlyDataProvider getReadOnlyProvider() {
    return getReadWriteProvider();
  }

}
