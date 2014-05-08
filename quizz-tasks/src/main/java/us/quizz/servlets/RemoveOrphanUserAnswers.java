package us.quizz.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Quiz;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.QuizService;
import us.quizz.utils.QueueUtils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Task for removing all the questions that have a quizID that is not existing
 * in the system anymore
 */
@SuppressWarnings("serial")
@Singleton
public class RemoveOrphanUserAnswers extends HttpServlet {
  private static Logger logger = Logger.getLogger(ExplorationExploitationService.class.getName());

  private QuizService quizService;
  private QuizQuestionRepository quizQuestionRepository;
  private UserAnswerRepository userAnswerRepository;

  @Inject
  public RemoveOrphanUserAnswers(QuizService quizService,
      QuizQuestionRepository quizQuestionRepository,
      UserAnswerRepository userAnswerRepository) {
    this.quizService = quizService;
    this.quizQuestionRepository = quizQuestionRepository;
    this.userAnswerRepository = userAnswerRepository;
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
      List<UserAnswer> answers = this.userAnswerRepository.getUserAnswers(quizId);
      logger.log(Level.INFO, "Removing " + answers.size() + " answers...");

      this.userAnswerRepository.removeAll(answers);
    }
  }

  private Set<String> getQuizIds() {
    List<Quiz> quizzes = quizService.list();
    Set<String> quizIds = new TreeSet<String>();
    for (Quiz q : quizzes) {
      quizIds.add(q.getQuizID());
    }
    return quizIds;
  }
}
