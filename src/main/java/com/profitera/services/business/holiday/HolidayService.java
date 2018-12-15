package com.profitera.services.business.holiday;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.RunnableTransactionSet;
import com.profitera.deployment.rmi.HolidayServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.util.Strings;

public class HolidayService extends ProviderDrivenService implements HolidayServiceIntf {

  private static final String INSERT_HOLIDAY_STATE = "insertHolidayState";
  private static final String INSERT_HOLIDAY = "insertHoliday";
  private static final String UPDATE_HOLIDAY = "updateHoliday";
  private static final String DELETE_HOLIDAY_STATES = "deleteHolidayStates";

  public TransferObject updateHolidays(Map[] maps) {
    List trans = new ArrayList();
    IReadWriteDataProvider p = getReadWriteProvider();
    for (int i = 0; i < maps.length; i++) {
      IRunnableTransaction t = updateHolday(maps[i], p);
      trans.add(t);
    }
    RunnableTransactionSet t = new RunnableTransactionSet((IRunnableTransaction[]) trans.toArray(new IRunnableTransaction[0]));
    try {
      p.execute(t);
    } catch (AbortTransactionException e) {
      return sqlFailure("update/insert", Strings.getListString(new String[]{INSERT_HOLIDAY, UPDATE_HOLIDAY, UPDATE_HOLIDAY, DELETE_HOLIDAY_STATES, INSERT_HOLIDAY_STATE}, "/"), maps, e);
    } catch (SQLException e) {
      return sqlFailure("update/insert", "", maps, e);
    }
    return new TransferObject();
  }

  private IRunnableTransaction updateHolday(final Map map, final IReadWriteDataProvider p) {
    return new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        Long id = (Long) map.get("ID");
        boolean insert = id == null;
        if (!insert){
          p.delete(DELETE_HOLIDAY_STATES, id, t);
          p.update(UPDATE_HOLIDAY, map, t);
        } else {
          id = (Long) p.insert(INSERT_HOLIDAY, map, t);
        }
        List rels = (List) map.get("HOLIDAY_STATES");
        if (rels == null)
          return;
        for (Iterator i = rels.iterator(); i.hasNext();) {
          Map r = (Map) i.next();
          r.put("HOLIDAY_ID", id);
          p.insert(INSERT_HOLIDAY_STATE, r, t);
        }
      }};
  }

}
