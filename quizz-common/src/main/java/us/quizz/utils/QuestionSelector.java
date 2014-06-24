package us.quizz.utils;

// TODO(kobren): explain why binning the questions by prior difficulty is necessary
// TODO(kobren): question selection won't work for quizzes with both
//               calibration and collection questions (because of exploration/exploitation)

import us.quizz.entities.Question;
import us.quizz.enums.QuestionSelectionStrategy;
import us.quizz.service.QuestionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Select questions from a list of questions according to a particular
 * strategy. This is used to select questions for some Quizzes.
 */
public class QuestionSelector {
  // Stores the questions from the quiz specified in constructor.
  private List<Question> questions;

  // Stores a copy of the questions sorted in ascending order of prior difficulty for efficiency.
  // Store the copy (instead of a single list) in case we need other orders in the future.
  private List<Question> questionsByDifficultyPrior;

  public QuestionSelector(List<Question> questions) {
    this.questions = questions;
    this.questionsByDifficultyPrior = new ArrayList<>(questions);
    Collections.copy(this.questionsByDifficultyPrior, this.questions);
    Collections.sort(this.questionsByDifficultyPrior, new Comparator<Question>() {
      @Override
      public int compare(Question question1, Question question2) {
        return question1.getDifficultyPrior().compareTo(question2.getDifficultyPrior());
      }
    });
  }

  public List<Question> getQuestions() {
    return questions;
  }

  // TODO(kobren): think of binning methods that are more comparable when quizzes
  //               have very different distributions over question prior difficulty
  /**
   * Return a list of "bins" (each bin is a list of questions) where bins
   * contains questions of similar prior difficulty. This methods tries to keep
   * the number of questions per bin distributed relatively uniformly.
   *
   * @param numBins the number of bins to return.
   */
  protected List<List<Question>> binQuestionsQuasiUniformlyByDifficultyPrior(int numBins) {
    List<List<Question>> binnedQuestions = new ArrayList<List<Question>>();
    for (int i = 0; i < numBins; ++i) {
      binnedQuestions.add(new ArrayList<Question>());
    }

    // Bin the questions by prior difficulty. If two questions have the same prior difficulty
    // they should be placed in the same bin.
    int binsRemaining = numBins;
    int currentBinIdx = 0;
    int numQuestions = questionsByDifficultyPrior.size();
    int questionsRemaining = questionsByDifficultyPrior.size();
    int questionsInCurrentBin = 0;
    double previousDifficultyPrior = 0.0;
    for (int i = 0; i < numQuestions; ++i) {
      Question currentQuestion = questionsByDifficultyPrior.get(i);
      // We try to bin uniformly but if multiple questions have the same prior difficulty
      // (and must be binned together) or if the number of questions isn't a multiple
      // of the number of bins, we could have an unequal number of questions in each bin.
      // Therefore, before trying to insert a question, check how many questions have not been
      // binned yet and decide whether the questions should be placed in the current bin
      if (currentQuestion.getDifficultyPrior() != previousDifficultyPrior) {
        int questionsPerBin = new Double(
            Math.ceil(new Double(questionsRemaining + questionsInCurrentBin) / binsRemaining))
            .intValue();
        if (questionsInCurrentBin >= questionsPerBin) {
          ++currentBinIdx;
          --binsRemaining;
          questionsInCurrentBin = 0;
        }
        previousDifficultyPrior = currentQuestion.getDifficultyPrior();
      }
      binnedQuestions.get(currentBinIdx).add(currentQuestion);
      ++questionsInCurrentBin;
      --questionsRemaining;
    }

    return binnedQuestions;
  }

  /**
   * Returns a random set of questions taken from the first bin.
   * If there aren't enough questions in the first bin, take questions from the next, etc.
   *
   * @param questionBins ordered set of bins from which the questions are taken.
   * @param numQuestions the number of questions to return.
   * @param questionIDs list of question ids that had been answered by the users.
   * @param repeatQuestions whether to repeat question already asked if not enough candidate
   *                        questions to reach the numQuestions questions desired.
   */
  private List<Question> firstNBinnedQuestions(
      List<List<Question>> questionBins, int numQuestions, Set<Long> questionIDs,
      Set<String> questionClientIDs, boolean repeatQuestions) {
    List<Question> questions = new ArrayList<Question>();
    for (List<Question> questionBin : questionBins) {
      Collections.shuffle(questionBin);
      questions.addAll(questionBin);
    }

    return QuestionService.validQuestionSetForQuizz(
        questions, numQuestions, questionIDs, questionClientIDs, repeatQuestions);
  }

