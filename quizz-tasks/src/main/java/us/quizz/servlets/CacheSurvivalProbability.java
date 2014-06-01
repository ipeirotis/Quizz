package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;
import us.quizz.service.QuizService;
import us.quizz.service.SurvivalProbabilityService;
import us.quizz.utils.QueueUtils;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class CacheSurvivalProbability extends HttpServlet {
  private static final String QUIZ_ID_PARAM = "quizId";
  private static final String EXECUTE_PARAM = "now";
  private static final String QUIZ_ID_ALL = "all";
  private static final String EXECUTE_NOW = "true";

  private SurvivalProbabilityService survivalProbabilityService;
  private QuizService quizService;

  @Inject
  public CacheSurvivalProbability(
      SurvivalProbabilityService survivalProbabilityService, QuizService quizService) {
    this.survivalProbabilityService = survivalProbabilityService;
    this.quizService = quizService;
  }

  // Extracts the params from the request and schedule tasks correspondingly.
  // If QUIZ_ID_PARAM = QUIZ_ID_ALL, schedules new tasks in survival queue to compute the survival
  // probability for all the quizId in the datastore, along with a task to compute global survival
  // probability.
  // Else, schedules a task to compute the survival probability for the corresponding quizId.
  // If EXECUTE_PARAM = EXECUTE_NOW, computes the survival probability in this job, else adds it
  // into the queue.
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    // In general, this risks generating a DeadlineException error because a task started from url
    // only has max 1 min execution time unless it is started from task queue (which will have
    // 10 mins deadline).
    String executeNow = req.getParameter(EXECUTE_PARAM);

    // The quizId value can be:
    // null: compute statistics across all quizzes
    // quizid: compute statistics for a given quiz
    // QUIZ_ID_ALL: compute statistics for all individual quizzes
    String quizId = req.getParameter(QUIZ_ID_PARAM);

    if (EXECUTE_NOW.equals(executeNow) && !QUIZ_ID_ALL.equals(quizId)) {
      survivalProbabilityService.cacheValuesInMemcache(quizId);
      survivalProbabilityService.saveValuesInDatastore(quizId);
      return;
    }

    if (QUIZ_ID_ALL.equals(quizId)) {
      List<Quiz> quizzes = quizService.listAll();
      for (Quiz q : quizzes) {
        executeInQueue(q.getQuizID());
      }
      executeInQueue(null);  // Also cache the overall statistics
    } else {
      executeInQueue(quizId);
    }
  }

  // Schedules a new task in the survival queue to compute the survival probability for the quizId
  // given.
  private void executeInQueue(String quizId) {
    Queue queue = QueueUtils.getSurvivalQueue();
    TaskOptions taskOptions = Builder.withUrl("/api/cacheSurvivalProbability")
        .method(TaskOptions.Method.GET)
        .param(EXECUTE_PARAM, EXECUTE_NOW);
    if (quizId != null) {
      taskOptions.param(QUIZ_ID_PARAM, quizId);
    }
    queue.add(taskOptions);
  }
}
