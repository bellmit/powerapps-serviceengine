package com.profitera.services.business.admin;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

import com.profitera.deployment.rmi.UserAndRoleServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.admin.AccessRightsBean;
import com.profitera.descriptor.business.admin.BusinessUnitBusinessBean;
import com.profitera.descriptor.business.admin.TeamBusinessBean;
import com.profitera.descriptor.business.admin.UserBusinessBean;
import com.profitera.descriptor.business.admin.UserRoleBusinessBean;
import com.profitera.descriptor.business.admin.UsersOfRoleBusinessBean;
import com.profitera.descriptor.business.reference.ReferenceBeanConverter;
import com.profitera.descriptor.db.contact.AddressDetails;
import com.profitera.descriptor.db.reference.AccessRightsRef;
import com.profitera.descriptor.db.reference.UserRoleRef;
import com.profitera.descriptor.db.relation.RoleAccessRel;
import com.profitera.descriptor.db.user.BusinessUnit;
import com.profitera.descriptor.db.user.Employee;
import com.profitera.descriptor.db.user.User;
import com.profitera.descriptor.db.user.UserTeams;
import com.profitera.persistence.SessionManager;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.system.dataaccess.BusinessUnitQueryManager;
import com.profitera.services.system.dataaccess.QueryManager;
import com.profitera.services.system.dataaccess.UserQueryManager;
import com.profitera.services.system.dataaccess.UserRoleRefQueryManager;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.PrimitiveValue;
import com.profitera.util.TopLinkQuery;

public class UserAndRoleService extends ProviderDrivenService implements UserAndRoleServiceIntf {
    private Map accessRightsMap;
    private UserQueryManager userqm;
    private BusinessUnitQueryManager bqm;
    private UserRoleRefQueryManager rqm;
    private QueryManager queryManager;
    private static final Double USER_COLLECTOR = new Double(1);
    
    private UserRoleRefQueryManager getRqm() {
      if (rqm == null){
        rqm = (UserRoleRefQueryManager) LookupManager.getInstance().getLookupItem(
            LookupManager.SYSTEM, "UserRoleRefQueryManager");
      }
      return rqm;
    }


    private BusinessUnitQueryManager getBqm() {
      if (bqm == null){
        bqm = (BusinessUnitQueryManager) LookupManager.getInstance().getLookupItem(
            LookupManager.SYSTEM, "BusinessUnitQueryManager");
      }
      return bqm;
    }

    private UserQueryManager getUqm() {
      if (userqm == null){
        userqm = (UserQueryManager) LookupManager.getInstance().getLookupItem(
            LookupManager.SYSTEM, "UserQueryManager");
      }
      return userqm;
    }

    public TransferObject getUserRoleBusinessBeans() {
        final Vector refs;
        final Vector roles = getQueryManager().get(roleIsNotDisabled(), UserRoleRef.class);
        refs = createUserRoleBusinessBean(roles);
        return new TransferObject(refs);
    }

    private static Expression roleIsNotDisabled() {
        final Expression d = new ExpressionBuilder().get(UserRoleRef.DISABLE);
        return d.notEqual(QueryManager.DISABLED).or(d.isNull());
    }

    public TransferObject getUsersOfRole(Double roleId) {
        final Vector userBeans = new Vector();
        final Expression exp = new ExpressionBuilder().anyOf(User.ROLES).get(UserRoleRef.ROLE_ID).equal(
            roleId).and(userIsNotDeleted());
        final Vector users = getQueryManager().get(exp, User.class);
        for (int i = 0; i < users.size(); i++) {
            final User user = (User) users.get(i);
            final UsersOfRoleBusinessBean bean = new UsersOfRoleBusinessBean();
            final Character status = user.getActiveStatus();
            bean.setActiveStatus(
                Boolean.valueOf(status != null && status.equals(UserBusinessBean.ACTIVE_STATUS) ? true : false));
            bean.setCreateDate(user.getCreateDate());
            bean.setEmailAddress(user.getUserEmailAddress());
            final Character logonStatus = user.getLogonStatus();
            bean.setLogonStatus(Boolean.valueOf(logonStatus.equals(UserBusinessBean.LOGIN_ACTIVE) ? true : false ));
            if (null != user.getEmployee()) {
                bean.setEmployeeId(user.getEmployee().getEmployeeId());
            }
            bean.setPasswordExpiry(user.getPasswdExpDate());
            bean.setUserExpiry(user.getUserExpDate());
            bean.setUserId(user.getUserId());
            userBeans.add(bean);
        }
        return new TransferObject(userBeans);
    }

