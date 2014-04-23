package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.utils.QueueUtils;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateAllUserStatistics extends HttpServlet {
  private QuizRepository quizRepository;
  private UserAnswerRepository userAnswerRepository;

  @Inject
  public UpdateAllUserStatistics(QuizRepository quizRepository,
      UserAnswerRepository userAnswerRepository) {
    this.quizRepository = quizRepository;
    this.userAnswerRepository = userAnswerRepository;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    List<Quiz> quizzes = quizRepository.getQuizzes();
    for (Quiz q : quizzes) {
      Set<String> userids = userAnswerRepository.getUserIDs(q.getQuizID());
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
