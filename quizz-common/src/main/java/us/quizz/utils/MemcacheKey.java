package us.quizz.utils;

// MemcacheKey organizes the construction of all the keys that we use for the Memcache system.
public final class MemcacheKey {
  // Returns a key to a UserAnswer entity for the given questionID and userID.
  public static String getUserAnswer(String questionID, String userID) {
    return "useranswer_" + questionID + userID;
  }

  // Returns a key to a UserAnswerFeedback entity for the given questionID and userID.
  public static String getUserAnswerFeedback(Long questionID, String userID) {
    return "useranswerfeedback_" + questionID + userID;
  }

  // Returns a key to a map of quiz questions cached for the particular quizID.
  // The value of the cache is a Map<String, Set<Question>> where the keys of the Map
  // are "gold" and "silver", corresponding to n gold questions and n silver questions.
  public static String getQuizQuestionsByQuiz(String quizID, int n) {
    return "quizquestions_" + quizID + n;
  }

  // Returns a key to a list of all Question entities for the given quizID.
  public static String getAllQuizQuestionsByQuiz(String quizID) {
    return "quizquestions_all_" + quizID;
  }

  // Returns a key to a list of all gold Question entities for the given quizID.
  public static String getGoldQuizQuestionsByQuiz(String quizID) {
    return "quizquestions_gold_" + quizID;
  }

  // Returns a key to a list of Quiz entities in the datastore.
  public static String getQuizzesList() {
    return "listquizzes";
  }

  // Returns a key to the number of user answers available for the quizID.
  public static String getNumUserAnswers(String quizID) {
    return "numquizuseranswers_" + quizID;
  }

  // Returns a key to the number of quiz questions available for the quizID.
  public static String getNumQuizQuestions(String quizID) {
    return "numquizquestions_" + quizID;
  }

  // Returns a key to the number of gold questions available for the quizID.
  public static String getNumGoldQuestions(String quizID) {
    return "numquizgoldquestions_" + quizID;
  }

  // Returns a key to a QuizPerformance entity for the given userID in the given quizID quiz.
  public static String getQuizPerformanceByUser(String quizID, String userID) {
    return "quizperformance_" + quizID + "_" + userID;
  }

  // Returns a key to a Map<Integer, Map<Integer, Integer>> containing survival probabilities.
  // The Map is of the form of num_correct_answers -> (num_questions_left -> number_such_users).
  public static String getSurvivalProbabilities(String quiz) {
    if (quiz == null) return "survivalprobability";
    else return "survivalprobability_" + quiz;
  }
}
