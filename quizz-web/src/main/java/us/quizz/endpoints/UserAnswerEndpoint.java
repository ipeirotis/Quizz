package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerChallengeStatus;
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserRepository;

import java.util.List;

import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1",
     namespace = @ApiNamespace(ownerDomain = "crowd-power.appspot.com",
                               ownerName = "crowd-power.appspot.com",
                               packagePath = "us.quizz.endpoints"))
public class UserAnswerEndpoint {
  private UserAnswerRepository userAnswerRepository;
  private UserRepository userRepository;
  private AnswerChallengeCounterRepository answerChallengeCounterRepository;
  private QuizQuestionRepository quizQuestionRepository;

  @Inject
  public UserAnswerEndpoint(
      UserAnswerRepository userAnswerRepository,
      UserRepository userRepository,
      AnswerChallengeCounterRepository answerChallengeCounterRepository,
      QuizQuestionRepository quizQuestionRepository) {
    this.userAnswerRepository = userAnswerRepository;
    this.userRepository = userRepository;
    this.answerChallengeCounterRepository = answerChallengeCounterRepository;
    this.quizQuestionRepository = quizQuestionRepository;
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
    ue.setQuizID(quizQuestionRepository.getQuizQuestion(questionID).getQuizID());
    if (isCorrect != null) {
      ue.setIsCorrect(isCorrect);
    }
    userAnswerRepository.singleMakePersistent(ue);
  }

  @ApiMethod(name = "getNumberOfSubmittedAnswers", httpMethod = HttpMethod.POST,
             path = "getNumberOfSubmittedAnswers")
  public NumberOfUnswersResponse getNumberOfSubmittedAnswers(
      @Named("quiz") String quiz, @Named("userid") String userid){
    int answers = userAnswerRepository.getUserAnswers(quiz, userid).size();
    return new NumberOfUnswersResponse(quiz, answers);
  }

  @ApiMethod(name = "addAnswerFeedback", httpMethod = HttpMethod.POST, path = "addAnswerFeedback")
  public UserAnswer addAnswerFeedback(
      @Named("quizID") String quizID,
      @Named("questionID") Long questionID, 
      @Named("userAnswerID") Long userAnswerID,
      @Named("userid") String userid,
      @Named("message") String message) {
    AnswerChallengeCounter cc = answerChallengeCounterRepository.get(quizID, questionID);
    if (cc == null) {
      cc = new AnswerChallengeCounter(quizID, questionID);
      cc.setCount(1L);
    } else {
      cc.incCount();
    }
    answerChallengeCounterRepository.save(cc);

    UserAnswer userAnswer = userAnswerRepository.get(userAnswerID);
    userAnswer.setAnswerChallengeText(new Text(message));

    List<UserAnswer> userAnswers = userAnswerRepository.getUserAnswersWithChallenge(quizID, userid);

    if (userAnswers.size() != 0) {
      boolean exist = false;
      for (UserAnswer ua : userAnswers) {
        if (ua.getAnswerChallengeText() != null && ua.getAnswerChallengeText().equals(message)) {
          userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);

          User user = userRepository.singleGetObjectById(userAnswer.getUserid());
          user.incChallengeBudget();
          userRepository.singleMakePersistent(user);
          exist = true;
          break;
        }
      }
      if (!exist) {
        userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.REJECTED);
      }
    }
    return userAnswerRepository.update(userAnswer);
  }

  @ApiMethod(name = "approveAnswerChallenge", path = "answerChallenge/approve")
  public UserAnswer approveChallenge(@Named("userAnswerID") Long userAnswerID) {
    UserAnswer userAnswer = userAnswerRepository.singleGetObjectById(userAnswerID);
    userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);

    User user = userRepository.singleGetObjectById(userAnswer.getUserid());
    user.incChallengeBudget();
    userRepository.singleMakePersistent(user);

    return userAnswerRepository.update(userAnswer);
  }

  @ApiMethod(name = "rejectAnswerChallenge", path = "answerChallenge/reject")
  public UserAnswer rejectChallenge(@Named("userAnswerID") Long userAnswerID) {
    UserAnswer userAnswer = userAnswerRepository.singleGetObjectById(userAnswerID);
    userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.REJECTED);

    User user = userRepository.singleGetObjectById(userAnswer.getUserid());
    user.decChallengeBudget();
    userRepository.singleMakePersistent(user);

    return userAnswerRepository.update(userAnswer);
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
