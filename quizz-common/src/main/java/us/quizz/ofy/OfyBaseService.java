package us.quizz.ofy;

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

  public List<T> listAll() {
    return baseRepository.listAllByCursor();
  }

  public List<T> listAll(Map<String, Object> params) {
    return baseRepository.listAllByCursor(params);
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

  public void deleteAll(List<T> entities) {
    baseRepository.delete(entities);
  }

  public List<T> listByIds(Iterable<Long> ids) {
    return baseRepository.listByIds(ids);
  }
}
