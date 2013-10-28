package com.ipeirotis.crowdquiz.entities;

import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.common.base.Strings;
import com.ipeirotis.crowdquiz.utils.PMF;

public abstract class BaseCollectionEndpoint<T> {
	
	private Class<T> cls;
	private String name;
	
	protected BaseCollectionEndpoint(Class<T> cls, String name) {
		this.cls = cls;
		this.name = name;
	}
	
	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings("unchecked")
	protected CollectionResponse<T> listItems(
				String cursorString, Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<T> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(cls);
			if (!Strings.isNullOrEmpty(cursorString)) {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<T>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (T obj : execute) {
				fetchItem(obj);
			}
		} finally {
			mgr.close();
		}

		return CollectionResponse.<T> builder()
				.setItems(execute).setNextPageToken(cursorString).build();
	}
	
	abstract protected Key getKey(T item);
	
	/**
	 * In typical case we don't need to do anything
	 * @param item
	 */
	protected void fetchItem(T item){};
	
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
