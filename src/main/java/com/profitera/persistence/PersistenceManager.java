package com.profitera.persistence;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import oracle.toplink.publicinterface.Descriptor;
import oracle.toplink.queryframework.DataModifyQuery;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.SQLCall;
import oracle.toplink.sessions.DatabaseLogin;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;
import oracle.toplink.threetier.ServerSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.server.ServiceEngine;
import com.profitera.util.Strings;

public final class PersistenceManager {
    private static final Log LOG = LogFactory.getLog(PersistenceManager.class);
    public static final String LOG_MESSAGES_PROP_NAME = "LOG_MESSAGES";
    public static final String DRIVER_PROP_NAME = "DRIVER";
    public static final String DATABASE_VENDOR_PROP_NAME = "DATABASE_VENDOR";
    public static final String CHECK_STRUCTURE_PROP_NAME = "CHECK_STRUCTURE";
    public static final String READ_POOL_MIN = "READ_POOL_MIN";
    public static final String READ_POOL_MAX = "READ_POOL_MAX";
    public static final String DB2_PROP_VALUE = "DB2";
    public static final String OTHERDATABASE_PROP_VALUE = "OTHERDATABASE";
    public static final String MSSQLSERVER_PROP_VALUE = "MSSQLSERVER";
    public static final String ORACLE_PROP_VALUE = "ORACLE";
    public static final String[] VENDORS = {
        DB2_PROP_VALUE, OTHERDATABASE_PROP_VALUE, MSSQLSERVER_PROP_VALUE, ORACLE_PROP_VALUE
    };
    private static ServerSession session = null;

    private PersistenceManager() {
    }

    public static final ServerSession getSession() {
        if (session == null) {
            log("Starting Profitera Server....");
            final PASWorkBench project = new PASWorkBench(isUserCode3Enabled());
            session = (ServerSession) project.createServerSession();
            final int minRead = ServiceEngine.getIntProp(READ_POOL_MIN, 5);
            final int maxRead = ServiceEngine.getIntProp(READ_POOL_MAX, 10);
            session.useReadConnectionPool(minRead, maxRead);
            configureLogin(session.getLogin());
            session.setName(project.getName());
            //check if message log is enabled
            if (Boolean.toString(true).equalsIgnoreCase(
                ServiceEngine.getConfig(false).getProperty(LOG_MESSAGES_PROP_NAME))) {
                session.logMessages();
                String logRedirect = ServiceEngine.getConfig(false).getProperty("REDIRECT_LOG");
                if (logRedirect != null && logRedirect.toUpperCase().startsWith("T")){
                  session.setLog(new LoggingWriter(LogFactory.getLog(ServerSession.class)));
                }
            }
            session.login();
            if (Boolean.toString(true).equalsIgnoreCase(
                ServiceEngine.getConfig(false).getProperty(CHECK_STRUCTURE_PROP_NAME, "FALSE"))) {
                ensureConsistency(session);
            }
        }
        return session;
    }

    public static boolean isUserCode3Enabled() {
      return true;
    }


    private static void ensureConsistency(ServerSession session) {
        final Map m = session.getDescriptors();
        boolean errorOccurred = false;
        for (Iterator i = m.values().iterator(); i.hasNext();) {
            final Descriptor d = (Descriptor) i.next();
            try {
                final ReadAllQuery q = new ReadAllQuery(d.getJavaClass());
                q.setMaxRows(1); // get 1 row for testing purposes
                session.executeQuery(q);
            } catch (Throwable t) {
                errorOccurred = true;
                System.err.println("Table for descriptor: " + d.getJavaClass().getName() +
                    " is inconsistent with mapping");
            }
        }
        if (errorOccurred) {
            System.out.println("Database is inconsistent, service engine will not start");
            System.exit(-1);
        }
    }

