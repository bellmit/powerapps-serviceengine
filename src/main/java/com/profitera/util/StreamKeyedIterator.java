package com.profitera.util;

import oracle.toplink.queryframework.CursoredStream;

/**
 * This class enables the single object lookahead required
 * by the KeyedIterator by keeping an object 'on deck' at all
 * times. When the 'on deck' object is null then we have reached
 * the end of the stream.
 * @author jamison
 */
public abstract class StreamKeyedIterator extends KeyedListIterator implements KeyedIterator {
	
	public StreamKeyedIterator(CursoredStream stream){
		super(new StreamIterator(stream));
	}

	/**
	 * This is the required extension, return comparable by
	 * looking at the object 'on-deck' 
	 * @param nextObject2
	 * @return
	 */
	protected abstract Comparable getNextKey(Object nextObject);

}
