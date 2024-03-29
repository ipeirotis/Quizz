package us.quizz.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

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
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizService;
import us.quizz.service.UserActionService;
import us.quizz.service.UserAnswerFeedbackService;
import us.quizz.service.UserAnswerService;
import us.quizz.service.UserService;
import us.quizz.utils.Constants;
import us.quizz.utils.QueueUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Api(name = "quizz",
     description = "The API for Quizz.us",
     version = "v1",
     clientIds = {Constants.PROD_WEB_CLIENT_ID, Constants.PROD_SERVICE_CLIENT_ID,
                  Constants.DEV_WEB_CLIENT_ID, Constants.DEV_SERVICE_CLIENT_ID,
                  API_EXPLORER_CLIENT_ID},
     scopes = {Constants.EMAIL_SCOPE})
public class ProcessUserAnswerEndpoint {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ProcessUserAnswerEndpoint.class.getName());

  private QuizService quizService;
  private UserService userService;
  private QuestionService questionService;
  private UserAnswerService userAnswerService;
  private UserAnswerFeedbackService userAnswerFeedbackService;
  private ExplorationExploitationService explorationExploitationService;
  private UserActionService userActionService;

  @Inject
  public ProcessUserAnswerEndpoint(
      QuizService quizService,
      UserService userService,
      QuestionService questionService,
      UserAnswerService userAnswerService,
      UserAnswerFeedbackService userAnswerFeedbackService,
      ExplorationExploitationService explorationExploitationService,
      UserActionService userActionService) {
    this.quizService = quizService;
    this.userService = userService;
    this.userAnswerFeedbackService = userAnswerFeedbackService;
    this.questionService = questionService;
    this.userAnswerService = userAnswerService;
    this.explorationExploitationService = explorationExploitationService;
    this.userActionService = userActionService;
  }

  /**
   * Processes user answer and returns results to the user. This does a lot of things (perhaps
   * too many), including:
   * - Logging the corresponding UserAction (ANSWER_SENT/ANSWER_SKIPPED).
   * - Figures out the answer index for a free text user answer or creates a new USER_SUBMITTED
   *   answer if the text is new.
   * - Constructs and stores a new UserAnswer entity.
   * - Constructs and stores a new UserAnswerFeedback entity.
   * - Kicks off a task in the task queue to update QuizPerformance of the given user in background.
   * - Kicks off a task in the task queue to update QuestionStatistics of the given user in
   *   background.
   * - Computes whether to ask a collection or a calibration question for the next question.
   *
   * @param req HttpServletRequest corresponding to this HTTP request.
   * @param quizID The id of the quiz the answer is about.
   * @param questionID The id of the question the user is answering.
   * @param answerID The id of the answer the user is submitting in multiple choice question or
   *                 -1 if the user skips a multiple choice question or it is a free text question.
   * @param userID The id of the user submitting this answer.
   * @param userInput The text the user submits in a free text question or an empty string if
   *                  the user skips a question (both free text and multiple choice) or
   *                  it is a multiple choice question.
   * @param numCorrect Number of questions user answered correctly in the current quiz session.
   * @param numIncorrect Number of questions user answered incorrectly in the current quiz session.
   * @param numExploit Number of collection questions shown to the users.
   * @param questionIndex The question index in the current quiz session that the user is answering.
   *                      This is nullable because it is recently added and some of the cached js
   *                      files might be stale and does not provide this value, in which case, we
   *                      will just put a placeholder value of -1 for now.
   *
   * @return a Map of five items:
   *         - userAnswer: UserAnswer entity created after processing the user answer.
   *         - userAnswerFeedback: UserAnswerFeedback entity created after processing this answer.
   *         - exploit: Whether to exploit (to ask a collection question).
   *         - bestAnswer: The best answer for this question (either ground truth or inferred).
   *                       Null if we don't have any idea of the best answer.
   *         - question: The updated question (since a new answer might be added if the user
   *                     submits a new answer previously unseen in a free-text question).
   */
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
      @Named("numExploit") Integer numExploit,
      @Nullable @Named("questionIndex") Integer questionIndex) throws Exception {
    if (questionIndex == null) {
      questionIndex = -1;
    }
    Long timestamp = (new Date()).getTime();

    // TODO(chunhowt): Modifies these getters to be asynchronous using futures.
    User user = userService.get(userID);
    Question question = questionService.get(questionID);
    Quiz quiz = quizService.get(quizID);

    Integer numChoices = 0;
    if (quiz.getKind() == QuizKind.MULTIPLE_CHOICE) {
      numChoices = quizService.get(quizID).getNumChoices();
      if (numChoices == null) {
        numChoices = 4;
      }
    }

    String action = answerID == -1 && userInput.isEmpty() ? UserAnswer.SKIP : UserAnswer.SUBMIT;
    if (action.equals(UserAnswer.SKIP)) {
      userActionService.asyncSaveAnswerSkipped(userID, timestamp, quizID, questionID);
    } else {
      userActionService.asyncSaveAnswerSent(
          userID, timestamp, quizID, questionID, answerID, userInput);
    }
    QuestionService.Result qResult = questionService.verifyAnswer(question, answerID, userInput);

    Integer internalAnswerID = answerID;
    // If we have a free text quiz, we need to store the submitted answer if it is not a SKIP.
    if (quiz.getKind() == QuizKind.FREE_TEXT && !userInput.isEmpty()) {
      internalAnswerID = null;

      // We check to see if the submitted answer is already among the answers for the question
      // If yes, we increase the count of picks
      // TODO(chunhowt): There is a lot of potential that there might be race condition in
      // updating the numberOfPicks and creating the new answer.
      for (Answer a : question.getAnswers()) {
        if (a.getText().equalsIgnoreCase(userInput)) {
          int numberOfPicks = a.getNumberOfPicks();
          a.setNumberOfPicks(numberOfPicks + 1);

          internalAnswerID = a.getInternalID();
          // If this is the second time that we see a particular answer in a free text quiz
          // then we launch a verification question (multiple choice)
          if (numberOfPicks + 1 == 2 &&
              (question.getKind() == QuestionKind.FREETEXT_COLLECTION ||
               question.getKind() == QuestionKind.FREETEXT_CALIBRATION)) {
            updateVerificationQuiz(quiz, question.getId(), internalAnswerID);
          }
          break;
        }
      }
      // If the answer has not been seen before, we store it as a user submitted answer
      if (internalAnswerID == null) {
        internalAnswerID = question.getAnswers().size();
        Answer answer = new Answer(
            questionID, quizID, userInput, AnswerKind.USER_SUBMITTED, internalAnswerID);
        answer.setNumberOfPicks(1);
        question.addAnswer(answer);
      }
      questionService.save(question);
    }

    // TODO(chunhowt): Have a cron task to anonymize IP after 9 months.
    String ipAddress = req.getRemoteAddr();
    String browser = req.getHeader("User-Agent");

    UserAnswerFeedback uaf = asyncStoreUserAnswerFeedback(question, user, questionID,
        internalAnswerID, userInput, qResult.getIsCorrect(), qResult.getMessage());
    UserAnswer ua = storeUserAnswer(user, quizID, questionID, action, internalAnswerID,
        userInput, ipAddress, browser, timestamp, qResult.getIsCorrect(), questionIndex);

    updateQuizPerformance(user, quizID);
    updateQuestionStatistics(questionID);

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("userAnswer", ua);
    result.put("userAnswerFeedback", uaf);
    result.put("exploit", isExploit(numCorrect, numIncorrect, numExploit, numChoices));
    result.put("bestAnswer", qResult.getBestAnswer());
    result.put("question", question);
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

    String answerText = userInput;
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
      String browser, Long timestamp, Boolean isCorrect, Integer questionIndex) {
    UserAnswer ue = new UserAnswer(user.getUserid(), questionID, useranswerID);
    ue.setBrowser(browser);
    ue.setIpaddress(ipAddress);
    ue.setTimestamp(timestamp);
    ue.setAction(action);
    ue.setIsCorrect(isCorrect);
    ue.setQuizID(quizID);
    ue.setUserInput(userInput);
    ue.setQuestionIndex(questionIndex);
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

  // Schedules a task to update the verification quiz.
  private void updateVerificationQuiz(Quiz quiz, Long questionID, Integer internalAnswerID) {
    // Ensure that the verification quiz exists
    // If not, create it
    String verificationQuizId = quiz.getQuizID() + "-verification";
    Quiz verificationQuiz = quizService.get(verificationQuizId);
    if (verificationQuiz == null) {
      String verificationQuizName = quiz.getName() + " (Verification)";
      quiz = new Quiz(verificationQuizName, verificationQuizId, QuizKind.MULTIPLE_CHOICE);
      quiz.setNumChoices(2);
      quizService.save(quiz);
    }

    Queue queueUserStats = QueueUtils.getVerificationQueue();
    queueUserStats
        .add(Builder.withUrl("/api/updateVerificationQuiz")
            .param("quizId", verificationQuizId)
            .param("questionID", String.valueOf(questionID))
            .param("internalAnswerID", String.valueOf(internalAnswerID))
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
