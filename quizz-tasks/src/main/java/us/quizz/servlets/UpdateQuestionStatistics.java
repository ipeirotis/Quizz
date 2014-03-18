package us.quizz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Question;
import us.quizz.service.QuestionStatisticsService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UpdateQuestionStatistics extends HttpServlet {
  private QuestionStatisticsService questionStatisticsService;

  @Inject
  public UpdateQuestionStatistics(QuestionStatisticsService questionStatisticsService) {
    this.questionStatisticsService = questionStatisticsService;
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String questionID = req.getParameter("questionID");
    Question question = questionStatisticsService.updateStatistics(questionID);
    
    resp.getWriter().print("QuestionID:" + questionID + "\n");
    resp.getWriter().print("Number of user answers:" + question.getNumberOfUserAnswers() + "\n");
    resp.getWriter().print("Number of correct user answers:" + 
        question.getNumberOfCorrentUserAnswers() + "\n");
  }
}
