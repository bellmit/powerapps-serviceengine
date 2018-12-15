/*
 * Created on Nov 13, 2006
 *
 */
package com.profitera.services.business.admin;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.UserAndRoleManagementServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.system.license.LicenseInterceptor;
import com.profitera.services.system.license.LicenseVerifier;

public class UserAndRoleManagementService  extends ProviderDrivenService implements UserAndRoleManagementServiceIntf{
	private static final String INSERT_BUSINESS_UNIT_QUERY_NAME = "insertBusinessUnit";
	private static final String UPDATE_BUSINESS_UNIT_QUERY_NAME = "updateBusinessUnit";
	private static final String INSERT_USER_TEAMS_QUERY_NAME = "insertUserTeam";
	private static final String UPDATE_USER_TEAMS_QUERY_NAME = "updateUserTeam";
	private static final String INSERT_USER_QUERY_NAME = "insertUser";
	private static final String UPDATE_USER_QUERY_NAME = "updateUser";
	private static final String DELETE_USER_QUERY_NAME = "deleteUser";
	private static final String INSERT_USER_TEAM_LINK_QUERY_NAME = "insertUserTeamLink";
	private static final String DELETE_USER_TEAM_LINK_QUERY_NAME = "deleteUserTeamLink";
	private static final String INSERT_USER_ROLE_LINK_QUERY_NAME = "insertUserRoleLink";
	private static final String DELETE_USER_ROLE_LINK_QUERY_NAME = "deleteUserRoleLink";
	private static final String INSERT_ROLE_QUERY_NAME = "insertRole";
	private static final String UPDATE_ROLE_QUERY_NAME = "updateRole";
	private static final String INSERT_ROLE_ACCESS_REL_QUERY_NAME = "insertRoleAccessRel";
	private static final String DELETE_ROLE_ACCESS_REL_QUERY_NAME = "deleteRoleAccessRel";
	private static final String CHECK_IF_USER_EXIST_QUERY_NAME = "checkIfUserExist";

	public TransferObject update(Map object, int type, String userId) {
		try {
			if(type==UserAndRoleManagementServiceIntf.BUSINESS_UNIT)
				return updateBusinessUnit(object, userId);
			else if(type==UserAndRoleManagementServiceIntf.TEAM)
				return updateUserTeam(object, userId);
			else if(type==UserAndRoleManagementServiceIntf.USER)
				return updateUser(object, userId);
			else if(type==UserAndRoleManagementServiceIntf.ROLE)
				return updateRole(object, userId);
			else
				return returnFailWithTrace("USER_AND_ROLE_MANAGEMENT_UPDATE_FAILED", null, null, object, null);
		} catch (AbortTransactionException e) {
		  return returnFailWithTrace(e.getMessage(), null, null, object, e);
		} catch (SQLException e) {
		  return returnFailWithTrace("USER_AND_ROLE_MANAGEMENT_UPDATE_FAILED", null, null, object, e);
		}
	}
	
