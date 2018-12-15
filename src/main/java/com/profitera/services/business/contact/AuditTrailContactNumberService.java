package com.profitera.services.business.contact;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.ContactNumberServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IContactNumber;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;
import com.profitera.util.MapListUtil;

public class AuditTrailContactNumberService extends ProviderDrivenService implements ContactNumberServiceIntf {
  private static final String INSERT_CONTACT_NUMBER = "insertContactNumber";
  private static final String UPDATE_CONTACT_NUMBER = "updateContactNumber";
  private static final String INSERT_CONTACT_NUMBER_HISTORY = "insertContactNumberHistory";
  private static final String GET_MAXIMUM_PREFERRED_POSITION_FOR_CONTACT = "getMaximumPreferredPositionForContact";
  private static final String GET_RELATED_PREFERRED_CONTACT_NUMBERS = "getRelatedPreferredContactNumbers";
  private static final String UPDATE_CONTACT_NUMBER_PREFERRED_POSITION = "updateContactNumberPreferredPosition";
  private static final String GET_CONTACT_NUMBER_PREFERRED_POSITION = "getContactNumberPreferredPosition";
  private static final String UPDATE_CONTACT_NUMBER_AS_DELETED = "updateContactNumberAsDeleted";
  private static final String GET_CURRENT_CUSTOMER_CONTACT_ID = "getCurrentCustomerContactId";

