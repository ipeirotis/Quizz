package us.quizz.ofy;

import static us.quizz.ofy.OfyService.ofy;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyFilter;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfyBaseRepository<T> {
  protected Class<T> clazz;

  protected OfyBaseRepository(Class<T> clazz) {
    this.clazz = clazz;
  }

  public int count() {
    return query().count();
  }

  public int countByProperty(String propName, Object propValue) {
    return query().filter(propName, propValue).count();
  }

  public int countByProperties(Map<String, Object> params) {
    Query<T> q = query();
    if (params != null) {
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        q = q.filter(entry.getKey(), entry.getValue());
      }
    }
    return q.count();
  }

  public void asyncSave(T entity) {
    ofy().save().entity(entity);
  }

  // All the save operations here are synchronous (now()).
  public Key<T> save(T entity) {
    return ofy().save().entity(entity).now();
  }

  public T saveAndGet(T entity) {
    Key<T> key = ofy().save().entity(entity).now();
    return ofy().load().key(key).now();
  }

  public void saveAll(Collection<T> entities) {
    ofy().save().entities(entities).now();
  }

  // All the delete operations here are asynchronous (without now()).
  public void delete(T entity) {
    ofy().delete().entity(entity);
  }

  public void delete(List<T> entities) {
    ofy().delete().entities(entities);
  }

  public void delete(Key<T> key) {
    ofy().delete().key(key);
  }

  public void delete(Long id) {
    ofy().delete().type(clazz).id(id);
  }

  public void delete(String id) {
    ofy().delete().type(clazz).id(id);
  }

  public void deleteAll(List<Key<T>> keys) {
    ofy().delete().keys(keys);
  }

  public void deleteAll(Iterable<Key<T>> keys) {
    ofy().delete().keys(keys);
  }

  // All the get operations here are synchronous.
  public T get(Long id) {
    return ofy().load().type(clazz).id(id).now();
  }

  public T getNoCache(Long id) {
    return ofy().cache(false).load().type(clazz).id(id).now();
  }

  public T safeGet(Long id) {
    return ofy().load().type(clazz).id(id).safe();
  }

  public T get(String id) {
    return ofy().load().type(clazz).id(id).now();
  }

  public T getNoCache(String id) {
    return ofy().cache(false).load().type(clazz).id(id).now();
  }

  public T safeGet(String id) {
    return ofy().load().type(clazz).id(id).safe();
  }

  public T get(Key<T> key) {
    return ofy().load().key(key).now();
  }

  public Collection<T> getAll(List<Key<T>> keys) {
    return ofy().cache(true).load().keys(keys).values();
  }

  public T getByProperty(String propName, Object propValue) {
    return query().filter(propName, propValue).first().now();
  }

  public List<T> listByIds(Iterable<Long> ids) {
    Map<Long, T> map = ofy().load().type(clazz).ids(ids);
    if (map != null)
      return new ArrayList<T>(map.values());
    else
      return null;
  }

  public List<T> listByStringIds(Iterable<String> ids) {
    Map<String, T> map = ofy().load().type(clazz).ids(ids);
    if (map != null)
      return new ArrayList<T>(map.values());
    else
      return null;
  }

  public Map<Long, T> mapByIds(Collection<Long> ids) {
    return ofy().load().type(clazz).ids(ids);
  }

  public List<T> listAllByChunkForQuery(Query<T> q) {
    return q.chunk(1000).list();
  }

  public List<T> listAllByChunk() {
    return listAllByChunkForQuery(ofy().cache(true).load().type(clazz));
  }

  public List<T> listAllByChunk(String sortOrder) {
    return listAllByChunkForQuery(query().order(sortOrder));
  }

  public List<T> listAllByChunk(Map<String, Object> params) {
    return listAllByChunkForQuery(query(params));
  }

  public List<T> listAllByCursor(){
    return listAllByCursor(null);
  }

  // Note(chunhowt): Cursor doesn't work with certain filtering such as "!=" operator
  // and thus will only return the first 1000 results. More information at:
  // https://developers.google.com/appengine/docs/java/datastore/queries#Java_Limitations_of_cursors
  // Use listAllByChunkForQuery() instead.
  public List<T> listAllByCursorForQuery(Query<T> q) {
    List<T> list = new ArrayList<T>();
    Cursor cursor = null;

    while (true) {
      if (cursor != null) {
        q = q.startAt(cursor);
      }

      boolean continu = false;
      QueryResultIterator<T> iterator = q.iterator();
      cursor = iterator.getCursor();

      while (iterator.hasNext()) {
        T t = iterator.next();
        list.add(t);
        continu = true;
      }

      if (continu) {
        cursor = iterator.getCursor();
        if (cursor == null) {
          break;
        }
      } else {
        break;
      }
    }
    return list;
  }

  public List<T> listAllByCursor(Map<String, Object> params) {
    return listAllByCursorForQuery(query(params).limit(1000));
  }

  public CollectionResponse<T> listByCursor(String cursorString, Integer limit) {
    List<T> result = new ArrayList<T>();
    Query<T> query = query();

    if (cursorString != null) {
      query = query.startAt(Cursor.fromWebSafeString(cursorString));
    }

    if (limit != null) {
      query = query.limit(limit);
    }

    boolean cont = false;
    QueryResultIterator<T> iterator = query.iterator();

    while (iterator.hasNext()) {
      T item = iterator.next();
      result.add(item);
      cont = true;
    }

    if (cont) {
      Cursor cursor = iterator.getCursor();
      return CollectionResponse.<T> builder().setItems(result)
          .setNextPageToken(cursor.toWebSafeString()).build();
    } else {
      return CollectionResponse.<T> builder().setItems(result).build();
    }
  }

  public List<T> listAllByProperty(String propName, Object propValue) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(propName, propValue);
    return listAllByCursor(params);
  }

  public Query<T> query() {
    return ofy().load().type(clazz);
  }

  public Query<T> query(int limit, String sortOrder) {
    return query().order(sortOrder).limit(limit);
  }

  public Query<T> query(Map<String, Object> params) {
    Query<T> q = query();
    if (params != null) {
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        q = q.filter(entry.getKey(), entry.getValue());
      }
    }
    return q;
  }

  public void flush() {
    ObjectifyFilter.complete();
  }
}
