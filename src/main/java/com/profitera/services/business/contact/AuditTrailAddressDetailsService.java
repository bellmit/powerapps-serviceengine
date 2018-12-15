package com.profitera.services.business.contact;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.AddressDetailsServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IAddress;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.MapVerifyingMapCar;

public final class AuditTrailAddressDetailsService extends ProviderDrivenService implements AddressDetailsServiceIntf {

  private static final String GET_CUSTOMER_ID_BY_CONTACT_ID = "getCustomerIdByContactId";
  private static final String UPDATE_ADDRESS_DETAIL_AS_DELETED = "updateAddressDetailAsDeleted";
  private static final String INSERT_ADDRESS_CUSTOMER_RELATIONSHIP = "insertAddressCustomerRelationship";
  private static final String INSERT_ADDRESS_DETAIL = "insertAddressDetail";
  private static final String GET_CURRENT_MAX_PREFERRED_POSITION_FOR_CUSTOMER = "getCurrentMaxPreferredPositionForCustomer";
  private static final String UPDATE_TIME = "UPDATE_TIME";
  private static final String UPDATE_ADDRESS_DETAIL = "updateAddressDetail";
  private static final String GET_ADDRESS_DETAIL = "getAddressDetail";
  private static final String UPDATE_ADDRESS_DETAIL_PREFERRED_POSITION = "updateAddressDetailPreferredPosition";
  private static final String INSERT_ADDRESS_DETAIL_HISTORY = "insertAddressDetailHistory";

