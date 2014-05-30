package us.quizz.ofy;

import com.google.api.server.spi.response.CollectionResponse;

import java.util.List;
import java.util.Map;

public class OfyBaseService<T> {
  protected OfyBaseRepository<T> baseRepository;

  protected OfyBaseService(OfyBaseRepository<T> baseRepository) {
    this.baseRepository = baseRepository;
  }

  public T get(Long id) {
    return baseRepository.get(id);
  }

  public T get(String id) {
    return baseRepository.get(id);
  }

  public void asyncSave(T entity) {
    baseRepository.asyncSave(entity);
  }

  public T save(T entity) {
    return baseRepository.saveAndGet(entity);
  }

  public void saveAll(List<T> entities) {
    baseRepository.saveAll(entities);
  }

  public void delete(Long id){
    baseRepository.delete(id);
  }

  public void delete(String id) {
    baseRepository.delete(id);
  }

  public void deleteAll(List<T> entities) {
    baseRepository.delete(entities);
  }

  public List<T> listByIds(Iterable<Long> ids) {
    return baseRepository.listByIds(ids);
  }

  public List<T> listByStringIds(Iterable<String> ids) {
    return baseRepository.listByStringIds(ids);
  }

  public List<T> listAll() {
    return baseRepository.listAllByCursor();
  }

  public List<T> listAll(Map<String, Object> params) {
    return baseRepository.listAllByCursor(params);
  }

  public CollectionResponse<T> listWithCursor(String cursorString, Integer limit) {
    return baseRepository.listByCursor(cursorString, limit);
  }

  public void flush() {
    baseRepository.flush();
  }
}
