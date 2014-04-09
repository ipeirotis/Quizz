package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.service.ExplorationExploitationService;

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
public class RemoveOrphanQuestions extends HttpServlet {
  private static Logger logger = Logger.getLogger(ExplorationExploitationService.class.getName());
  private QuizRepository quizRepository;
  private QuizQuestionRepository quizQuestionRepository;
  private AnswersRepository answersRepository;

  @Inject
  public RemoveOrphanQuestions(QuizRepository quizRepository,
      QuizQuestionRepository quizQuestionRepository, AnswersRepository answersRepository) {
    this.quizRepository = quizRepository;
    this.quizQuestionRepository = quizQuestionRepository;
    this.answersRepository = answersRepository;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // The schedule parameter puts the task in the background
    // and returns immediately
    String schedule = req.getParameter("schedule");
    if (schedule != null) {
      Queue queue = QueueFactory.getQueue("survival");
      queue.add(Builder.withUrl("/consistency/removeOrphanQuestions")
           .method(TaskOptions.Method.GET));
      logger.log(Level.INFO, "Placed request in queue...");
      return;
    }

    // If we have a quizID, we will remove these questions
    String quizId = req.getParameter("quizid");
    if (quizId != null) {
      List<Question> questions = this.quizQuestionRepository.getQuizQuestions(quizId);
      logger.log(Level.INFO, "Removing " + questions.size() + " questions...");
      this.quizQuestionRepository.removeAll(questions);
      //List<Answer> quesansawerstions = this.answersRepository.
      return;
    }
  

    List<Quiz> quizzes = this.quizRepository.getQuizzes();
    Set<String> quizIds = new TreeSet<String>();
    for (Quiz q : quizzes) {
      quizIds.add(q.getQuizID());
    }

    List<Question> questions = this.quizQuestionRepository.getQuizQuestions();
    logger.log(Level.INFO, "Fetched " + questions.size() + " questions...");
    for (Question question : questions) {
      String qquiz = question.getQuizID();
      if (quizIds.contains(qquiz)) {
        continue;
      }

      // If we see a new quiz, we add in the queue to be processed for
      // deletion, and add the quiz in the "existing" ones to avoid
      // creating duplicate entries in the Tasks Queue
      quizIds.add(qquiz);
      Queue queue = QueueFactory.getQueue("quizquestions");
      queue.add(Builder.withUrl("/consistency/removeOrphanQuestions")
          .param("quizid", qquiz)
          .method(TaskOptions.Method.GET));
      logger.log(Level.INFO, "Placed request in queue to remove quiz " + qquiz);
    }
    // Check all answers that have a parent question that exists in the system
    // Delete GoldAnswer, SilverAnswer, QuizQuestion
  }
}