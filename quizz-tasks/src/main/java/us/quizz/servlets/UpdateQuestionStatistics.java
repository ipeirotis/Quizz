package us.quizz.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Question;
import us.quizz.service.QuestionStatisticsService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateQuestionStatistics extends HttpServlet {
  protected static final String QUESTION_ID_PARAM = "questionID";

  private QuestionStatisticsService questionStatisticsService;
  private static final Logger logger = Logger.getLogger(UpdateQuestionStatistics.class.getName());

  @Inject
  public UpdateQuestionStatistics(QuestionStatisticsService questionStatisticsService) {
    this.questionStatisticsService = questionStatisticsService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String questionID = req.getParameter(QUESTION_ID_PARAM);
    Question question = questionStatisticsService.updateStatistics(questionID);
    logger.log(Level.FINEST, "QuestionID:" + questionID + "\n");
    logger.log(Level.FINEST, "Number of user answers:" + question.getNumberOfUserAnswers() + "\n");
    logger.log(Level.FINEST, "Number of correct user answers:" + 
        question.getNumberOfCorrectUserAnswers() + "\n");
    logger.log(Level.FINEST, "Kind:" + question.getKind() + "\n");
  }
}
