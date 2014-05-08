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
  private QuestionStatisticsService questionStatisticsService;
  private static final Logger logger = Logger.getLogger(UpdateQuestionStatistics.class.getName());
  @Inject
  public UpdateQuestionStatistics(QuestionStatisticsService questionStatisticsService) {
    this.questionStatisticsService = questionStatisticsService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String questionID = req.getParameter("questionID");
    Question question = questionStatisticsService.updateStatistics(questionID);
    
    logger.log(Level.INFO, "QuestionID:" + questionID + "\n");
    logger.log(Level.INFO, "Number of user answers:" + question.getNumberOfUserAnswers() + "\n");
    logger.log(Level.INFO, "Number of correct user answers:" + 
        question.getNumberOfCorrentUserAnswers() + "\n");
  }
}
