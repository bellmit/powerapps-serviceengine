package com.profitera.descriptor.rpm;

import java.util.ArrayList;

import com.profitera.util.Strings;

/**
 * @author jamison
 */
public class TableMap {

	private String schema;
	private String tableName;
	public static final String KEY_FIELD = "KEY_FIELD";
	public static final String VALUE_FIELD = "VALUE_FIELD";
	private static final String INDEX_SUFFIX = "IDX";
	public static final String JOIN_FIELD_PREFIX = "FIELD_";
	public static final String JOIN_KEY_FIELD_NAME = "JOIN_KEY_FIELD";
	private String idFieldDef;

	public TableMap(String schemaName, String name, String idFieldDef) {
		this.idFieldDef = idFieldDef;
		schema = schemaName;
		tableName = name;
	}

	public String getTableDDL() {
		return "CREATE TABLE " + getQualTableName()
			+ " ( "
			+ KEY_FIELD
			+ " " + idFieldDef + " NOT NULL, "
			+ VALUE_FIELD
			+ " DECIMAL(14,2) )";
	}

	public String getTableDrop() {
		return "DROP TABLE " + getQualTableName() + "";
	}

	public String getIndexDefinition() {
		return "CREATE  UNIQUE INDEX  "
			+ getQualIndexName()
			+ " ON " + getQualTableName()
			+ " ("
			+ KEY_FIELD
			+ " ASC)  CLUSTER  PCTFREE 10 MINPCTUSED 10";
	}

	public String getIndexDrop() {
		return "DROP INDEX " + getQualIndexName() + "";
	}

	/**
	 * You need to follow this up with a select statement that returns 2 columns per row
	 * the first will be assigned to the key and the second to the value. They need to 
	 * be compatible with BIGINT and DECIMAL(14,2)
	 * @return
	 * 
	 */
	public String getInsertFragment() {
		return "INSERT INTO " + getQualTableName() + " " + "(" + KEY_FIELD + ", " + VALUE_FIELD + ") ";
	}

	private String getQualTableName() {
		if (schema!=null)
			return schema + "." + tableName;
		else
			return tableName;
	}

	private String getQualIndexName() {
		return getQualTableName() + "_" + INDEX_SUFFIX;
	}
	
	private static String getQualKeyFieldName(TableMap m){
		return m.getQualTableName()+"."+KEY_FIELD;
	}
	
	private static String getQualValueFieldName(TableMap m){
		return m.getQualTableName() + "." + VALUE_FIELD;
	}
	
	public static String getCompleteJoin(TableMap[] maps){
		ArrayList selectList = new ArrayList();
		ArrayList tableList = new ArrayList();
		ArrayList whereList = new ArrayList();
		selectList.add(getQualKeyFieldName(maps[0]));
		for(int i=0;i<maps.length;i++){
			selectList.add(getQualValueFieldName(maps[i]) + " as " + JOIN_KEY_FIELD_NAME);
			tableList.add(maps[i].getQualTableName());
			whereList.add(getQualKeyFieldName(maps[0]) + "=" + getQualKeyFieldName(maps[i]));
		}
		// remove the first one since it says that the key in 0 should be equal to the key in 0
		whereList.remove(0);
		String selectLine = "SELECT " + Strings.getListString(selectList,", ") + "";
		String fromLine = " FROM " + Strings.getListString(tableList,", ") + "";
		String whereLine = " WHERE " + Strings.getListString(whereList, "AND ") + "";
		return selectLine + fromLine + whereLine;
		
	}
	
	public static String getCompleteOuterJoin(TableMap[] maps){
		return getCompleteOuterJoin(maps, -1);
	}
	
	public static String getCompleteOuterJoin(TableMap[] maps, int minId){
		return getCompleteOuterJoin(maps, minId, -1);
	}
	public static String getCompleteOuterJoin(TableMap[] maps, int minId, int maxId){
		ArrayList selectList = new ArrayList();
		ArrayList tableList = new ArrayList();
		ArrayList whereList = new ArrayList();
		selectList.add(getQualKeyFieldName(maps[0]) + " as " + JOIN_KEY_FIELD_NAME);
		for(int i=0;i<maps.length;i++){
			selectList.add(getQualValueFieldName(maps[i]) + " as " + JOIN_FIELD_PREFIX+i);
			if (i == 0)
				tableList.add(maps[i].getQualTableName());
			else
				tableList.add(" LEFT OUTER JOIN " + maps[i].getQualTableName() + " ON " + getQualKeyFieldName(maps[0]) + " = " + getQualKeyFieldName(maps[i]));
			whereList.add(getQualKeyFieldName(maps[0]) + "=" + getQualKeyFieldName(maps[i]));
		}
		// remove the first one since it says that the key in 0 should be equal to the key in 0
		whereList.remove(0);
		String selectLine = "SELECT " + Strings.getListString(selectList,", ") + "";
		String fromLine = " FROM " + Strings.getListString(tableList," ") + "";
		
		String whereLine = "";
		if (minId > -1){
			whereLine = " WHERE " + getQualKeyFieldName(maps[0]) + " >= " + minId;
			if (maxId > -1)
				whereLine = whereLine + " AND " + getQualKeyFieldName(maps[0]) + " <= " + maxId;
			
		}
		return selectLine + fromLine + " " + whereLine + " order by " + getQualKeyFieldName(maps[0]) + " asc"; // + whereLine;
	}
	// TODO, this is a copy of above.. generalize this!
	public static String getCompleteOuterJoin(TableMap[] maps, String minId){
		ArrayList selectList = new ArrayList();
		ArrayList tableList = new ArrayList();
		ArrayList whereList = new ArrayList();
		selectList.add(getQualKeyFieldName(maps[0]) + " as " + JOIN_KEY_FIELD_NAME);
		for(int i=0;i<maps.length;i++){
			selectList.add(getQualValueFieldName(maps[i]) + " as " + JOIN_FIELD_PREFIX+i);
			if (i == 0)
				tableList.add(maps[i].getQualTableName());
			else
				tableList.add(" LEFT OUTER JOIN " + maps[i].getQualTableName() + " ON " + getQualKeyFieldName(maps[0]) + " = " + getQualKeyFieldName(maps[i]));
			whereList.add(getQualKeyFieldName(maps[0]) + "=" + getQualKeyFieldName(maps[i]));
		}
		// remove the first one since it says that the key in 0 should be equal to the key in 0
		whereList.remove(0);
		String selectLine = "SELECT " + Strings.getListString(selectList,", ") + "";
		String fromLine = " FROM " + Strings.getListString(tableList," ") + "";
		
		String whereLine = "";
		if (minId != null){
			whereLine = " WHERE " + getQualKeyFieldName(maps[0]) + " >= '" + minId + "'";
		}
		return selectLine + fromLine + " " + whereLine + " order by " + getQualKeyFieldName(maps[0]) + " asc"; // + whereLine;
	}
}