    private static Expression userIsNotDeleted() {
        final Expression s = new ExpressionBuilder().get(User.ACTIVE_STATUS);
        return s.notEqual(QueryManager.DELETED).or(s.isNull());
    }

    public TransferObject getAllUsersOfTeam(String teamId) {
        final Vector userBeans;
        ExpressionBuilder builder = new ExpressionBuilder();
        final Expression exp = builder.anyOf(User.TEAMS).get(UserTeams.TEAM_ID).equal(
            teamId);
        final Expression exp1 =  builder.get(User.ACTIVE_STATUS).notEqual(QueryManager.DELETED);
        ReadAllQuery q = new ReadAllQuery(User.class, exp.and(exp1));
        q.addJoinedAttribute(builder.getAllowingNull(User.EMPLOYEE));
        //The advance join of address det cause perf problems!
        //q.addJoinedAttribute(builder.getAllowingNull(User.EMPLOYEE).getAllowingNull(Employee.ADDRESS_DETAILS));
        List users = TopLinkQuery.asList(q, SessionManager.getClientSession());
        userBeans = createUserBusinessBean(users);
        return new TransferObject(userBeans);
    }

    private static Expression buIsCollectionAndNotDisabled() {
        final ExpressionBuilder eb = new ExpressionBuilder();
        return eb.get(BusinessUnit.IS_COLLECTION).equal(BusinessUnitQueryManager.COLLECTION).and(
            eb.get(BusinessUnit.DISABLE).notEqual(QueryManager.DISABLED));
    }

    public TransferObject getAllBusinessUnits() {
        final Session session = SessionManager.getClientSession();
        final ExpressionBuilder eb = new ExpressionBuilder();
        final ReadAllQuery q = new ReadAllQuery(BusinessUnit.class, buIsCollectionAndNotDisabled());
        q.addOrdering(eb.get(BusinessUnit.SORT_PRIORITY));
        q.addOrdering(eb.get(BusinessUnit.BRANCH_NAME));
        final Vector units = (Vector) session.executeQuery(q);
        return new TransferObject(createBusinessUnitBusinessBean(units));
    }

    public TransferObject addBusinessUnit(BusinessUnitBusinessBean bean) {
        final String v = validateBusinessUnitBean(bean);
        if (null != v) return new TransferObject(TransferObject.ERROR, v);
        final BusinessUnit unit = getBusinessUnitFromBusinessBean(bean);
        final BusinessUnit newUnit = getBusinessUnit(unit.getBranchId());
        if (null != newUnit && null != newUnit.getBranchId()) {
            return new TransferObject(Boolean.FALSE, TransferObject.ERROR,
                "Business unit to be added is already exist, please enter different id!");
        } else {
            getBqm().add(unit);
        }
        return new TransferObject(Boolean.TRUE);
    }

    public TransferObject updateBusinessUnit(BusinessUnitBusinessBean bean) {
        final String v = validateBusinessUnitBean(bean);
        if (null != v) return new TransferObject(TransferObject.ERROR, v);
        final BusinessUnit unit = getBusinessUnitFromBusinessBean(bean);
        final BusinessUnit oldUnit = getBusinessUnit(unit.getBranchId());
        if (!(null == oldUnit || null == oldUnit.getBranchId())) {
            getBqm().update(oldUnit, unit);
        }
        return new TransferObject(Boolean.valueOf(true));
    }

    public final void addTeam(UserTeams team, UnitOfWork uow) {
      final UserTeams regTeam = (UserTeams) uow.registerNewObject(team);
      regTeam.setSortPriority(new Double(0));
      regTeam.setDisable(new Double(0));
  }
  private static Expression teamIsNotDisabled() {
        return new ExpressionBuilder().get(UserTeams.DISABLE).notEqual(QueryManager.DISABLED);
    }


