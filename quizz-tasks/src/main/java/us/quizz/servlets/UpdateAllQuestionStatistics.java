package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateAllQuestionStatistics extends HttpServlet {
  private QuizRepository quizRepository;
  private QuizQuestionRepository quizQuestionRepository;

  @Inject
  public UpdateAllQuestionStatistics(QuizRepository quizRepository,
      QuizQuestionRepository quizQuestionRepository) {
    this.quizRepository = quizRepository;
    this.quizQuestionRepository = quizQuestionRepository;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Queue queue = QueueFactory.getQueue("updateUserStatistics");
    String quizID = req.getParameter("quizID");
    if (quizID==null) {
      List<Quiz> quizzes = quizRepository.getQuizzes();
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
