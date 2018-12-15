package com.profitera.services.system.dataaccess;

import com.profitera.descriptor.db.user.BusinessUnit;
import com.profitera.descriptor.db.user.User;
import com.profitera.persistence.SessionManager;
import java.util.Vector;
import oracle.toplink.sessions.UnitOfWork;

public class BusinessUnitQueryManager extends QueryManager {
    public static final Double COLLECTION = new Double(1); // Business Unit is a collection

    public void update(BusinessUnit oldPtrbusinessUnit, BusinessUnit newPtrbusinessUnit) {
        UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        BusinessUnit wCopy = (BusinessUnit) handle.registerObject(oldPtrbusinessUnit);
        wCopy.setBranchId(newPtrbusinessUnit.getBranchId());
        wCopy.setBranchName(newPtrbusinessUnit.getBranchName());
        wCopy.setBranchType(newPtrbusinessUnit.getBranchType());
        wCopy.setHeadUser((User) handle.registerObject(newPtrbusinessUnit.getHeadUser()));
        wCopy.setParentBranchId(newPtrbusinessUnit.getParentBranchId());
        wCopy.setParentBusinessUnit((BusinessUnit) handle.registerObject(newPtrbusinessUnit.getParentBusinessUnit()));
        wCopy.setBranchCreateBy(newPtrbusinessUnit.getBranchCreateBy());
        wCopy.setBranchCreateDate(newPtrbusinessUnit.getBranchCreateDate());
        handle.commit();
    }

    public void add(BusinessUnit unit) {
        UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        Vector children = handle.registerAllObjects(unit.getChildBusinessUnit());
        User newUser = (User) handle.registerObject(unit.getHeadUser());
        BusinessUnit parent = (BusinessUnit) handle.registerObject(unit.getParentBusinessUnit());
        Vector teams = handle.registerAllObjects(unit.getTeams());
        BusinessUnit newUnit = (BusinessUnit) handle.registerObject(unit);
        newUnit.setChildBusinessUnit(children);
        newUnit.setHeadUser(newUser);
        newUnit.setParentBusinessUnit(parent);
        newUnit.setTeams(teams);
        newUnit.setIsCollection(COLLECTION);
        newUnit.setDisable(defaultvalue);
        newUnit.setSortPriority(defaultvalue);
        handle.commit();
        refreshObject(newUnit);
    }

    public void delete(BusinessUnit unit) {
        UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        unit = (BusinessUnit) handle.registerExistingObject(unit);
        unit.setDisable(DISABLED);
        handle.commit();
    }
}