package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.enums.QuizKind;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizService;
import us.quizz.service.UserAnswerFeedbackService;
import us.quizz.service.UserAnswerService;
import us.quizz.service.UserService;
import us.quizz.utils.QueueUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class ProcessUserAnswerEndpoint {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ProcessUserAnswerEndpoint.class.getName());

  private QuizService quizService;
  private UserService userService;
  private QuestionService questionService;
  private UserAnswerService userAnswerService;
  private UserAnswerFeedbackService userAnswerFeedbackService;
  private ExplorationExploitationService explorationExploitationService;

  @Inject
  public ProcessUserAnswerEndpoint(
      QuizService quizService,
      UserService userService,
      QuestionService questionService,
      UserAnswerService userAnswerService,
      UserAnswerFeedbackService userAnswerFeedbackService,
      ExplorationExploitationService explorationExploitationService) {
    this.quizService = quizService;
    this.userService = userService;
    this.userAnswerFeedbackService = userAnswerFeedbackService;
    this.questionService = questionService;
    this.userAnswerService = userAnswerService;
    this.explorationExploitationService = explorationExploitationService;
  }

  @ApiMethod(name = "processUserAnswer", path = "processUserAnswer", httpMethod = HttpMethod.POST)
  public Map<String, Object> processUserAnswer(
      HttpServletRequest req,
      @Named("quizID") String quizID, 
      @Named("questionID") Long questionID,
      @Named("answerID") Integer answerID,
      @Named("userID") String userID,
      @Named("userInput") String userInput,
      @Named("numCorrect") Integer numCorrect,
      @Named("numIncorrect") Integer numIncorrect,
      @Named("numExploit") Integer numExploit) throws Exception {
    // TODO(chunhowt): Modifies these getters to be asynchronous using futures.
    User user = userService.get(userID);
    Question question = questionService.get(questionID);
    // TODO(chunhowt): Don't need this for free-text question.
    Quiz quiz = quizService.get(quizID);
    
    Integer numChoices = null;
    if (quiz.getKind()== QuizKind.MULTIPLE_CHOICE) {
      numChoices = quizService.get(quizID).getNumChoices();
      if (numChoices == null) {
        numChoices = 4;
      }
    }

    String action = answerID == -1 ? UserAnswer.SKIP : UserAnswer.SUBMIT;
    QuestionService.Result qResult = questionService.verifyAnswer(question, answerID, userInput);

    // TODO(chunhowt): Have a cron task to anonymize IP after 9 months.
    String ipAddress = req.getRemoteAddr();
    String browser = req.getHeader("User-Agent");
    Long timestamp = (new Date()).getTime();

    UserAnswerFeedback uaf = asyncStoreUserAnswerFeedback(question, user, questionID, answerID,
        userInput, qResult.getIsCorrect(), qResult.getMessage());
    UserAnswer ua = storeUserAnswer(user, quizID, questionID, action, answerID,
        userInput, ipAddress, browser, timestamp, qResult.getIsCorrect());

    updateQuizPerformance(user, quizID);
    updateQuestionStatistics(questionID);

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("userAnswer", ua);
    result.put("userAnswerFeedback", uaf);
    result.put("exploit", isExploit(numCorrect, numIncorrect, numExploit, numChoices));
    result.put("bestAnswer", qResult.getBestAnswer());
    return result;
  }

  private boolean isExploit(int numCorrect, int numIncorrect, int numExploit, int numChoices)
      throws Exception {
    explorationExploitationService.setN(numChoices);
    return explorationExploitationService
        .getAction(numCorrect, numIncorrect, numExploit).getActionExploit();
  }

  protected UserAnswerFeedback asyncStoreUserAnswerFeedback(Question question, User user,
      Long questionID, Integer useranswerID, String userInput, Boolean isCorrect, String message) {
    UserAnswerFeedback uaf = new UserAnswerFeedback(
        questionID, user.getUserid(), useranswerID, isCorrect);

    String answerText = "";
    if (useranswerID != -1) {
      Answer a = question.getAnswer(useranswerID);
      if (a != null) answerText = a.userAnswerText(userInput);
    }

    uaf.setUserAnswerText(answerText);
    uaf.setMessage(message);
    // TODO(chunhowt): This doesn't make sense, this is computing the "quality" of user, not
    // difficulty of the question.
    uaf.computeDifficulty();
    userAnswerFeedbackService.asyncSave(uaf);
    return uaf;
  }

  private UserAnswer storeUserAnswer(User user, String quizID, Long questionID,
      String action, Integer useranswerID, String userInput, String ipAddress,
      String browser, Long timestamp, Boolean isCorrect) {
    UserAnswer ue = new UserAnswer(user.getUserid(), questionID, useranswerID);
    ue.setBrowser(browser);
    ue.setIpaddress(ipAddress);
    ue.setTimestamp(timestamp);
    ue.setAction(action);
    ue.setIsCorrect(isCorrect);
    ue.setQuizID(quizID);
    ue.setUserInput(userInput);
    return userAnswerService.save(ue);
  } 

  // Schedules a task to update the quiz performance for the given user and quiz.
  private void updateQuizPerformance(User user, String quizID) {
    Queue queueUserStats = QueueUtils.getUserStatisticsQueue();
    queueUserStats
        .add(Builder.withUrl("/api/updateUserQuizStatistics")
            .param("quizID", quizID)
            .param("userID", user.getUserid())
            .param("channelNotify", "true")
            .method(TaskOptions.Method.POST));
  }

  // Schedules a task to update the question statistics for the given questionID since we have
  // a new answer for the question.
  private void updateQuestionStatistics(Long questionID) {
    Queue queue = QueueUtils.getQuestionStatisticsQueue();
    queue.add(Builder
        .withUrl("/api/updateQuestionStatistics")
        .param("questionID", String.valueOf(questionID))
        .method(TaskOptions.Method.GET));
  }
}
