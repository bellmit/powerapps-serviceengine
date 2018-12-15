package com.profitera.services.business.admin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.EmployeeServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IEmployee;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class EmployeeService extends ProviderDrivenService implements EmployeeServiceIntf {

  private static final String INSERT_EMPLOYEE_REPORTS_TO = "insertEmployeeReportsTo";
  private static final String UPDATE_EMPLOYEE_REPORTS_TO = "updateEmployeeReportsTo";
  private static final String DELETE_EMPLOYEE_REPORTS_TO = "deleteEmployeeReportsTo";
  private static final String INSERT_ADDRESS_DETAIL = "insertAddressDetail";
  private static final String UPDATE_ADDRESS_DETAIL = "updateAddressDetail";
  private static final String GET_EMPLOYEE_COUNT = "getEmployeeCount";
  private static final String GET_EMPLOYEE_CONTACT_ID = "getEmployeeContactId";
  protected static final String UPDATE_EMPLOYEE_AS_ENABLED = "updateEmployeeAsEnabled";
  protected static final String UPDATE_EMPLOYEE = "updateEmployee";
  protected static final String INSERT_EMPLOYEE = "insertEmployee";
  protected static final String UPDATE_EMPLOYEE_AS_DISABLED = "updateEmployeeAsDisabled";

  public TransferObject addEmployee(final Map emp, String userId) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    final String employeeId = (String) emp.get(IEmployee.EMPLOYEE_ID);
    Long count;
    try {
      count = (Long) p.queryObject(GET_EMPLOYEE_COUNT, employeeId);
    } catch (SQLException e1) {
      return returnFailWithTrace("Select failed", GET_EMPLOYEE_COUNT, "select", emp, e1);
    }
    if (count.longValue() > 0){
      try {
        Long contactId = (Long) p.queryObject(GET_EMPLOYEE_CONTACT_ID, employeeId);
        emp.put(IEmployee.CONTACT_ID, contactId);
      } catch (SQLException e1) {
        return returnFailWithTrace("Select failed", GET_EMPLOYEE_CONTACT_ID, "select", emp, e1);
      }
      IRunnableTransaction t = new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.update(UPDATE_EMPLOYEE_AS_ENABLED, employeeId, t);
          p.update(UPDATE_EMPLOYEE, emp, t);
          p.update(UPDATE_ADDRESS_DETAIL, emp, t);
          if (emp.get(IEmployee.REPORT_TO) == null){
            p.delete(DELETE_EMPLOYEE_REPORTS_TO, employeeId, t);
          } else {
            if (p.update(UPDATE_EMPLOYEE_REPORTS_TO, emp, t) == 0){
              p.insert(INSERT_EMPLOYEE_REPORTS_TO, emp, t);
            }
          }
        }};
      try {
        p.execute(t);
      } catch (AbortTransactionException e) {
        return returnFailWithTrace("Update failed", UPDATE_EMPLOYEE, "update", emp, e);
      } catch (SQLException e) {
        return returnFailWithTrace("Update failed", UPDATE_EMPLOYEE, "update", emp, e);
      }
      return new TransferObject(new Boolean(true));
    } else {
      IRunnableTransaction t = getInsertTransaction(emp, p);
      try {
        p.execute(t);
      } catch (AbortTransactionException e) {
        return returnFailWithTrace("Insert failed", INSERT_EMPLOYEE, "insert", emp, e);
      } catch (SQLException e) {
        return returnFailWithTrace("Insert failed", INSERT_EMPLOYEE, "insert", emp, e);
      }
    }
    return new TransferObject(new Boolean(true));
  }

  private IRunnableTransaction getInsertTransaction(final Map emp, final IReadWriteDataProvider p) {
    return new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        Object contactId = p.insert(INSERT_ADDRESS_DETAIL, emp, t);
        emp.put(IEmployee.CONTACT_ID, contactId);
        p.insert(INSERT_EMPLOYEE, emp, t);
        if (emp.get(IEmployee.REPORT_TO) != null){
          p.insert(INSERT_EMPLOYEE_REPORTS_TO, emp, t);
        }
      }};
  }

  public TransferObject deleteEmployee(final String employeeId, String userId) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    Long count;
    try {
      count = (Long) p.queryObject(GET_EMPLOYEE_COUNT, employeeId);
    } catch (SQLException e1) {
      return returnFailWithTrace("Select failed", GET_EMPLOYEE_COUNT, "select", employeeId, e1);
    }
    final Map m = new HashMap();
    m.put(IEmployee.EMPLOYEE_ID, employeeId);
    m.put(IUser.USER_ID, userId);
    if (count.longValue() > 0){
      IRunnableTransaction t = new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.update(UPDATE_EMPLOYEE_AS_DISABLED, m, t);
        }};
      try {
        p.execute(t);
      } catch (AbortTransactionException e) {
        return returnFailWithTrace("Update failed", UPDATE_EMPLOYEE, "update", employeeId, e);
      } catch (SQLException e) {
        return returnFailWithTrace("Update failed", UPDATE_EMPLOYEE, "update", employeeId, e);
      }
      return new TransferObject(new Boolean(true));
    } else {
      return new TransferObject(TransferObject.ERROR, "NO_SUCH_EMPLOYEE");
    }
  }

  public TransferObject updateEmployee(final Map emp, String userId) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    IRunnableTransaction t = new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        p.update(UPDATE_EMPLOYEE, emp, t);
        p.update(UPDATE_ADDRESS_DETAIL, emp, t);
        if (emp.get(IEmployee.REPORT_TO) == null){
          p.delete(DELETE_EMPLOYEE_REPORTS_TO, emp.get(IEmployee.EMPLOYEE_ID), t);
        } else {
          if (p.update(UPDATE_EMPLOYEE_REPORTS_TO, emp, t) == 0){
            p.insert(INSERT_EMPLOYEE_REPORTS_TO, emp, t);
          }
        }
      }};
    try {
      p.execute(t);
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("Update failed", UPDATE_EMPLOYEE, "update", emp, e);
    } catch (SQLException e) {
      return returnFailWithTrace("Update failed", UPDATE_EMPLOYEE, "update", emp, e);
    }
    return new TransferObject(new Boolean(true));
  }

}