  /*
   * @return A TransferObject that holds a fresh copy of an update ContactNumber
   * @param oldContactNumber The old ContactNumber @param newContactNumber The
   * new ContactNumber
   */
  public TransferObject updateContactNumber(final Map newContactNumber, final String oldContactNumber, String userId) {
    // Old contact number is ignored, we use a real key for updates instead,
    // this is here for legacy reasons only.
    final IReadWriteDataProvider p = getReadWriteProvider();
    final Long id = (Long) newContactNumber.get("ID");
    addAuditInfo(newContactNumber, userId, new Date());
    try {
      p.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.insert(INSERT_CONTACT_NUMBER_HISTORY, id, t);
          p.update(UPDATE_CONTACT_NUMBER, newContactNumber, t);
        }
      });
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER, newContactNumber, e);
    } catch (SQLException e) {
      return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER, newContactNumber, e);
    }
    return new TransferObject(Boolean.TRUE);
  }

  private void addAuditInfo(Map contactNumber, String userId, Date date) {
    contactNumber.put("USER_ID", userId);
    contactNumber.put("UPDATE_TIME", date);
  }

  public TransferObject setAsPreferredNumber(final Map bean, String userId) {
    final Long contactId = (Long) bean.get(IContactNumber.CONTACT_ID);
    final Long id = (Long) bean.get("ID");
    Long maxpp = null;
    try {
      maxpp = (Long) getReadWriteProvider().queryObject(GET_MAXIMUM_PREFERRED_POSITION_FOR_CONTACT, contactId);
    } catch (SQLException e) {
      return returnFailWithTrace("", null, GET_MAXIMUM_PREFERRED_POSITION_FOR_CONTACT, bean, e);
    }
    if (maxpp == null) {
      maxpp = new Long(1);
    } else {
      maxpp = new Long(maxpp.longValue() + 1);
    }
    bean.put(IContactNumber.PREFERRED_POSITION, maxpp);
    addAuditInfo(bean, userId, new Date());
    try {
      final IReadWriteDataProvider p = getReadWriteProvider();
      p.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.insert(INSERT_CONTACT_NUMBER_HISTORY, id, t);
          p.update(UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, bean, t);
        }
      });
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, bean, e);
    } catch (SQLException e) {
      return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, bean, e);
    }
    return new TransferObject(Boolean.TRUE);
  }

  /*
   * Swaps two ContactNumber's preferred position @return A TransferObject that
   * holds whether or not the update is successful @param contactNumber The
   * first ContactNumber @param contactNumber2 The second ContactNumber
   */
  public TransferObject swapContactNumberPreferredPosition(final Map contactNumber, final Map contactNumber2,
      String userId) {
    final Long id1 = (Long) contactNumber.get("ID");
    final Long id2 = (Long) contactNumber2.get("ID");
    Long pp1 = null;
    Long pp2 = null;
    try {
      pp1 = (Long) getReadWriteProvider().queryObject(GET_CONTACT_NUMBER_PREFERRED_POSITION, id1);
      pp2 = (Long) getReadWriteProvider().queryObject(GET_CONTACT_NUMBER_PREFERRED_POSITION, id2);
    } catch (SQLException e) {
      return returnFailWithTrace("", null, GET_CONTACT_NUMBER_PREFERRED_POSITION, id1 + "/" + id2, e);
    }
    if (pp1 == null || pp2 == null) {
      return new TransferObject(TransferObject.ERROR, "CONTACT_NUMBER_NOT_PREFERRED");
    }
    // Swap the pref. positions
    contactNumber.put(IContactNumber.PREFERRED_POSITION, pp2);
    contactNumber2.put(IContactNumber.PREFERRED_POSITION, pp1);
    Date date = new Date();
    addAuditInfo(contactNumber, userId, date);
    addAuditInfo(contactNumber2, userId, date);
    try {
      final IReadWriteDataProvider p = getReadWriteProvider();
      p.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.insert(INSERT_CONTACT_NUMBER_HISTORY, id1, t);
          p.update(UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, contactNumber, t);
          p.insert(INSERT_CONTACT_NUMBER_HISTORY, id2, t);
          p.update(UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, contactNumber2, t);
        }
      });
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, id1 + "/" + id2, e);
    } catch (SQLException e) {
      return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, id1 + "/" + id2, e);
    }
    return new TransferObject(Boolean.TRUE);
  }

  /*
   * Clears a ContactNumber preferred position @return A TransferObject that
   * holds a ContactNumber object @param contactNumber The ContactNumber object
   * in which to removed the preferred position
   */
  public TransferObject clearPreferredPosition(final Map contactNumber, List contactIds, boolean initialize,
      String userId) {
    final Long id = (Long) contactNumber.get("ID");
    Long preferredPosition = (Long) contactNumber.get(IContactNumber.PREFERRED_POSITION);
    contactNumber.put(IContactNumber.PREFERRED_POSITION, null);
    if (preferredPosition == null) {
      return new TransferObject(TransferObject.ERROR, "CONTACT_NUMBER_NOT_PREFERRED");
    }
    try {
      final List otherPreferred = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS,
          GET_RELATED_PREFERRED_CONTACT_NUMBERS, id, new MapVerifyingMapCar(new String[] { "ID",
              IContactNumber.PREFERRED_POSITION }), getReadWriteProvider());
      // If the query returned the number we are removing from preferred, then
      // remove it from the list.
      int removalIndex = MapListUtil.firstIndexOf("ID", id, otherPreferred);
      if (removalIndex != -1) {
        otherPreferred.remove(removalIndex);
      }
      Date date = new Date();
      addAuditInfo(contactNumber, userId, date);
      for (Iterator i = otherPreferred.iterator(); i.hasNext();) {
        Map no = (Map) i.next();
        Long pp = (Long) no.get(IContactNumber.PREFERRED_POSITION);
        // If there is no PP assigned for this number, remove it from
        // the list, the query above is wrong but that is OK,
        if (pp == null) {
          i.remove();
        } else if (pp.compareTo(preferredPosition) > 0) {
          no.put(IContactNumber.PREFERRED_POSITION, new Long(pp.longValue() - 1));
          addAuditInfo(no, userId, date);
        }
      }
      try {
        final IReadWriteDataProvider p = getReadWriteProvider();
        p.execute(new IRunnableTransaction() {
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            for (Iterator i = otherPreferred.iterator(); i.hasNext();) {
              Map no = (Map) i.next();
              Long id = (Long) no.get("ID");
              p.insert(INSERT_CONTACT_NUMBER_HISTORY, id, t);
              p.update(UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, no, t);
            }
            p.insert(INSERT_CONTACT_NUMBER_HISTORY, id, t);
            p.update(UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, contactNumber, t);
          }
        });
      } catch (AbortTransactionException e) {
        return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, id, e);
      } catch (SQLException e) {
        return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER_PREFERRED_POSITION, id, e);
      }
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
    return new TransferObject(Boolean.TRUE);
  }

  public TransferObject addContactNumber(final Map bean, String userId) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    addAuditInfo(bean, userId, new Date());
    try {
      p.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.insert(INSERT_CONTACT_NUMBER, bean, t);
        }
      });
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("", null, INSERT_CONTACT_NUMBER, bean, e);
    } catch (SQLException e) {
      return returnFailWithTrace("", null, INSERT_CONTACT_NUMBER, bean, e);
    }
    return new TransferObject(Boolean.TRUE);
  }

  public TransferObject deleteContactNumber(final Map bean, String userId) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    final Long id = (Long) bean.get("ID");
    try {
      addAuditInfo(bean, userId, new Date());
      p.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.insert(INSERT_CONTACT_NUMBER_HISTORY, id, t);
          p.update(UPDATE_CONTACT_NUMBER_AS_DELETED, bean, t);
        }
      });
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER_AS_DELETED, bean, e);
    } catch (SQLException e) {
      return returnFailWithTrace("", null, UPDATE_CONTACT_NUMBER_AS_DELETED, bean, e);
    }
    return new TransferObject(Boolean.TRUE);
  }

  public TransferObject addContactNumber(final Map bean, final String customerId, String userId) {
    final IReadWriteDataProvider p = getReadWriteProvider();
    Long contactId;
    try {
      contactId = (Long) p.queryObject(GET_CURRENT_CUSTOMER_CONTACT_ID, customerId);
    } catch (SQLException e1) {
      return returnFailWithTrace("Select failed", GET_CURRENT_CUSTOMER_CONTACT_ID, "select", bean, e1);
    }
    bean.put(IContactNumber.CONTACT_ID, contactId);
    addAuditInfo(bean, userId, new Date());
    IRunnableTransaction t = new IRunnableTransaction() {
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        p.insert(INSERT_CONTACT_NUMBER, bean, t);
      }
    };
    try {
      p.execute(t);
    } catch (AbortTransactionException e) {
      return returnFailWithTrace("Insert failed", INSERT_CONTACT_NUMBER, "insert", bean, e);
    } catch (SQLException e) {
      return returnFailWithTrace("Insert failed", INSERT_CONTACT_NUMBER, "insert", bean, e);
    }
    return new TransferObject(bean);
  }
}