    public TransferObject addUser(UserBusinessBean user) {
        final Expression exp = new ExpressionBuilder().get(User.USER_ID).equal(user.getUserId());
        final User oldUser = (User) getQueryManager().getObject(exp, User.class);
        if (oldUser == null){
        	final User newUser = createUserFromUserBusinessBean(user, new User());
            getUqm().add(newUser);
            return new TransferObject(Boolean.TRUE);
        }
        // from here on we know that oldUser is not null.
        if (oldUser.getActiveStatus().equals(QueryManager.DELETED)){
            return new TransferObject(TransferObject.ERROR,
                "User " + oldUser.getUserId() + " has previously been used");
        }
        final Vector teams = oldUser.getTeams();
        String teamName = "";
        String branchName = "";
        if (null != teams && 0 < teams.size()) {
        	final UserTeams team = (UserTeams) teams.get(0);
            teamName = team.getTeamId();
            final BusinessUnit unit = team.getBusinessUnit();
            if (null != unit) {
                branchName = unit.getBranchId();
            }
        }
        return new TransferObject(TransferObject.ERROR,
                "User " + oldUser.getUserId() + " already exists in branch " + branchName + ", team " + teamName);
    }

    public TransferObject deleteUser(String userId) {
        final Expression exp = new ExpressionBuilder().get(User.USER_ID).equal(userId);
        final User user = (User) getUqm().getObject(exp, User.class);
        getUqm().delete(user);
        return new TransferObject(Boolean.TRUE);
    }

    public TransferObject updateUser(UserBusinessBean user) {  //Fix bugs 805 & 711
        final Expression exp = new ExpressionBuilder().get(User.USER_ID).equal(user.getUserId());
        final User oldUser = (User) getQueryManager().getObject(exp, User.class);
        if (null == oldUser) {
            return new TransferObject(TransferObject.ERROR, "No such User");
        }
        UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        User updateUser;
        updateUser = (User) handle.registerExistingObject(oldUser);
        updateUser.setActiveStatus(user.getActiveStatus());
        updateUser.setLogonStatus(user.getLogonStatus());
        //savingUser.setCreateDate(user.getCreateDate());
        updateUser.setUserExpDate(user.getUserExpDate());
        //Employee
        final Expression expEmp = new ExpressionBuilder().get(Employee.EMPLOYEE_ID).equal(
            user.getEmployeeId());
        ReadObjectQuery q = new ReadObjectQuery(Employee.class, expEmp);
        Employee emp = (Employee) handle.executeQuery(q);
        updateUser.setEmployee(emp);
        //Roles
        Vector roles = user.getRoles();
        Vector userRoles = new Vector();
        for (int i = 0; i < roles.size(); i++) {
            final Expression roleExp = new ExpressionBuilder().get(UserRoleRef.ROLE_ID).equal(
                roles.get(i));
            ReadObjectQuery qRole = new ReadObjectQuery(UserRoleRef.class, roleExp);
            Object rolesObject = handle.executeQuery(qRole);
            userRoles.add(rolesObject);
        }
        final Vector ids = user.getTeams();
        final Vector teams = new Vector();
        for (int i = 0; i < ids.size(); i++) {
            final Expression exp1 = new ExpressionBuilder().get(UserTeams.TEAM_ID).equal(ids.get(i));
            teams.add(handle.readObject(UserTeams.class,exp1));
        }
        updateUser.setTeams(teams);
        updateUser.setRoles(userRoles);
        updateUser.setPasswdExpDate(user.getPasswdExpDate());
        if (user.getPassword() != null && !user.getPassword().equals("UNCHANGED")) {
          String ecryptedPassword = getEncryptedPassword(user);
          updateUser.setPassword(ecryptedPassword);
        }
        updateUser.setUserEmailAddress(user.getUserEmailAddress());
        handle.commit();
        return new TransferObject(Boolean.TRUE);
    }


    private String getEncryptedPassword(UserBusinessBean user) {
      String cleartext = user.getPassword();
      MapLoginService l = (MapLoginService) getLogin();
      String ecryptedPassword = (String) l.encrypt(user.getUserId(), cleartext).getBeanHolder();
      return ecryptedPassword;
    }

