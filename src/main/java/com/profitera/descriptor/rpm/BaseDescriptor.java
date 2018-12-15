package com.profitera.descriptor.rpm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import com.profitera.util.QueryBundle;
import com.profitera.util.TopLinkQuery;

import oracle.toplink.publicinterface.DatabaseRow;
import oracle.toplink.queryframework.DataReadQuery;
import oracle.toplink.sessions.Session;

/**
 * @author jamison
 */
public abstract class BaseDescriptor implements Descriptor {
	public static final String MAIN_TABLE_ALIAS = "MAIN";
	public static final String MAIN_QUERY_NAME = "MAIN_QUERY";

	protected PropertyCache propertyCache;
	protected DatabaseRow currentRow;
	protected Session session;
	private HashMap methodCacheMap;
	private String bundleName;
	// Laziness of this guy is key for performance
	// descriptors will get instantiated alot during RPM
	// processing
	private QueryBundle qBundle;
	private Object id;

	public BaseDescriptor(String bundleName) {
		this(bundleName, null);
	}

	public BaseDescriptor(String bundleName, PropertyCache prefetchedProperties) {
		this.propertyCache = prefetchedProperties;
		this.bundleName = bundleName;
	}

	public void usePropertyCache(PropertyCache pc) {
		propertyCache = pc;
	}

	private String getQuery(String propertyName) {
		return getQBundle().getQuery(propertyName);
	}
	
	/**
	 * If the query starts with the main table name (i.e. not the select) 
	 * then its not a query but a 'short-cut' into the main query results 
	 * @param queryName
	 * @param args
	 * @return
	 */
	public String getQuery(String queryName, Object[] args){
		String query = getQBundle().getQuery(queryName, args);
		if (query == null || query.startsWith(MAIN_QUERY_NAME))
			return null;
		return query;
	}

	private String getQueryOnChar(String query, String keyValue) {
		return getQueryOn(query, keyValue, true);
	}
	private String getQueryOnNumber(String query, String keyValue) {
		return getQueryOn(query, keyValue, false);
	}
	/**
	 * TODO: Extract some of the functionality here and add it to QueryBundle
	 * call the new method insertCondition(String condText)
	 * @param query
	 * @param keyValue
	 * @param isChar
	 * @return
	 */
	private String getQueryOn(String query, String keyValue, boolean isChar) {
		//Get the first attribute returned, that is the ID so its what I want to filter on
		String id = QueryBundle.getSelectClauseMembers(query).values().iterator().next().toString();
		if (isChar)
			keyValue = "'" + keyValue + "'";
		return QueryBundle.insertQueryCondition(query, id
			+ "="
			+ keyValue, "AND");
	}

	/**
	 * @see com.profitera.descriptor.rpm.Descriptor#getValue(java.lang.String, java.lang.Object[])
	 */
	public String getValue(String propertyName, Object[] arguments) throws UnsupportedAttributeException{
		if (propertyCache != null && propertyCache.isSupported(propertyName, arguments)) {
			Object val = propertyCache.getPropertyValue(propertyName, arguments);
			if (val == null)
				return null;
			return val.toString();
		}
		if (currentRow!=null && (arguments == null || arguments.length == 0)){
			Object val = currentRow.getIndicatingNoEntry(propertyName.toUpperCase());
			if (!(val instanceof DatabaseRow.NoEntry)){
				if (val == null) return null;
				return val.toString();
			}
			
		}
		Exception thrown = null;
		try {
			Method match = findMatchingMethod(propertyName, arguments, this);
			Object[] params = null;
			try {
				params = convertArguments(arguments, match.getParameterTypes());
			} catch (Exception e) {
				throw new IllegalArgumentException(e.getMessage());
			}
			// Invoke it
			Object returnValue = match.invoke(this, params);
			// Construct fact with the value.
			if (returnValue instanceof Boolean) {
				boolean b = Boolean.getBoolean(returnValue.toString());
				if (b)
					returnValue = new Integer(1);
				else
					returnValue = new Integer(0);
			}
			return returnValue.toString();
		} catch (NoSuchMethodException e) {
			thrown = e;
		} catch (IllegalArgumentException e) {
			thrown = e;
		} catch (IllegalAccessException e) {
			thrown = e;
		} catch (InvocationTargetException e) {
			thrown = e;
		}
		// Try getting the queryBundle and composing our own sql, exploiting the
		// contract for the sql
		if (session != null){
			if (getId() instanceof Number)
				return getValueByQuery(propertyName, arguments, (Number)getId(),new Double(0),session).toString();
			else
				return getValueByQuery(propertyName, arguments, (String)getId(),new Double(0),session).toString();
		}
		throw new UnsupportedAttributeException("Unsupported: " + propertyName, thrown);
	}

