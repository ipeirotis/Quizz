package us.quizz.utils;

// MemcacheKey organizes the construction of all the keys that we use for the Memcache system.
public final class MemcacheKey {
  // Returns a key to the number of quiz questions available for the quizID.
  public static String getNumQuizQuestions(String quizID) {
    return "numquizquestions_" + quizID;
  }

  // Returns a key to the number of gold questions available for the quizID.
  public static String getNumGoldQuestions(String quizID) {
    return "numquizgoldquestions_" + quizID;
  }

  // Returns a key to a Map<Integer, Map<Integer, Integer>> containing survival probabilities.
  // The Map is of the form of num_correct_answers -> (num_incorrect_answers -> number_users).
  public static String getSurvivalProbabilities(String quiz) {
    if (quiz == null) return "survivalprobability";
    else return "survivalprobability_" + quiz;
  }

  // Returns a key for saving the explore exploit action.
  public static String getExploreExploitAction(int a, int b, int c, int N) {
    return "exploreExploit_" + a + "_" + b + "_" +c + "_" +  N;
  }
}
