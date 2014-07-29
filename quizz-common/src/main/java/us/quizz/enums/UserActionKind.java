package us.quizz.enums;

/** Types of user actions */
public enum UserActionKind {
  QUESTION_SHOWN,  // User is given a question to answer.
  ANSWER_SENT,  // User submits an answer.
  ANSWER_SKIPPED,  // User skips a question.
  EXPAND_QUESTION_CONTEXT,  // User presses the button to expand the question context.
  HIDE_QUESTION_CONTEXT  // User presses the button to hide the question context.
}
