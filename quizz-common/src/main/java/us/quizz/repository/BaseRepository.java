package us.quizz.repository;

import java.util.HashMap;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import us.quizz.utils.PMF;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.common.base.Strings;

public abstract class BaseRepository<T> {

	private Class<T> cls;

	protected BaseRepository(Class<T> cls) {
		this.cls = cls;
	}
	
	abstract protected Key getKey(T item);

	/**
	 * This method lists all the entities inserted in datastore. It uses HTTP
	 * GET method and paging support.
	 * 
	 * @return A CollectionResponse class containing the list of all entities
	 *         persisted and a cursor to the next page.
	 */
	@SuppressWarnings("unchecked")
	public CollectionResponse<T> listItems(String cursorString, Integer limit) {

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

			// Tight loop for fetching all entities from datastore and
			// accomodate
			// for lazy fetch.
			for (T obj : execute) {
				fetchItem(obj);
			}
		} finally {
			mgr.close();
		}

		return CollectionResponse.<T> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}
	
	@SuppressWarnings("unchecked")
	public List<T> list(){
		PersistenceManager pm = PMF.getPM();
		try{
			Query query = pm.newQuery(cls);
			return (List<T>)query.execute();
		}finally{
			pm.close();
		}
	}

	/**
	 * In typical case we don't need to do anything
	 * 
	 * @param item
	 */
	public void fetchItem(T item) {
	};

	public boolean contains(T item) {
		if(getKey(item) == null)
			return false;
		else
			return PMF.singleGetObjectById(cls, getKey(item)) != null;
	}

	public T insert(T item) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (contains(item)) {
				throw new EntityExistsException(cls.getSimpleName() + " already exists");
			}
			mgr.makePersistent(item);
		} finally {
			mgr.close();
		}
		return item;
	}

	public T update(T item) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!contains(item)) {
				throw new EntityNotFoundException(cls.getSimpleName() + " does not exist");
			}
			mgr.makePersistent(item);
		} finally {
			mgr.close();
		}
		return item;
	}
	
	public T save(T item) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			mgr.makePersistent(item);
		} finally {
			mgr.close();
		}
		return item;
	}

	public void remove(Key key) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			T item = mgr.getObjectById(cls, key);
			mgr.deletePersistent(item);
		} finally {
			mgr.close();
		}
	}
	
	public T get(Long id){
		PersistenceManager mgr = getPersistenceManager();
		T item = null;
		try {
			item = mgr.getObjectById(cls, id);
		} finally {
			mgr.close();
		}
		return item;
	}
	
	public T singleMakePersistent(T item) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			return mgr.makePersistent(item);
		} finally {
			mgr.close();
		}
	}
	
	public T singleGetObjectByIdThrowing(Object key) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			return mgr.getObjectById(cls, key);
		} finally {
			mgr.close();
		}
	}
	
	public T singleGetObjectById(Object key) {
		try {
			return singleGetObjectByIdThrowing(key);
		} catch (JDOObjectNotFoundException ex) {
			return null;
		}
	}
	
	public void saveAll(List<T> list){
		PersistenceManager mgr = getPersistenceManager();
	
		try {
			mgr.makePersistentAll(list);
		} finally {
			mgr.close();
		}
	}

	public PersistenceManager getPersistenceManager() {
		return PMF.getPM();
	}
}
