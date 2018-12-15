package com.profitera.persistence;

import oracle.toplink.sessions.Session;

/**
 * Ensures that every thread will have its own ClientSession.  Also ensures that
 * every thread will use the same ClientSession
 */
public class SessionManager {
    private static final SessionDispenser dispenser = new SessionDispenser();
    private static class SessionDispenser extends ThreadLocal {
        protected Object initialValue() {
            return PersistenceManager.getClientSession();
        }

        //protected SessionBroker getBroker() {
        protected Session getClientSession() {
            return (Session) get();
        }
    }

    /**
     * @return an instance of SessionBroker for this current thread
     */
    public static Session getClientSession() {
        return dispenser.getClientSession();
    }
}