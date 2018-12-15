package com.profitera.services.business.admin;

import java.io.File;
import java.util.Vector;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.OptimisticLockException;
import oracle.toplink.publicinterface.UnitOfWork;
import oracle.toplink.queryframework.CursoredStream;
import oracle.toplink.queryframework.ReadAllQuery;

import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.deployment.rmi.SecurityMaintenanceServiceIntf;
import com.profitera.descriptor.db.history.PasswordHistory;
import com.profitera.descriptor.db.user.User;
import com.profitera.persistence.PersistenceManager;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.BusinessService;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.PassUtils;

public class SecurityMaintenanceService extends BusinessService implements SecurityMaintenanceServiceIntf
{
  public SecurityMaintenanceService()
  {
  }

  public String getDbEncryptionKeyFile()
  {
    IDataSourceConfiguration defaultDataSource = getDataSourceConfigurations().getDefaultDataSource();
    return returnAbsoluteFileName(defaultDataSource.getProperties().getProperty(PassUtils.DB_CRYPTKEY, null));
  }

  public String getDbPasswordFile()
  {
    IDataSourceConfiguration defaultDataSource = getDataSourceConfigurations().getDefaultDataSource();
    return returnAbsoluteFileName(defaultDataSource.getProperties().getProperty(IDataSourceConfiguration.PASSWORD_PROP_NAME, null));
  }

  public String getAppKeyFile()
  {
    final MapLoginService l = (MapLoginService) getLogin();
    if (l.isUsingHashedPasswords()) {
      throw new UnsupportedOperationException("Hashed passwords in use, application encryption key can not be changed");
    }
    return returnAbsoluteFileName(ServiceEngine.getProp(PassUtils.APP_CRYPTKEY, null));
  }

  public boolean refreshServerConfigs()
  {
    return ServiceEngine.refreshConfig();
  }

  public boolean migratePasswords(String oldKey, String newKey)
  {
    boolean migrated = false;
    UnitOfWork uow = PersistenceManager.getSession().acquireUnitOfWork();
    ReadAllQuery q = new ReadAllQuery(User.class);
    q.useCursoredStream(100, 100);
    CursoredStream stream = (CursoredStream) uow.executeQuery(q);
    log.info("Starting password migration..");
    while (stream.hasMoreElements())
    {
      User user = (User) stream.nextElement();
      Vector history = (Vector)user.getPasswordHistory();
      user.setPassword(PassUtils.encrypt(PassUtils.desDecrypt(user.getPassword(), oldKey).toCharArray(), newKey));
      for (int i = 0; i < (history != null ? history.size() : 0); i++)
      {
        PasswordHistory tmpHist = (PasswordHistory)history.get(i);
        tmpHist.setPassword(PassUtils.encrypt(PassUtils.desDecrypt(tmpHist.getPassword(), oldKey).toCharArray(), newKey));
      }
    }
    log.info("Commiting changes to db..");
    try
    {
      uow.commit();
      uow.release();
      migrated = true;
    }
    catch (DatabaseException ex)
    {
      log.error("Database exception while trying to commit password migration to db: " + ex.getMessage(), ex);
      uow.rollbackTransaction();
      uow.release();
    }
    catch (OptimisticLockException ex)
    {
      log.error("Lock exception while trying to commit password migration to db: " + ex.getMessage(), ex);
      uow.rollbackTransaction();
      uow.release();
    }
    return migrated;
  }

  private String returnAbsoluteFileName(String fileName)
  {
    File file = new File(fileName);
    return file.getAbsolutePath();
  }
}