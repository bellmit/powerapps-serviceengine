package com.profitera.descriptor.rpm;

/**
 * @author jamison
 */
public interface PropertyCache {
	public abstract boolean isSupported(String name, Object[] arguments);
	public abstract Object getPropertyValue(String name, Object[] arguments);
}