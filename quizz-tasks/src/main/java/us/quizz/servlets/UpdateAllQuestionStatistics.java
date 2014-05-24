package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizService;
import us.quizz.utils.QueueUtils;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateAllQuestionStatistics extends HttpServlet {
  private QuizService quizService;
  private QuestionService questionService;

  @Inject
  public UpdateAllQuestionStatistics(QuizService quizService,
      QuestionService questionService) {
    this.quizService = quizService;
    this.questionService = questionService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Queue queue = QueueUtils.getQuestionStatisticsQueue();
    String quizID = req.getParameter("quizID");
    if (quizID == null) {
      List<Quiz> quizzes = quizService.listAll();
      for (Quiz q : quizzes) {
        queue.add(Builder
            .withUrl("/api/updateAllQuestionStatistics")
            .param("quizID", q.getQuizID())
            .method(TaskOptions.Method.GET));
      }
    } else {
      List<Question> questions = questionService.getQuizQuestions(quizID);
       for (Question question : questions) {
        queue.add(Builder
            .withUrl("/api/updateQuestionStatistics")
            .param("questionID", String.valueOf(question.getId()))
            .method(TaskOptions.Method.GET));
      }
    }
  }
}
