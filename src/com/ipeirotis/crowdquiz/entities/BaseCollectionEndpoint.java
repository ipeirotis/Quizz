package com.ipeirotis.crowdquiz.entities;

import javax.jdo.PersistenceManager;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import com.google.appengine.api.datastore.Key;
import com.ipeirotis.crowdquiz.utils.PMF;

public abstract class BaseCollectionEndpoint<T> {
	
	private Class<T> cls;
	private String name;
	
	protected BaseCollectionEndpoint(Class<T> cls, String name) {
		this.cls = cls;
		this.name = name;
	}
	
	abstract protected Key getKey(T item);
	
	protected boolean contains(T item) {
		return PMF.singleGetObjectById(cls, getKey(item)) != null;
	}
	
	protected T insert(T item) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (contains(item)) {
				throw new EntityExistsException(name + " already exists");
			}
			mgr.makePersistent(item);
		} finally {
			mgr.close();
		}
		return item;
	}
	
	protected T update(T item){
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!contains(item)) {
				throw new EntityNotFoundException(name + " does not exist");
			}
			mgr.makePersistent(item);
		} finally {
			mgr.close();
		}
		return item;
	}
	
	protected void remove(Key key) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			T item = mgr.getObjectById(cls, key);
			mgr.deletePersistent(item);
		} finally {
			mgr.close();
		}
	}

	protected static PersistenceManager getPersistenceManager() {
		return PMF.getPM();
	}
}
