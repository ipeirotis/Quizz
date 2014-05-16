package us.quizz.servlets;

import static us.quizz.ofy.OfyService.ofy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Answer;
import us.quizz.entities.Badge;
import us.quizz.entities.DomainStats;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.Treatment;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.QuestionRepository;
import us.quizz.utils.QueueUtils;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class MigrateToObjectify extends HttpServlet {

  private static final Logger logger = Logger.getLogger(MigrateToObjectify.class.getName());

  private AnswersRepository answerRepository;
  private QuestionRepository questionRepository;

  @Inject
  public MigrateToObjectify(AnswersRepository answerRepository, QuestionRepository questionRepository){
    this.answerRepository = answerRepository;
    this.questionRepository = questionRepository;
  }

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
        } else if("Treatment".equals(kind)){
          updateTreatments();
        } else if("Quiz".equals(kind)){
          updateQuizzes();
        } else if("Badge".equals(kind)){
          updateBadges();
        } else if("User".equals(kind)){
          updateUsers();
        } else if("Question".equals(kind)){
          updateQuestions();
        }
      }else{
        sched(kind);
      }
    }
  }
  
  private void updateQuestions(){
    //remove 'answers' property from all questions
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService(); 
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000);
    Cursor cursor = null;
    long counter = 0L;
    
    while (true) {
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }
      
      Query q = new Query("Question");
      PreparedQuery pq = ds.prepare(q);
      QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
      cursor = entities.getCursor();
     
      if (cursor == null || entities.size() == 0) {
        break;
      }

      for(Entity e : entities){
        e.removeProperty("answers");
        counter++;
      }
      
      ds.put(entities);
    }
    //add answers to questions
    List<Answer> answers = answerRepository.listAll();
    List<Question> questions = questionRepository.listAll();
    
    Map<Long, ArrayList<Answer>> map = new HashMap<Long, ArrayList<Answer>>();
    for(Answer answer : answers){
      if(map.containsKey(answer.getQuestionID())){
        map.get(answer.getQuestionID()).add(answer);
      } else {
        map.put(answer.getQuestionID(), new ArrayList<Answer>(Arrays.asList(answer)));
      }
    }
    
    for(Question question : questions){
      question.setAnswers(map.get(question.getId()));
    }
    
    for (int i = 0; i < questions.size(); i += 1000) {
      List<Question> sublist = questions.subList(i, Math.min(i + 1000, questions.size()));
      ofy().save().entities(sublist);
    }
    
    logger.log(Level.INFO, counter + " Question entities updated successfully");
    
    //test reading with objectify
    Question question = ofy().load().type(Question.class).limit(1).first().now();
    logger.log(Level.INFO, "Test reading Questions: " + question.getQuizID());
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
      
      Query q = new Query("DomainStats");
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

  private void updateTreatments(){
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000);
    Cursor cursor = null;
    long counter = 0L;
    List<Entity> newEntities = new ArrayList<Entity>();

    while (true) {
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }

      Query q = new Query("Treatment");
      PreparedQuery pq = ds.prepare(q);
      QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
      List<Key> toDeleteKeys = new ArrayList<Key>();
      cursor = entities.getCursor();

      if (cursor == null || entities.size() == 0) {
        break;
      }

      for(Entity e : entities){
        Entity newEntity = new Entity("Treatment", (String)e.getProperty("name"));
        newEntity.setPropertiesFrom(e);
        newEntity.removeProperty("name");
        newEntities.add(newEntity);
        toDeleteKeys.add(e.getKey());
        counter++;
      }
      ds.delete(toDeleteKeys);
    }

    ds.put(newEntities);

    logger.log(Level.INFO, counter + " Treatment entities updated successfully");

    //test reading with objectify
    Treatment treatment = ofy().load().type(Treatment.class).limit(1).first().now();
    logger.log(Level.INFO, "Test reading Treatment: " + treatment.getProbability());
  }
  
  private void updateBadges(){
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000);
    Cursor cursor = null;
    long counter = 0L;
    List<Entity> newEntities = new ArrayList<Entity>();

    while (true) {
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }

      Query q = new Query("Badge");
      PreparedQuery pq = ds.prepare(q);
      QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
      List<Key> toDeleteKeys = new ArrayList<Key>();
      cursor = entities.getCursor();

      if (cursor == null || entities.size() == 0) {
        break;
      }

      for(Entity e : entities){
        Entity newEntity = new Entity("Badge", (String)e.getProperty("badgename"));
        newEntity.setPropertiesFrom(e);
        newEntity.removeProperty("badgename");
        newEntities.add(newEntity);
        toDeleteKeys.add(e.getKey());
        counter++;
      }
      ds.delete(toDeleteKeys);
    }

    ds.put(newEntities);

    logger.log(Level.INFO, counter + " Badge entities updated successfully");

    //test reading with objectify
    Badge badge = ofy().load().type(Badge.class).limit(1).first().now();
    logger.log(Level.INFO, "Test reading Badge: " + badge.getBadgename());
  }

  private void updateUsers(){
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000);
    Cursor cursor = null;
    long counter = 0L;

    while (true) {
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }

      Query q = new Query("User");
      PreparedQuery pq = ds.prepare(q);
      QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
      cursor = entities.getCursor();

      if (cursor == null || entities.size() == 0) {
        break;
      }

      for(Entity e : entities){
        Object key = e.getProperty("experiment_key_OID");
        if(key != null){
          e.setProperty("experimentId", ((Key)key).getId());
          e.removeProperty("experiment_key_OID");
        }
        counter++;
      }
      ds.put(entities);
    }

    logger.log(Level.INFO, counter + " User entities updated successfully");
  }

  private void updateQuizzes(){
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000);
    Cursor cursor = null;
    long counter = 0L;
    List<Entity> newEntities = new ArrayList<Entity>();

    while (true) {
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }

      Query q = new Query("Quiz");
      PreparedQuery pq = ds.prepare(q);
      QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
      List<Key> toDeleteKeys = new ArrayList<Key>();
      cursor = entities.getCursor();

      if (cursor == null || entities.size() == 0) {
        break;
      }

      for(Entity e : entities){
        Entity newEntity = new Entity("Quiz", (String)e.getProperty("quizID"));
        newEntity.setPropertiesFrom(e);
        newEntity.removeProperty("quizID");
        newEntities.add(newEntity);
        toDeleteKeys.add(e.getKey());
        counter++;
      }
      ds.delete(toDeleteKeys);
    }

    ds.put(newEntities);

    logger.log(Level.INFO, counter + " Quiz entities updated successfully");

    //test reading with objectify
    Quiz quiz = ofy().load().type(Quiz.class).limit(1).first().now();
    logger.log(Level.INFO, "Test reading Quiz: " + quiz.getName());
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