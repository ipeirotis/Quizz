package us.quizz.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizService;

import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UpdateVerificationQuiz extends HttpServlet {
  
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(UpdateVerificationQuiz.class.getName());
  
  private static final String VERIFICATION_QUIZ_ID = "verificationquiz";
  private static final String VERIFICATION_QUIZ_NAME = "Verification quiz";
  private static final String VERIFICATION_QUESTION = "%s Answer: %s Correct?";

  private QuestionService questionService;
  private QuizService quizService;

  @Inject
  public UpdateVerificationQuiz(QuestionService questionService, QuizService quizService) {
    this.questionService = questionService;
    this.quizService = quizService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Quiz quiz = quizService.get(VERIFICATION_QUIZ_ID);
    if(quiz == null) {
      quiz = new Quiz(VERIFICATION_QUIZ_NAME, VERIFICATION_QUIZ_ID, QuizKind.FREE_TEXT);
      quizService.save(quiz);
    }
    
    String questionID = req.getParameter("questionID");
    String internalAnswerID = req.getParameter("internalAnswerID");
    Question question = questionService.get(Long.parseLong(questionID));
    Answer answer = question.getAnswer(Integer.parseInt(internalAnswerID));
    
    Question verificationQuestion = new Question(VERIFICATION_QUIZ_ID, 
        new Text(String.format(VERIFICATION_QUESTION, question.getQuestionText(), answer.getText())), 
        QuestionKind.FREETEXT_COLLECTION);
    verificationQuestion.addAnswer(new Answer("Yes", AnswerKind.USER_SUBMITTED));
    verificationQuestion.addAnswer(new Answer("No", AnswerKind.USER_SUBMITTED));
    
    questionService.save(verificationQuestion);
  }
}
