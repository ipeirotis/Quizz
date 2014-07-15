package us.quizz.enums;

/** Types of user actions */
public enum UserActionKind {
  QUESTION_SHOWN, // User is given a question to answer
  ANSWER_SENT, // User submits an answer
  SKIP, // User pressed Skip
  //TODO(kobren, ipeirotis): need to record which help button was clicked
  ANSWER_HELP, // User pressed the help button for an answer
  QUESTION_HELP, // User pressed the help button for a question
}