    /**
     * Allocates the next 'allocationSize' numbers in the sequence for use
     * by the caller
     * <br></br>
     * CODE POTENTIALLY BRITTLE!
     *
     * @return the start of the sequnce of numbers available for the caller
     */
    public static int allocateSequenceNumbers(Class clazz, int allocationSize) {
        final ServerSession session = getSession();
        final DatabaseLogin login = session.getLogin();
        final String seqTableName = login.getSequenceTableName();
        final String seqNameFieldName = login.getSequenceNameFieldName();
        final String seqCounterFieldName = login.getSequenceCounterFieldName();
        final Session s = getClientSession();
        final Descriptor descriptor = s.getDescriptor(clazz);
        final UnitOfWork handle = s.acquireUnitOfWork();
        final Vector v = handle.executeSelectingCall(new SQLCall("select " + seqCounterFieldName +
            " from " + seqTableName + " where " + seqNameFieldName + " = '" + descriptor.getSequenceNumberName() + "'"));
        handle.executeQuery(
            new DataModifyQuery(
                "Update " + seqTableName + " set  " + seqCounterFieldName + " = " + seqCounterFieldName + " + " +
            allocationSize +
            " where " +
            seqNameFieldName +
            " = '" +
            descriptor.getSequenceNumberName() +
            "'"));
        handle.commit();
        return ((Number) ((Map) v.get(0)).values().iterator().next()).intValue();
    }

    /**
     * Acquires and returns a new client session
     */
    public static final Session getClientSession() {
        return getSession().acquireClientSession();
    }

    /**
     * Initialize a session login information
     */
    public static void configureLogin(DatabaseLogin login) {
        IDataSourceConfigurationSet sources = ServiceEngine.getDataSourceConfigurations();
        final Properties dbconf = sources.getDefaultDataSource().getProperties();
        if (dbconf.getProperty(IDataSourceConfiguration.URL_PROP_NAME) == null) {
            throw new RuntimeException("Required property '" + IDataSourceConfiguration.URL_PROP_NAME +
                "' not specified in server configuration file");
        }
        final String vendor = dbconf.getProperty(DATABASE_VENDOR_PROP_NAME);
        if (dbconf.getProperty(DATABASE_VENDOR_PROP_NAME) == null) {
            throw new RuntimeException("Required property '" + DATABASE_VENDOR_PROP_NAME +
                "' not specified in server configuration file");
        }
        login.setDriverClassName(dbconf.getProperty(DRIVER_PROP_NAME));
        login.setUserName(dbconf.getProperty(IDataSourceConfiguration.USERNAME_PROP_NAME));
        String password = dbconf.getProperty(IDataSourceConfiguration.CLEARTEXT_PASSWORD_PROP_NAME);
        if (password != null) {
            login.setPassword(password);
        }
        login.setConnectionString(dbconf.getProperty(IDataSourceConfiguration.URL_PROP_NAME));
        if (vendor.toUpperCase().equals(DB2_PROP_VALUE)) {
            login.useDB2();
        } else if (vendor.toUpperCase().equals(ORACLE_PROP_VALUE)) {
            login.useOracleThinJDBCDriver();
        } else if (vendor.toUpperCase().equals(MSSQLSERVER_PROP_VALUE)) {
            login.useSQLServer();
        } else if (vendor.toUpperCase().equals(OTHERDATABASE_PROP_VALUE)) {
            login.setConnectionString(dbconf.getProperty(IDataSourceConfiguration.URL_PROP_NAME));
            login.useJDBC();
        } else {
            throw new RuntimeException(DATABASE_VENDOR_PROP_NAME + " value '" + vendor +
                "' is invalid, use one of " + Strings.getListString(VENDORS, ", "));
        }
        login.useByteArrayBinding();
        // Batch writing added to server session, this may have bad
        // implications for Online, but its damn good for loading!
        boolean useBatch = dbconf.getProperty("BATCH_WRITE", "FALSE").substring(0,1).equalsIgnoreCase("T");
        boolean useBinding = dbconf.getProperty("BIND_PARAMS","FALSE").substring(0,1).equalsIgnoreCase("T");
        if (useBatch && useBinding){
        	log("Batch Writing AND Parameter binding requested, neither will be enabled");
        }else if (useBatch) {
            log("Batch Writing enabled");
            login.useBatchWriting();
        }else if (useBinding){
        	login.bindAllParameters();
        	login.cacheAllStatements();
        	login.setStatementCacheSize(Integer.parseInt(dbconf.getProperty("STATEMENT_CACHE")));
        	log("Parameter binding enabled, statement cache: " + dbconf.getProperty("STATEMENT_CACHE"));
        }
        log("######################################################");
        log(" Using project : " + getSession().getProject().getName());
        log(" Connecting to : " + dbconf.getProperty(IDataSourceConfiguration.URL_PROP_NAME));
        log("######################################################");
    }

    /**
     * Logs a message
     */
    private static void log(String message) {
      LOG.info("[PersistenceManager] " + message);
    }
}