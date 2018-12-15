package com.profitera.rpm;

import java.util.Iterator;

import com.profitera.descriptor.db.account.Account;
import com.profitera.util.KeyedListIterator;


public class AccountIterator extends KeyedListIterator{
	public AccountIterator(Iterator stream) {	super(stream);	}
	protected Comparable getNextKey(Object nextObject) { return ((Account)nextObject).getAccountId(); }
}