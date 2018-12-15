package com.profitera.services.business.archive;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.profitera.deployment.rmi.DataArchivingServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.archive.ColumnBean;
import com.profitera.descriptor.business.archive.TableBean;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.archive.impl.ArchiveTracker;
import com.profitera.services.business.archive.impl.ArchivingService;
import com.profitera.services.system.dataaccess.SqlMapReadOnlyProvider;
import com.profitera.util.ArrayUtil;

public class DataArchivingService extends ProviderDrivenService implements
		DataArchivingServiceIntf {
	private static final String SERVICE = "dataarchivingservice";
	private static final String PATH = "location";
	public static final String ARCHIVE_PATH_PROP = SERVICE + "." + PATH;
	private ArchiveTracker tracker = new ArchiveTracker();

	public TransferObject checkNoIncompleteArchiveProcess() {
		return build().checkNoIncompleteArchiveProcess(null, null, tracker,
				getReadWriteProvider());
	}

	public TransferObject getArchiveDefinitions() {
		ServiceEngine.refreshConfig();
		return build().getArchiveDefinitions(getReadWriteProvider());
	}

	private ArchivingService build() {
		return new ArchivingService(log, getDataSourceConfigurations());
	}

	public TransferObject getArchiveMinimumRange(Long archiveId) {
		return build().getArchiveMinimumRange(archiveId, getReadWriteProvider());
	}

	public TransferObject getSelectedArchiveRecordCountInRange(Long archiveId,
			Object minValue, Object maxValue) {
		return build().getSelectedArchiveRecordCountInRange(archiveId, minValue,
				maxValue, getReadWriteProvider());
	}

	public TransferObject getArchiveProcesses(Long archiveId) {
		return build().getArchiveProcesses(archiveId, getReadWriteProvider());
	}

	public TransferObject startArchiving(Long archiveId, Long archiveProcessId,
			Comparable min, Comparable max, int commitSize, boolean waitForCompletion) {
		return build().startArchiving(archiveId, archiveProcessId, min, max,
				tracker, waitForCompletion, commitSize, getReadWriteProvider());
	}

	public TransferObject startCopying(Long archiveId, Long archiveProcessId,
			String fromArchive, String toArchive, int commitSize, boolean waitForCompletion) {
		return build().startCopying(archiveId, archiveProcessId, fromArchive,
				toArchive, tracker, waitForCompletion, commitSize, getReadWriteProvider());
	}

	public TransferObject startDeleting(Long archiveId, Long archiveProcessId,
			String fromArchive, boolean waitForCompletion) {
		return build().startDeleting(archiveId, archiveProcessId, fromArchive,
				tracker, waitForCompletion, getReadWriteProvider());
	}

	public TransferObject startDeletingSource(Long archiveId,
			Long archiveProcessId, int commitSize, boolean waitForCompletion) {
		return build().startDeletingSource(archiveId, archiveProcessId, tracker,
		    waitForCompletion, commitSize, getReadWriteProvider());
	}

	/*
	 * Archive Table Editor Services
	 */
	
	public class MetaDataProvider extends SqlMapReadOnlyProvider {
		public SqlMapClient getSqlMapClient() {
			return super.getSqlMapClient();
		}
	}
	
	public TransferObject getTables(String tableName) {
		SqlMapClient client = new MetaDataProvider().getSqlMapClient();
		try {
			Connection conn = client.getDataSource().getConnection();
			String catalog = conn.getCatalog();
			DatabaseMetaData meta = conn.getMetaData();
			List tables = new ArrayList();
			if(tableName==null){
				log.debug("[ArchiveEditor] Retrieving all Profitera's tables.");
				getTablesWithPrefix("PTR", catalog, meta, tables);
				getTablesWithPrefix("ptr", catalog, meta, tables);
			}else{ // get first level child table only
				log.debug("[ArchiveEditor] Retrieving child tables for table "+tableName);
				ResultSet rs = meta.getExportedKeys(catalog, null, tableName);
				while(rs.next()){
					String name = rs.getString(7); // child table name
					TableBean table = new TableBean();
					table.setName(name);
					ResultSet keyRs = meta.getPrimaryKeys(catalog, null, name);
					List keys = new ArrayList();
					while (keyRs.next()) {
						keys.add(keyRs.getString(4)); // key name
					}
					keyRs.close();
					ResultSet colRs = meta.getColumns(catalog, null, name, null);
					while (colRs.next()) {
						ColumnBean column = new ColumnBean();
						String colName = colRs.getString(4);
						column.setName(colName);
						boolean isKey = ArrayUtil.indexOf(colName, keys.toArray()) != -1;
						if (isKey) {
							column.isKey(isKey);
							/*int sqlType = colRs.getInt(5);
							column.setJavaType(sqlType);*/
						}
						int sqlType = colRs.getInt(5);
						column.setJavaType(sqlType);
						String exportCol = rs.getString(8);
						if(exportCol.equals(colName)){
							String parentKey = rs.getString(4);
							column.isKey(true);
							column.setParentKey(parentKey);
						}
						table.addColumn(column);
					}
					colRs.close();
					tables.add(table);
				}
				rs.close();
			}	
			conn.close();
			return new TransferObject(tables);
		} catch (SQLException e) {
			log.error("FAILED_TO_GET_TABLES", e);
			return new TransferObject(TransferObject.ERROR, "FAILED_TO_GET_TABLES");
		}
	}

	private void getTablesWithPrefix(String prefix, String catalog, DatabaseMetaData meta, List<TableBean> tables) throws SQLException {
		ResultSet rs = meta.getTables(catalog, null, prefix+"%", new String[] { "TABLE" });
		a:while (rs.next()) {
			String name = rs.getString(3); // table name
			for(int i=0;i<tables.size();i++){
				if(tables.get(i).getName().equals(name))
					continue a;
			}
			log.debug("[ArchiveEditor] Retrieving metadata for table "+name);
			TableBean table = new TableBean();
			table.setName(name);
			ResultSet keyRs = meta.getPrimaryKeys(catalog, null, name);
			List keys = new ArrayList();
			while (keyRs.next()) {
				keys.add(keyRs.getString(4));
			}
			keyRs.close();
			ResultSet colRs = meta.getColumns(catalog, null, name, null);
			while (colRs.next()) {
				ColumnBean column = new ColumnBean();
				String colName = colRs.getString(4);
				column.setName(colName);
				boolean isKey = ArrayUtil.indexOf(colName, keys.toArray()) != -1;
				if (isKey) {
					column.isKey(isKey);
					/*int sqlType = colRs.getInt(5);
					column.setJavaType(sqlType);*/
				}
				int sqlType = colRs.getInt(5);
				column.setJavaType(sqlType);
				table.addColumn(column);
			}
			colRs.close();
			tables.add(table);
		}
		rs.close();
	}
	
}
