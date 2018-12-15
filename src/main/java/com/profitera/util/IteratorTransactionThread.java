package com.profitera.util;

import com.profitera.persistence.PersistenceManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import oracle.toplink.sessions.DatabaseLogin;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

/**
 * @author jamison
 */
public abstract class IteratorTransactionThread extends IteratorConsumerThread {
	private Session session = null;
	private int commitSize;
	private int i;
	private UnitOfWork uow = null;
	protected boolean doCommit = true;
	List transactionObjects = null;

	/**
	 * @param i
	 */
	public IteratorTransactionThread(Iterator i, int commit) {
		super(i);
		commitSize = commit;
	}
	
	/**
	 * @see com.profitera.util.IteratorConsumerThread#setup()
	 */
	protected void setup() {
		session = getSession();
	}

	/**
	 * @see com.profitera.util.IteratorConsumerThread#consume(java.lang.Object)
	 */
	protected void consume(Object o) {
		i++;
		if (uow == null){
			uow = configureUnitOfWork(session.acquireUnitOfWork());
			transactionObjects = new ArrayList(commitSize); 
		}
		try{
			transactionObjects.add(o);
			process(o, uow);
		}catch (RuntimeException e){
			handleException(e, transactionObjects);
			uow.release();
			uow = null;
		}
		if (i % commitSize == 0 && uow!=null) {
			commitUoW();
		}
	}

	/**
	 * 
	 */
	private void commitUoW() {
		try{
			if (doCommit)
				uow.commit();
		}catch (RuntimeException e){
			handleException(e, transactionObjects);
		} finally {
			uow.release();
		}
		uow = null;
	}

	/**
	 * @see com.profitera.util.IteratorConsumerThread#cleanup()
	 */
	protected void cleanup() {
		if (uow != null)
			commitUoW();
		// I should be a client session, so I will release!
		session.release();
	}
	
	/**
	 * Called only once imediately after the thread is started, override this
	 * if you want to control the connection being used. But default it creates
	 * a new DB login and uses that to create a new session. Its very expensive
	 * but you only do it once.
	 * @return
	 */
	protected Session getSession() {
		DatabaseLogin login = new DatabaseLogin();
		PersistenceManager.configureLogin(login);
		return PersistenceManager.getSession().acquireClientSession(login);
	}
	
	/**
	 * You can override this method to config your UoW if you need to
	 * (basicly add read only classes if you need to).
	 * @param work
	 * @return
	 */
	protected UnitOfWork configureUnitOfWork(UnitOfWork work) {
		return work;
	}
	
	/**
	 * Provides an excessively lame error handler that prints the failed list
	 * of objects to stderr and then spits out the stack trace.
	 * @param e
	 * @param transactionObjects
	 */
	protected void handleException(RuntimeException e, List transactionObjects) {
		System.err.println(transactionObjects);
		e.printStackTrace();
	}
	
	/**
	 * Called on every object in the stream that this stream receives,
	 * the unit of work will be committed when the designated commit point
	 * is reached, don't mess with it -- just register your objects and 
	 * move on.
	 * @param o
	 * @param uow
	 */
	protected abstract void process(Object o, UnitOfWork uow);

}
