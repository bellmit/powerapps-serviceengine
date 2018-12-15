package com.profitera.services.business.archive.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.descriptor.business.meta.IArchive;
import com.profitera.ibatis.SQLMapFileRenderer;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.dataaccess.ProtocolLoadedSqlMapProvider;
import com.profitera.util.CollectionUtil;
import com.profitera.util.MapCar;
import com.profitera.util.NoopMapCar;
import com.profitera.util.Strings;
import com.profitera.util.xml.DOMDocumentUtil;

public class Archive {
  static final String INCREMENT_ARCHIVE = "updateArchiveRecordIdForIncrement";
  static final String DELETE_ARCHIVE_RECORDS = "deleteArchiveRecords";
  static final String SELECT_ARCHIVE_RECORD_COUNT = "selectArchiveRecordCount";
  //
  static final String SELECT_DATA_IN_RANGE = "selectDataInRange";
  static final String COUNT_IN_PROGRESS_ARCHIVE_PROCESSES = "countInProgressArchiveProcesses";
  static final String GET_IN_PROGRESS_ARCHIVE_IDS = "getInProgressArchiveIds";
  static final String GET_MAX_ARCHIVE_PROCESS_ID = "getMaxArchiveProcessId";
  static final String GET_ALL_KEYS_FROM_ARCHIVE = "getAllKeysFromArchive";
  static final String INSERT_ARCHIVE_PROCESS = "insertArchiveProcess";
  static final String UPDATE_ARCHIVE_RECORD_ID_RESET = "updateArchiveRecordIdReset";
  //
  static final String SELECT_ARCHIVE_RECORD_KEY_T = "selectArchiveRecordKeyT";
  //
  private static final String DELETE_SOURCE_T = "deleteSourceT";
  private static final String SELECT_ARCHIVE_T = "selectArchiveT";
  private static final String SELECT_TABLESET_DATA = "selectTablesetDataT";
  private static final String INSERT_TABLESET_DATA = "insertTablesetDataT";
  private static final String INSERT_ARCHIVE_DATA_T = "insertArchiveDataT";
  private static final String DELETE_ARCHIVE_T = "deleteArchiveT";
  private static final String GET_RANGE_COUNT = "getRangeCount";
  private static final String GET_RANGE_MINIMUM = "getRangeMinimum";
  private Table rootTable;
  private Map<String, ProtocolLoadedSqlMapProvider> providers = new HashMap();
  private final String name;
  private final String sourceDataSource;
  private final TableSet[] sets;
  private final Map<String, Table> tableMap = new HashMap();
  private IDataSourceConfigurationSet dataSources;
  
  private class Column {
    final String name;
    final Class type;
    public Column(String n, Class type) {
      this.name = n;
      this.type = type;
    }

  }
  private class Key extends Column{
    final String parentKey;

    public Key(String n, String p, Class t) {
      super(n, t);
      parentKey = p;
    }
  }
  private class Range {
    public Range(String c, Class t, String n) {
      column = c;
      type = t;
      name = n;
    }
    public final String column;
    public final String name;
    public final Class type;
  }
  
  private class Table {
    Table(String name, Column[] cols, Table parent) {
      this.name = name;
      this.columns = new String[cols.length];
      this.types = new Class[cols.length];
      for (int i = 0; i < cols.length; i++) {
        columns[i] = cols[i].name;
        types[i] = cols[i].type;
      }
      this.parent = parent;
    }
    public final String name;
    public final String[] columns;
    public final Class[] types;
    public final Table parent;
    public Key[] keys;
    public Range range;
    public Table[] tables;
    public String archiveName;
  }
  
  private class TableSet {
    final String name;
    final Map<String, String> tables = new HashMap<String, String>();
    final String dataSource;
    public TableSet(String name, String datasource){
      this.name = name;
      this.dataSource = datasource;
    }
  }

  public Archive(String name, Document d, IDataSourceConfigurationSet dataSources) {
    this.name = name;
    this.dataSources = dataSources;
    Element arch = d.getDocumentElement();
    if (!arch.getNodeName().equals("archive")){
      throw new IllegalArgumentException("Root element should be 'archive', was '" + arch.getNodeName() + "'");
    }
    String dataSource = arch.getAttribute("datasource");
    if (dataSource.equals("")) {
      dataSource = dataSources.getDefaultDataSource().getName();
    }
    this.sourceDataSource = dataSource;
    try {
      Element table = getRootTableElement(arch);
      rootTable = buildTable(true, table, null);
      sets = getTableSets(arch);
    } catch (XPathExpressionException e) {
      // this is virtually unreachable, the XPath errors can only be
      // runtime since the static xpath in this class is 100% syntactically valid
      throw new IllegalArgumentException("Illegal archive XML format supplied", e);
    }
    
    
  }
  
