package us.quizz.enums;

public enum UserActionKind {
  QUESTION_SHOWN, // User is given a question to answer
  ANSWER_SENT, // User submits an answer
  SKIP, // User pressed Skip
  ANSWER1_HELP, // User pressed the help button for an answer
  QUESTION_HELP, // User pressed the help button for a question
}
