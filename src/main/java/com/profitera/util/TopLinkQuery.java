/*
 * Created on Aug 25, 2003
 */
package com.profitera.util;

import java.io.CharArrayReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.util.reflect.Reflect;

import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.*;
import oracle.toplink.sessions.Session;

/**
 * @author jamison
 */
public class TopLinkQuery {
	public static Log log = LogFactory.getLog(TopLinkQuery.class);
	public static final String STREAM = "STREAM";
	public static final String LIST = "LIST";
    /**
     * @see TopLinkQuery#getSingleRowQueryResult(DatabaseQuery, Session)
     */
    public static ReportQueryResult getSingleRowQueryResult(ReportQuery query, Session session) {
        Iterator i = asList(query, session).iterator();
        if (i.hasNext()) {
            return (ReportQueryResult) i.next();
        } else {
            return null;
        }
    }

    /**
     * If your query will only return a single row, this method will get a client session
     * and execute it for you and return the first (and only) result row as a ReportQueryResult.
     * If there are no results (which seems to be a common occurance) it returns null, so watch out!
     */
    public static Object getSingleRowQueryResult(DatabaseQuery query, Session session) {
        Object o = asObject(query, session);
        if (o instanceof Vector) {
            Vector v = (Vector) o;
            Iterator i = v.iterator();
            if (i.hasNext()) {
                return i.next();
            } else {
                return null;
            }
        } else {
            return o;
        }
    }

    /**
     * Whatever is in the defaults array will be untouched when nulls are returned.
     * If the results are two few or the array too short this method will only fill
     * up to the shortest (it won't throw an exception).
     * <br/>
     * NOTE: The double[] return is just a convinience, the array param passed in
     * is modified itself, a ref to that is returned.
     */
    public static double[] getSingleRowQueryResult(double[] defaults, ReportQuery rq, Session session) {
        ReportQueryResult qResult = getSingleRowQueryResult(rq, session);
        if (qResult != null) {
            for (int i = 0; i < defaults.length && i < qResult.size(); i++) {
                if (qResult.getByIndex(i) != null) {
                    defaults[i] = ((Number) qResult.getByIndex(i)).doubleValue();
                }
            }
        }
        return defaults;
    }
    
    public static double getSingleRowQueryResult(double defaultValue, ReportQuery rq, Session session) {
    	return getSingleRowQueryResult(new double[]{defaultValue}, rq, session)[0];
    }

    /**
     * The code for this one is self explanitory, pass in an expression that is really
     * a date field ref and it will give you a between (which is sorely missing in toplink
     * itself.
     *
     * @return if date1 or date2 is null: expression will use the start and end time of that date
     *         if both are null: expression just returns null
     *         otherwise, expression is the range between the two dates
     */
    public static Expression between(Expression e, Date date1, Date date2) {
        if (date1 == null && date2 == null) return null;
        /* These conditions are needed if only one date is specified*/
        if (date1 == null) date1 = date2;
        if (date2 == null) date2 = date1;
        /* This condition is needed if the dates are chronologically reversed */
        if (date2.before(date1)) { // swap the dates if date2 is chronologically before date1
            Date temp = date1;
            date1 = date2;
            date2 = temp;
        }
        return e.greaterThanEqual(DateParser.getStartTimeOfDate(date1)).and(
            e.lessThanEqual(DateParser.getEndTimeOfDate(date2)));
    }

    /**
     * TopLink's CLOB support is 'ultra-crappy freeze-up style' so if you need to
     * update a CLOB column do it here. Pass in a connection object and the details of
     * where to find the table and you CLOB will be updated. It is assume that the
     * primary key is long-compatible, if not, write your own damn method!
     * <b> This doesn't commit, you have to handle ACID yo'self</b>
     */
    public static void updateCLOBColumn(Connection connection, String tableName, String clobColumnName, String newClobValue, String keyColumn, long key) throws SQLException {
        char[] charArray = newClobValue.toCharArray();
        CharArrayReader charReader = new CharArrayReader(charArray);
        PreparedStatement stmt = connection.prepareStatement(
            "UPDATE " + tableName + " SET " + clobColumnName + " = ? WHERE " + keyColumn + " = ?");
        stmt.setCharacterStream(1, charReader, charArray.length);
        //TODO: Changed to setInt for MS Access
        stmt.setInt(2, new Long(key).intValue());
        // Update clob
        stmt.executeUpdate();
    }

