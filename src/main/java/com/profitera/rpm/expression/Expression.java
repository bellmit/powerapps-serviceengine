package com.profitera.rpm.expression;

import java.util.Vector;

/**
 * @author Jamison Masse
 * <br/>
 * Expressions are a representation of logical and/or
 * mathematical expression. There's 3 types:<br/><pre>
 * - Atom: a static value
 * - Compound (logical/math): an expression with a LHS and a RHS
 * - Method: a method call/attribute of an object.
 * </pre>
 * Expressions are recursively defined, a compound is 2 expressions
 * and a connective, a method call is a method name and a list
 * of argument expressions.
 * 
 */

public class Expression {
	// Atom Types
	final public static int NONE = 0;
	final public static int REAL = 1;
	final public static int WHOLE = 2;
	final public static int ENUMERATION = 3;

	// LOGICAL OPS
	final public static int AND = 1;
	final public static int OR = 2;
	final public static int EQUAL = 3;
	final public static int NOT_EQUAL = 4;
	final public static int LESS_THAN = 5;
	final public static int GREATER_THAN = 6;
	final public static int LESS_THAN_OR_EQUAL = 7;
	final public static int GREATER_THAN_OR_EQUAL = 8;

	//MATH OPS
	final public static int ASSIGN = 9;
	final public static int MULTIPLY = 10;
	final public static int DIVIDE = 11;
	final public static int ADD = 12;
	final public static int SUBTRACT = 13;

	// A quick look-up for connective strings
	final public static String[] CONNECTIVE_TEXT =
		{ "", "and", "or", "=", "!=", "<", ">", "<=", ">=", "=", "*", "/", "+", "-" };
	// use in isLogical
	final public static int[] LOGICAL_CONNECTIVES =
		{ AND, OR, EQUAL, NOT_EQUAL, LESS_THAN, GREATER_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL };
	//	Nice to have around
	final public static int[] CONNECTIVES =
		{
			NONE,
			AND,
			OR,
			EQUAL,
			NOT_EQUAL,
			GREATER_THAN,
			GREATER_THAN_OR_EQUAL,
			LESS_THAN,
			LESS_THAN_OR_EQUAL,
			ASSIGN,
			MULTIPLY,
			DIVIDE,
			ADD,
			SUBTRACT };

	// Atom
	private boolean isAtom = false;
	private int atomType = 0;
	private Object atomValue = null;
	// Enumeration Atom
	private String enumerationId = null; //corresponds to the id of the group of valid values (i.e. 'getProductCode')
	private String enumerationName = null; // Used for toString

	// Compound Expression
	private boolean isCompound = false;
	private Expression leftExpression = null;
	private Expression rightExpression = null;

	//Method call/Attribute expression
	private int connective = 0;
	private boolean isMethod = false;
	private String methodDisplay = null;
	private String methodName = null;
	private Expression[] methodArguments = null;

	/**
	 * Creates a new compound Expression using the logical or mathematical 
	 * connective and the right and left hand sides supplied.
	 * @param connective
	 * @param left
	 * @param right
	 */
	public Expression(int connective, Expression left, Expression right) {
		setConnective(connective);
		setLeftExpression(left);
		setRightExpression(right);
		setCompound(true);
	}

	/**
	 * Creates an Expression Atom of the type atomType. Note
	 * that the atomValue will be persisted using its toString
	 * method, so anything that produces a string that looks like,
	 * say, an Integer, could be used as one in an Expression.
	 * @param atomValue
	 * @param atomType
	 */
	public Expression(Object atomValue, int atomType) {
		setAtomValue(atomValue);
		setAtomType(atomType);
		setAtom(true);
	}

	/**
	 * Creates an Expression Enumeration with the value atomValue
	 * and a display name name for convienience. The ref is indended
	 * for use to identify the source/lookup/code-table from which
	 * this enum is derived if such information was needed.
	 * @param atomValue
	 * @param ref
	 * @param name
	 */
	public Expression(Object atomValue, String enumExternalID, String name) {
		setAtomValue(atomValue);
		setEnumerationId(enumExternalID);
		setEnumerationName(name);
		setAtomType(ENUMERATION);
		setAtom(true);
	}

