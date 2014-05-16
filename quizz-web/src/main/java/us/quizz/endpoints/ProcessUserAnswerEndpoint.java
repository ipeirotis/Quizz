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
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.enums.AnswerKind;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.QuizPerformanceService;
import us.quizz.service.QuizService;
import us.quizz.service.UserAnswerFeedbackService;
import us.quizz.service.UserAnswerService;
import us.quizz.service.UserService;
import us.quizz.utils.LevenshteinAlgorithm;
import us.quizz.utils.QueueUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
  private AnswersRepository answersRepository;
  private QuizQuestionRepository quizQuestionRepository;
  private BadgeRepository badgeRepository;
  private QuizPerformanceService quizPerformanceService;
  private UserAnswerService userAnswerService;
  private UserAnswerFeedbackService userAnswerFeedbackService;
  private ExplorationExploitationService explorationExploitationService;

  @Inject
  public ProcessUserAnswerEndpoint(
      QuizService quizService,
      UserService userService,
      AnswersRepository answersRepository,
      QuizQuestionRepository quizQuestionRepository,
      BadgeRepository badgeRepository,
      QuizPerformanceService quizPerformanceService,
      UserAnswerService userAnswerService,
      UserAnswerFeedbackService userAnswerFeedbackService,
      ExplorationExploitationService explorationExploitationService) {
    this.quizService = quizService;
    this.userService = userService;
    this.userAnswerFeedbackService = userAnswerFeedbackService;
    this.answersRepository = answersRepository;
    this.quizQuestionRepository = quizQuestionRepository;
    this.badgeRepository = badgeRepository;
    this.quizPerformanceService = quizPerformanceService;
    this.userAnswerService = userAnswerService;
    this.explorationExploitationService = explorationExploitationService;
  }

  private String constructCollectionFeedback(
      String bestAnswerText, double probability, boolean isCorrect) {
    String feedback = "";
    long roundedProbability = Math.round(probability * 100);
    boolean isFirst = roundedProbability == 0;
    feedback += (isCorrect || isFirst) ? "Great! " : "Sorry! ";
    feedback += "We are not 100% sure about the correct answer ";
    feedback += (isFirst) ? "and you are the first user to answer!" :
        "but we believe " + bestAnswerText + " to be correct and " +
        roundedProbability + "% of the users agree.";
    return feedback;
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
    User user = userService.get(userID);
    String action;

    Boolean isCorrect = false;
    String message = "";
    if (answerID == -1) {
      action = "I don't know";
    } else {
      action = "Submit";
      totalanswers++;
    }

    // TODO(chunhowt): Moves this logic to some other place.
    // TODO(chunhowt): Deal with I don't know action.
    Question question = quizQuestionRepository.getQuizQuestion(questionID);
    Answer bestAnswer = null;
    switch (question.getKind()) {
      case MULTIPLE_CHOICE_CALIBRATION:
        for (final Answer answer : question.getAnswers()) {
          if (answer.getKind()  == AnswerKind.GOLD) {
            bestAnswer = answer;
            break;
          }
        }
        if (bestAnswer.getInternalID() == answerID) {
          isCorrect = true;
          correctanswers++;
          message = "Correct! The correct answer is " + bestAnswer.getText();
        } else {
          isCorrect = false;
          message = "Sorry! The correct answer is " + bestAnswer.getText();
        }
        break;
      case MULTIPLE_CHOICE_COLLECTION:
        double maxProbability = -1;
        for (final Answer answer : question.getAnswers()) {
          Double prob = answer.getProbCorrect();
          if (prob == null) prob = 0.0;
          if (prob > maxProbability) {
            maxProbability = prob;
            bestAnswer = answer;
          }
        }
        isCorrect = bestAnswer.getInternalID() == answerID;
        correctanswers += isCorrect ? 1 : 0;
        message = constructCollectionFeedback(bestAnswer.getText(), maxProbability, isCorrect);
        break;
      case FREETEXT_CALIBRATION:
        // TODO: We need to work further on free text quizzes
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
        break;
      case FREETEXT_COLLECTION:
        // TODO: We need to work further on free text quizzes
        break;
    }

    String ipAddress = req.getRemoteAddr();
    String browser = req.getHeader("User-Agent");
    String referer = req.getHeader("Referer");
    if (referer == null) {
      referer = "";
    }
    Long timestamp = (new Date()).getTime();

    UserAnswerFeedback uaf = createUserAnswerFeedback(user, questionID,
        answerID, userInput, isCorrect, correctanswers, totalanswers, message);
    
    UserAnswer ua = storeUserAnswer(user, quizID, questionID, action, answerID,
        userInput, ipAddress, browser, referer, timestamp, isCorrect);
    updateQuizPerformance(user, questionID);
    updateQuestionStatistics(questionID);

    // Get the number of multiple choices for the quiz
    Integer N = quizService.get(quizID).getNumChoices();
    if (N == null) {
      N = 4;
    }

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("userAnswer", ua);
    result.put("userAnswerFeedback", uaf);
    result.put("exploit", isExploit(a, b, c, N));
    return result;
  }


  private boolean isExploit(int a, int b, int c, int N) throws Exception {
    explorationExploitationService.setN(N);
    return explorationExploitationService.getAction(a, b, c).getActionExploit();
  }

  protected UserAnswerFeedback createUserAnswerFeedback(User user,
      Long questionID, Integer useranswerID, String userInput,
      Boolean isCorrect, Integer correctanswers,
      Integer totalanswers, String message) {
    
    UserAnswerFeedback uaf = new UserAnswerFeedback(questionID,
        user.getUserid(), useranswerID, isCorrect);
    uaf.setNumCorrectAnswers(correctanswers);
    uaf.setNumTotalAnswers(totalanswers);
    Question question = quizQuestionRepository.getQuizQuestion(questionID);
    
    String answerText = "";
    if (useranswerID != -1) {
      Answer a = question.getAnswer(useranswerID);
      if (a != null) answerText = a.userAnswerText(userInput);
    }
        
    uaf.setUserAnswerText(answerText);
    uaf.setMessage(message);
    uaf.computeDifficulty();
    userAnswerFeedbackService.save(uaf);
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
    return userAnswerService.save(ue);
  }

}
