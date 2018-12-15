package com.profitera.rpm;

import java.util.HashMap;
import java.util.Iterator;

import oracle.toplink.publicinterface.DatabaseRow;

import com.profitera.util.KeyedListIterator;


public class DatabaseRowIterator extends KeyedListIterator{
	private String key;
	public DatabaseRowIterator(Iterator stream, String key) {	
		super(stream);
		this.key = key;	
	}
	
	protected Comparable getNextKey(Object nextObject) {
		DatabaseRow row = (DatabaseRow) nextObject;
		Object keyVal = getKeyValue(row);
		if (keyVal instanceof Number)
			return new Double(((Number)keyVal).intValue());
		else
			throw new RuntimeException("Key value (column: " + key + ") does not implement " + Number.class.getName() + "as expected, is a " + keyVal.getClass().getName());
	}

	private Object getKeyValue(DatabaseRow row) {
		Object keyVal = row.get(key);
		if (keyVal == null){
			keyVal = row.get(key.toLowerCase());
			if (keyVal == null){
				keyVal = row.get(key.toUpperCase());
				if (keyVal == null)
					throw new RuntimeException("Key value (column: " + key + ") was null for row: " + new HashMap(row));
			}
		}
		return keyVal;
	}
}