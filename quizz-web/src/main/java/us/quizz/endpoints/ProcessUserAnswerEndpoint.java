package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuizKind;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerFeedbackRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserRepository;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.utils.LevenshteinAlgorithm;
import us.quizz.utils.QueueUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1",
     namespace = @ApiNamespace(ownerDomain = "crowd-power.appspot.com",
                               ownerName = "crowd-power.appspot.com",
                               packagePath = "us.quizz.endpoints"))
public class ProcessUserAnswerEndpoint {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ProcessUserAnswerEndpoint.class.getName());

  private QuizRepository quizRepository;
  private UserRepository userRepository;
  private AnswersRepository answersRepository;
  private QuizQuestionRepository quizQuestionRepository;
  private BadgeRepository badgeRepository;
  private QuizPerformanceRepository quizPerformanceRepository;
  private UserAnswerRepository userAnswerRepository;
  private UserAnswerFeedbackRepository userAnswerFeedbackRepository;
  private ExplorationExploitationService explorationExploitationService;

  @Inject
  public ProcessUserAnswerEndpoint(
      QuizRepository quizRepository,
      UserRepository userRepository,
      AnswersRepository answersRepository,
      QuizQuestionRepository quizQuestionRepository,
      BadgeRepository badgeRepository,
      QuizPerformanceRepository quizPerformanceRepository,
      UserAnswerRepository userAnswerRepository,
      UserAnswerFeedbackRepository userAnswerFeedbackRepository,
      ExplorationExploitationService explorationExploitationService) {
    this.quizRepository = quizRepository;
    this.userRepository = userRepository;
    this.userAnswerFeedbackRepository = userAnswerFeedbackRepository;
    this.answersRepository = answersRepository;
    this.quizQuestionRepository = quizQuestionRepository;
    this.badgeRepository = badgeRepository;
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.userAnswerRepository = userAnswerRepository;
    this.explorationExploitationService = explorationExploitationService;
  }

  @ApiMethod(name = "processUserAnswer", path = "processUserAnswer", httpMethod = HttpMethod.POST)
  public Map<String, Object> processUserAnswer(
      HttpServletRequest req,
      @Named("quizID") String quizID, 
      @Named("questionID") Long questionID,
      @Named("answerID") Integer answerID,
      @Named("userID") String userID,
      @Named("correctanswers") Integer correctanswers,
      @Named("totalanswers") Integer totalanswers,
      @Named("userInput") String userInput,
      @Named("a") Integer a,
      @Named("b") Integer b,
      @Named("c") Integer c) throws Exception {
    User user = userRepository.get(userID);
    String action;

    Boolean isCorrect = false;
    String message = "";
    if (answerID == -1) {
      action = "I don't know";
    } else {
      action = "Submit";
      totalanswers++;

      isCorrect = isUserAnswerCorrect(questionID, answerID, userInput);
      if (isCorrect) {
        correctanswers++;
      }      
    }

    String ipAddress = req.getRemoteAddr();
    String browser = req.getHeader("User-Agent");
    String referer = req.getHeader("Referer");
    if (referer == null) {
      referer = "";
    }
    Long timestamp = (new Date()).getTime();

    UserAnswerFeedback uaf = createUserAnswerFeedback(user, questionID,
        answerID, userInput, isCorrect, correctanswers, totalanswers);
    quickUpdateQuizPerformance(user, quizID, isCorrect, action);
    
    UserAnswer ua = storeUserAnswer(user, quizID, questionID, action, answerID,
        userInput, ipAddress, browser, referer, timestamp, isCorrect);
    updateQuizPerformance(user, questionID);
    updateQuestionStatistics(questionID);

    // Get the number of multiple choices for the quiz
    Integer N = this.quizRepository.get(quizID).getNumChoices();
    if (N == null) {
      N = 4;
    }

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("userAnswer", ua);
    result.put("userAnswerFeedback", uaf);
    result.put("exploit", isExploit(a, b, c, N));
    return result;
  }

  private Boolean isUserAnswerCorrect(Long questionID, Integer answerID, String userInput) {
    Boolean isCorrect = false;
    Question question = quizQuestionRepository.getQuizQuestion(questionID);

    if (question.getKind() == QuizKind.MULTIPLE_CHOICE) {
      Answer goldAnswer = question.goldAnswer();
      isCorrect = goldAnswer.getInternalID() == answerID;
    }
    if (question.getKind() == QuizKind.FREE_TEXT) {
      // since we do not have an id, to immediately retrieve
      // the answer, we need to scan and see if the submitted answer 
      // matches
      List<Answer> answers = question.getAnswers();
      for (Answer ans : answers) {
        AnswerKind ak = ans.getKind();
        if (ak == AnswerKind.GOLD || ak == AnswerKind.SILVER) {
          if (ans.getText().equalsIgnoreCase(userInput)) {
            isCorrect = true;
            break;
          } 
          int editDistance = LevenshteinAlgorithm
              .getLevenshteinDistance(userInput, ans.getText());
          if (editDistance <= 1) {
            isCorrect = true;
            break;
          }
        }
      }
    }
    return isCorrect;
  }

  private boolean isExploit(int a, int b, int c, int N) throws Exception {
    explorationExploitationService.setN(N);
    return explorationExploitationService.getAction(a, b, c).getActionExploit();
  }

  protected UserAnswerFeedback createUserAnswerFeedback(User user,
      Long questionID, Integer useranswerID, String userInput,
      Boolean isCorrect, Integer correctanswers,
      Integer totalanswers) {
    
    UserAnswerFeedback uaf = new UserAnswerFeedback(questionID,
        user.getUserid(), useranswerID, isCorrect);
    uaf.setNumCorrectAnswers(correctanswers);
    uaf.setNumTotalAnswers(totalanswers);
    Question question = quizQuestionRepository.getQuizQuestion(questionID);
    uaf.setUserAnswerText((useranswerID == -1) ? "" : question.getAnswer(
        useranswerID).userAnswerText(userInput));
    uaf.setCorrectAnswerText(question.goldAnswer().getText());
    uaf.computeDifficulty();
    userAnswerFeedbackRepository.storeUserAnswerFeedback(uaf);
    return uaf;
  }

  private void updateQuizPerformance(User user, Long questionID) {
    Queue queueUserStats = QueueUtils.getUserStatisticsQueue();
    String quizID = quizQuestionRepository.getQuizQuestion(questionID)
        .getQuizID();
    queueUserStats
        .add(Builder.withUrl("/api/updateUserQuizStatistics")
            .param("quizID", quizID)
            .param("userid", user.getUserid())
            .param("channelNotify", "true")
            .method(TaskOptions.Method.POST));
  }

  private void updateQuestionStatistics(Long questionID) {
    Queue queue = QueueUtils.getQuestionStatisticsQueue();
    queue.add(Builder
        .withUrl("/api/updateQuestionStatistics")
        .param("questionID", questionID.toString())
        .method(TaskOptions.Method.POST));
  }

  /**
   * @param user
   * @param quizID
   * @param mid
   * @param action
   * @param useranswer
   * @param ipAddress
   * @param browser
   * @param referer
   * @param timestamp
   * @param isCorrect
   */
  private UserAnswer storeUserAnswer(User user, String quizID, Long questionID,
      String action, Integer useranswerID, String userInput,
      String ipAddress, String browser, String referer, Long timestamp,
      Boolean isCorrect) {
    UserAnswer ue = new UserAnswer(user.getUserid(), questionID, useranswerID);
    ue.setReferer(referer);
    ue.setBrowser(browser);
    ue.setIpaddress(ipAddress);
    ue.setTimestamp(timestamp);
    ue.setAction(action);
    ue.setIsCorrect(isCorrect);
    ue.setQuizID(quizID);
    ue.setUserInput(userInput);
    return userAnswerRepository.singleMakePersistent(ue);
  }

  /**
   * With this call, we just update the counts of correct and incorrect
   * answers. The full update happens asynchronously from the
   * updateUserStatistics call that is placed in the task queue.
   * 
   * @param user
   * @param quizID
   * @param isCorrect
   * @param action
   */
  private void quickUpdateQuizPerformance(User user, String quizID,
      Boolean isCorrect, String action) {

    QuizPerformance qp = quizPerformanceRepository.getQuizPerformance(
        quizID, user.getUserid());
    if (qp == null) {
      qp = new QuizPerformance(quizID, user.getUserid());
    }

    if (isCorrect) {
      qp.increaseCorrect();
    } else {
      qp.increaseIncorrect();
    }

    if (action.equals("Submit")) {
      qp.increaseTotal();
    }

    quizPerformanceRepository.cacheQuizPerformance(qp);
  }
}
