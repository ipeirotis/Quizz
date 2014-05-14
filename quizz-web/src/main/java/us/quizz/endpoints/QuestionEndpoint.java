package us.quizz.endpoints;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import us.quizz.entities.Answer;
import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.service.QuizService;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Key;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class QuestionEndpoint {
  private QuizService quizService;
  private QuizQuestionRepository quizQuestionRepository;
  private AnswerChallengeCounterRepository answerChallengeCounterRepository;

  @Inject
  public QuestionEndpoint(
      QuizService quizService,
      QuizQuestionRepository quizQuestionRepository,
      AnswerChallengeCounterRepository answerChallengeCounterRepository) {
    this.quizService = quizService;
    this.quizQuestionRepository = quizQuestionRepository;
    this.answerChallengeCounterRepository = answerChallengeCounterRepository;
  }

  @ApiMethod(name = "listQuestions", path = "listQuestions", httpMethod = HttpMethod.GET)
  public CollectionResponse<Question> listQuestions(
      @Nullable @Named("cursor") String cursor) {
    List<Question> questions = quizQuestionRepository.getQuizQuestions();
    return CollectionResponse.<Question> builder().setItems(questions)
        .setNextPageToken(cursor).build();
  }

  @ApiMethod(name = "listAllQuestions", path = "listAllQuestions", httpMethod = HttpMethod.POST)
  public List<Question> listAllQuestions(@Named("quizID") String quizID) {
    return this.quizQuestionRepository.getQuizQuestions(quizID);
  }

  @ApiMethod(name = "listQuestionsWithChallenges", path = "/listQuestionsWithChallenges")
  public CollectionResponse<QuestionWithChallenges> listQuestionsWithChallenges(
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit) {
    List<AnswerChallengeCounter> challenges = answerChallengeCounterRepository.list(cursor, limit);

    List<Key> keys = new ArrayList<Key>();
    for (AnswerChallengeCounter c : challenges) {
      keys.add(Question.generateKeyFromID(c.getQuestionID()));
    }

    List<QuestionWithChallenges> result = new ArrayList<QuestionWithChallenges>();
    if (keys.size() != 0) {
      List<Question> questions = quizQuestionRepository.getQuizQuestionsByKeys(keys);
      int i = 0;
      for (Question question : questions) {
        result.add(new QuestionWithChallenges(question, challenges.get(i).getCount()));
        i++;
      }
    }

    return CollectionResponse.<QuestionWithChallenges> builder().setItems(result)
        .setNextPageToken(cursor).build();
  }

  @ApiMethod(name = "getQuestion", path = "getQuestion", httpMethod = HttpMethod.GET)
  public Question getQuestion(@Named("questionID") Long questionID) {
    return quizQuestionRepository.getQuizQuestion(questionID);
  }

  @ApiMethod(name = "insertQuestion", path = "insertQuestion", httpMethod = HttpMethod.POST)
  public Question insertQuestion(final Question question) throws BadRequestException {
    Quiz quiz = quizService.get(question.getQuizID());
    QuizKind quizKind = quiz.getKind();
    if (quizKind ==QuizKind.MULTIPLE_CHOICE &&
        (!question.getKind().equals(QuestionKind.MULTIPLE_CHOICE_CALIBRATION) &&
         !question.getKind().equals(QuestionKind.MULTIPLE_CHOICE_COLLECTION))) {
      throw new BadRequestException("Can't add " + question.getKind() +
          " question to " + quiz.getKind() + " quiz");
    }
    if (quizKind ==QuizKind.FREE_TEXT &&
        (!question.getKind().equals(QuestionKind.FREETEXT_CALIBRATION) &&
         !question.getKind().equals(QuestionKind.FREETEXT_COLLECTION))) {
      throw new BadRequestException("Can't add " + question.getKind() +
          " question to " + quiz.getKind() + " quiz");
    }

    Question newQuestion = new Question(
        question.getQuizID(), question.getQuestionText(), question.getKind());
    newQuestion.setClientID(question.getClientID());

    // We save the object, because we need to get the questionID assigned by datastore.
    newQuestion = quizQuestionRepository.insert(newQuestion);

    if (question.getAnswers() != null) {
      int internalID = 0;
      for (final Answer answer : question.getAnswers()) {
        Preconditions.checkNotNull(answer.getKind(), "Answer kind can't be empty");

        // Create a new Answer to generate a new answer ID.
        Answer newAnswer = new Answer(
            newQuestion.getID(), newQuestion.getQuizID(),
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
      return quizQuestionRepository.singleMakePersistent(newQuestion, true  /* use transaction */);
    } else {
      return newQuestion;
    }
  }

  @ApiMethod(name = "updateQuestion", path = "updateQuestion", httpMethod = HttpMethod.PUT)
  public Question updateQuestion(Question newQuestion) {
    return quizQuestionRepository.update(newQuestion);
  }

  @ApiMethod(name = "removeQuestion", path="removeQuestion", httpMethod = HttpMethod.DELETE)
  public void removeQuestion(@Named("questionID") Long questionID) {
    quizQuestionRepository.remove(Question.generateKeyFromID(questionID));
  }

  class QuestionWithChallenges {
    private Question question;
    private Long challenges;

    public QuestionWithChallenges(Question question, Long challenges) {
      this.question = question;
      this.challenges = challenges;
    }

    public Question getQuestion() {
      return question;
    }

    public void setQuestion(Question question) {
      this.question = question;
    }

    public Long getChallenges() {
      return challenges;
    }

    public void setChallenges(Long challenges) {
      this.challenges = challenges;
    }
  }
}
