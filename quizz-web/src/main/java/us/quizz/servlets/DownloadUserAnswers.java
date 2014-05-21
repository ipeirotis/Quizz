package us.quizz.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.UserAnswer;
import us.quizz.service.QuizService;
import us.quizz.service.UserAnswerService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class DownloadUserAnswers extends HttpServlet {
  final static Logger logger = Logger.getLogger("com.ipeirotis.crowdquiz");

  private QuizService quizService;
  private UserAnswerService userAnswerService;

  @Inject
  public DownloadUserAnswers(QuizService quizService,
      UserAnswerService userAnswerService) {
    this.quizService = quizService;
    this.userAnswerService = userAnswerService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String quizID = request.getParameter("quizID");
    String name = quizService.get(quizID).getName();

    List<UserAnswer> answers = userAnswerService.getUserAnswersForQuiz(quizID);
    StringBuffer sb = new StringBuffer();
    sbApp(sb, "userid");
    sbApp(sb, "questionID");
    sbApp(sb, "useranswer");
    sbApp(sb, "action");
    sbApp(sb, "ipaddress");
    sbApp(sb, "timestamp");
    sbApp(sb, "browser");
    sb.append("referer").append("\n");

    for (UserAnswer ue : answers) {
      String userid = safeStr(ue.getUserid());
      String questionID = safeStr(ue.getQuestionID());
      String useranswer = safeStr(ue.getAnswerID());
      String action = safeStr(ue.getAction());
      String ipaddress = safeStr(ue.getIpaddress());
      String timestamp = safeStr(ue.getTimestamp());
      String browser = safeStr(ue.getBrowser());
      String referer = safeStr(ue.getReferer());

      sbApp(sb, userid);
      sbApp(sb, questionID);
      sbApp(sb, useranswer);
      sbApp(sb, action);
      sbApp(sb, ipaddress);
      sbApp(sb, timestamp);
      sbApp(sb, browser);
      sb.append(referer).append("\n");
    }

    response.setContentType("text/tab-separated-values; charset=UTF-8");
    response.addHeader("cache-control", "must-revalidate");
    response.addHeader("pragma", "must-revalidate");
    response.addHeader("content-disposition", "attachment; filename=\""
        + name + "-answers.tsv\"");
    response.resetBuffer();
    response.setBufferSize(sb.length());
    response.getOutputStream().print(sb.toString());
    response.flushBuffer();
  }

  protected String safeStr(Object obj) {
    return (obj == null) ? "" : obj.toString();
  }

  protected void sbApp(StringBuffer sb, String text) {
    sb.append(text).append("\t");
  }
}
