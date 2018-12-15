package com.profitera.descriptor.rpm;

/**
 * @author jamison
 */
public interface Descriptor {
	/**
	 * Returns the value of a property, the property is usually defined as a database query
	 * and as such the arguemnts are parameters passed into the query.
	 * @param propertyName
	 * @param arguments
	 * @return
	 * @throws UnsupportedAttributeException
	 */
	public String getValue(String propertyName, Object[] arguments) throws UnsupportedAttributeException;
	/**
	 * @return A unique identifier for the descriptor
	 */
	public Object getId();
}