    /*
   * Swaps two AddressDetails preferred position
   * @return A TransferObject
   * @param addressDetails The first AddressDetails
   * @param addressDetails The second AddressDetails
   */
    public TransferObject swapAddressDetailsPreferredPosition(final Map addressDetails, final Map addressDetails2, String userId) {
      //TODO: Behaviour is weird when preferred is not actually defined, i.e. 2 or more items have same preferred pos.
      long contact1 = ((Number)addressDetails.get(IAddress.CONTACT_ID)).longValue();
      long contact2 = ((Number)addressDetails2.get(IAddress.CONTACT_ID)).longValue();
      try {
        final IReadWriteDataProvider readWriteProvider = getReadWriteProvider();
        final Map a1 = getAddressDetail(contact1, readWriteProvider);
        final Map a2 = getAddressDetail(contact2, readWriteProvider);
        if (a1 == null || a2 == null){
          return new TransferObject(TransferObject.ERROR, "NO_SUCH_CONTACT");
        }
        Long p1 = (Long) a1.get(IAddress.PREFERRED_POSITION);
        Long p2 = (Long) a2.get(IAddress.PREFERRED_POSITION);
        if (p1 == null && p2 == null){
          p1 = new Long(1);
          p2 = new Long(2);
        } else if (p1 == null){
          p1 = new Long(p2.longValue() + 1);
        } else if (p2 == null || p1.equals(p2)){
          p2 = new Long(p1.longValue() + 1);
        }
        a1.put(IAddress.PREFERRED_POSITION, p2);
        a2.put(IAddress.PREFERRED_POSITION, p1);
        addAuditInfo(a1, userId);
        addAuditInfo(a2, userId);
        IRunnableTransaction t = new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            readWriteProvider.insert(INSERT_ADDRESS_DETAIL_HISTORY, a1.get(IAddress.CONTACT_ID), t);
            readWriteProvider.insert(INSERT_ADDRESS_DETAIL_HISTORY, a2.get(IAddress.CONTACT_ID), t);
            readWriteProvider.update(UPDATE_ADDRESS_DETAIL_PREFERRED_POSITION, a1, t);
            readWriteProvider.update(UPDATE_ADDRESS_DETAIL_PREFERRED_POSITION, a2, t);
          }};
        readWriteProvider.execute(t);
      } catch (TransferObjectException e) {
        return e.getTransferObject();
      } catch (AbortTransactionException e) {
        List a = new ArrayList();
        a.add(addressDetails);
        a.add(addressDetails2);
        sqlFailure("update", UPDATE_ADDRESS_DETAIL_PREFERRED_POSITION, a, e);
      } catch (SQLException e) {
        List a = new ArrayList();
        a.add(addressDetails);
        a.add(addressDetails2);
        sqlFailure("update", UPDATE_ADDRESS_DETAIL_PREFERRED_POSITION, a, e);
      }
      return new TransferObject(Boolean.TRUE);
    }
    
    private Map getAddressDetail(long contact1, final IReadWriteDataProvider readWriteProvider) throws TransferObjectException {
      List l1 = executeListQuery(IReadOnlyDataProvider.LIST_RESULTS, GET_ADDRESS_DETAIL, new Long(contact1), new MapVerifyingMapCar(new String[]{IAddress.CONTACT_ID, IAddress.PREFERRED_POSITION}), readWriteProvider);
      if (l1.size() > 0)
        return (Map) l1.get(0);
      return null;
    }

    public TransferObject updateContact(final Map bean, String userId) {
      addAuditInfo(bean, userId);
      final IReadWriteDataProvider p = getReadWriteProvider();
      IRunnableTransaction t = new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          p.insert(INSERT_ADDRESS_DETAIL_HISTORY, bean.get(IAddress.CONTACT_ID), t);
          p.update(UPDATE_ADDRESS_DETAIL, bean, t);
        }};
      try {
        p.execute(t);
        return new TransferObject(getAddressDetail(((Number)bean.get(IAddress.CONTACT_ID)).longValue(), getReadWriteProvider()));
      } catch (AbortTransactionException e) {
        return sqlFailure("update", UPDATE_ADDRESS_DETAIL, bean, e);
      } catch (SQLException e) {
        return sqlFailure("update", UPDATE_ADDRESS_DETAIL, bean, e);
      } catch (TransferObjectException e) {
        return e.getTransferObject();
      }
    }

    private void addAuditInfo(final Map bean, String userId) {
      bean.put(IUser.USER_ID, userId);
      bean.put(UPDATE_TIME, new Date());
    }

    public TransferObject addCustomerContacts(final Map bean, final Object customerId, String userId) {
        final Object ownerId = customerId;
        bean.put(IAddress.CONTACT_OWNER_ID, ownerId);
        final IReadWriteDataProvider p = getReadWriteProvider();
        Long pp;
        try {
          pp = (Long) p.queryObject(GET_CURRENT_MAX_PREFERRED_POSITION_FOR_CUSTOMER, customerId);
        } catch (SQLException e1) {
          return returnFailWithTrace("Select failed", GET_CURRENT_MAX_PREFERRED_POSITION_FOR_CUSTOMER, "select", bean, e1);
        }
        if (pp == null)
          pp = new Long(1);
        else
          pp = new Long(pp.longValue() + 1);
        bean.put(IAddress.PREFERRED_POSITION, pp);
        addAuditInfo(bean, userId);
        IRunnableTransaction t = new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            p.insert(INSERT_ADDRESS_DETAIL, bean, t);
            p.insert(INSERT_ADDRESS_CUSTOMER_RELATIONSHIP, bean, t);
          }};
        try {
          p.execute(t);
        } catch (AbortTransactionException e) {
          return returnFailWithTrace("Insert failed", INSERT_ADDRESS_DETAIL, "insert", bean, e);
        } catch (SQLException e) {
          return returnFailWithTrace("Insert failed", INSERT_ADDRESS_DETAIL, "insert", bean, e);
        }
        return new TransferObject(bean);
    }

    public TransferObject deleteContactInformation(final Long id, String userId) {
      final IReadWriteDataProvider p = getReadWriteProvider();
      try {
        final Map m = getAddressDetail(id.longValue(), p);
        if(m==null){
        	return new TransferObject(TransferObject.ERROR, "NO_SUCH_CONTACT");
        }
        addAuditInfo(m, userId);
        IRunnableTransaction t = new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException, AbortTransactionException {
            p.insert(INSERT_ADDRESS_DETAIL_HISTORY, id, t);
            p.update(UPDATE_ADDRESS_DETAIL_AS_DELETED, m, t);
          }};
        String customer = (String) p.queryObject(GET_CUSTOMER_ID_BY_CONTACT_ID, id);
        if (customer != null){
          return getErrorTransferObject("CAN_NOT_DELETE_CUSTOMER_PRIMARY_CONTACT");
        }
        p.execute(t);
        return new TransferObject(Boolean.TRUE);
      } catch (AbortTransactionException e) {
        return sqlFailure("update", UPDATE_ADDRESS_DETAIL_AS_DELETED, id, e);
      } catch (SQLException e) {
        return sqlFailure("update", UPDATE_ADDRESS_DETAIL_AS_DELETED, id, e);
      } catch (TransferObjectException e) {
        return e.getTransferObject();
      }
    }
}