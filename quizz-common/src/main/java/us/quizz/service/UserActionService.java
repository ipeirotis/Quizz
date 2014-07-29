package us.quizz.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import us.quizz.entities.UserAction;
import us.quizz.enums.UserActionKind;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.UserActionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Interface for getting user actions. */
public class UserActionService extends OfyBaseService<UserAction> {
  @Inject
  public UserActionService(UserActionRepository userActionRepository){
    super(userActionRepository);
  }

  public List<UserAction> list(String userId) {
    Map<String, Object> params = new HashMap<String, Object>();
    if (userId != null) {
      params.put("userid", userId);
    }
    return listAll(params);
  }

  public List<UserAction> getUserActions(String userid) {
    return ((UserActionRepository) baseRepository).getUserActions(userid);
  }

  private void validateParam(Object param, String paramName) {
    Preconditions.checkNotNull(param, paramName + " cannot be null.");
  }

  private void validateBaseParams(String userID, Long timestamp) {
    validateParam(userID, "userID");
    validateParam(timestamp, "timestamp");
  }

  private void asyncSaveUserAction(
      String userID, Long timestamp, String quizID, Long questionID, UserActionKind kind) {
    validateBaseParams(userID, timestamp);
    validateParam(quizID, "quizID");
    validateParam(questionID, "questionID");

    UserAction userAction = new UserAction(userID, timestamp, kind);
    userAction.setQuizID(quizID);
    userAction.setQuestionID(questionID);
    asyncSave(userAction);
  } 

  /**
   * Saves the QUESTION_SHOWN UserAction for the given user.
   *
   * @param userID The user where the question is shown.
   * @param timestamp The timestamp when the question is shown.
   * @param quizID The quiz the user is doing.
   * @param questionID The question shown to the user.
   */
  public void asyncSaveQuestionShown(
      String userID, Long timestamp, String quizID, Long questionID) {
    asyncSaveUserAction(userID, timestamp, quizID, questionID, UserActionKind.QUESTION_SHOWN);
  }

  /**
   * Saves the ANSWER_SENT UserAction for the given user.
   *
   * @param userID The user sending the answer.
   * @param timestamp The timestamp when the answer is submitted.
   * @param quizID The quiz the user is doing.
   * @param questionID The question the user answered.
   * @param answerID The answerID of the answer chosen by user in multiple choice question, or
   *                 -1 in a free-text question.
   * @param userAnswer The text input from user in free text question, or empty in a multiple
   *                   choice question.
   */
  public void asyncSaveAnswerSent(
      String userID, Long timestamp, String quizID, Long questionID, Integer answerID,
      String userAnswer) {
    validateBaseParams(userID, timestamp);
    validateParam(quizID, "quizID");
    validateParam(questionID, "questionID");
    // Check that either it is a free text answer or a multiple choice answer.
    Preconditions.checkArgument(
        (answerID == -1 && !userAnswer.isEmpty())
        || (answerID != -1 && userAnswer.isEmpty()));

    UserAction userAction = new UserAction(userID, timestamp, UserActionKind.ANSWER_SENT);
    userAction.setQuizID(quizID);
    userAction.setQuestionID(questionID);

    if (answerID != -1) {
      userAction.setAnswerID(answerID);
    }
    if (!userAnswer.isEmpty()) {
      userAction.setUserAnswer(userAnswer);
    }
    asyncSave(userAction);
  }

  /**
   * Saves the ANSWER_SKIPPED UserAction for the given user.
   *
   * @param userID The user skipping the question.
   * @param timestamp The timestamp when the question is skipped.
   * @param quizID The quiz the user is doing.
   * @param questionID The question the user skipped.
   */
  public void asyncSaveAnswerSkipped(
      String userID, Long timestamp, String quizID, Long questionID) {
    asyncSaveUserAction(userID, timestamp, quizID, questionID, UserActionKind.ANSWER_SKIPPED);
  }

  /**
   * Saves the EXPAND_QUESTION_CONTEXT UserAction for the given user.
   *
   * @param userID The user expanding question context.
   * @param timestamp The timestamp when the expanding of question context is performed.
   * @param quizID The quiz the user is doing.
   * @param questionID The question the user is doing.
   */
  public void asyncSaveExpandQuestionContext(
      String userID, Long timestamp, String quizID, Long questionID) {
    asyncSaveUserAction(
        userID, timestamp, quizID, questionID, UserActionKind.EXPAND_QUESTION_CONTEXT);
  }

  /**
   * Saves the HIDE_QUESTION_CONTEXT UserAction for the given user.
   *
   * @param userID The user hiding question context.
   * @param timestamp The timestamp when the hiding of question context is performed.
   * @param quizID The quiz the user is doing.
   * @param questionID The question the user is doing.
   */
  public void asyncSaveHideQuestionContext(
      String userID, Long timestamp, String quizID, Long questionID) {
    asyncSaveUserAction(
        userID, timestamp, quizID, questionID, UserActionKind.HIDE_QUESTION_CONTEXT);
  }
}
