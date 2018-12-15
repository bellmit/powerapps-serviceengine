package com.profitera.services.system.dataaccess;

import com.profitera.descriptor.db.contact.AddressDetails;
import com.profitera.descriptor.db.reference.EmployeeTypeRef;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.descriptor.db.reference.UserRoleRef;
import com.profitera.descriptor.db.user.BusinessUnit;
import com.profitera.descriptor.db.user.Employee;
import com.profitera.descriptor.db.user.User;
import com.profitera.descriptor.db.user.UserTeams;
import com.profitera.persistence.SessionManager;
import java.util.Vector;
import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.sessions.UnitOfWork;

public class UserQueryManager extends QueryManager {
    public void update(User user){
		UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        handle.registerExistingObject(user);
		handle.commit();
    }

    public void add(User user) {
        UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        handle.addReadOnlyClass(Employee.class);
        handle.addReadOnlyClass(UserRoleRef.class);
        handle.addReadOnlyClass(UserTeams.class);
        handle.registerNewObject(user);
        handle.commit();
    }

    public void addAllNew(User user, UserTeams team, UserRoleRef role, BusinessUnit branch, Employee emp,
                          AddressDetails add, EmployeeTypeRef empTypeRef) {
        UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        AddressDetails newAdd = (AddressDetails) handle.registerObject(add);
        handle.assignSequenceNumber(newAdd);
        EmployeeTypeRef newEmpTypeRef = (EmployeeTypeRef) handle.registerObject(empTypeRef);
        Employee newEmp = (Employee) handle.registerObject(emp);
        BusinessUnit newBranch = (BusinessUnit) handle.registerObject(branch);
        UserRoleRef newRole = (UserRoleRef) handle.registerObject(role);
        UserTeams newTeam = (UserTeams) handle.registerObject(team);
        TreatmentStageRef stageRef = (TreatmentStageRef) handle.registerObject(team.getTreatmentStageRef());
        User newUser = (User) handle.registerObject(user);
        newEmp.setAddressDetails(newAdd);
        newEmp.setEmployeeTypeRef(newEmpTypeRef);
        newUser.setEmployee(newEmp);
        Vector roles = new Vector();
        roles.add(newRole);
        newUser.setRoles(roles);
        newTeam.setBusinessUnit(newBranch);
        newTeam.setTreatmentStageRef(stageRef);
        Vector userTeams = new Vector();
        userTeams.add(newTeam);
        newUser.setTeams(userTeams);
        handle.commit();
        refreshObject(newUser);
    }

    public void delete(Vector users) {
        if (users == null) return;
        for (int i = 0; i < users.size(); i++) {
            delete((User) users.get(i));
        }
    }

    public void delete(User user) {
        UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        user = (User) handle.registerExistingObject(user);
        user.setActiveStatus(DELETED);
        handle.commit();
    }

    //get users from given teamId
    public Vector getUsersByTeamId(String teamId) {
        final ExpressionBuilder builder = new ExpressionBuilder();
        Expression exp1 = builder.anyOf("teams").get("teamId").equal(teamId);
        Expression exp2 = builder.get("userId").notEqual("SYSTEM");
        return getUserIdsByUserObjects(get(exp1.and(exp2), User.class));
    }

    //get users of given branch id
    public Vector getUsersByBranchId(String branchId) {
        final ExpressionBuilder builder = new ExpressionBuilder();
        Expression exp1 = builder.anyOf("teams").get("businessUnit").get("branchId").equal(branchId);
        Expression exp2 = builder.get("userId").notEqual("SYSTEM");
        return getUserIdsByUserObjects(get(exp1.and(exp2), User.class));
    }

    private Vector getUserIdsByUserObjects(Vector users) {
        Vector userIds = new Vector();
        for (int i = 0; i < users.size(); i++) {
            userIds.add(((User) users.get(i)).getUserId());
        }
        return userIds;
    }
}