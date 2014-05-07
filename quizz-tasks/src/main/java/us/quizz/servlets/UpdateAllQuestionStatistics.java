package us.quizz.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.service.QuizService;
import us.quizz.utils.QueueUtils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UpdateAllQuestionStatistics extends HttpServlet {
  private QuizService quizService;
  private QuizQuestionRepository quizQuestionRepository;

  @Inject
  public UpdateAllQuestionStatistics(QuizService quizService,
      QuizQuestionRepository quizQuestionRepository) {
    this.quizService = quizService;
    this.quizQuestionRepository = quizQuestionRepository;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Queue queue = QueueUtils.getQuestionStatisticsQueue();
    String quizID = req.getParameter("quizID");
    if (quizID == null) {
      List<Quiz> quizzes = quizService.list();
      for (Quiz q : quizzes) {
        queue.add(Builder
            .withUrl("/api/updateAllQuestionStatistics")
            .param("quizID", q.getQuizID())
            .method(TaskOptions.Method.GET));
      }
    } else {
      List<Question> questions = quizQuestionRepository.getQuizQuestions(quizID);
       for (Question question : questions) {
        queue.add(Builder
            .withUrl("/api/updateQuestionStatistics")
            .param("questionID", question.getID().toString())
            .method(TaskOptions.Method.POST));
      }
    }
  }
}
