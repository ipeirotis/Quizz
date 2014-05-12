package us.quizz.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Quiz;
import us.quizz.service.QuizService;
import us.quizz.service.UserAnswerService;
import us.quizz.utils.QueueUtils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UpdateAllUserStatistics extends HttpServlet {
  private QuizService quizService;
  private UserAnswerService userAnswerService;

  @Inject
  public UpdateAllUserStatistics(QuizService quizService,
      UserAnswerService userAnswerService) {
    this.quizService = quizService;
    this.userAnswerService = userAnswerService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    List<Quiz> quizzes = quizService.list();
    for (Quiz q : quizzes) {
      Set<String> userids = userAnswerService.getUserIDs(q.getQuizID());
      Queue queue = QueueUtils.getUserStatisticsQueue();
      for (String userid : userids) {
        queue.add(Builder
            .withUrl("/api/updateUserQuizStatistics")
            .param("userid", userid).param("quizID", q.getQuizID())
            .method(TaskOptions.Method.POST));
      }
    }
  }
}
