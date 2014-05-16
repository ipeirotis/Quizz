package us.quizz.ofy;

import static us.quizz.ofy.OfyService.ofy;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OfyBaseRepository<T> {
  protected Class<T> clazz;

  protected OfyBaseRepository(Class<T> clazz) {
    this.clazz = clazz;
  }

  public int count() {
    return ofy().load().type(clazz).count();
  }

  public int countByProperty(String propName, Object propValue) {
    return ofy().load().type(clazz).filter(propName, propValue).count();
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
    return ofy().load().type(clazz).filter(propName, propValue).first().now();
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

  public List<T> list() {
    return ofy().cache(true).load().type(clazz).chunk(1000).list();
  }

  public List<T> list(String sortOrder) {
    return ofy().load().type(clazz).order(sortOrder).list();
  }

  public List<T> listAll(){
    return listAll(null);
  }

  public List<T> listAll(Map<String, Object> params){
    List<T> list = new ArrayList<T>();
    Query<T> q = ofy().load().type(clazz).limit(1000);
    Cursor cursor = null;
    
    if (params != null) {
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        q = q.filter(entry.getKey(), entry.getValue());
      }
    }

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

  public CollectionResponse<T> listWithCursor(String cursorString, Integer limit) {
    List<T> result = new ArrayList<T>();
    Query<T> query = ofy().load().type(clazz);

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

  public List<T> listByProperty(String propName, Object propValue) {
    return ofy().load().type(clazz).filter(propName, propValue).list();
  }

  public Query<T> query(int limit, String sortOrder) {
    return ofy().load().type(clazz).order(sortOrder).limit(limit);
  }

  public Query<T> query() {
    return ofy().load().type(clazz);
  }

}