    public TransferObject addRole(UserRoleBusinessBean role, String user) {
        final String v = validateRole(role, false);
        if (null != v) return new TransferObject(TransferObject.ERROR, v);
        final UserRoleRef newRole = new UserRoleRef();
        newRole.setRoleDesc(role.getRoleDescription());
        newRole.setRoleCreatedDate(new Timestamp(System.currentTimeMillis()));
        newRole.setRoleCreatedBy(user);
        //not available in gui i presume.. jambu issue
        //currently i default to 1 yr from creation
        //ref.setRoleExpiryDate();
        newRole.setRoleName(role.getRoleName());
        getRqm().add(newRole);
        role.setRoleId(newRole.getRoleId());
        return new TransferObject(role);
    }

    public TransferObject deleteRole(Double roleId) {
        final UserRoleRef role = (UserRoleRef) getRqm().getObject(
            new ExpressionBuilder().get(UserRoleRef.ROLE_ID).equal(roleId), UserRoleRef.class);
        getRqm().delete(role);
        return new TransferObject(Boolean.TRUE);
    }

    public TransferObject updateRole(UserRoleBusinessBean role) {
        final String v = validateRole(role, true);
        if (null != v) return new TransferObject(TransferObject.ERROR, v);
        final Expression exp = new ExpressionBuilder().get(UserRoleRef.ROLE_ID).equal(
            role.getRoleId());
        final UserRoleRef oldRole = (UserRoleRef) getQueryManager().getObject(exp, UserRoleRef.class);
        if (null == oldRole) {
            return new TransferObject(TransferObject.ERROR,
                "User role with the id do not exist.. update can only be performed on existing user role!");
        }
        oldRole.setRoleName(role.getRoleName());
        oldRole.setRoleDesc(role.getRoleDescription());
        getRqm().update(oldRole);
        return new TransferObject(Boolean.TRUE);
    }

    private static Vector getTeamIdsFromTeams(Vector teams) {
        final Vector teamIds = new Vector();
        for (int i = 0; i < teams.size(); i++) {
            teamIds.add(((UserTeams) teams.get(i)).getTeamId());
        }
        return teamIds;
    }

    private static String validateRole(UserRoleBusinessBean bean, boolean forUpdate) {
        if (null == bean) {
            return "Invalid role information!";
        } else if (forUpdate && (null == bean.getRoleId() || 0 == bean.getRoleId().intValue())) {
            return "Invalid id of role to be updated!";
        } else if (null == bean.getRoleName() || 0 == bean.getRoleName().trim().length()) {
            return "Invalid role name, please provide a valid role name!";
        }
        return null;
    }

    private User createUserFromUserBusinessBean(UserBusinessBean bean, User user) {
        //final User user = new User();
        user.setActiveStatus(bean.getActiveStatus());
        user.setCreateDate(new Timestamp(System.currentTimeMillis()));
        final ExpressionBuilder builder = new ExpressionBuilder();
        final Expression exp = builder.get(Employee.EMPLOYEE_ID).equal(bean.getEmployeeId());
        user.setEmployee((Employee) getQueryManager().getObject(exp, Employee.class));
        user.setIsCollector(USER_COLLECTOR);
        user.setLogonStatus(bean.getLogonStatus());
        user.setPasswdExpDate(bean.getPasswdExpDate());
        String newPasswordPlainText = bean.getPassword();
        user.setPassword(getEncryptedPassword(bean));
        final Vector roleIds = bean.getRoles();
        final Vector roles = new Vector();
        for (int i = 0; i < roleIds.size(); i++) {
            final Expression roleexp = builder.get(UserRoleRef.ROLE_ID).equal(roleIds.get(i));
            roles.add(getQueryManager().getObject(roleexp, UserRoleRef.class));
        }
        user.setRoles(roles);
        final Vector ids = bean.getTeams();
        final Vector teams = new Vector();
        for (int i = 0; i < ids.size(); i++) {
            final Expression exp1 = builder.get(UserTeams.TEAM_ID).equal(ids.get(i));
            teams.add(getQueryManager().getObject(exp1, UserTeams.class));
        }
        user.setTeams(teams);
        user.setUserEmailAddress(bean.getUserEmailAddress());
        user.setUserExpDate(bean.getUserExpDate());
        user.setUserId(bean.getUserId());
        return user;
    }

