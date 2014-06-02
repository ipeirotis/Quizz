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

    Question newQuestion = new Question(
        question.getQuizID(), question.getQuestionText(), question.getKind());
    newQuestion.setClientID(question.getClientID());

    // TODO(chunhowt): We might not need this anymore since Answer is embedded in Question entity
    // in objectify now.
    // We save the object, because we need to get the questionID assigned by datastore.
    newQuestion = questionService.save(newQuestion);

    if (question.getAnswers() != null) {
      int internalID = 0;
      for (final Answer answer : question.getAnswers()) {
        Preconditions.checkNotNull(answer.getKind(), "Answer kind can't be empty");

        // Create a new Answer to generate a new answer ID.
        Answer newAnswer = new Answer(
            newQuestion.getId(), newQuestion.getQuizID(),
            answer.getText(), answer.getKind(), internalID);
        newAnswer.setProbability(answer.getProbability());

        switch (newAnswer.getKind()) {
          case SILVER: {
            newQuestion.setHasSilverAnswers(true);
            break;
          }
          case GOLD: {
            newQuestion.setHasGoldAnswer(true);
            break;
          }
          default:
            break;
        }
        internalID++;
        newQuestion.addAnswer(newAnswer);
      }
      return questionService.save(newQuestion);
    } else {
      return newQuestion;
    }
  }
}