	private TransferObject updateBusinessUnit(final Map businessUnit, final String userId) throws AbortTransactionException, SQLException {
		if(Boolean.TRUE.equals(businessUnit.get(IS_NEW))){
			getReadWriteProvider().execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					businessUnit.put("BRANCH_CREATE_BY", userId);
					businessUnit.put("BRANCH_CREATE_DATE", new Date());					
					Object id = getReadWriteProvider().insert(INSERT_BUSINESS_UNIT_QUERY_NAME,businessUnit,t);
					businessUnit.put("ID", id);
				}});
		} else {
			getReadWriteProvider().execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					if(businessUnit.get("DISABLE")==null)
						businessUnit.put("DISABLE",new Integer(0));
					getReadWriteProvider().update(UPDATE_BUSINESS_UNIT_QUERY_NAME,businessUnit,t);					
			}});
		}
		return new TransferObject(businessUnit.get("ID"));
	}
	
	private TransferObject updateUserTeam(final Map userTeam, final String userId) throws AbortTransactionException, SQLException{
		if(Boolean.TRUE.equals(userTeam.get(IS_NEW))){
			getReadWriteProvider().execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					userTeam.put("TEAM_CREATE_BY", userId);
					userTeam.put("TEAM_CREATE_DATE", new Date());					
					getReadWriteProvider().insert(INSERT_USER_TEAMS_QUERY_NAME,userTeam,t);
				}});
		} else {
			getReadWriteProvider().execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					if(userTeam.get("DISABLE")==null)
						userTeam.put("DISABLE",new Integer(0));
					getReadWriteProvider().update(UPDATE_USER_TEAMS_QUERY_NAME,userTeam,t);					
			}});
		}
		return new TransferObject(userTeam.get("TEAM_ID"));
	}
	
	private TransferObject updateUser(final Map user, final String userId) throws AbortTransactionException, SQLException{
		boolean isNew = Boolean.TRUE.equals(user.get(IS_NEW));
		final boolean isDisabled = isDisabled(user);
		final boolean isActive = !isInactive(user);
		final String updateUserId = (String) user.get(IUser.USER_ID);
		if (updateUserId == null) {
		  return new TransferObject(TransferObject.ERROR, "NO_USER_ID_SUPPLIED");
		}
    if(isNew){
			if(getReadOnlyProvider().queryObject(CHECK_IF_USER_EXIST_QUERY_NAME,user)!= null){
				throw new AbortTransactionException(USER_EXIST);
			}
    }
    // if the update is active & not new
    // I need to know the current status
    // to check license count
    if (isActive && !isNew){
      Long licensedActiveUserCount = getLicenseActiveUserCount();
      if (licensedActiveUserCount != null) {
        boolean isCurrentlyActive = isCurrentlyActiveUser(updateUserId);
        if (!isCurrentlyActive) {
          Long currentActiveUserCount = getCurrectActiveUserCount();
          if (currentActiveUserCount.longValue() + 1 > licensedActiveUserCount.longValue()){
            return new TransferObject(new Object[]{licensedActiveUserCount}, TransferObject.ERROR, "WOULD_EXCEED_NAMED_USER_LICENSE");
          }
        }
      }
    } else if(isNew) {
      Long licensedActiveUserCount = getLicenseActiveUserCount();
      if (licensedActiveUserCount != null) {
        Long currentActiveUserCount = getCurrectActiveUserCount();
        if (currentActiveUserCount.longValue() + 1 > licensedActiveUserCount.longValue()){
          return new TransferObject(new Object[]{licensedActiveUserCount}, TransferObject.ERROR, "WOULD_EXCEED_NAMED_USER_LICENSE");
        }
      }
    }
    final MapLoginService l = (MapLoginService) getLogin();
    String userid = (String) user.get(IUser.USER_ID);
    if (user.get("PASSWORD").equals("UNCHANGED")) {
      user.put("PASSWORD", l.getEncrypted(userid).getBeanHolder());
    } else {
      user.put("PASSWORD", l.encrypt(userid, (String) user.get("PASSWORD")).getBeanHolder());
    }
    if (isNew) {
			getReadWriteProvider().execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
				  
					getReadWriteProvider().insert(INSERT_USER_QUERY_NAME,user,t);
					getReadWriteProvider().delete(DELETE_USER_TEAM_LINK_QUERY_NAME,user,t);
					getReadWriteProvider().delete(DELETE_USER_ROLE_LINK_QUERY_NAME,user,t);
					insertList((List) user.get("TEAM_LIST"),user, INSERT_USER_TEAM_LINK_QUERY_NAME,t);
					insertList((List) user.get("ROLE_LIST"),user,INSERT_USER_ROLE_LINK_QUERY_NAME,t);
				}});
		} else {
			getReadWriteProvider().execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					if(!isDisabled){
						// is not disabled, doing normal update
						getReadWriteProvider().update(UPDATE_USER_QUERY_NAME,user,t);
						getReadWriteProvider().delete(DELETE_USER_TEAM_LINK_QUERY_NAME,user,t);
						getReadWriteProvider().delete(DELETE_USER_ROLE_LINK_QUERY_NAME,user,t);
						insertList((List) user.get("TEAM_LIST"),user,INSERT_USER_TEAM_LINK_QUERY_NAME,t);
						insertList((List) user.get("ROLE_LIST"),user,INSERT_USER_ROLE_LINK_QUERY_NAME,t);
					} else{
						// it is disabled
						if(user.get(USER_DELETION_FAILED)==null){
							getReadWriteProvider().delete(DELETE_USER_TEAM_LINK_QUERY_NAME,user,t);
							getReadWriteProvider().delete(DELETE_USER_ROLE_LINK_QUERY_NAME,user,t);
							try {
								getReadWriteProvider().delete(DELETE_USER_QUERY_NAME,user,t);
							} catch (SQLException e){
								throw new AbortTransactionException(USER_DELETION_FAILED,e);
							}
						}else{
							// Can't delete, so I just update the user to disable mode 
						  // and set the ACTIVE_STATUS to D
							user.put("ACTIVE_STATUS", Boolean.FALSE);
							getReadWriteProvider().update(UPDATE_USER_QUERY_NAME,user,t);
							getReadWriteProvider().delete(DELETE_USER_TEAM_LINK_QUERY_NAME,user,t);
							getReadWriteProvider().delete(DELETE_USER_ROLE_LINK_QUERY_NAME,user,t);
							insertList((List) user.get("TEAM_LIST"),user,INSERT_USER_TEAM_LINK_QUERY_NAME,t);
							insertList((List) user.get("ROLE_LIST"),user,INSERT_USER_ROLE_LINK_QUERY_NAME,t);
						}
					}
			}});
		}
		return new TransferObject(user.get("USER_ID"));
	}

  protected Long getLicenseActiveUserCount() {
    LicenseVerifier verifier = LicenseInterceptor.buildVerifier();
    return verifier.getLicenseInternalNamedUsers();
  }

  protected Long getCurrectActiveUserCount() {
    LicenseVerifier verifier = LicenseInterceptor.buildVerifier();
    return verifier.getCurrentInternalNamedUserCount();
  }

  protected boolean isCurrentlyActiveUser(final String updateUserId) {
    LicenseVerifier verifier = LicenseInterceptor.buildVerifier();
    return verifier.isActiveUser(updateUserId);
  }

  private void insertList(List list, Map arguments, String queryName, ITransaction t) throws SQLException{
		if(list==null) return;		
		for(int i=0;i<list.size();i++){
			Map m = new HashMap(arguments);
			m.putAll((Map) list.get(i));
			getReadWriteProvider().insert(queryName,m,t);
		}
	}
	
	private TransferObject updateRole(final Map role, final String userId) throws AbortTransactionException, SQLException{
		if(Boolean.TRUE.equals(role.get(IS_NEW))){
			getReadWriteProvider().execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					role.put("ROLE_CREATE_DATE", new Date());		
					role.put("ROLE_CREATE_BY", userId);		
					Object id = getReadWriteProvider().insert(INSERT_ROLE_QUERY_NAME,role,t);
					role.put("ROLE_ID", id);
				}});
		} else {
			getReadWriteProvider().execute(new IRunnableTransaction() {
				public void execute(ITransaction t) throws SQLException, AbortTransactionException {
					if(role.get("DISABLE")==null)
						role.put("DISABLE",new Integer(0));
					getReadWriteProvider().update(UPDATE_ROLE_QUERY_NAME,role,t);	
					if(role.containsKey("ACCESS_RIGHTS_LIST")){
						getReadWriteProvider().delete(DELETE_ROLE_ACCESS_REL_QUERY_NAME,role,t);
						insertList((List) role.get("ACCESS_RIGHTS_LIST"),role,INSERT_ROLE_ACCESS_REL_QUERY_NAME,t);
					}
			}});
		}
		return new TransferObject(role.get("ROLE_ID"));
	}

  private boolean isDisabled(final Map user) {
    Integer disable = (Integer) user.get("DISABLE");
    if (disable == null) {
      user.put("DISABLE", new Integer(0));
      return false;
    }
    return disable.intValue() == 1;
  }

  private boolean isInactive(final Map user) {
    Boolean active = (Boolean)user.get("ACTIVE_STATUS");
    if (active == null) {
      return false;
    }
    return !active.booleanValue();
  }

}
