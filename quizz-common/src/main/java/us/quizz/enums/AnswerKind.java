package us.quizz.enums;

public enum AnswerKind {
  GOLD,  // We are certain that this is a correct answer.
  INCORRECT,  // We are certain that this is an incorrect answer.
  SILVER,  // We are uncertain about this answer.
  USER_SUBMITTED,  // This is for the answers submitted by users as free text.
}