    /**
     * Same as the other method of the same name, but the count
     * query is needed for custom SQL
     *
     * @see TopLinkQuery#asCursoredStream(ReadAllQuery query, int initialBlockSize, int blockSize, Session session)
     */
    public static CursoredStream asCursoredStream(ReadAllQuery query, int initialBlockSize, int blockSize, ValueReadQuery itemCountQuery, Session session) {
        if (itemCountQuery == null) {
            query.useCursoredStream(initialBlockSize, blockSize);
        } else {
            query.useCursoredStream(initialBlockSize, blockSize, itemCountQuery);
        }
        return (CursoredStream) asObject(query, session);
    }

    /**
     * A simplification to allow you to easy use a query for a stream rather
     * than the usual Vector (which is pretty much always a bad idea in
     * the over-night processes) in one line. The data access layer needs to
     * be moved towards more of a "Query-factory -> executor -> result" type
     * of a pattern so the same queries can be used with different output styles,
     * for which this will be useful.
     */
    public static CursoredStream asCursoredStream(ReadAllQuery query, int initialBlockSize, int blockSize, Session session) {
        return asCursoredStream(query, initialBlockSize, blockSize, null, session);
    }

    /**
     * Will 'and' together the two expressions and take care of nulls for you,
     * if either of the expressions is null it is ignored and the non-null expression is
     * returned (i.e. null AND something == something) if both are null, null is returned.
     * This is a common problem when building complex expressions where based on arguments
     * to a method you may make a query more or less selective
     */
    public static Expression andExpressions(Expression e1, Expression e2) {
        if (e1 == null) return e2;
        if (e2 == null) return e1;
        return e1.and(e2);
    }

    /**
     * Executes the SQL and returns the first value returned, assumes
     * that the query has only 1 item in select clause (select X from...
     * not select X, Y from...)
     *
     * @return value returned by query, or null
     */
    public static Object queryOneValue(String name, String query, Session session) {
        Iterator i = flatten(asList(name, query, session)).iterator();
        if (i.hasNext()) {
            return i.next();
        }
        return null;
    }

    public static List asList(ReadAllQuery q, Session tlSession) {
        return asList((DatabaseQuery)q, tlSession);
    }

    public static List asList(String name, String query, Session tlSession) {
        DataReadQuery dataReadQuery = new DataReadQuery(query);
        dataReadQuery.setName(name);
		return asList(dataReadQuery, tlSession);
    }
    
    public static List asList(DatabaseQuery q, Session tlSession){
        return (List) asObject(q, tlSession);
    }
    
    public static Object asObject(DatabaseQuery q, Session tlSession){
    	long start = System.currentTimeMillis();
    	Object o = tlSession.executeQuery(q);
    	log(start, q);
        return o;
    }
    
    private static void log(long start, DatabaseQuery q){
    	log.debug("Query ran " + (System.currentTimeMillis() - start) + ": " + q.getName() + "  - " + q.getSQLString());
    }

    /**
     * Naively replaces the select clause with a count * to get the count
     */
    public static CursoredStream asCursoredStream(String mainQuery, int initialBlockSize, int blockSize, Session session) {
        DataReadQuery q = new DataReadQuery(mainQuery);
        ValueReadQuery count = new ValueReadQuery(
            QueryBundle.replaceSelectClause(mainQuery, "count(*)"));
        q.useCursoredStream(initialBlockSize, blockSize, count);
        return (CursoredStream) session.executeQuery(q);
    }
    
    public static void executeUpdateSQL(String queryName, String sql, Session session) {
        DataModifyQuery query = new DataModifyQuery(sql);
        query.setName(queryName);
        asObject(query, session);
    }