	/**
		 * Find the method with the name passed in that can take the paramters
		 * passed in as arguemnts and return the method object. This method
		 * will be called alot and should implement some kind of caching in the
		 * instance in the future when we tune performance.
		 * @param name
		 * @param args
		 * @param o
		 * @return
		 * @throws NoSuchMethodException
		 */
	private Method findMatchingMethod(String name, Object[] args, Object o) throws NoSuchMethodException {
		if (o == null) {
			throw new NoSuchMethodException("Method not found: null objects have no type.");
		}
		if (methodCacheMap == null) {
			methodCacheMap = new HashMap();
			Method[] methods = o.getClass().getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (!methods[i].getReturnType().equals(Void.TYPE))
					methodCacheMap.put(methods[i].getName(), methods[i]);
			}
		}
		Method m = (Method) methodCacheMap.get(name);

		// Assme no overloading in the descriptors, args will always match
		//if (argumentsMatch(args, m.getParameterTypes()))
		if (m != null)
			return m;
		else
			throw new NoSuchMethodException(
				"Method not found: " + name + " that can match a method in " + o.getClass().getName());

	}

	/**
		 * Given a set of values as strings and a set of class objects (all
		 * must be primitive type wrappers or String) we return an array of the converted
		 * objects so they can be passed into an Method.invoke(), etc.
		 * Throws a vanilla exception if the string's can't match (Hey, I'm not
		 * going make an exception class for a private method!).
		 * @param argVals
		 * @param argTypes
		 * @return
		 */
	private Object[] convertArguments(Object[] argVals, Class[] argTypes) throws Exception {
		Object[] params = new Object[argVals.length];
		for (int i = 0; i < argVals.length; i++) {
			Class c = argTypes[i];
			String v = argVals[i].toString();
			//One if for each, followed by a continue
			if (c.equals(Integer.TYPE)) {
				params[i] = Integer.valueOf(v);
				continue;
			}
			if (c.equals(Long.TYPE)) {
				params[i] = Long.valueOf(v);
				continue;
			}
			if (c.equals(v.getClass())) {
				// String, just pass it in
				params[i] = argVals[i];
				continue;
			}
			if (c.equals(Boolean.TYPE)) {
				params[i] = Boolean.valueOf(v);
				continue;

			}
			if (c.equals(Short.TYPE)) {
				params[i] = Short.valueOf(v);
				continue;
			}

			if (c.equals(Float.TYPE)) {
				params[i] = Float.valueOf(v);
				continue;
			}
			if (c.equals(Double.TYPE)) {
				params[i] = Double.valueOf(v);
				continue;
			}
			throw new Exception("Arguments could not be converted to parameter class types.");
		}
		return params;
	}

	/**
	 * @param query
	 * @param objects
	 * @return
	 */
	public static String insertArguments(String query, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			query = query.replaceAll("[?]" + (i + 1), objects[i].toString());
		}
		return query;
	}

	protected Number getValueByQuery(
		String propName,
		Object[] args,
		Number key,
		Number defaultValue,
		Session session) {
		return getValueByQuery(propName, args, key, false, defaultValue, session);
	}

	protected Number getValueByQuery(
		String propName,
		Object[] args,
		String key,
		Number defaultValue,
		Session session) {
		return getValueByQuery(propName, args, key, true, defaultValue, session);
	}
	protected Number getValueByQuery(
		String propName,
		Object[] args,
		Object key,
		boolean isChar,
		Number defaultValue,
		Session session) {
		String query = getQuery(propName);
		if (query == null)
			throw new IllegalArgumentException(propName + " is an unsupported property");
		query = insertArguments(query, args);
		if (isChar)
			query = getQueryOnChar(query, key.toString());
		else
			query = getQueryOnNumber(query, key.toString());

		DataReadQuery readQuery = new DataReadQuery(query);
		// Required so DB2 won't freak out when lots of queries are put to it.
		readQuery.dontCacheStatement();
		readQuery.setName(propName);
		List resultV = TopLinkQuery.asList(readQuery, session);
		if (resultV.size() == 0)
			return defaultValue;
		DatabaseRow row = (DatabaseRow) resultV.get(0);
		if (row.getValues().get(1) == null)
			return defaultValue;
		else
			return (Number) row.getValues().get(1);
	}

	/**
	 * @return Returns the id.
	 */
	public Object getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(Object id) {
		this.id = id;
	}

	/**
	 * @return Returns the qBundle.
	 */
	public QueryBundle getQBundle() {
		if (qBundle == null)
			qBundle = new QueryBundle(bundleName,new String[]{"MAIN_TABLE_REGEX"}, new String[]{MAIN_TABLE_ALIAS});
		return qBundle;
	}

	/**
	 * @param currentRow The currentRow to set.
	 */
	public void setCurrentRow(DatabaseRow currentRow) {
		this.currentRow = currentRow;
	}
	
	public DatabaseRow getCurrentRow() {
		return currentRow;
	}

}