    public TransferObject deleteBusinessUnit(String bussUnitId) {
        getBqm().delete(getBusinessUnit(bussUnitId));
        return new TransferObject(Boolean.TRUE);
    }

    private static BusinessUnit getBusinessUnitFromBusinessBean(BusinessUnitBusinessBean bean) {
        final BusinessUnit unit = new BusinessUnit();
        unit.setBranchCreateDate(bean.getDateCreated());
        unit.setBranchCreateBy(bean.getCreatedBy());
        unit.setBranchId(bean.getBranchId());
        unit.setBranchName(bean.getName());
        unit.setBranchType(bean.getType());
        unit.setHeadUser(getUser(bean.getUnitHeadId()));
        unit.setParentBranchId(bean.getParentUnitId());
        return unit;
    }

    public static User getUser(String userId) {
        final Expression exp = new ExpressionBuilder().get(User.USER_ID).equal(userId);
        return (User) SessionManager.getClientSession().readObject(User.class, exp);
    }

    private BusinessUnit getBusinessUnit(String unitId) {
        final Expression exp = new ExpressionBuilder().get(BusinessUnit.BRANCH_ID).equal(unitId);
        return (BusinessUnit) getQueryManager().getObject(exp, BusinessUnit.class);
    }

    private static String validateBusinessUnitBean(BusinessUnitBusinessBean bean) {
        if (null == bean) {
            return "Business unit to be added/updated is invalid!";
        } else if (null == bean.getBranchId() || 0 == bean.getBranchId().trim().length()) {
            return "Please provide a valid branch id!";
        } else if (null == bean.getName() || 0 == bean.getName().trim().length()) {
            return "Please provide a valid branch name!";
        } else if (null == bean.getType() || 0 == bean.getType().trim().length()) {
            return "Please provide a valid branch type!";
        }
        return null;
    }

    private static Vector createBusinessUnitBusinessBean(Vector units) {
        final Vector beans = new Vector();
        for (int i = 0; i < units.size(); i++) {
            beans.add(createBusinessUnitBusinessBean((BusinessUnit) units.get(i)));
        }
        return beans;
    }

    private static BusinessUnitBusinessBean createBusinessUnitBusinessBean(BusinessUnit unit) {
        if (null == unit) return null;
        final BusinessUnitBusinessBean bean = new BusinessUnitBusinessBean();
        bean.setBranchId(unit.getBranchId());
        bean.setCreatedBy(unit.getBranchCreateBy());
        bean.setDateCreated(unit.getBranchCreateDate());
        bean.setName(unit.getBranchName());
        bean.setParentUnitId(unit.getParentBranchId());
        //bean.setPhoneNo(getPhoneNumber(add));
        bean.setType(unit.getBranchType());
        final User head = unit.getHeadUser();
        bean.setUnitHeadId(null != head ? head.getUserId() : "");
        bean.setTeams(createTeamBusinessBeans(unit));
        return bean;
    }

    static Vector createTeamBusinessBeans(BusinessUnit unit) {
        final Vector teams = unit.getTeams();
        final Vector beans = new Vector();
        for (int i = 0; i < teams.size(); i++) {
            beans.add(createTeamBusinessBean((UserTeams) teams.get(i)));
        }
        return beans;
    }

    private static TeamBusinessBean createTeamBusinessBean(UserTeams team) {
        if (null == team) return null;
        final TeamBusinessBean bean = new TeamBusinessBean();
        bean.setCreatedUser(team.getTeamCreateBy());
        bean.setDateCreated(team.getTeamCreateDate());
        bean.setDescription(team.getTeamDesc());
        bean.setTeamId(team.getTeamId());
        if (null != team.getBusinessUnit()) {
            bean.setLocationId(team.getBusinessUnit().getBranchId());
            if (null != team.getBusinessUnit().getHeadUser()) {
                bean.setTeamLeader(team.getBusinessUnit().getHeadUser().getUserId());
            }
        }
        bean.setDepartment(team.getDepartment());
        if (null != team.getTreatmentStageRef()) {
            bean.setTreatmentStage(
                ReferenceBeanConverter.convertToBusinessBean(team.getTreatmentStageRef()));
        }
        bean.setAutoAssign(team.getAutoAssign());
        bean.setCapacity(team.getTeamCapacity());
        return bean;
    }

