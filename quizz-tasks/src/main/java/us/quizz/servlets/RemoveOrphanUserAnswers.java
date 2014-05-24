package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.entities.UserAnswer;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizService;
import us.quizz.service.UserAnswerService;
import us.quizz.utils.QueueUtils;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Task for removing all the questions that have a quizID that is not existing
 * in the system anymore
 */
@SuppressWarnings("serial")
@Singleton
public class RemoveOrphanUserAnswers extends HttpServlet {
  private static Logger logger = Logger.getLogger(ExplorationExploitationService.class.getName());

  private QuizService quizService;
  private QuestionService questionService;
  private UserAnswerService userAnswerService;

  @Inject
  public RemoveOrphanUserAnswers(QuizService quizService,
      QuestionService questionService,
      UserAnswerService userAnswerService) {
    this.quizService = quizService;
    this.questionService = questionService;
    this.userAnswerService = userAnswerService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // The schedule parameter puts the task in the background
    // and returns immediately
    String schedule = req.getParameter("schedule");
    if (schedule != null) {
      Queue queue = QueueUtils.getConsistencyQueue();
      queue.add(Builder.withUrl("/consistency/removeOrphanUserAnswers")
                       .method(TaskOptions.Method.GET));
      logger.log(Level.INFO, "Placed request in queue...");
      return;
    }

    Set<String> quizIds = getQuizIds();
    // If we have a quizID, we will remove these questions
    String quizId = req.getParameter("quizid");
    if (quizId != null) {
      List<UserAnswer> answers = this.userAnswerService.getUserAnswersForQuiz(quizId);
      logger.log(Level.INFO, "Removing " + answers.size() + " answers...");

      this.userAnswerService.remove(answers);
    }
  }

  private Set<String> getQuizIds() {
    List<Quiz> quizzes = quizService.listAll();
    Set<String> quizIds = new TreeSet<String>();
    for (Quiz q : quizzes) {
      quizIds.add(q.getQuizID());
    }
    return quizIds;
  }
}
