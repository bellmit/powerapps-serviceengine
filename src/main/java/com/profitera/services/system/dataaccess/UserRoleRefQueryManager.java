package com.profitera.services.system.dataaccess;

import com.profitera.descriptor.db.reference.UserRoleRef;
import com.profitera.persistence.SessionManager;
import oracle.toplink.sessions.UnitOfWork;

/**
 * <p>Title: Profitera Application Suite</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Profitera Corporation Sdn. Bhd.</p>
 *
 * @author Jambugesvarar Marimuthu
 * @version 1.0
 */
public final class UserRoleRefQueryManager extends QueryManager {
    public final UserRoleRef add(UserRoleRef ref) {
        final UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        ref.setSortPriority(defaultvalue);
        ref.setDisable(defaultvalue);
        handle.registerNewObject(ref);
        handle.commit();
        return (UserRoleRef) refreshObject(ref);
    }

    public final void delete(UserRoleRef ref) {
        final UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        ref = (UserRoleRef) handle.registerExistingObject(ref);
        ref.setDisable(DISABLED);
        handle.commit();
    }

    public final void update(UserRoleRef newRef) {
        final UnitOfWork handle = SessionManager.getClientSession().acquireUnitOfWork();
        handle.registerExistingObject(newRef);
        handle.commit();
    }
}