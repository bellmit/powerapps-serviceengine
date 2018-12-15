package com.profitera.util;

import java.io.Writer;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.ExceptionHandler;
import oracle.toplink.exceptions.IntegrityChecker;
import oracle.toplink.exceptions.OptimisticLockException;
import oracle.toplink.exceptions.QueryException;
import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.expressions.Expression;
import oracle.toplink.publicinterface.DatabaseRow;
import oracle.toplink.publicinterface.Descriptor;
import oracle.toplink.queryframework.Call;
import oracle.toplink.queryframework.DatabaseQuery;
import oracle.toplink.queryframework.InMemoryQueryIndirectionPolicy;
import oracle.toplink.remote.CacheSynchronizationManager;
import oracle.toplink.sessions.DatabaseLogin;
import oracle.toplink.sessions.ExternalTransactionController;
import oracle.toplink.sessions.ObjectCopyingPolicy;
import oracle.toplink.sessions.Project;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.SessionEventManager;
import oracle.toplink.sessions.SessionLog;
import oracle.toplink.sessions.SessionLogEntry;
import oracle.toplink.sessions.SessionProfiler;
import oracle.toplink.sessions.UnitOfWork;

/**
 * @author jamison
 */
public class FakeUnitOfWork implements UnitOfWork {
	private Session s;
	
	public FakeUnitOfWork(Session session){
		s = session;
	}