  private TableSet[] getTableSets(Element arch) throws XPathExpressionException {
    String[] currentTables = getTables();
    Element[] elements = getElements("tablesets/tableset", arch);
    TableSet[] sets = new TableSet[elements.length + 1];
    String archiveSource = arch.getAttribute("archivedatasource");
    if (archiveSource.equals("")) {
      archiveSource = dataSources.getDefaultDataSource().getName();
    }
    sets[0] = new TableSet("Archive", archiveSource);
    for (int i = 0; i < currentTables.length; i++) {
      Table t = getTable(currentTables[i]);
      sets[0].tables.put(t.name, t.archiveName);
    }
    for (int i = 1; i < sets.length; i++) {
      Element e = elements[i-1];
      String id = DOMDocumentUtil.getRequiredAttribute("id", e);
      String datasource = e.getAttribute("datasource");
      if (datasource.equals("")) {
        datasource = dataSources.getDefaultDataSource().getName();
      }
      sets[i] = new TableSet(DOMDocumentUtil.getRequiredAttribute("name", e), datasource);
      Element[] mappings = getElements("//table/tablesets/tableset[@id]", e);
      for (int j = 0; j < mappings.length; j++) {
        Element m = mappings[j];
        if (id.equals(m.getAttribute("id"))) {
          // Up 2 levels to the containing table
          Element table = (Element) m.getParentNode().getParentNode();
          String source = DOMDocumentUtil.getRequiredAttribute("name", table);
          String dest = DOMDocumentUtil.getRequiredAttribute("target", m);
          sets[i].tables.put(source, dest);
        }
      }
      for (int j = 0; j < currentTables.length; j++) {
        if (!sets[i].tables.containsKey(currentTables[j])){
          throw new IllegalArgumentException("Tableset " + sets[i].name + " missing table mapping for " + currentTables[j]);
        }
      }
    }
    return sets;
  }

  private Table getTable(String name) {
    return getTable(rootTable, name);
  }

  private Table getTable(Table t, String name) {
    if (t.name.equals(name)){
      return t;
    } else {
      for (int i = 0; i < t.tables.length; i++) {
        Table c = t.tables[i];
        c = getTable(c, name);
        if (c != null) return c;
      }
    }
    return null;
  }

  protected IReadWriteDataProvider getProvider(String dataSource) {
    if (dataSource == null) {
      dataSource = dataSources.getDefaultDataSource().getName();
    }
    ProtocolLoadedSqlMapProvider provider = providers.get(dataSource);
    if (provider == null) {
      IDataSourceConfiguration[] configurations = dataSources.getDataSources();
      for (int i = 0; i < configurations.length; i++) {
        if (configurations[i].getName().equals(dataSource)) {
          provider = buildProvider(configurations[i]);
          providers.put(dataSource, provider);
        }
      }
    }
    return provider;
  }

  protected ProtocolLoadedSqlMapProvider buildProvider(IDataSourceConfiguration dataSource) {
    return new ProtocolLoadedSqlMapProvider("archive", getArchiverSQL(), dataSource, dataSource.getName() + "-Archiver");
  }
  
  public String getName(){
    return name;
  }
  

  private Table buildTable(boolean isRoot, Element table, Table parent) throws XPathExpressionException {
    String tableName = getTableName(table);
    Table tab = new Table(tableName, getTableColumns(tableName, parent, table), parent);
    tab.archiveName = DOMDocumentUtil.getRequiredAttribute("archivename", table);
    tab.keys = getTableKeys(tab, table, isRoot);
    if (isRoot) {
      tab.range = getTableRange(table, tab);
    }
    Element[] tableElts = DOMDocumentUtil.getChildElementsWithName("table", table);
    Table[] t = new Table[tableElts.length];
    for (int i = 0; i < t.length; i++) {
      t[i] = buildTable(false, tableElts[i], tab);
    }
    tab.tables = t;
    tableMap.put(tab.name, tab);
    return tab;
  }

  private String getTableName(Element table) {
    return DOMDocumentUtil.getRequiredAttribute("name", table);
  }

  private Range getTableRange(Element table, Table t) {
    Element r = getTableRangeElement(table);
    String column = getTableRangeColumn(r);
    String name = DOMDocumentUtil.getRequiredAttribute("name", r);
    for (int i = 0; i < t.columns.length; i++) {
      if (t.columns[i].equals(column)) {
        return new Range(column, t.types[i], name);
      }
    }
    throw new IllegalArgumentException("Column matching range column '" + column + "' not found for table " + t.name);
  }

  private String getTableRangeColumn(Element r) {
    return DOMDocumentUtil.getRequiredAttribute("column", r);
  }

  private Key[] getTableKeys(Table tab, Element table, boolean isRoot) throws XPathExpressionException {
    Element[] keys = getTableKeyElements(table);
    Key[] keyNames = new Key[keys.length];
    for (int i = 0; i < keys.length; i++) {
      Key k = loadKeyFromElement(tab.name, tab.parent, keys[i]);
      keyNames[i] = k;
    }
    return keyNames;
  }

