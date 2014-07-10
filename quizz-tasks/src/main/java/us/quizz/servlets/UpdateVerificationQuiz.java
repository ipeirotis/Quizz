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
  
  private static final String VERIFICATION_QUESTION_TEMPLATE = "%s <div class=\"label label-info\">%s</div>";

  private QuestionService questionService;

  @Inject
  public UpdateVerificationQuiz(QuestionService questionService) {
    this.questionService = questionService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String quizId = req.getParameter("quizId");
    String questionID = req.getParameter("questionID");
    String internalAnswerID = req.getParameter("internalAnswerID");
    Question question = questionService.get(Long.parseLong(questionID));
    Text verificationQuestionText = question.getVerificationText();
    Answer answer = question.getAnswer(Integer.parseInt(internalAnswerID));
    
    Question verificationQuestion = new Question(quizId, 
        new Text(String.format(VERIFICATION_QUESTION_TEMPLATE, verificationQuestionText, answer.getText())), 
        QuestionKind.FREETEXT_COLLECTION);
    verificationQuestion.addAnswer(new Answer("Yes", AnswerKind.SILVER));
    verificationQuestion.addAnswer(new Answer("No", AnswerKind.SILVER));
    
    questionService.save(verificationQuestion);
  }
}
