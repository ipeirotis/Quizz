package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.BadRequestException;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class QuestionEndpoint {
  private QuizService quizService;
  private QuestionService questionService;

  @Inject
  public QuestionEndpoint(QuizService quizService, QuestionService questionService) {
    this.quizService = quizService;
    this.questionService = questionService;
  }

  // Lists all the questions in the quizID.
  @ApiMethod(name = "listAllQuestions", path = "listAllQuestions", httpMethod = HttpMethod.POST)
  public List<Question> listAllQuestions(@Named("quizID") String quizID) {
    return this.questionService.getQuizQuestions(quizID);
  }

  // Inserts the question given into the datastore.
  @ApiMethod(name = "insertQuestion", path = "insertQuestion", httpMethod = HttpMethod.POST)
  public Question insertQuestion(Question question) throws BadRequestException {
    // Sanity check for QuizKind and QuestionKind.
    Quiz quiz = quizService.get(question.getQuizID());
    QuizKind quizKind = quiz.getKind();
    if (quizKind == QuizKind.MULTIPLE_CHOICE &&
        (!question.getKind().equals(QuestionKind.MULTIPLE_CHOICE_CALIBRATION) &&
         !question.getKind().equals(QuestionKind.MULTIPLE_CHOICE_COLLECTION))) {
      throw new BadRequestException("Can't add " + question.getKind() +
          " question to " + quiz.getKind() + " quiz");
    }
    if (quizKind == QuizKind.FREE_TEXT &&
        (!question.getKind().equals(QuestionKind.FREETEXT_CALIBRATION) &&
         !question.getKind().equals(QuestionKind.FREETEXT_COLLECTION))) {
      throw new BadRequestException("Can't add " + question.getKind() +
          " question to " + quiz.getKind() + " quiz");
    }

    if (question.getAnswers() != null) {
      int internalID = 0;
      for (Answer answer : question.getAnswers()) {
        Preconditions.checkNotNull(answer.getKind(), "Answer kind can't be empty");
        answer.setInternalID(internalID);
        answer.setQuizID(question.getQuizID());

        switch (answer.getKind()) {
          case SILVER: {
            question.setHasSilverAnswers(true);
            break;
          }
          case GOLD: {
            question.setHasGoldAnswer(true);
            break;
          }
          default:
            break;
        }
        internalID++;
      }
    }

    return questionService.save(question);
  }
}
