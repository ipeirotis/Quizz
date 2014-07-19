package us.quizz.enums;

// The strategies here define how we aggregate votes from multiple users (with different
// accuracy) to determine the best answer for a given question.
// Specifically, given a list of users, each with different accuracy U_i, and each picks a
// user answer UA_i for the given question Q, we want to compute the probability that an
// answer is correct, i.e. P(A_j | U, UA, Q).
public enum AnswerAggregationStrategy {
  // Posterior probability using Bayes rules by assuming independent users.
  // So, the P(A_j | U) = PRODUCT(U_i | A_j).
  //   where user quality, U_i is one of the following:
  //     1. If user picks the answer, A_j being evaluted now:
  //        - Use the smoothed (laplacian) user accuracy.
  //     2. If user picks another answer rather than the current answer, A_j evaluated:
  //        - Use (1 - smoothed_accuracy) / (num_answers - 1).
  //          (i.e. assumes user picks the rest of the answer uniformly).
  NAIVE_BAYES,
  // Posterior probability by weighing each user vote by her smoothed laplacian user accuracy.
  WEIGHTED_VOTE,
  // Posterior probability by weighing each user equally.
  MAJORITY_VOTE
}
