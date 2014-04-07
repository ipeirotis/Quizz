package us.quizz.repository;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.common.base.Strings;

import us.quizz.utils.CachePMF;
import us.quizz.utils.PMF;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

public abstract class BaseRepository<T> {
  private static final Logger logger = Logger.getLogger(BaseRepository.class.getName());

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
      if (cursor != null) {
        cursorString = cursor.toWebSafeString();
      }
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
  public List<T> list() {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = pm.newQuery(cls);
      return (List<T>)query.execute();
    } finally {
      pm.close();
    }
  }

  /**
   * In typical case we don't need to do anything
   * @param item
   */
  public void fetchItem(T item) {
  };

  public boolean contains(T item) {
    if (getKey(item) == null) {
      return false;
    }
    else {
      return singleGetObjectById(cls, getKey(item)) != null;
    }
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
  
  public void removeAll(Collection<T> objects) {
    PersistenceManager mgr = getPersistenceManager();
    try {
      
      mgr.deletePersistentAll(objects);
    } finally {
      mgr.close();
    }
  }


  public T get(Long id) {
    return get(id, false  /* use transaction */);
  }

  public T get(Long id, boolean useTransaction) {
    PersistenceManager mgr = getPersistenceManager();
    Transaction tx = mgr.currentTransaction();
    T item = null;
    try {
      if (useTransaction) {
        tx.begin();
        item = mgr.getObjectById(cls, id);
        tx.commit();
      } else {
        item = mgr.getObjectById(cls, id);
      }
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      mgr.close();
    }
    return item;
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

  public void saveAll(List<T> list) {
    saveAll(list, false);
  }

  public void saveAll(List<T> list, boolean useTransaction) {
    PersistenceManager mgr = getPersistenceManager();
    Transaction tx = mgr.currentTransaction();
    try {
      if (useTransaction) {
        tx.begin();
        mgr.makePersistentAll(list);
        tx.commit();
      } else {
        mgr.makePersistentAll(list);
      }
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      mgr.close();
    }
  }

  public PersistenceManager getPersistenceManager() {
    return PMF.getPM();
  }

  public void singleMakePersistent(Object... items) {
    PersistenceManager pm = getPersistenceManager();
    try {
      pm.makePersistentAll(items);
    } finally {
      pm.close();
    }
  }

  public T singleMakePersistent(T item) {
    return singleMakePersistent(item, false  /* use transaction */);
  }

  public T singleMakePersistent(T item, boolean useTransaction) {
    PersistenceManager pm = getPersistenceManager();
    Transaction tx = pm.currentTransaction();
    try {
      if (useTransaction) {
        tx.begin();
        T results = pm.makePersistent(item);
        tx.commit();
        return results;
      } else {
        return pm.makePersistent(item);
      }
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
  }

  public void makePersistentIterative(Object... items) {
    PersistenceManager pm = getPersistenceManager();
    try {
      for (Object item : items) {
        pm.makePersistent(item);
      }
    } finally {
      pm.close();
    }
  }

  public <T> T singleGetObjectById(Class<T> cls, Object key) {
    try {
      return singleGetObjectByIdThrowing(cls, key);
    } catch (JDOObjectNotFoundException ex) {
      logger.warning("PM: Didn't found object: " + cls.getCanonicalName()
          + " , key: " + key.toString());
      return null;
    }
  }

  public <T> T singleGetObjectByIdThrowing(Class<T> cls, Object key) {
    PersistenceManager pm = getPersistenceManager();
    try {
      return pm.getObjectById(cls, key);
    } finally {
      pm.close();
    }
  }

  public <T> T singleGetObjectByIdWithCaching(String cacheKey,
      Class<T> cls, Object key) {
    T item = CachePMF.get(cacheKey, cls);
    if (item != null) {
      return item;
    }
    item = singleGetObjectById(cls, key);
    if (item != null) {
      CachePMF.put(cacheKey, item);
    }
    return item;
  }
}
