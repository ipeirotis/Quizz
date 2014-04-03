package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;
import us.quizz.service.AnswerCountsStatisticsService;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateAnswerCountsStatistics extends HttpServlet {
  private QuizRepository quizRepository;
  private AnswerCountsStatisticsService answerCountsStatisticsService;

  @Inject
  public UpdateAnswerCountsStatistics(
      QuizRepository quizRepository,
      AnswerCountsStatisticsService answerCountsStatisticsService) {
    this.quizRepository = quizRepository;
    this.answerCountsStatisticsService = answerCountsStatisticsService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if ("true".equals(request.getParameter("all"))) {
      List<Quiz> list = quizRepository.getQuizzes();
      Queue queue = QueueFactory.getDefaultQueue();
      for (Quiz quiz : list) {
        queue.add(Builder
            .withUrl("/api/updateAnswerCountsStatistics")
            .param("quizID", quiz.getQuizID().toString())
            .retryOptions(RetryOptions.Builder.withTaskRetryLimit(1))
            .method(TaskOptions.Method.GET));
      }
    } else {
      answerCountsStatisticsService.updateStatistics(request.getParameter("quizID"));
    }
  }
}
