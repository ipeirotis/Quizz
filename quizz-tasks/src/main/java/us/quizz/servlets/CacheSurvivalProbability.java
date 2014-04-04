package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;
import us.quizz.service.SurvivalProbabilityService;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class CacheSurvivalProbability extends HttpServlet {
  private SurvivalProbabilityService survivalProbabilityService;
  private QuizRepository quizRepository;

  @Inject
  public CacheSurvivalProbability(
      SurvivalProbabilityService survivalProbabilityService, QuizRepository quizRepository) {
    this.survivalProbabilityService = survivalProbabilityService;
    this.quizRepository = quizRepository;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    // If we ever need to cache synchronously
    // In general, this risks generating a DeadlineException error
    String executeNow = req.getParameter("now");

    // The quizId value can be:
    // null: compute statistics across all quizzes
    // quizid: compute statistics for a given quiz
    // all: compute statistics for all individual quizzes
    String quizId = req.getParameter("quizId");

    if ("true".equals(executeNow)) {
      survivalProbabilityService.cacheValuesInMemcache(quizId);
      survivalProbabilityService.saveValuesInDatastore(quizId);
      return;
    }

    if ("all".equals(quizId)) {
      List<Quiz> quizzes = quizRepository.getQuizzes();
      for (Quiz q : quizzes) {
        executeInQueue(q.getQuizID());
      }
      executeInQueue(null); // to also cache the overall statistics
    } else {
      executeInQueue(quizId);
    }
  }

  private void executeInQueue(String quizId) {
    Queue queue = QueueFactory.getQueue("survival");
    queue.add(Builder.withUrl("/api/cacheSurvivalProbability")
        .method(TaskOptions.Method.GET)
        .param("quizId", quizId)
        .param("now", "true"));
  }
}
