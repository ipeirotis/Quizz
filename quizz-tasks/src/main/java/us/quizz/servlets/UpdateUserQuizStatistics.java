package us.quizz.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.service.QuizPerformanceService;
import us.quizz.utils.ServletUtils;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Takes as input a userid and a quizID, updates the user scores for the quiz, and
// then computes the rank of the user within the set of all other users.
// Finally, it puts the QuizPerformance object in the memcache for quick retrieval.
@SuppressWarnings("serial")
@Singleton
public class UpdateUserQuizStatistics extends HttpServlet {
  protected static final String QUIZ_ID_PARAM = "quizID";
  protected static final String USER_ID_PARAM = "userID";

  private QuizPerformanceService quizPerformanceService;

  @Inject
  public UpdateUserQuizStatistics(QuizPerformanceService quizPerformanceService) {
    this.quizPerformanceService = quizPerformanceService;
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ServletUtils.ensureParameters(req, QUIZ_ID_PARAM, USER_ID_PARAM);
    String quizID = req.getParameter(QUIZ_ID_PARAM);
    String userid = req.getParameter(USER_ID_PARAM);
    quizPerformanceService.updateStatistics(quizID, userid);
  }
}
