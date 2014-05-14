// This class fixes the question's answers field to restore the link between
// each question to the list of answers.
package us.quizz.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.service.QuizService;
import us.quizz.utils.PMF;
import us.quizz.utils.QueueUtils;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class FixQuestions extends HttpServlet {
  
  private static final Logger logger = Logger.getLogger(FixQuestions.class.getName());
  
  private QuizService quizService;

  @Inject
  public FixQuestions(QuizService quizService) {
    this.quizService = quizService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    
    String quizID = req.getParameter("quizID");
    if(quizID != null){
      fixQuestions(quizID);
      return;
    }
    
    if (quizID == null) {
      List<Quiz> quizzes = quizService.list();
      for (Quiz q : quizzes) {
        sched(q.getQuizID());
      }
    } 
  }
  
  private void fixQuestions(String quizID){
    PersistenceManager mgr = null;
    Cursor cursor = null;
    Map<Long, ArrayList<Answer>> answers = loadAnswers(quizID);
    
    try {
      List<Question> result = new ArrayList<Question>();
      mgr = PMF.getPM();
      while (true) {
        Query query = mgr.newQuery(Question.class);
        query.setFilter("quizID == quizIDParam");
        query.declareParameters("String quizIDParam");
        if (cursor != null) {
          HashMap<String, Object> extensionMap = new HashMap<String, Object>();
          extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
          query.setExtensions(extensionMap);
        }
  
        query.setRange(0, 1000);
        List<Question> questions = (List<Question>) query.execute(quizID);
        cursor = JDOCursorHelper.getCursor(questions);
        
        if (questions.size() == 0)
          break;
        
        for(Question question : questions){
          question.setAnswers(answers.get(question.getID()));
        }
  
        result.addAll(questions);
      }
      logger.info("fixed questions: " + result.size());
      
    } finally {
      mgr.close();
    }
  }
  
  private Map<Long, ArrayList<Answer>> loadAnswers(String quizID){
    PersistenceManager mgr = null;
    Cursor cursor = null;
    
    try {
      Map<Long, ArrayList<Answer>> map = new HashMap<Long, ArrayList<Answer>>();
      mgr = PMF.getPM();
      while (true) {
        Query query = mgr.newQuery(Answer.class);
        query.setFilter("quizID == quizIDParam");
        query.declareParameters("String quizIDParam");
        if (cursor != null) {
          HashMap<String, Object> extensionMap = new HashMap<String, Object>();
          extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
          query.setExtensions(extensionMap);
        }
  
        query.setRange(0, 1000);
        List<Answer> answers = (List<Answer>) query.execute(quizID);
        cursor = JDOCursorHelper.getCursor(answers);
        
        if (answers.size() == 0)
          break;
  
        for(Answer answer : answers){
          if(map.containsKey(answer.getQuestionID())){
            map.get(answer.getQuestionID()).add(answer);
          } else {
            map.put(answer.getQuestionID(), new ArrayList<Answer>(Arrays.asList(answer)));
          }
        }
      }
      return map;
      
    } finally {
      mgr.close();
    }
  }
  
  private void sched(String quizID){
    Queue queue = QueueUtils.getConsistencyQueue();
    queue.add(Builder
        .withUrl("/fixquestions")
        .param("quizID", quizID)
        .retryOptions(RetryOptions.Builder.withTaskRetryLimit(0))
        .method(TaskOptions.Method.GET));
  } 

} 
