package com.profitera.services.business.appserver.impl;

import java.util.HashMap;
import java.util.Map;

import com.profitera.deployment.rmi.ApplicationServerServiceIntf;
import com.profitera.ibatis.SQLMapFileRenderer;
import com.profitera.util.Strings;

public class AppServerSQL {
  private static String V = SQLMapFileRenderer.V;
  private static String N = SQLMapFileRenderer.N;
  private static Class S = String.class;
  
  /*
   create table PTRAPP_SERVER (
     ID INTEGER NOT NULL,
     SERVER_NAME VARCHAR(200) not null,
     DESCRIPTION VARCHAR(1000),
     ACTIVE_SERVER VARCHAR(1) not null
   );
   alter table PTRAPP_SERVER ADD PRIMARY KEY (ID);
   
   create table PTRAPP_SERVER_NET (
     APP_SERVER_ID INTEGER NOT NULL,
     HOST_NAME VARCHAR(200),
     IP_ADDRESS VARCHAR(200)
   );
   alter table PTRAPP_SERVER_NET add foreign key (APP_SERVER_ID)
   references PTRAPP_SERVER (ID);
   
   create table PTRAPP_SERVER_MEMORY (
     APP_SERVER_ID INTEGER NOT NULL,
     VERIFIED_FLAGS VARCHAR(500),
     MEMORY_FLAGS VARCHAR(500)
   );
   alter table PTRAPP_SERVER_MEMORY add foreign key (APP_SERVER_ID)
   references PTRAPP_SERVER (ID);
   */

  public String getAppServerSQL(){
    SQLMapFileRenderer r = new SQLMapFileRenderer();
    StringBuilder b = new StringBuilder();
    b.append(r.renderHeader("appserverservice"));
    b.append(getSelectAllServers(r));
    b.append(r.renderSelect(
        AppServerService.SELECT_MAX_APPLICATION_SERVER_ID, Long.class, 
        "select max(ID) from PTRAPP_SERVER"));
    b.append(r.renderSelect(
        AppServerService.GET_APPLICATION_SERVER_ID_BY_NAME, Long.class, String.class, 
        "select ID from PTRAPP_SERVER where SERVER_NAME = #value#"));
    b.append(getServerInsertUpdate(r));
    //
    b.append(getServerNetworkInsertDelete(r));
    //
    b.append(r.renderUpdate(AppServerService.DISABLE_UPDATE, Long.class, 
        "update PTRAPP_SERVER set ACTIVE_SERVER = 'N' where ID = #value#"));
    b.append(r.renderUpdate(AppServerService.ENABLE_UPDATE, Long.class, 
        "update PTRAPP_SERVER set ACTIVE_SERVER = 'Y' where ID = #value#"));
    //
    b.append(r.renderDelete(AppServerService.DELETE_APP_MEM, Long.class, 
    "delete from PTRAPP_SERVER_MEMORY where APP_SERVER_ID = #value#"));
    b.append(r.renderParameterMap(AppServerService.INSERT_APP_MEM, Map.class, new String[]{
      AppServerService.APP_SERVER_ID, AppServerService.MEMORY_FLAGS, AppServerService.VERIFIED_FLAGS},
      new Class[]{Long.class, String.class, String.class}, 
      new String[]{N, V, V}));
    b.append(r.renderInsert(AppServerService.INSERT_APP_MEM, AppServerService.INSERT_APP_MEM, 
    "insert into PTRAPP_SERVER_MEMORY (" + AppServerService.APP_SERVER_ID + ", " 
        + AppServerService.MEMORY_FLAGS + ", " 
        + AppServerService.VERIFIED_FLAGS +") values (?,?,?)"));
    //
    b.append(r.renderFooter());
    return b.toString();
  }

  private String getServerNetworkInsertDelete(SQLMapFileRenderer r) {
    StringBuilder b = new StringBuilder();
    b.append(r.renderParameterMap(
        AppServerService.INSERT_APP_NET, Map.class, 
        new String[]{AppServerService.APP_SERVER_ID, 
          ApplicationServerServiceIntf.HOST_NAME, 
          ApplicationServerServiceIntf.IP_ADDRESS}, 
        new Class[]{Long.class, S, S}, 
        new String[]{N, V, V}));
    b.append(r.renderInsert(AppServerService.INSERT_APP_NET, 
        AppServerService.INSERT_APP_NET, 
        "insert into PTRAPP_SERVER_NET (APP_SERVER_ID, HOST_NAME, IP_ADDRESS) values (?, ?, ?)"));
    b.append(r.renderDelete(AppServerService.DELETE_APP_NET, 
        Long.class, "delete from PTRAPP_SERVER_NET where APP_SERVER_ID = #value#"));
    return b.toString();
  }

  private String getServerInsertUpdate(SQLMapFileRenderer r) {
    StringBuilder b = new StringBuilder();
    b.append(r.renderParameterMap(
        AppServerService.INSERT_APPLICATION_SERVER, Map.class, 
        new String[]{"SERVER_NAME", "DESCRIPTION", "ID"}, 
        new Class[]{S, S, Long.class}, 
        new String[]{V, V, N}));
    b.append(r.renderInsert(AppServerService.INSERT_APPLICATION_SERVER, 
        AppServerService.INSERT_APPLICATION_SERVER, 
        "insert into PTRAPP_SERVER (SERVER_NAME, DESCRIPTION, ID, ACTIVE_SERVER) values (?, ?, ?, 'Y')"));
    b.append(r.renderUpdate(AppServerService.UPDATE_APPLICATION_SERVER, 
        AppServerService.INSERT_APPLICATION_SERVER, 
        "update PTRAPP_SERVER set SERVER_NAME = ?, DESCRIPTION = ? where ID = ?"));
    return b.toString();
  }
  
  private String getSelectAllServers(SQLMapFileRenderer r) {
    String[] fields = new String[]{
        ApplicationServerServiceIntf.ID, 
        ApplicationServerServiceIntf.SERVER_NAME, 
        ApplicationServerServiceIntf.DESCRIPTION, 
        ApplicationServerServiceIntf.ACTIVE_SERVER, 
        ApplicationServerServiceIntf.IP_ADDRESS, 
        ApplicationServerServiceIntf.HOST_NAME};
    StringBuilder b = new StringBuilder();
    b.append(r.renderResultMap(AppServerService.GET_APPLICATION_SERVERS, HashMap.class, 
        fields, 
        new String[]{N, V, V, V, V, V}, null,
        new Class[]{Long.class, S, S, S, S, S}));
    b.append(r.renderSelect(AppServerService.GET_APPLICATION_SERVERS, 
        AppServerService.GET_APPLICATION_SERVERS, 
        "select " + Strings.getListString(fields, ", ") + " from PTRAPP_SERVER inner join PTRAPP_SERVER_NET on ID = APP_SERVER_ID" ));
    b.append(
        r.renderResultMap(AppServerService.GET_APPLICATION_SERVER_MEMORY_SETTINGS, 
            HashMap.class, 
            new String[]{AppServerService.MEMORY_FLAGS, AppServerService.VERIFIED_FLAGS}, 
            new String[]{V, V}, null, new Class[]{String.class, String.class}));
    b.append(r.renderSelect(AppServerService.GET_APPLICATION_SERVER_MEMORY_SETTINGS, 
        AppServerService.GET_APPLICATION_SERVER_MEMORY_SETTINGS,  
        Long.class, "select " + AppServerService.MEMORY_FLAGS + ", " + AppServerService.VERIFIED_FLAGS + " from PTRAPP_SERVER_MEMORY where APP_SERVER_ID = #value#" ));
    
    return b.toString();
  }

}