    private static Vector createUserBusinessBean(List users) {
        final Vector beans = new Vector();
        for (int i = 0; i < users.size(); i++) {
            beans.add(createUserBusinessBean((User) users.get(i)));
        }
        return beans;
    }

    public static UserBusinessBean createUserBusinessBean(User user) {
        final Employee emp = user.getEmployee();
        AddressDetails add = null;
        Vector reportsTo = new Vector();
        if (null != emp) {
            add = emp.getAddressDetails();
            reportsTo = emp.getReportsTo();
        }
        final Employee rpt = null == reportsTo || 0 == reportsTo.size() ? null : (Employee) reportsTo.get(
            0);
        final UserBusinessBean bean = new UserBusinessBean();
        bean.setActiveStatus(user.getActiveStatus());
        bean.setCreateDate(user.getCreateDate());
        if (null != add) {
            bean.setFirstName(PrimitiveValue.stringValue(add.getContactFirstName(), ""));
            bean.setMiddleName(PrimitiveValue.stringValue(add.getContactMiddleName(), ""));
            bean.setLastName(PrimitiveValue.stringValue(add.getContactLastName(), ""));
        } else {
            bean.setFirstName("");
            bean.setMiddleName("");
            bean.setLastName("");
        }
        if (null != emp) {
            bean.setEmployeeId(user.getEmployee().getEmployeeId());
        }
        bean.setLogonStatus(user.getLogonStatus());
        bean.setPasswdExpDate(user.getPasswdExpDate());
        bean.setPassword("UNCHANGED");
        bean.setReportTo(null == rpt ? "" : rpt.getEmployeeId());
        bean.setRoles(createUserRoleBusinessBean(user.getRoles()));
        bean.setTeams(getTeamIdsFromTeams(user.getTeams()));
        bean.setUserEmailAddress(user.getUserEmailAddress());
        bean.setUserExpDate(user.getUserExpDate());
        bean.setUserId(user.getUserId());
        return bean;
    }

    private static Vector createUserRoleBusinessBean(Vector roles) {
        final Vector beans = new Vector();
        for (int i = 0; i < roles.size(); i++) {
            final UserRoleRef ref = (UserRoleRef) roles.get(i);
            if (ref.getDisable().equals(new Double(0))) {
               final UserRoleBusinessBean bean = new UserRoleBusinessBean();
               bean.setRoleDescription(ref.getRoleDesc());
               bean.setRoleId(ref.getRoleId());
               bean.setRoleName(ref.getRoleName());
               beans.add(bean);
            }
        }
        return beans;
    }

    public TransferObject getAllTeams() {
        final Session session = SessionManager.getClientSession();
        final ExpressionBuilder eb = new ExpressionBuilder();
        final ReadAllQuery q = new ReadAllQuery(UserTeams.class, teamIsNotDisabled());
        q.addJoinedAttribute(eb.get(UserTeams.BUSINESS_UNIT));
        q.addJoinedAttribute(
            eb.get(UserTeams.BUSINESS_UNIT).getAllowingNull(BusinessUnit.HEAD_USER));
        final Iterator i = ((Vector) session.executeQuery(q)).iterator();
        final Vector beans = new Vector();
        while (i.hasNext()) {
            beans.add(createTeamBusinessBean((UserTeams) i.next()));
        }
        return new TransferObject(beans);
    }

    public TransferObject getAccessRights() {
    	ReadAllQuery q = new ReadAllQuery(AccessRightsRef.class);
    	q.setName("Get All Access Rights");
    	List roleAccessRels = TopLinkQuery.asList(q, SessionManager.getClientSession());
    	List beans = beanifyChildRights(null, roleAccessRels, 0);
    	return new TransferObject(beans);
    }

