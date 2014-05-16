package us.quizz.endpoints;

import java.util.List;

import javax.inject.Named;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerChallengeStatus;
import us.quizz.service.AnswerChallengeCounterService;
import us.quizz.service.QuestionService;
import us.quizz.service.UserAnswerService;
import us.quizz.service.UserService;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class UserAnswerEndpoint {
  private UserAnswerService userAnswerService;
  private UserService userService;
  private AnswerChallengeCounterService answerChallengeCounterService;
  private QuestionService questionService;

  @Inject
  public UserAnswerEndpoint(
      UserAnswerService userAnswerService,
      UserService userService,
      AnswerChallengeCounterService answerChallengeCounterService,
      QuestionService questionService) {
    this.userAnswerService = userAnswerService;
    this.userService = userService;
    this.answerChallengeCounterService = answerChallengeCounterService;
    this.questionService = questionService;
  }

  @ApiMethod(name = "addUserAnswer", httpMethod = HttpMethod.POST, path = "addUserAnswer")
  public void addUserAnswer(
      @Named("userid") String userid,
      @Named("quizID") String quizID,
      @Named("questionID") String questionID,
      @Named("useranswerID") String useranswerID,
      @Named("referer") String referer,
      @Named("browser") String browser,
      @Named("ipAddress") String ipAddress,
      @Named("timestamp") Long timestamp,
      @Named("action") String action,
      @Named("isCorrect") Boolean isCorrect) {
    UserAnswer ue = new UserAnswer(userid, questionID, useranswerID);
    ue.setReferer(referer);
    ue.setBrowser(browser);
    ue.setIpaddress(ipAddress);
    ue.setTimestamp(timestamp);
    ue.setAction(action);
    ue.setQuizID(questionService.get(Long.parseLong(questionID)).getQuizID());
    if (isCorrect != null) {
      ue.setIsCorrect(isCorrect);
    }
    userAnswerService.save(ue);
  }

  @ApiMethod(name = "getNumberOfSubmittedAnswers", httpMethod = HttpMethod.POST,
             path = "getNumberOfSubmittedAnswers")
  public NumberOfUnswersResponse getNumberOfSubmittedAnswers(
      @Named("quiz") String quiz, @Named("userid") String userid){
    int answers = userAnswerService.getUserAnswers(quiz, userid).size();
    return new NumberOfUnswersResponse(quiz, answers);
  }

  @ApiMethod(name = "getUserAnswers", httpMethod = HttpMethod.POST,
             path = "getUserAnswers")
  public List<UserAnswer> getUserAnswers(@Named("quizID") String quizID) {
    return userAnswerService.getUserAnswersForQuiz(quizID);
  }

  @ApiMethod(name = "addAnswerFeedback", httpMethod = HttpMethod.POST, path = "addAnswerFeedback")
  public UserAnswer addAnswerFeedback(
      @Named("quizID") String quizID,
      @Named("questionID") Long questionID, 
      @Named("userAnswerID") Long userAnswerID,
      @Named("userid") String userid,
      @Named("message") String message) {
    AnswerChallengeCounter cc = answerChallengeCounterService.get(quizID, questionID);
    if (cc == null) {
      cc = new AnswerChallengeCounter(quizID, questionID);
      cc.setCount(1L);
    } else {
      cc.incCount();
    }
    answerChallengeCounterService.save(cc);

    UserAnswer userAnswer = userAnswerService.get(userAnswerID);
    userAnswer.setAnswerChallengeText(new Text(message));

    List<UserAnswer> userAnswers = userAnswerService.getUserAnswers(quizID, userid);

    if (userAnswers.size() != 0) {
      boolean exist = false;
      for (UserAnswer ua : userAnswers) {
        if (ua.getAnswerChallengeText() != null && ua.getAnswerChallengeText().equals(message)) {
          userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);

          User user = userService.get(userAnswer.getUserid());
          user.incChallengeBudget();
          userService.save(user);
          exist = true;
          break;
        }
      }
      if (!exist) {
        userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.REJECTED);
      }
    }
    return userAnswerService.save(userAnswer);
  }

  @ApiMethod(name = "approveAnswerChallenge", path = "answerChallenge/approve")
  public UserAnswer approveChallenge(@Named("userAnswerID") Long userAnswerID) {
    UserAnswer userAnswer = userAnswerService.get(userAnswerID);
    userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);

    User user = userService.get(userAnswer.getUserid());
    user.incChallengeBudget();
    userService.save(user);

    return userAnswerService.save(userAnswer);
  }

  @ApiMethod(name = "rejectAnswerChallenge", path = "answerChallenge/reject")
  public UserAnswer rejectChallenge(@Named("userAnswerID") Long userAnswerID) {
    UserAnswer userAnswer = userAnswerService.get(userAnswerID);
    userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.REJECTED);

    User user = userService.get(userAnswer.getUserid());
    user.decChallengeBudget();
    userService.save(user);

    return userAnswerService.save(userAnswer);
  }

  class NumberOfUnswersResponse {
    private String quiz;
    private int answers;

    NumberOfUnswersResponse(String quiz, int answers) {
      this.quiz = quiz;
      this.answers = answers;
    }

    public String getQuiz() {
      return quiz;
    }

    public void setQuiz(String quiz) {
      this.quiz = quiz;
    }

    public int getAnswers() {
      return answers;
    }

    public void setAnswers(int answers) {
      this.answers = answers;
    }
  }
}
