package us.quizz.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.service.UserQuizStatisticsService;
import us.quizz.utils.ServletUtils;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Takes as input a userid and a quiz, updates the user scores for the quiz, and
 * then computes the rank of the user within the set of all other users.
 * Finally, it puts the QuizPerformance object in the memcache for quick
 * retrieval.
 */
@SuppressWarnings("serial")
@Singleton
public class UpdateUserQuizStatistics extends HttpServlet {
  private UserQuizStatisticsService userQuizStatisticsService;

  @Inject
  public UpdateUserQuizStatistics(UserQuizStatisticsService userQuizStatisticsService) {
    this.userQuizStatisticsService = userQuizStatisticsService;
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    ServletUtils.ensureParameters(req, "quizID", "userid");
    String quiz = req.getParameter("quizID");
    String userid = req.getParameter("userid");

    userQuizStatisticsService.updateStatistics(quiz, userid);
  }
}
