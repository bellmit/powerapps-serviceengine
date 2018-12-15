package com.profitera.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import oracle.toplink.queryframework.CursoredStream;

/**
 * This class uses a single object lookahead by keeping an object 
 * 'on deck' at all times. When the 'on deck' object is null then 
 * we have reached the end of the stream. This is not strictly 
 * necessary, but it makes extending this class much easier.
 * @author jamison
 */
public class StreamIterator implements Iterator {
	
	private CursoredStream stream;
	protected Object nextObject;

	public StreamIterator(CursoredStream stream){
		this.stream = stream;
		if (stream.hasMoreElements()) nextObject = stream.read();
		else nextObject = null;
		if (!stream.hasMoreElements()) stream.close();
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	public final void remove() {
		throw new UnsupportedOperationException("remove() not supported by " + this.getClass().getName()); 
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public final boolean hasNext() {
		return nextObject != null;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public final Object next() {
		if (nextObject == null) throw new NoSuchElementException();
		stream.releasePrevious();
		Object returnVal = nextObject;
		if (stream.hasMoreElements())
			nextObject = stream.read();
		else
			nextObject = null;
		if (!stream.hasMoreElements()) 
			stream.close();
		return returnVal;
	}
	
	public void close(){
		if (!stream.isClosed()) stream.close();
	}
}