    private Map getAccessRightsMap() {
        if (null != accessRightsMap) return accessRightsMap;
        ReadAllQuery query = new ReadAllQuery(AccessRightsRef.class);
        query.setSelectionCriteria(new ExpressionBuilder().get(AccessRightsRef.DISABLE).equal(0d));
        final Vector rights = getQueryManager().get(query);
        accessRightsMap = new HashMap(rights.size());
        for (int i = 0; i < rights.size(); i++) {
            final AccessRightsRef ar = (AccessRightsRef) rights.get(i);
            accessRightsMap.put(ar.getAccessRightsId(), ar);
        }
        return accessRightsMap;
    }

    public static TransferObject getRoleRights(Double roleId) {
    	ExpressionBuilder eb = new ExpressionBuilder();
    	Expression e = eb.get(RoleAccessRel.USER_ROLE_REF).get(UserRoleRef.ROLE_ID).equal(roleId);
    	ReadAllQuery q = new ReadAllQuery(RoleAccessRel.class, e);
    	q.addJoinedAttribute(RoleAccessRel.ACCESS_RIGHTS_REF);
    	q.setName("Get Role Access Relations");
    	List roleAccessRels = TopLinkQuery.asList(q, SessionManager.getClientSession());
    	List beans = beanifyChildRights(null, roleAccessRels, 0);
    	return new TransferObject(beans);
    }

    public static List beanifyChildRights(AccessRightsBean parentBean, List relsOrRefs, int level) {
    	List beans = new ArrayList();
    	for (Iterator i = relsOrRefs.iterator(); i.hasNext();) {
    		Object o = i.next();
    		AccessRightsRef ref = null;
    		if (o instanceof RoleAccessRel)
    			ref = ((RoleAccessRel)o).getAccessRightsRef();
    		else
    			ref = (AccessRightsRef) o;
            if (ref.getParentAccessRightsId() == null && parentBean == null ||
                  parentBean != null && parentBean.getAccessRightsId().equals(ref.getParentAccessRightsId())){
               AccessRightsBean b = buildAccessBean(ref, parentBean);
               beans.add(b);
               beans.addAll(beanifyChildRights(b, relsOrRefs, level + 1));
            }
		}
		return beans;
	}

	public static AccessRightsBean buildAccessBean(AccessRightsRef ref, AccessRightsBean parentBean) {
		AccessRightsBean b = new AccessRightsBean();
		b.setAccessRightsId(ref.getAccessRightsId());
		b.setAccessRightsDesc(ref.getAccessRightsDesc());
		b.setParentBean(parentBean);
		b.setDisabled(new Double(1).equals(ref.getDisable()));
		b.setSortPriority(ref.getSortPriority() == null ? 0:ref.getSortPriority().intValue());
		return b;
	}

	public TransferObject addRoleRights(Vector rights, Double roleId) {
        final ExpressionBuilder builder = new ExpressionBuilder();
        final Expression expRole = builder.get(UserRoleRef.ROLE_ID).equal(roleId);
        final UserRoleRef userRoleRef = (UserRoleRef) getQueryManager().getObject(expRole,
            UserRoleRef.class);
        final Expression expRel = builder.get(RoleAccessRel.USER_ROLE_REF).equal(userRoleRef);
        final Vector relVector = getQueryManager().get(expRel, RoleAccessRel.class);
        if (null != relVector) {
            getQueryManager().deleteAllObjects(relVector);
        }
        final UnitOfWork uow = SessionManager.getClientSession().acquireUnitOfWork();
        uow.addReadOnlyClass(AccessRightsRef.class);
        uow.addReadOnlyClass(UserRoleRef.class);
        for (int i = 0; i < rights.size(); i++) {
            final RoleAccessRel rel = (RoleAccessRel) uow.newInstance(RoleAccessRel.class);
            rel.setAccessRightsRef((AccessRightsRef) getAccessRightsMap().get(rights.get(i)));
            rel.setUserRoleRef(userRoleRef);
        }
        uow.commit();
        return new TransferObject(Boolean.TRUE);
    }

    private static Expression employeeIsNotDisabled() {
        final Expression r = new ExpressionBuilder().get(Employee.REMARKS);
        return r.notEqual(QueryManager.DELETED).or(r.isNull());
    }


    private QueryManager getQueryManager() {
      if (queryManager == null) {
        queryManager = (QueryManager) lookup.getLookupItem(LookupManager.SYSTEM, "QueryManager");
      }
      return queryManager;
    }
}