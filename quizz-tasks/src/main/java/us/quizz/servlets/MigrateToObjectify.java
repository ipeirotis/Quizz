package us.quizz.servlets;

import static us.quizz.ofy.OfyService.ofy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.DomainStats;
import us.quizz.utils.QueueUtils;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class MigrateToObjectify extends HttpServlet {
  
  private static final Logger logger = Logger.getLogger(MigrateToObjectify.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    String kind = req.getParameter("kind");
    String now = req.getParameter("now");
    
    if (kind == null) {
      resp.sendError(400,"'kind' param is required");
    } else {
      if ("true".equals(now)) {
        if("DomainStats".equals(kind)){
          updateDomainStats();
        }
      }else{
        sched(kind);
      }
    }
  }

  private void updateDomainStats(){
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService(); 
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000);
    Cursor cursor = null;
    long counter = 0L;
    
    while (true) {
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }
      
      Query q = new Query("DomainStats").setKeysOnly();
      PreparedQuery pq = ds.prepare(q);
      QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
      cursor = entities.getCursor();
     
      if (cursor == null || entities.size() == 0) {
        break;
      }

      for(Entity e : entities){
        e.removeProperty("domain");
        counter++;
      }
      
      ds.put(entities);
    }
    
    logger.log(Level.INFO, counter + " DomainStats entities updated successfully");
    
    //test reading with objectify
    DomainStats domainStats = ofy().load().type(DomainStats.class).limit(1).first().now();
    logger.log(Level.INFO, "Test reading DomainStats: " + domainStats.getDomain());
  }

  private void sched(String kind){
    Queue queue = QueueUtils.getConsistencyQueue();
    queue.add(Builder
        .withUrl("/ofy")
        .param("kind", kind)
        .param("now", "true")
        .retryOptions(RetryOptions.Builder.withTaskRetryLimit(0))
        .method(TaskOptions.Method.GET));
  } 
} 