	/**
	 * Creates a Expression representing a method call, or an 
	 * attribute. The name, as it would be call obj.name is the
	 * methodName, args is an array of arguments, the args should
	 * be atoms, no nested method calls for now.
	 * @param methodName
	 * @param methodDisplay
	 * @param args
	 */
	public Expression(String methodName, String methodDisplay, Expression[] args) {
		setMethodDisplay(methodDisplay);
		setMethodName(methodName);
		// If args is null set it to a zero-length array
		if (null == args)
			args = new Expression[0];
		setMethodArguments(args);
		setMethod(true);
	}

	/**
	 * @return
	 */
	public int getAtomType() {
		return atomType;
	}

	/**
	 * @return
	 */
	public Object getAtomValue() {
		return atomValue;
	}

	/**
	 * @return
	 */
	public boolean isAtom() {
		return isAtom;
	}

	/**
	 * @return
	 */
	public boolean isCompound() {
		return isCompound;
	}

	/**
	 * I.e. Its a truth-value expression not a mathematical one.
	 * @return
	 */
	public boolean isLogicalCompound() {
		for (int i = 0; i < LOGICAL_CONNECTIVES.length; i++)
			if (LOGICAL_CONNECTIVES[i] == getConnective())
				return true;
		return false;
	}

	/**
	 * @return LHS
	 */
	public Expression getLeftExpression() {
		return leftExpression;
	}

	/**
	 * @return RHS
	 */
	public Expression getRightExpression() {
		return rightExpression;
	}

	/**
	 * @param i
	 */
	private void setAtomType(int i) {
		atomType = i;
	}

	/**
	 * @param object
	 */
	private void setAtomValue(Object object) {
		atomValue = object;
	}

	/**
	 * @param b
	 */
	private void setAtom(boolean b) {
		isAtom = b;
	}

	/**
	 * @param b
	 */
	private void setCompound(boolean b) {
		isCompound = b;
	}

	/**
	 * @param expression
	 */
	public void setLeftExpression(Expression expression) {
		leftExpression = expression;
	}

	/**
	 * @param expression
	 */
	public void setRightExpression(Expression expression) {
		rightExpression = expression;
	}

	/**
	 * @return Connective
	 */
	public int getConnective() {
		return connective;
	}

	/**
	 * @param i
	 */
	private void setConnective(int i) {
		connective = i;
	}

	/**
	 * @return Enum ID
	 */
	public String getEnumerationId() {
		return enumerationId;
	}

	/**
	 * @param string
	 */
	private void setEnumerationId(String string) {
		enumerationId = string;
	}

	/**
	 * @return -
	 */
	public boolean isMethod() {
		return isMethod;
	}

	/**
	 * @return args
	 */
	public Expression[] getMethodArguments() {
		return methodArguments;
	}

	/**
	 * @return Display Name
	 */
	public String getMethodDisplay() {
		return methodDisplay;
	}

	/**
	 * @param b
	 */
	private void setMethod(boolean b) {
		isMethod = b;
	}

	/**
	 * @param expressions
	 */
	private void setMethodArguments(Expression[] expressions) {
		methodArguments = expressions;
	}

	/**
	 * @param string
	 */
	public void setMethodDisplay(String string) {
		methodDisplay = string;
	}

	private String methodToString(String leftArgDelim, String rightArgDelim) {
		String[] descs = new String[getMethodArguments().length];
		for (int i = 0; i < descs.length; i++)
			descs[i] = leftArgDelim + getMethodArguments()[i].toString() + rightArgDelim;
		return Expression.getCompleteDisplayName(getMethodDisplay(), descs);
	}

	private String atomToString() {
		String s = "";
		switch (getAtomType()) {
			case NONE :
				s = s + "<NONE>";
				break;
			case ENUMERATION :
				s = s + getEnumerationName();
				break;
			default :
				s = s + getAtomValue().toString();
		}
		return s;
	}
	public String getConnectiveString() {
		return " " + CONNECTIVE_TEXT[getConnective()] + " ";
	}
	private String compoundToString(String leftArgDelim, String rightArgDelim) {

		String left = "<NONE>";
		String right = "<NONE>";
		if (getLeftExpression() != null)
			left = getLeftExpression().toString(leftArgDelim, rightArgDelim);
		if (getRightExpression() != null)
			right = getRightExpression().toString(leftArgDelim, rightArgDelim);
		return left + getConnectiveString() + right;
	}

