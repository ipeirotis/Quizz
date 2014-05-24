package us.quizz.servlets;

import static us.quizz.ofy.OfyService.ofy;

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

import us.quizz.entities.Answer;
import us.quizz.entities.BadgeAssignment;
import us.quizz.entities.ExplorationExploitationResult;
import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.SurvivalProbabilityResult;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.QuestionRepository;
import us.quizz.utils.QueueUtils;

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
    String cursor = req.getParameter("cursor");

    if (kind == null) {
      resp.sendError(400,"'kind' param is required");
    } else {
      if ("true".equals(now)) {
        if ("DomainStats".equals(kind)) {
          updateDomainStats(cursor);
        } else if ("Treatment".equals(kind)) {
          updateTreatments(cursor);
        } else if ("Quiz".equals(kind)) {
          updateQuizzes(cursor);
        } else if ("Badge".equals(kind)) {
          updateBadges(cursor);
        } else if ("User".equals(kind)) {
          updateUsers(cursor);
        } else if ("BadgeAssignment".equals(kind)) {
          updateBadgeAssignments(cursor);
        } else if ("BrowserStats".equals(kind)) {
          updateBrowserStats(cursor);
        } else if ("Experiment".equals(kind)) {
          updateExperiments(cursor);
        } else if ("ExplorationExploitationResult".equals(kind)) {
          updateExplorationExploitationResults(cursor);
        } else if ("QuizPerformance".equals(kind)) {
          updateQuizPerformances(cursor);
        } else if ("SurvivalProbabilityResult".equals(kind)) {
          updateSurvivalProbabilityResult(cursor);
        } else if ("UserAnswer".equals(kind)) {
          updateUserAnswers(cursor);
        } else if ("UserAnswerFeedback".equals(kind)) {
          updateUserAnswerFeedbacks(cursor);
        } else if ("UserReferal".equals(kind)) {
          updateUserReferal(cursor);
        } else if("Question".equals(kind)){
          updateQuestions(cursor);
        } else if("FixQuestion".equals(kind)) {
          fixQuestions();
        }
      }else{
        sched(kind, cursor);
      }
    }
  }

  private void updateQuestions(String cursorString){
    String kind = "Question";
    //remove 'answers' property from all questions
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;

    for(Entity e : entities){
      e.removeProperty("answers");
      counter++;
    }
    ds.put(entities);
    logger.log(Level.INFO, counter + " Question entities updated successfully");
    if (counter < 1000L) {
      fixQuestions();
      return;
    }
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
      return;
    }
  }

  private void fixQuestions() {
    //add answers to questions
    List<Answer> answers = answerRepository.listAllByCursor();
    List<Question> questions = questionRepository.listAllByCursor();

    Map<Long, ArrayList<Answer>> map = new HashMap<Long, ArrayList<Answer>>();
    for (Answer answer : answers) {
      if (map.containsKey(answer.getQuestionID())) {
        map.get(answer.getQuestionID()).add(answer);
      } else {
        map.put(answer.getQuestionID(), new ArrayList<Answer>(Arrays.asList(answer)));
      }
    }

    for (Question question : questions){
      question.setAnswers(map.get(question.getId()));
    }

    for (int i = 0; i < questions.size(); i += 1000) {
      List<Question> sublist = questions.subList(i, Math.min(i + 1000, questions.size()));
      ofy().save().entities(sublist);
    }

    //test reading with objectify
    Question question = ofy().load().type(Question.class).limit(1).first().now();
    logger.log(Level.INFO, "Test reading Questions: " + question.getQuizID());
  }

  private QueryResultList<Entity> executeQuery(DatastoreService ds, String kind, String cursorString) {
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000);
    Cursor cursor = null;
    if (cursorString != null) {
      cursor = Cursor.fromWebSafeString(cursorString);
    }
    if (cursor != null) {
      fetchOptions.startCursor(cursor);
    }
    Query q = new Query(kind);
    PreparedQuery pq = ds.prepare(q);
    QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
    return entities;
  }

  private void updateUserReferal(String cursorString) {
    String kind = "UserReferal";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for (Entity e : entities) {
      Entity newEntity = new Entity(kind);
      newEntity.setPropertiesFrom(e);
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " UserReferal entities updated successfully");
  }

  private void updateUserAnswerFeedbacks(String cursorString) {
    String kind = "UserAnswerFeedback";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }

    Long counter = 0L;
    for (Entity e : entities) {
      Entity newEntity = new Entity(kind,
          UserAnswerFeedback.generateId((Long)e.getProperty("questionID"),
                                        (String)e.getProperty("userid")));
      newEntity.setPropertiesFrom(e);
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }

    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }

    logger.log(Level.INFO, counter + " UserAnswerFeedback entities updated successfully");
  }

  private void updateUserAnswers(String cursorString) {
    String kind = "UserAnswer";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for (Entity e : entities) {
      Entity newEntity = new Entity(kind);
      newEntity.setPropertiesFrom(e);
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " UserAnswer entities updated successfully");
  }

  private void updateSurvivalProbabilityResult(String cursorString) {
    String kind = "SurvivalProbabilityResult";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for (Entity e : entities) {
      Entity newEntity = new Entity(kind,
          SurvivalProbabilityResult.generateId(
              (int)(long)(Long)e.getProperty("correctFrom"),
              (int)(long)(Long)e.getProperty("incorrectFrom"),
              (int)(long)(Long)e.getProperty("exploitFrom"),
              (int)(long)(Long)e.getProperty("correctTo"),
              (int)(long)(Long)e.getProperty("incorrectTo"),
              (int)(long)(Long)e.getProperty("exploitTo")));
      newEntity.setPropertiesFrom(e);
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " SurvivalProbabilityResult entities updated successfully");
  }

  private void updateQuizPerformances(String cursorString) {
    String kind = "QuizPerformance";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for (Entity e : entities) {
      Entity newEntity = new Entity(kind,
          QuizPerformance.generateId((String)e.getProperty("quiz"),
                                     (String)e.getProperty("userid")));
      newEntity.setPropertiesFrom(e);
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " QuizPerformance entities updated successfully");
  }

  private void updateExplorationExploitationResults(String cursorString) {
    String kind = "ExplorationExploitationResult";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for (Entity e : entities) {
      Entity newEntity = new Entity(kind,
          ExplorationExploitationResult.generateId(
              (int)(long)(Long)e.getProperty("a"),
              (int)(long)(Long)e.getProperty("b"),
              (int)(long)(Long)e.getProperty("c")));
      newEntity.setPropertiesFrom(e);
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " ExplorationExploitationResult entities updated successfully");
  }

  private void updateExperiments(String cursorString) {
    String kind = "Experiment";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for (Entity e : entities) {
      Entity newEntity = new Entity(kind);
      newEntity.setPropertiesFrom(e);
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " Experiment entities updated successfully");
  }

  private void updateBadgeAssignments(String cursorString) {
    String kind = "BadgeAssignment";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for (Entity e : entities) {
      Entity newEntity = new Entity(kind,
          BadgeAssignment.generateId((String)e.getProperty("userid"),
                                     (String)e.getProperty("badgename")));
      newEntity.setPropertiesFrom(e);
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " BadgeAssignment entities updated successfully");
  }

  private void updateBrowserStats(String cursorString) {
    String kind = "BrowserStats";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for (Entity e : entities) {
      Entity newEntity = new Entity(kind, e.getProperty("browser").toString());
      newEntity.setPropertiesFrom(e);
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }

    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " BrowserStats entities updated successfully");
  }

  private void updateDomainStats(String cursorString){
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService(); 
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1000);
    Cursor cursor = null;
    if (cursorString != null) {
      cursor = Cursor.fromWebSafeString(cursorString);
    }
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
  }

  private void updateTreatments(String cursorString){
    String kind = "Treatment";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for(Entity e : entities){
      Entity newEntity = new Entity(kind, (String)e.getProperty("name"));
      newEntity.setPropertiesFrom(e);
      newEntity.removeProperty("name");
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " Treatment entities updated successfully");
  }
  
  private void updateBadges(String cursorString){
    String kind = "Badge";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }

    Long counter = 0L;
    for(Entity e : entities){
      Entity newEntity = new Entity(kind, (String)e.getProperty("badgename"));
      newEntity.setPropertiesFrom(e);
      newEntity.removeProperty("badgename");
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " Badge entities updated successfully");
  }

  private void updateUsers(String cursorString){
    String kind = "User";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }

    Long counter = 0L;
    for (Entity e : entities) {
      Object key = e.getProperty("experiment_key_OID");
      if (key != null) {
        e.setProperty("experimentId", ((Key)key).getId());
        e.removeProperty("experiment_key_OID");
      }
      Entity newEntity = new Entity(kind, (String)e.getProperty("userid"));
      newEntity.setPropertiesFrom(e);
      newEntity.removeProperty("userid");
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " User entities updated successfully");
  }

  private void updateQuizzes(String cursorString){
    String kind = "Quiz";
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    QueryResultList<Entity> entities = executeQuery(ds, kind, cursorString);
    List<Key> toDeleteKeys = new ArrayList<Key>();
    List<Entity> newEntities = new ArrayList<Entity>();
    Cursor cursor = entities.getCursor();
    if (cursor == null || entities.size() == 0) {
      return;
    }
    Long counter = 0L;
    for(Entity e : entities){
      Entity newEntity = new Entity(kind, (String)e.getProperty("quizID"));
      newEntity.setPropertiesFrom(e);
      newEntity.removeProperty("quizID");
      newEntities.add(newEntity);
      toDeleteKeys.add(e.getKey());
      counter++;
    }
    ds.delete(toDeleteKeys);
    ds.put(newEntities);
    if (cursor != null) {
      sched(kind, cursor.toWebSafeString());
    }
    logger.log(Level.INFO, counter + " Quiz entities updated successfully");
  }

  private void sched(String kind, String cursor){
    Queue queue = QueueUtils.getConsistencyQueue();
    queue.add(Builder
        .withUrl("/ofy")
        .param("kind", kind)
        .param("now", "true")
        .param("cursor", cursor)
        .retryOptions(RetryOptions.Builder.withTaskRetryLimit(0))
        .method(TaskOptions.Method.GET));
  } 
} 