  /**
   * Returns the least difficult questions given the query criteria without repeating the questions
   * in the questionIDs set or question with client id in the questionClientIds set.
   *
   * @param questionIDs list of question ids that had been answered by the users.
   * @param questionClientIDs list of client ids of the questions that hat been answered by the user
   *                          OR that will be shown to the user. Thus, here, we modify it to
   *                          include those client ids of the questions chosen in this function.
   * @param numQuestions number of questions attempted to be picked.
   * @param repeatQuestions whether to repeat question already asked if not enough candidate
   *                        questions to reach the numQuestions questions desired.
   * @param numBins number of bins to use when binning questions by prior difficulty.
   */
  protected List<Question> getLeastDifficultQuestions(
      Set<Long> questionIDs, Set<String> questionClientIDs,
      int numQuestions, boolean repeatQuestions, int numBins) {
    List<List<Question>> questionBins = binQuestionsQuasiUniformlyByDifficultyPrior(numBins);
    return firstNBinnedQuestions(questionBins, numQuestions, questionIDs,
        questionClientIDs, repeatQuestions);
  }

  /**
   * Returns the most difficult questions given the query criteria without repeating the questions
   * in the questionIDs set or question with client id in the questionClientIds set.
   *
   * @param questionIDs list of question ids that had been answered by the users.
   * @param questionClientIDs list of client ids of the questions that hat been answered by the user
   *                          OR that will be shown to the user. Thus, here, we modify it to
   *                          include those client ids of the questions chosen in this function.
   * @param numQuestions number of questions attempted to be picked.
   * @param repeatQuestions Whether to repeat question already asked if not enough candidate
   *                        questions to reach the numQuestions questions desired.
   * @param numBins number of bins to use when binning questions by prior difficulty.
   */
  protected List<Question> getMostDifficultQuestions(
      Set<Long> questionIDs, Set<String> questionClientIDs,
      int numQuestions, boolean repeatQuestions, int numBins) {
    List<List<Question>> questionBins = binQuestionsQuasiUniformlyByDifficultyPrior(numBins);
    Collections.reverse(questionBins);
    return firstNBinnedQuestions(questionBins, numQuestions, questionIDs,
        questionClientIDs, repeatQuestions);
  }

  /**
   * Returns least difficult questions given the query criteria without repeating the questions
   * in the questionIDs set or question with client id in the questionClientIds set.
   *
   * @param questionIDs list of question ids that had been answered by the users.
   * @param questionClientIDs List of client ids of the questions that hat been answered by the user
   *                          OR that will be shown to the user. Thus, here, we modify it to
   *                          include those client ids of the questions chosen in this function.
   * @param numQuestions number of questions attempted to be picked.
   * @param repeatQuestions Whether to repeat question already asked if not enough candidate
   *                        questions to reach the numQuestions questions desired.
   */
  protected List<Question> getRandomQuestions(
      Set<Long> questionIDs, Set<String> questionClientIDs,
      int numQuestions, boolean repeatQuestions) {
    List<List<Question>> questionBins = new ArrayList<List<Question>>();
    questionBins.add(questions);
    return firstNBinnedQuestions(questionBins, numQuestions, questionIDs,
        questionClientIDs, repeatQuestions);
  }

  /**
   * Returns a set of questions based on the input strategy.
   *
   * @param strategy an enum representing the strategy used to select questions.
   * @param questionIDs list of question ids that had been answered by the users.
   * @param questionClientIDs List of client ids of the questions that hat been answered by the user
   *                          OR that will be shown to the user. Thus, here, we modify it to
   *                          include those client ids of the questions chosen in this function.
   * @param numQuestions number of questions attempted to be picked.
   * @param repeatQuestions Whether to repeat question already asked if not enough candidate
   *                        questions to reach the numQuestions questions desired.
   * @param numBins number of bins to use when binning questions by prior difficulty.
   * @return
   */
  public List<Question> questionsByStrategy(
      QuestionSelectionStrategy strategy, Set<Long> questionIDs,
      Set<String> questionClientIDs, int numQuestions,
      boolean repeatQuestions, int numBins) {
    Random rand = new Random();
    switch (strategy) {
      case LEAST_DIFFICULT:
        return getLeastDifficultQuestions(
            questionIDs, questionClientIDs, numQuestions, repeatQuestions, numBins);
      case MOST_DIFFICULT:
        return getMostDifficultQuestions(
          questionIDs, questionClientIDs, numQuestions, repeatQuestions, numBins);
      default:
        return getRandomQuestions(questionIDs, questionClientIDs, numQuestions, repeatQuestions);
    }
  }
}