	/**
	 * @return
	 */
	public oracle.toplink.publicinterface.UnitOfWork acquireUnitOfWork() {
		return s.acquireUnitOfWork();
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public void addQuery(String arg0, DatabaseQuery arg1) {
		s.addQuery(arg0, arg1);
	}

	/**
	 * 
	 */
	public void clearIntegrityChecker() {
		s.clearIntegrityChecker();
	}

	/**
	 * 
	 */
	public void clearProfile() {
		s.clearProfile();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public boolean containsObjectInIdentityMap(Object arg0) {
		return s.containsObjectInIdentityMap(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public boolean containsObjectInIdentityMap(Vector arg0, Class arg1) {
		return s.containsObjectInIdentityMap(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public boolean containsQuery(String arg0) {
		return s.containsQuery(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Object copyObject(Object arg0) {
		return s.copyObject(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object copyObject(Object arg0, ObjectCopyingPolicy arg1) {
		return s.copyObject(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 */
	public boolean doesObjectExist(Object arg0) throws DatabaseException {
		return s.doesObjectExist(arg0);
	}

	/**
	 * 
	 */
	public void dontLogMessages() {
		s.dontLogMessages();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return s.equals(obj);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public int executeNonSelectingCall(Call arg0) {
		return s.executeNonSelectingCall(arg0);
	}

	/**
	 * @param arg0
	 * @deprecated
	 */
	public void executeNonSelectingSQL(String arg0) {
		s.executeNonSelectingSQL(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Object executeQuery(String arg0) {
		return s.executeQuery(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object executeQuery(String arg0, Class arg1) {
		return s.executeQuery(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public Object executeQuery(String arg0, Class arg1, Object arg2) {
		return s.executeQuery(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 */
	public Object executeQuery(String arg0, Class arg1, Object arg2, Object arg3) {
		return s.executeQuery(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @return
	 */
	public Object executeQuery(String arg0, Class arg1, Object arg2, Object arg3, Object arg4) {
		return s.executeQuery(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public Object executeQuery(String arg0, Class arg1, Vector arg2) {
		return s.executeQuery(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object executeQuery(String arg0, Object arg1) {
		return s.executeQuery(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public Object executeQuery(String arg0, Object arg1, Object arg2) {
		return s.executeQuery(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 */
	public Object executeQuery(String arg0, Object arg1, Object arg2, Object arg3) {
		return s.executeQuery(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object executeQuery(String arg0, Vector arg1) {
		return s.executeQuery(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws oracle.toplink.exceptions.TopLinkException
	 */
	public Object executeQuery(DatabaseQuery arg0) throws TopLinkException {
		return s.executeQuery(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object executeQuery(DatabaseQuery arg0, Vector arg1) {
		return s.executeQuery(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Vector executeSelectingCall(Call arg0) {
		return s.executeSelectingCall(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @deprecated
	 */
	public Vector executeSQL(String arg0) {
		return s.executeSQL(arg0);
	}

	/**
	 * @return
	 */
	public Session getActiveSession() {
		return s.getActiveSession();
	}

	/**
	 * @return
	 */
	public UnitOfWork getActiveUnitOfWork() {
		return s.getActiveUnitOfWork();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws oracle.toplink.exceptions.QueryException
	 * @deprecated
	 */
	public Vector getAllFromIdentityMap(Expression arg0, Class arg1, DatabaseRow arg2) throws QueryException {
		return s.getAllFromIdentityMap(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws oracle.toplink.exceptions.QueryException
	 */
	public Vector getAllFromIdentityMap(
		Expression arg0,
		Class arg1,
		DatabaseRow arg2,
		InMemoryQueryIndirectionPolicy arg3)
		throws QueryException {
		return s.getAllFromIdentityMap(arg0, arg1, arg2, arg3);
	}

	/**
	 * @return
	 */
	public CacheSynchronizationManager getCacheSynchronizationManager() {
		return s.getCacheSynchronizationManager();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Descriptor getDescriptor(Class arg0) {
		return s.getDescriptor(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Descriptor getDescriptor(Object arg0) {
		return s.getDescriptor(arg0);
	}

	/**
	 * @return
	 */
	public Hashtable getDescriptors() {
		return s.getDescriptors();
	}

	/**
	 * @return
	 */
	public SessionEventManager getEventManager() {
		return s.getEventManager();
	}

	/**
	 * @return
	 */
	public ExceptionHandler getExceptionHandler() {
		return s.getExceptionHandler();
	}

	/**
	 * @return
	 */
	public ExternalTransactionController getExternalTransactionController() {
		return s.getExternalTransactionController();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Object getFromIdentityMap(Object arg0) {
		return s.getFromIdentityMap(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object getFromIdentityMap(Vector arg0, Class arg1) {
		return s.getFromIdentityMap(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws oracle.toplink.exceptions.QueryException
	 * @deprecated
	 */
	public Object getFromIdentityMap(Expression arg0, Class arg1, DatabaseRow arg2) throws QueryException {
		return s.getFromIdentityMap(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws oracle.toplink.exceptions.QueryException
	 */
	public Object getFromIdentityMap(
		Expression arg0,
		Class arg1,
		DatabaseRow arg2,
		InMemoryQueryIndirectionPolicy arg3)
		throws QueryException {
		return s.getFromIdentityMap(arg0, arg1, arg2, arg3);
	}

	/**
	 * @return
	 */
	public IntegrityChecker getIntegrityChecker() {
		return s.getIntegrityChecker();
	}

	/**
	 * @return
	 */
	public Writer getLog() {
		return s.getLog();
	}

	/**
	 * @return
	 */
	public DatabaseLogin getLogin() {
		return s.getLogin();
	}

	/**
	 * @return
	 */
	public String getName() {
		return s.getName();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Number getNextSequenceNumberValue(Class arg0) {
		return s.getNextSequenceNumberValue(arg0);
	}

	/**
	 * @return
	 */
	public SessionProfiler getProfiler() {
		return s.getProfiler();
	}

	/**
	 * @return
	 */
	public Project getProject() {
		return s.getProject();
	}

	/**
	 * @return
	 */
	public Hashtable getProperties() {
		return s.getProperties();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Object getProperty(String arg0) {
		return s.getProperty(arg0);
	}

	/**
	 * @return
	 */
	public Hashtable getQueries() {
		return s.getQueries();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public DatabaseQuery getQuery(String arg0) {
		return s.getQuery(arg0);
	}

	/**
	 * @return
	 */
	public SessionLog getSessionLog() {
		return s.getSessionLog();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Object getWriteLockValue(Object arg0) {
		return s.getWriteLockValue(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object getWriteLockValue(Vector arg0, Class arg1) {
		return s.getWriteLockValue(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws java.lang.RuntimeException
	 */
	public Object handleException(RuntimeException arg0) throws RuntimeException {
		return s.handleException(arg0);
	}

	/**
	 * @return
	 */
	public boolean hasCacheSynchronizationManager() {
		return s.hasCacheSynchronizationManager();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public boolean hasDescriptor(Class arg0) {
		return s.hasDescriptor(arg0);
	}

	/**
	 * @return
	 */
	public boolean hasExceptionHandler() {
		return s.hasExceptionHandler();
	}

	/**
	 * @return
	 */
	public boolean hasExternalTransactionController() {
		return s.hasExternalTransactionController();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return s.hashCode();
	}

	/**
	 * 
	 */
	public void initializeAllIdentityMaps() {
		s.initializeAllIdentityMaps();
	}

	/**
	 * @param arg0
	 */
	public void initializeIdentityMap(Class arg0) {
		s.initializeIdentityMap(arg0);
	}

	/**
	 * 
	 */
	public void initializeIdentityMaps() {
		s.initializeIdentityMaps();
	}

	/**
	 * @return
	 */
	public boolean isClientSession() {
		return s.isClientSession();
	}

	/**
	 * @return
	 */
	public boolean isConnected() {
		return s.isConnected();
	}

	/**
	 * @return
	 */
	public boolean isDatabaseSession() {
		return s.isDatabaseSession();
	}

	/**
	 * @return
	 */
	public boolean isDistributedSession() {
		return s.isDistributedSession();
	}

	/**
	 * @return
	 */
	public boolean isInProfile() {
		return s.isInProfile();
	}

	/**
	 * @return
	 */
	public boolean isProxySession() {
		return s.isProxySession();
	}

	/**
	 * @return
	 */
	public boolean isRemoteSession() {
		return s.isRemoteSession();
	}

	/**
	 * @return
	 */
	public boolean isServerSession() {
		return s.isServerSession();
	}

	/**
	 * @return
	 */
	public boolean isSessionBroker() {
		return s.isSessionBroker();
	}

	/**
	 * @return
	 */
	public boolean isUnitOfWork() {
		return s.isUnitOfWork();
	}

	/**
	 * @param arg0
	 * @return
	 * @throws oracle.toplink.exceptions.ValidationException
	 */
	public Vector keyFromObject(Object arg0) throws ValidationException {
		return s.keyFromObject(arg0);
	}

	/**
	 * @param arg0
	 */
	public void log(SessionLogEntry arg0) {
		s.log(arg0);
	}

	/**
	 * @param arg0
	 */
	public void logDebug(String arg0) {
		s.logDebug(arg0);
	}

	/**
	 * @param arg0
	 */
	public void logException(Exception arg0) {
		s.logException(arg0);
	}

	/**
	 * @param arg0
	 */
	public void logMessage(String arg0) {
		s.logMessage(arg0);
	}

	/**
	 * 
	 */
	public void logMessages() {
		s.logMessages();
	}

	/**
	 * @param arg0
	 */
	public void printIdentityMap(Class arg0) {
		s.printIdentityMap(arg0);
	}

	/**
	 * 
	 */
	public void printIdentityMaps() {
		s.printIdentityMaps();
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Object putInIdentityMap(Object arg0) {
		return s.putInIdentityMap(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public Object putInIdentityMap(Object arg0, Vector arg1) {
		return s.putInIdentityMap(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public Object putInIdentityMap(Object arg0, Vector arg1, Object arg2) {
		return s.putInIdentityMap(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 */
	public Vector readAllObjects(Class arg0) throws DatabaseException {
		return s.readAllObjects(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 * @deprecated
	 */
	public Vector readAllObjects(Class arg0, String arg1) throws DatabaseException {
		return s.readAllObjects(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 */
	public Vector readAllObjects(Class arg0, Expression arg1) throws DatabaseException {
		return s.readAllObjects(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 */
	public Vector readAllObjects(Class arg0, Call arg1) throws DatabaseException {
		return s.readAllObjects(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 */
	public Object readObject(Class arg0) throws DatabaseException {
		return s.readObject(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 * @deprecated
	 */
	public Object readObject(Class arg0, String arg1) throws DatabaseException {
		return s.readObject(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 */
	public Object readObject(Class arg0, Expression arg1) throws DatabaseException {
		return s.readObject(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 */
	public Object readObject(Class arg0, Call arg1) throws DatabaseException {
		return s.readObject(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @throws oracle.toplink.exceptions.DatabaseException
	 */
	public Object readObject(Object arg0) throws DatabaseException {
		return s.readObject(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Object refreshObject(Object arg0) {
		return s.refreshObject(arg0);
	}

	/**
	 * 
	 */
	public void release() {
		s.release();
	}

	/**
	 * @param arg0
	 */
	public void removeFromIdentityMap(Object arg0) {
		s.removeFromIdentityMap(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public void removeFromIdentityMap(Vector arg0, Class arg1) {
		s.removeFromIdentityMap(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public void removeProperty(String arg0) {
		s.removeProperty(arg0);
	}

	/**
	 * @param arg0
	 */
	public void removeQuery(String arg0) {
		s.removeQuery(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setCacheSynchronizationManager(CacheSynchronizationManager arg0) {
		s.setCacheSynchronizationManager(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setExceptionHandler(ExceptionHandler arg0) {
		s.setExceptionHandler(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setExternalTransactionController(ExternalTransactionController arg0) {
		s.setExternalTransactionController(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setIntegrityChecker(IntegrityChecker arg0) {
		s.setIntegrityChecker(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setLog(Writer arg0) {
		s.setLog(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setName(String arg0) {
		s.setName(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setProfiler(SessionProfiler arg0) {
		s.setProfiler(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public void setProperty(String arg0, Object arg1) {
		s.setProperty(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public void setSessionLog(SessionLog arg0) {
		s.setSessionLog(arg0);
	}

	/**
	 * @param arg0
	 */
	public void setShouldLogMessages(boolean arg0) {
		s.setShouldLogMessages(arg0);
	}

	/**
	 * @return
	 */
	public boolean shouldLogMessages() {
		return s.shouldLogMessages();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return s.toString();
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public void updateWriteLockValue(Object arg0, Object arg1) {
		s.updateWriteLockValue(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public void updateWriteLockValue(Vector arg0, Class arg1, Object arg2) {
		s.updateWriteLockValue(arg0, arg1, arg2);
	}

	/**
	 * @return
	 */
	public boolean usesExternalTransactionController() {
		return s.usesExternalTransactionController();
	}

	/**
	 * 
	 */
	public void validateCache() {
		s.validateCache();
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#addReadOnlyClass(java.lang.Class)
	 */
	public void addReadOnlyClass(Class arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#addReadOnlyClasses(java.util.Vector)
	 */
	public void addReadOnlyClasses(Vector arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#assignSequenceNumber(java.lang.Object)
	 */
	public void assignSequenceNumber(Object arg0) throws DatabaseException {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#assignSequenceNumbers()
	 */
	public void assignSequenceNumbers() throws DatabaseException {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#beginEarlyTransaction()
	 */
	public void beginEarlyTransaction() throws DatabaseException {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#commit()
	 */
	public void commit() throws DatabaseException, OptimisticLockException {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#commitAndResume()
	 */
	public void commitAndResume() throws DatabaseException, OptimisticLockException {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#commitAndResumeOnFailure()
	 */
	public void commitAndResumeOnFailure() throws DatabaseException, OptimisticLockException {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#deepMergeClone(java.lang.Object)
	 */
	public Object deepMergeClone(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#deepRevertObject(java.lang.Object)
	 */
	public Object deepRevertObject(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#deepUnregisterObject(java.lang.Object)
	 */
	public void deepUnregisterObject(Object arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#deleteAllObjects(java.util.Collection)
	 */
	public void deleteAllObjects(Collection arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#deleteAllObjects(java.util.Vector)
	 */
	public void deleteAllObjects(Vector arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#deleteObject(java.lang.Object)
	 */
	public Object deleteObject(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#dontPerformValidation()
	 */
	public void dontPerformValidation() {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#forceUpdateToVersionField(java.lang.Object, boolean)
	 */
	public void forceUpdateToVersionField(Object arg0, boolean arg1) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#getOriginalVersionOfObject(java.lang.Object)
	 */
	public Object getOriginalVersionOfObject(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#getParent()
	 */
	public oracle.toplink.publicinterface.Session getParent() {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#getValidationLevel()
	 */
	public int getValidationLevel() {
		// Unused method in stub implementation
		return 0;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#hasChanges()
	 */
	public boolean hasChanges() {
		// Unused method in stub implementation
		return false;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#isActive()
	 */
	public boolean isActive() {
		// Unused method in stub implementation
		return false;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#isClassReadOnly(java.lang.Class)
	 */
	public boolean isClassReadOnly(Class arg0) {
		// Unused method in stub implementation
		return false;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#isNestedUnitOfWork()
	 */
	public boolean isNestedUnitOfWork() {
		// Unused method in stub implementation
		return false;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#mergeClone(java.lang.Object)
	 */
	public Object mergeClone(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#mergeCloneWithReferences(java.lang.Object)
	 */
	public Object mergeCloneWithReferences(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#newInstance(java.lang.Class)
	 */
	public Object newInstance(Class arg0) {
		try {
			return arg0.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#performFullValidation()
	 */
	public void performFullValidation() {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#performPartialValidation()
	 */
	public void performPartialValidation() {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#printRegisteredObjects()
	 */
	public void printRegisteredObjects() {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#refreshAndLockObject(java.lang.Object)
	 */
	public Object refreshAndLockObject(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#refreshAndLockObject(java.lang.Object, short)
	 */
	public Object refreshAndLockObject(Object arg0, short arg1) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#registerAllObjects(java.util.Collection)
	 */
	public Vector registerAllObjects(Collection arg0) {
		return (Vector) arg0;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#registerAllObjects(java.util.Vector)
	 */
	public Vector registerAllObjects(Vector arg0) {
		return arg0;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#registerExistingObject(java.lang.Object)
	 */
	public Object registerExistingObject(Object arg0) {
		return arg0;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#registerNewObject(java.lang.Object)
	 */
	public Object registerNewObject(Object arg0) {
		return arg0;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#registerObject(java.lang.Object)
	 */
	public Object registerObject(Object arg0) {
		return arg0;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#removeAllReadOnlyClasses()
	 */
	public void removeAllReadOnlyClasses() {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#removeForceUpdateToVersionField(java.lang.Object)
	 */
	public void removeForceUpdateToVersionField(Object arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#removeReadOnlyClass(java.lang.Class)
	 */
	public void removeReadOnlyClass(Class arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#revertAndResume()
	 */
	public void revertAndResume() {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#revertObject(java.lang.Object)
	 */
	public Object revertObject(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#setShouldNewObjectsBeCached(boolean)
	 */
	public void setShouldNewObjectsBeCached(boolean arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#setShouldPerformDeletesFirst(boolean)
	 */
	public void setShouldPerformDeletesFirst(boolean arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#setValidationLevel(int)
	 */
	public void setValidationLevel(int arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#shallowMergeClone(java.lang.Object)
	 */
	public Object shallowMergeClone(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#shallowRevertObject(java.lang.Object)
	 */
	public Object shallowRevertObject(Object arg0) {
		// Unused method in stub implementation
		return null;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#shallowUnregisterObject(java.lang.Object)
	 */
	public void shallowUnregisterObject(Object arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#shouldNewObjectsBeCached()
	 */
	public boolean shouldNewObjectsBeCached() {
		// Unused method in stub implementation
		return false;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#shouldPerformDeletesFirst()
	 */
	public boolean shouldPerformDeletesFirst() {
		// Unused method in stub implementation
		return false;
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#unregisterObject(java.lang.Object)
	 */
	public void unregisterObject(Object arg0) {
		// Unused method in stub implementation
		
	}

	/**
	 * @see oracle.toplink.sessions.UnitOfWork#validateObjectSpace()
	 */
	public void validateObjectSpace() {
		// Unused method in stub implementation
		
	}

}