    /**
     * This takes a List is this in fact the results from a report query and returns a list
     * that allows you to access the results as if they are top-level items in the orginal
     * list, this is intended for queries that have returned single values.
     * The items are extracted by assuming each item implements the Map interface, getting
     * the first key in the first item in the List and using that to extract the
     * values.
     */
    public static List flatten(List list) {
        if (list.size() == 0) return Collections.EMPTY_LIST;
        Object key;
        if (list.get(0) instanceof Map) {
            Map row = (Map) list.get(0);
            if (row.keySet().size() != 1) {
                throw new RuntimeException("Can not flatten report results if column count not equal to 1: " +
                    row.keySet());
            }
            key = row.keySet().iterator().next();
        } else {
            throw new RuntimeException(
                "List is of an unsupported item type: " + list.get(0).getClass());
        }
        ArrayList flat = new ArrayList(list.size());
        for (Iterator i = list.iterator(); i.hasNext();) {
            flat.add(((Map) i.next()).get(key));
        }
        return flat;
    }

    /**
     * Uses the base expression to tack on an equal(value) at the end, but if
     * ignoreOnNull is true it will return null if the value is null and if
     * ignoreOnBlank is true and the value.toString().equals("") then it will
     * return null. Goes well with a small dash of andExpressions()
     */
    public static Expression equalityExpression(Expression base, Object value, boolean ignoreOnNull, boolean ignoreOnBlank) {
        if (ignoreOnNull && value == null) {
            return null;
        } else if (value == null) {
            return base.isNull();
        }
        if (ignoreOnBlank && value.toString().trim().equals("")) {
            return null;
        }
        return base.equal(value);
    }

    /**
     * Uses the base expression to tack on the constraints based on bottom and top.
     * If top or bottom are null the other is used and the query becomes a
     * greater than eq (bottom not null) or less than eq (top not null). If
     * both are null, null is returned. Best served with
     * andExpressions :)
     */
    public static Expression getRangeExpression(Expression base, Number bottom, Number top) {
        if (bottom != null && top != null) {
            return base.between(bottom, top);
        }
        if (bottom != null) {
            return base.greaterThanEqual(bottom);
        }
        if (top != null) {
            return base.lessThanEqual(top);
        }
        return null;
    }
    public static Expression getRangeExpression(Expression base, Date bottom, Date top) {
      if (bottom != null && top != null) {
          return base.between(bottom, top);
      }
      if (bottom != null) {
          return base.greaterThanEqual(bottom);
      }
      if (top != null) {
          return base.lessThanEqual(top);
      }
      return null;
  }

	/**
	 * <code>query</code> is returned as a courtesy, it is in fact the same object as was passed in.
	 */
	public static DatabaseQuery buildSimpleQuery(DatabaseQuery query, String[] attributes, Object value) {
		ExpressionBuilder eb = new ExpressionBuilder();
		Expression e = eb.get(attributes[0]);
		for(int i = 1; i < attributes.length; i++)
			e = e.get(attributes[i]);
		query.setSelectionCriteria(e.equal(value));
		return query;
	}

	public static Object getObject(Class clazz, String[] attributes, Object keyValue, Session session) {
		ReadObjectQuery readObjectQuery = new ReadObjectQuery(clazz);
		readObjectQuery.setName("Single: " + Reflect.getUnqualifiedName(clazz) + ": " + Strings.getListString(attributes, ", "));
		return TopLinkQuery.asObject(TopLinkQuery.buildSimpleQuery(readObjectQuery, attributes, keyValue), session);
	}
	
	public static List getObjects(Class clazz, String[] attributes, Object keyValue, Session session) {
		ReadAllQuery all = new ReadAllQuery(clazz);
		all.setName("Many: " + Reflect.getUnqualifiedName(clazz) + ": " + Strings.getListString(attributes, ", "));
		return TopLinkQuery.asList(TopLinkQuery.buildSimpleQuery(all, attributes, keyValue), session);
	}

	public static Iterator asIterator(String name, String strategy, String mainQuery, int batchSize, Session session) {
		if (isListStrategy(strategy))
			return asList(name, mainQuery, session).iterator();	
		return new StreamIterator(asCursoredStream(mainQuery, batchSize, batchSize, session));
	}

	private static boolean isListStrategy(String strategy) {
		if (strategy == null)
			return false;
		if (strategy.trim().toUpperCase().startsWith(LIST))
			return true;
		return false;
	}

	public static Iterator asIterator(String strategy, ReadAllQuery raq, int batchSize, Session session) {
		if (isListStrategy(strategy))
			return asList(raq, session).iterator();	
		return new StreamIterator(asCursoredStream(raq, batchSize, batchSize, session));
	}

	
}