  private Key loadKeyFromElement(String tableName, Table parentTable, Element e) {
    String name = DOMDocumentUtil.getRequiredAttribute("name", e);
    String parent = e.getAttribute("parentkey");
    if (parent.equals("")){
      parent = null;
    }
    Class type = null;
    if (parent == null) {
      type = loadColumnTypeFromElement(tableName, e, name);
    } else {
      Attr definedType = e.getAttributeNode("type");
      if (definedType != null) {
        throw new IllegalArgumentException("Column key type should not be defined if a parent key is defined for " + name + " in " + tableName);
      }
      for (int j = 0; j < parentTable.keys.length; j++) {
        Key pKey = parentTable.keys[j];
        if (pKey.name.equals(parent)){
          type = pKey.type;
        }
      }
      if (type == null) {
        throw new IllegalArgumentException("Parent key defined for " + name + " in " + tableName +" does not exist in parent table " + parentTable.name);
      }
    }
    Key k = new Key(name, parent, type);
    return k;
  }

  private Class loadColumnTypeFromElement(String tableName, Element e, String columnName) {
    if (!e.hasAttribute("type")) {
      throw new IllegalArgumentException("Missing required attribute 'type' for column " + columnName);
    }
    String className = DOMDocumentUtil.getRequiredAttribute("type", e);
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException ex) {
      throw new RuntimeException("Illegal column type '" + className + "' for column " + columnName + " in " + tableName);
    }
  }
  
  
  private Column[] getTableColumns(String tableName, Table parent, Element table) throws XPathExpressionException {
    Element[] cols = getElements("columns/column", table);
    Column[] names = new Column[cols.length];
    for (int i = 0; i < names.length; i++) {
      if (isKeyElement(cols[i])) {
        names[i] = loadKeyFromElement(tableName, parent, cols[i]);
      } else {
        String columnName = DOMDocumentUtil.getRequiredAttribute("name", cols[i]);
        Class type = loadColumnTypeFromElement(tableName, cols[i], columnName);
        names[i] = new Column(columnName, type);
      }
      
    }
    return names;
  }

  private boolean isKeyElement(Element element) {
    return element.getAttribute("key").equals("true");
  }

  private Element[] getTableKeyElements(Element table) throws XPathExpressionException {
    return getElements("columns/column[@key='true']", table);
  }
  
  private Element[] getElements(String xpathExp, Element context) throws XPathExpressionException {
    return DOMDocumentUtil.getElements(xpathExp, context);
  }

  public String getArchiverSQL() {
    SQLMapFileRenderer r = new SQLMapFileRenderer();
    StringBuilder b = new StringBuilder(r.renderHeader("archive"));
    b.append(getStaticArchiveSQL());
    b.append(getDynamicSQL());
    b.append(r.renderFooter());
    return b.toString();
  }
  
  private String getDynamicSQL() {
    SQLMapFileRenderer r = new SQLMapFileRenderer();
    StringBuilder text = new StringBuilder();
    text.append(getSelectKeysInRange(r));
    text.append(getRangeStatements(r));
    text.append(getDeletePartialArchive(r));
    text.append(getSelectFromArchiveRootTable(r));
    text.append(getSelectFromArchiveChildTables(r));
    text.append(getCopyArchive(r));
    text.append(getDeleteSourceData(r));
    return text.toString();
  }
  
  private String getCopyArchive(SQLMapFileRenderer r) {
    StringBuilder sb = new StringBuilder();
    String[] tables = getTables();
    for (int i = 0; i < tables.length; i++) {
      Table t = getTableObject(tables[i]);
      String columnList = "AR_ID, AR_REC_ID, " + Strings.getListString(t.columns, ", ");
      String[] props = new String[t.columns.length + 2];
      Class[] javaTypes = new Class[t.columns.length + 2];
      props[0] = "AR_ID";
      javaTypes[0] = Long.class;
      props[1] = "AR_REC_ID";
      javaTypes[1] = Long.class;
      System.arraycopy(t.columns, 0, props, 2, t.columns.length);
      System.arraycopy(t.types, 0, javaTypes, 2, t.types.length);
      sb.append(r.renderResultMap(SELECT_TABLESET_DATA + i, HashMap.class, props, javaTypes));
      sb.append(r.renderSelect(SELECT_TABLESET_DATA + i, SELECT_TABLESET_DATA + i, Map.class, 
          " select " + columnList + " from $FROM_TABLE$ where AR_ID = #AR_ID#"));
      sb.append(r.renderParameterMap(SELECT_TABLESET_DATA + i, Map.class, props, javaTypes, this.mapToJdbcTypes(javaTypes)));
      sb.append(r.renderInsert(INSERT_TABLESET_DATA + i, SELECT_TABLESET_DATA + i, 
      "insert into $TO_TABLE$ (" + columnList + ") values (" + getQuestions(t.columns.length + 2) + ")"));
    }
    return sb.toString();
  }

  private String getRangeStatements(SQLMapFileRenderer r) {
    Class type = rootTable.range.type;
    String p = r.renderParameterMap(GET_RANGE_COUNT, Map.class, 
        new String[]{"MIN", "MAX"}, 
        new Class[]{type, type});
    String col = rootTable.range.column;
    String s = r.renderSelect(GET_RANGE_COUNT, Long.class, GET_RANGE_COUNT, 
        "select count(*) from " + rootTable.name + " where " 
        + col + " >= ? and ? >= " + col);
    String min = r.renderSelect(GET_RANGE_MINIMUM, type, 
        "select min(" + col + ") from " + rootTable.name);
    return p + s + min;
  }

  private String getDeleteSourceData(SQLMapFileRenderer r) {
    StringBuilder t = new StringBuilder();
    String[] nonLeafKeys = new String[0];
    Class[] nonLeafKeyTypes = new Class[0];
    StringBuilder select = new StringBuilder("select ");
    StringBuilder from = new StringBuilder("from ");
    if(rootTable.tables.length>0){
    	// Assemble a list of key names, excluding leaf tables
    	nonLeafKeys = getNonLeafTableKeys(rootTable);
      nonLeafKeyTypes = getNonLeafTableKeyTypes(rootTable);
      buildNonLeafKeySelection(rootTable, null, 0, 0, select, from);
    }else{
    	//
    	List keys = new ArrayList();
    	for (int i = 0; i < rootTable.keys.length; i++) {
        keys.add(rootTable.name + "_" + rootTable.keys[i].name);
      }
    	nonLeafKeys = (String[]) keys.toArray(new String[keys.size()]);
    	//
    	List keyTypes = new ArrayList();
    	for (int i = 0; i < rootTable.keys.length; i++) {
    		keyTypes.add(rootTable.keys[i].type);
    	}
    	nonLeafKeyTypes = (Class[]) keyTypes.toArray(new Class[keyTypes.size()]);
    	//
    	for (int i = 0; i < rootTable.keys.length; i++) {
        select.append("t0." + rootTable.keys[i].name + ", ");
      }
    	//
    	from.append(" " + rootTable.archiveName + " " + "t0");
    }
    nonLeafKeys = (String[]) CollectionUtil.extendArray(nonLeafKeys, "AR_REC_ID");
    nonLeafKeyTypes = (Class[]) CollectionUtil.extendArray(nonLeafKeyTypes, Long.class);
    t.append(r.renderResultMap(GET_ALL_KEYS_FROM_ARCHIVE, HashMap.class, 
        nonLeafKeys, 
        nonLeafKeyTypes));    
    t.append(r.renderSelect(GET_ALL_KEYS_FROM_ARCHIVE, GET_ALL_KEYS_FROM_ARCHIVE, Long.class, 
        select.toString() + "t0.AR_REC_ID " +
        from.toString() +
        " where t0.AR_ID = #value# order by t0.AR_REC_ID"));
    //TODO: this should render a parameter map instead of using inlines
    t.append(r.renderDelete(DELETE_SOURCE_T+0, Map.class, 
    getRootTableSourceDelete(rootTable)));
    getChildTablesSourceDeletes(rootTable, 0, t, r);
    return t.toString();
  }
  
  private int getChildTablesSourceDeletes(Table p, int c, StringBuilder b, SQLMapFileRenderer r) {
    for (int i = 0; i < p.tables.length; i++) {
      Table t = p.tables[i];
      c++;
      b.append(r.renderDelete(DELETE_SOURCE_T+c, Map.class,
          getTableSourceDelete(t)));
      c = getChildTablesSourceDeletes(t, c, b, r);
    }
    return c;
  }

  private String getRootTableSourceDelete(Table t){
    List l = new ArrayList();
    for (int i = 0; i < t.keys.length; i++) {
      l.add(t.keys[i].name + "=#" + t.name + "_" + t.keys[i].name + "#");
    }
    return "delete from " + t.name + " where " + Strings.getListString(l, " and ");
  }
  
  private String getTableSourceDelete(Table t){
    List l = new ArrayList();
    for (int i = 0; i < t.keys.length; i++) {
      Key k = t.keys[i];
      if (k.parentKey != null){
        l.add(k.name + "=#" + t.parent.name + "_" + k.parentKey + "#");
      }
    }
    return "delete from " + t.name + " where " + Strings.getListString(l, " and ");
  }
  
  public boolean hasValidSourceDeleteArguments(Map args, String table) {
    Table t = getTable(table);
    boolean isRoot = table.equals(rootTable.name);
    Key[] keys = t.keys;
    for (int i = 0; i < keys.length; i++) {
      Key k = keys[i];
      if (!isRoot && k.parentKey != null) {
        String keyArgName = t.parent.name + "_" + k.parentKey;
        if (args.get(keyArgName) == null) {
          return false;
        }
      } else if (isRoot) {
        String keyArgName = t.name + "_" + k.name;
        if (args.get(keyArgName) == null) {
          return false;
        }
      }
    }
    return true;
  }

  private int buildNonLeafKeySelection(Table t, Table parent, int counter,
      int parentCounter,
      StringBuilder select, StringBuilder from) {
    if (t.tables.length > 0){
      for (int i = 0; i < t.keys.length; i++) {
        select.append("t" + counter +"." + t.keys[i].name + ", ");
      }
      if (parent != null) {
        from.append(" left outer join ");
        from.append(" " + t.archiveName + " " + "t"+counter + " on ");
        List matches = new ArrayList();
        for (int i = 0; i < t.keys.length; i++) {
          Key k = t.keys[i];
          if (k.parentKey != null){
            matches.add(" t" + parentCounter + "." + k.parentKey + "= t"+counter + "." + k.name);
          }
        }
        from.append(Strings.getListString(matches, " and "));
      } else {
        from.append(" " + t.archiveName + " " + "t"+counter);
      }
    }
    int c = counter;
    for (int i = 0; i < t.tables.length; i++) {
      c++;
      c = buildNonLeafKeySelection(t.tables[i], t, c, counter, select, from);
    }
    return c;
  }

  private String[] getNonLeafTableKeys(Table t) {
    List l = new ArrayList();
    if (t.tables.length > 0) {
      for (int i = 0; i < t.keys.length; i++) {
        l.add(t.name + "_" + t.keys[i].name);
      }
    }
    for (int i = 0; i < t.tables.length; i++) {
      String[] kids = getNonLeafTableKeys(t.tables[i]);
      l.addAll(Arrays.asList(kids));
    }
    return (String[]) l.toArray(new String[l.size()]);
  }
  
  private Class[] getNonLeafTableKeyTypes(Table t) {
    List l = new ArrayList();
    if (t.tables.length > 0) {
      for (int i = 0; i < t.keys.length; i++) {
        l.add(t.keys[i].type);
      }
    }
    for (int i = 0; i < t.tables.length; i++) {
      Class[] kids = getNonLeafTableKeyTypes(t.tables[i]);
      l.addAll(Arrays.asList(kids));
    }
    return (Class[]) l.toArray(new Class[l.size()]);
  }

  private String getDeletePartialArchive(SQLMapFileRenderer r) {
    List<Table> tables = getTables(rootTable);
    StringBuilder b = new StringBuilder();
    int index = 0;
    for (Iterator i = tables.iterator(); i.hasNext();) {
      Table t = (Table) i.next();
      b.append(r.renderDelete(DELETE_ARCHIVE_T+index, Long.class, "delete from " + t.archiveName + " where AR_ID = #value#"));
      index++;
    }
    return b.toString();
  }

  private String getSelectFromArchiveChildTables(SQLMapFileRenderer r) {
    StringBuilder text = new StringBuilder();
    String[] tables = getTables();
    // We skip the root, start with 1
    for (int i = 1; i < tables.length; i++) {
      text.append(getSelectFromArchive(getTableObject(tables[i]), i, r));
    }
    return text.toString();
  }
  
  private Table getTableObject(String name){
    return tableMap.get(name);
  }
    
  private String temp(Table t, int i, SQLMapFileRenderer r){
  	String[] keys = new String[t.parent.keys.length];
  	Class[] types = new Class[keys.length];
  	for(int c=0;c<keys.length;c++){
  		keys[c] = t.parent.keys[c].name;
  		types[c] = t.parent.keys[c].type;
  	}
  	String select = 
  		" select " + Strings.getListString(keys, ",") + 
  		" from " + t.parent.archiveName +
  		" where AR_ID = ? and AR_REC_ID = ?";
  	StringBuilder text = new StringBuilder();
  	String id = SELECT_ARCHIVE_RECORD_KEY_T + i;
  	text.append(r.renderParameterMap(id, Map.class, new String[]{"AR_ID", "AR_REC_ID"}));
  	text.append(r.renderResultMap(id, HashMap.class, keys, types));
    text.append(r.renderSelect(id, id, id, select));
  	return text.toString();
  }
  
  private String getSelectFromArchive(Table t, int i, SQLMapFileRenderer r) {
    StringBuilder text = new StringBuilder();
    String id = SELECT_ARCHIVE_T + i;
    String[] keys = new String[t.parent.keys.length];
  	for(int c=0;c<keys.length;c++){
  		keys[c] = t.parent.keys[c].name;
  	}
    text.append(r.renderParameterMap(id, Map.class, keys));
    String t0ColumnList = "t0." + Strings.getListString(t.columns, ", t0.");
    String whereKeyMatching = "";
    for (int j = 0; j < t.keys.length; j++) {
      Key key = t.keys[j];
      if (key.parentKey != null) {
      	if(!whereKeyMatching.equals("")) whereKeyMatching+= " and ";
        String m = " t0." + key.name + " = t1." + t.keys[j].parentKey ;
        whereKeyMatching = whereKeyMatching + m;
      }
    }
    for (int j = 0; j < keys.length; j++) {
    	if(!whereKeyMatching.equals("")) whereKeyMatching+= " and ";
      String key = keys[j];
      String m = " t1." + key + " = ?";
      whereKeyMatching = whereKeyMatching + m;
    }
    Class[] types = new Class[t.columns.length];
    for (int j = 0; j < t.types.length; j++) {
      types[j] = t.types[j];
    }
    String[] resultColumns = new String[0];
    for (int j = 0; j < t.columns.length; j++) {
      resultColumns = (String[]) CollectionUtil.extendArray(resultColumns, t.columns[j]);
    }
    text.append(r.renderResultMap(id, HashMap.class, resultColumns, types));
    text.append(r.renderSelect(id, id, id,
        "select " + t0ColumnList +
        " from " + t.name + " t0, " + t.parent.name + " t1 " +
        "where " + whereKeyMatching ));
    // Now we insert the data we just selected
    resultColumns = (String[])CollectionUtil.extendArray(resultColumns, "AR_ID");
    resultColumns = (String[])CollectionUtil.extendArray(resultColumns, "AR_REC_ID");
    types = (Class[])CollectionUtil.extendArray(types, Long.class);
    types = (Class[])CollectionUtil.extendArray(types, Long.class);
    text.append(r.renderParameterMap(INSERT_ARCHIVE_DATA_T + i, Map.class, resultColumns, types, mapToJdbcTypes(types)));
    text.append(r.renderInsert(INSERT_ARCHIVE_DATA_T + i, INSERT_ARCHIVE_DATA_T + i, 
        "insert into " + t.archiveName + "(" + Strings.getListString(resultColumns, ", ")
        + ") values (" + getQuestions(resultColumns.length) + ")"));
    text.append(temp(t, i, r));
    return text.toString();
  }

  private String getQuestions(int length) {
    String[] questions = new String[length];
    Arrays.fill(questions, "?");
    return Strings.getListString(questions, ", ");
  }

  private String[] mapToJdbcTypes(Class[] types) {
    String[] jdbc = new String[types.length];
    Arrays.fill(jdbc, "OTHER");
    for (int i = 0; i < jdbc.length; i++) {
      Class c = types[i];
      if (Number.class.isAssignableFrom(c)) {
        jdbc[i] = SQLMapFileRenderer.N;
      } else if (String.class.equals(c)) {
        jdbc[i] = SQLMapFileRenderer.V;
      } else if (Date.class.isAssignableFrom(c)) {
        jdbc[i] = "TIMESTAMP";
      }
    }
    return jdbc;
  }


  private String getSelectFromArchiveRootTable(SQLMapFileRenderer r) {
    String[] params = new String[]{"AR_ID", "AR_REC_ID"};
    for (int i = 0; i < rootTable.keys.length; i++) {
      params = (String[]) CollectionUtil.extendArray(
          params, rootTable.keys[i].name);  
    }
    String[] keys = new String[rootTable.keys.length];
    for (int i = 0; i < keys.length; i++) {
      keys[i] = "table0." + rootTable.keys[i].name;
    }
    String text = r.renderParameterMap(SELECT_ARCHIVE_T + 0, Map.class, 
        params);
    text = text + "\n" + r.renderResultMap(SELECT_ARCHIVE_T + 0, HashMap.class, rootTable.columns, rootTable.types);
    
    String rawColumnList = Strings.getListString(rootTable.columns, ", ");
    text = text + r.renderSelect(SELECT_ARCHIVE_T + 0, SELECT_ARCHIVE_T + 0, SELECT_ARCHIVE_T + 0, 
        "select " 
        + rawColumnList 
        + " from "+ rootTable.name +" table0, PTRARCHIVE_PROCESS table1 where "
        + " table1.ID = ? and (table1.RECORD_ID + 1) = ? and "
        + Strings.getListString(keys, " = ? and ") + " = ?");
    //
 // Now we insert the data we just selected
    String[] resultColumns = new String[rootTable.columns.length + 2];
    resultColumns[0] = "AR_ID";
    resultColumns[1] = "AR_REC_ID";
    System.arraycopy(rootTable.columns, 0, resultColumns, 2, rootTable.columns.length);
    Class[] types = new Class[rootTable.columns.length + 2];
    Arrays.fill(types, Long.class);
    for (int i = 0; i < rootTable.types.length; i++) {
      types[i+2] = rootTable.types[i];
    }
    StringBuilder sb = new StringBuilder();
    sb.append(r.renderParameterMap(INSERT_ARCHIVE_DATA_T + 0, Map.class, resultColumns, types, mapToJdbcTypes(types)));
    sb.append(r.renderInsert(INSERT_ARCHIVE_DATA_T + 0, INSERT_ARCHIVE_DATA_T + 0, 
        "insert into " + rootTable.archiveName + "(" + Strings.getListString(resultColumns, ", ")
        + ") values (" + getQuestions(resultColumns.length) + ")"));
    return text + sb;
  }

  
  private String getSelectKeysInRange(SQLMapFileRenderer r) {
    StringBuilder text = new StringBuilder();
    int keyCount = rootTable.keys.length;
    String[] names = new String[keyCount];
    Class[] types = new Class[keyCount];
    for (int i = 0; i < types.length; i++) {
      names[i] = rootTable.keys[i].name;
      types[i] = rootTable.keys[i].type;
    }
    String rangeCol = rootTable.range.column;
    text.append(r.renderParameterMap(SELECT_DATA_IN_RANGE, Map.class, 
        new String[]{"MIN", "MAX"}, 
        new Class[]{rootTable.range.type, rootTable.range.type}));
    text.append(r.renderResultMap(SELECT_DATA_IN_RANGE, HashMap.class, 
        rootTable.columns, rootTable.types));
    text.append(r.renderSelect(SELECT_DATA_IN_RANGE, SELECT_DATA_IN_RANGE, 
        SELECT_DATA_IN_RANGE, 
        "select " + Strings.getListString(rootTable.columns, ", ") 
        + " from " + rootTable.name 
        + " where " + rangeCol + " >= ?" +
            " and ? >= " + rangeCol + " order by " + rangeCol));
    return text.toString();
  }
  
  static final String GET_LAST_RECORD_ID_FOR_ARCHIVE = "getLastRecordIdForArchive";
  static final String GET_ARCHIVE_STATUS = "getArchiveStatus";
  
  private String getStaticArchiveSQL() {
    SQLMapFileRenderer r = new SQLMapFileRenderer();
    StringBuilder text = new StringBuilder();
    text.append(r.renderSelect(COUNT_IN_PROGRESS_ARCHIVE_PROCESSES, Long.class,  
      "select count(*) from PTRARCHIVE_PROCESS where status = '" + IArchive.ARCHIVING_STATUS + "'"));
    text.append(r.renderSelect(GET_IN_PROGRESS_ARCHIVE_IDS, Long.class,  
        "select ARCHIVE_ID from PTRARCHIVE_PROCESS where status = '" + IArchive.ARCHIVING_STATUS + "'"));
    text.append(r.renderSelect(GET_MAX_ARCHIVE_PROCESS_ID, Long.class,  
        "select MAX(ID) from PTRARCHIVE_PROCESS"));
    text.append(r.renderSelect(GET_ARCHIVE_STATUS, String.class, Long.class, 
      "select STATUS from PTRARCHIVE_PROCESS where ID = #value#"));
    text.append(r.renderParameterMap(INSERT_ARCHIVE_PROCESS, Map.class, 
        new String[]{"ID", "ARCHIVE_ID", "NAME", "STATUS", "CREATED_TIME", "RECORD_ID"}, 
        new String[]{SQLMapFileRenderer.N, SQLMapFileRenderer.N,SQLMapFileRenderer.V, SQLMapFileRenderer.V, "TIMESTAMP", SQLMapFileRenderer.N}));
    text.append(r.renderInsert(INSERT_ARCHIVE_PROCESS, INSERT_ARCHIVE_PROCESS, 
        "insert into PTRARCHIVE_PROCESS (ID, ARCHIVE_ID, NAME, STATUS, CREATED_TIME,RECORD_ID) values (?,?,?,?,?,?)"));
    text.append(r.renderSelect(GET_LAST_RECORD_ID_FOR_ARCHIVE, Long.class, Long.class, "select RECORD_ID from PTRARCHIVE_PROCESS where ID = #value#"));
    text.append(r.renderUpdate(UPDATE_ARCHIVE_RECORD_ID_RESET, Long.class, "update PTRARCHIVE_PROCESS set RECORD_ID = 0 where ID = #value#"));
    text.append(r.renderParameterMap(INCREMENT_ARCHIVE, Map.class, 
        new String[]{"RECORD_COUNT", "AR_ID", "RECORD_COUNT", "AR_REC_ID"}, 
        new String[]{SQLMapFileRenderer.N, SQLMapFileRenderer.N, SQLMapFileRenderer.N, SQLMapFileRenderer.N}));
    text.append(r.renderUpdate(INCREMENT_ARCHIVE, INCREMENT_ARCHIVE, 
        "update PTRARCHIVE_PROCESS set RECORD_ID = RECORD_ID + ? where ID = ? and RECORD_ID + ? = ?"));
    text.append(r.renderUpdate("updateArchiveProcessComplete", Long.class, 
        "update ptrarchive_process set status = '" + IArchive.ARCHIVED_STATUS + "' where ID = #value#"));
    // 
    text.append(r.renderUpdate("updateSourceDeleteProcessComplete", Long.class, 
        "update ptrarchive_process set status = '" + IArchive.SOURCE_DELETED_STATUS + "' where ID = #value#"));
    text.append(r.renderSelect(SELECT_ARCHIVE_RECORD_COUNT, Long.class, Map.class, "Select count(*) from $TABLE_NAME$ where AR_ID = #AR_ID#"));
    text.append(r.renderDelete(DELETE_ARCHIVE_RECORDS, Map.class, "delete from $TABLE_NAME$ where AR_ID = #AR_ID#"));
    return text.toString();
  }

  private Element getTableRangeElement(Element table) {
    Element[] r = DOMDocumentUtil.getChildElementsWithName("range", table);
    if (r.length > 1) {
      throw new IllegalArgumentException("Multiple range elements defined for table");
    } else if (r.length == 1){
      return r[0];
    } else {
      throw new IllegalArgumentException("No range defined for table");
    }
  }

  private Element getRootTableElement(Element arch) {
    Element[] tables = DOMDocumentUtil.getChildElementsWithName("table", arch);
    if (tables.length > 1) {
      throw new IllegalArgumentException("Only one root table tag is permitted");
    }
    Element table = tables[0];
    return table;
  }


  public Object getMinimumRangeValue() throws SQLException {
    return getSourceTablesetProvider().queryObject(GET_RANGE_MINIMUM);
  }
  
  public Long[] getInProgressArchiveProcesses() throws SQLException {
    Iterator i = getProvider(null).query(IReadOnlyDataProvider.LIST_RESULTS, GET_IN_PROGRESS_ARCHIVE_IDS, null);
    List l = new ArrayList();
    MapCar.map(new NoopMapCar(), i, l);
    return (Long[]) l.toArray(new Long[0]);
  }
  
  public Class getRangeType(){
    return rootTable.range.type;
  }
  
  public String getRangeName() {
    return rootTable.range.name;
  }


  public Long getRecordCountInRange(Object max) throws SQLException {
    Object min = getMinimumRangeValue();
    Map args = getMinMaxArgs(min, max);
    return (Long) getProvider(sourceDataSource).queryObject(GET_RANGE_COUNT, args);
  }


  private Map getMinMaxArgs(Object min, Object max) {
    Map args = new HashMap();
    args.put("MIN", min);
    args.put("MAX", max);
    return args;
  }

  /**
   * @return a table list, the order is significant, it is the order
   * of copying and (reversed) deleting
   */
  public String[] getTables() {
    List l = getTables(rootTable);
    l = MapCar.map(new MapCar(){
      public Object map(Object o) {
        return ((Table)o).name;
      }}, l);
    return (String[]) l.toArray(new String[l.size()]);
  }


  private List<Table> getTables(Table t) {
    List<Table> l = new ArrayList<Table>();
    l.add(t);
    Table[] subs = t.tables;
    for (int i = 0; i < subs.length; i++) {
      l.addAll(getTables(subs[i]));
    }
    return l;
  }
  
  public String[] getDeleteArchiveStatements() {
    String prefix = DELETE_ARCHIVE_T;
    return getStatementPerTable(prefix);
  }
  public String[] getInsertDataArchiveStatements() {
    return getStatementPerTable(INSERT_ARCHIVE_DATA_T);
  }
  private String[] getStatementPerTable(String prefix) {
    String[] tables = getTables();
    String[] stmts = new String[tables.length];
    for (int i = 0; i < stmts.length; i++) {
      stmts[i] = prefix + i;
    }
    return stmts;
  }

  public String[] getDeleteSourceStatements() {
    return getStatementPerTable(DELETE_SOURCE_T);
  }
  
  public String[] getSelectFromArchiveStatements() {
    return getStatementPerTable(SELECT_TABLESET_DATA);
  }
  
  public String[] getInsertIntoArchiveStatements() {
    return getStatementPerTable(INSERT_TABLESET_DATA);
  }
  

  public String[] getTablesSets() {
    String[] names = new String[sets.length];
    for (int i = 0; i < names.length; i++) {
      names[i] = sets[i].name;
    }
    return names;
  }

  public String[] getTableSetTables(String from) {
    String[] tables = getTables();
    TableSet ts = getTableSet(from);
    String[] targets = new String[tables.length];
    for (int i = 0; i < targets.length; i++) {
      targets[i] = ts.tables.get(tables[i]);
    }
    return targets;
  }

  private TableSet getTableSet(String name) {
    for (int i = 0; i < sets.length; i++) {
      if (sets[i].name.equals(name)){
        return sets[i];
      }
    }
    return null;
  }

  public Long getLastArchiveProcessId() throws SQLException {
    Long id = (Long) getProvider(null).queryObject(GET_MAX_ARCHIVE_PROCESS_ID);
    if (id == null) {
      id = new Long(0);
    }
    return id;
  }

  public String[] getSelectArchiveStatements() {
    return getStatementPerTable(SELECT_ARCHIVE_T);
  }

  public IReadWriteDataProvider getArchiveTablesetProvider() {
    return getTablesetProvider("Archive");
  }
  
  public IReadWriteDataProvider getSourceTablesetProvider() {
    return getProvider(sourceDataSource);
  }


  IReadWriteDataProvider getTablesetProvider(String name) {
    for (int i = 0; i < this.sets.length; i++) {
      if (sets[i].name.equals(name)) {
        return getProvider(sets[i].dataSource);
      }
    }
    throw new IllegalArgumentException("No tableset named " + name);
  }

  public IReadWriteDataProvider getProvider() {
    return getProvider(dataSources.getDefaultDataSource().getName());
  }
}
