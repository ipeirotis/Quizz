package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.service.QuizService;
import us.quizz.utils.QueueUtils;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateCountStatistics extends HttpServlet {
  private QuizService quizService;

  @Inject
  public UpdateCountStatistics(QuizService quizService) {
    this.quizService = quizService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Queue queue = QueueUtils.getDefaultQueue();
    List<Quiz> list = quizService.listAll();

    for (Quiz quiz : list) {
      resp.getWriter().println("Updating quiz: " + quiz.getName());

      queue.add(Builder.withUrl("/api/getQuizCounts")
          .param("quizID", quiz.getQuizID()).param("cache", "no")
          .method(TaskOptions.Method.GET));
    }
  }
}