	public String toString(String leftArgDelim, String rightArgDelim) {
		if (isAtom())
			return atomToString();
		if (isCompound())
			return compoundToString(leftArgDelim, rightArgDelim);
		if (isMethod())
			return methodToString(leftArgDelim, rightArgDelim);
		return "";
	}
	
	public String toString(){
		return toString("{","}");
	}

	/**
	 * @return
	 */
	public String getEnumerationName() {
		return enumerationName;
	}

	/**
	 * @param string
	 */
	public void setEnumerationName(String string) {
		enumerationName = string;
	}

	/**
	 * @return
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param string
	 */
	private void setMethodName(String string) {
		methodName = string;
	}

	public boolean isValid() {
		if (isAtom() && getAtomType() == ENUMERATION)
			return getAtomValue() != null && getEnumerationId() != null && getEnumerationName() != null;

		if (isAtom())
			return getAtomValue() != null && getAtomType() != NONE;
		if (isMethod()) {
			for (int i = 0; i < getMethodArguments().length; i++)
				if (!getMethodArguments()[i].isValid())
					return false;
			return getMethodName() != null && getMethodDisplay() != null;
		}
		if (isCompound()) {
			return getConnective() != NONE
				&& getLeftExpression() != null
				&& getRightExpression() != null
				&& getLeftExpression().isValid()
				&& getRightExpression().isValid();

		}
		// If it is not one of the above, something is screwed.
		return false;
	}

	public static int guessExpressionType(Class clazz) {
		if (clazz.equals(Integer.TYPE) || clazz.equals(Long.TYPE) || clazz.equals(Short.TYPE)) {
			return Expression.WHOLE;
		} else if (clazz.equals(Float.TYPE) || clazz.equals(Double.TYPE)) {
			return Expression.WHOLE;
		} else if (clazz.equals(Void.TYPE)) {
			return Expression.NONE;
		} else {
			return Expression.ENUMERATION;
		}

	}

	private static String removeEscapedPercents(String label) {
		int pos = label.indexOf('%', 0);
		if (pos == -1)
			return label;
		return label.substring(0, pos) + label.substring(pos + 1, label.length());
	}

	private static int findPercent(String label, int start) {
		int pos = label.indexOf('%', start);
		if (label.length() > pos + 1 && label.charAt(pos + 1) == '%') {
			return findPercent(label, pos + 2);
		}
		return pos;

	}

	public String[] breakOutDisplayName() {
		return breakOutDisplayName(getMethodDisplay());
	}

	// Return will ALWAYS have a lead string, the first arg will always go
	// after the first string. If the arg is the first thing the lead string
	// will be ""
	public static String[] breakOutDisplayName(String displayName) {
		String label = displayName;
		Vector strings = new Vector();
		//TODO: There's a better way to parse this!
		// Checking string length fixes empty label bug
		if (label.length() > 1 && label.charAt(0) == '%' && label.charAt(1) != '%')
			strings.add("");
		else if (label.length() == 1 && label.charAt(0) == '%')
			strings.add("");
		int percentIndex = findPercent(label, 0);
		if (percentIndex == -1)
			strings.add(removeEscapedPercents(label));
		else
			strings.add(removeEscapedPercents(label.substring(0, percentIndex)));
		while (percentIndex != -1) {
			if (percentIndex != -1) {
				int oldIndex = percentIndex;
				percentIndex = findPercent(label, oldIndex + 1);
				if (percentIndex == -1)
					strings.add(removeEscapedPercents(label.substring(oldIndex + 1)));
				else
					strings.add(removeEscapedPercents(label.substring(oldIndex + 1, percentIndex)));
			}
		}
		String[] result = new String[strings.size()];
		strings.copyInto(result);
		return result;
	}
	public static String getCompleteDisplayName(String name, String[] arguments) {
		String[] display = Expression.breakOutDisplayName(name);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < display.length; i++) {
			sb.append(display[i]);
			if (i < arguments.length)
				sb.append(arguments[i]);
		}
		for (int i = display.length; i < arguments.length; i++)
			sb.append(arguments[i]);
		return sb.toString();
	}

	/**
	 * Warning! This method is not complete!
	 * TODO: Finish this method Expression.equals
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Expression))
			return false;
		Expression e = (Expression) o;
		if (isAtom() && e.isAtom() && e.getAtomValue().equals(getAtomValue()))
			return true;
		return false;
	}

}
