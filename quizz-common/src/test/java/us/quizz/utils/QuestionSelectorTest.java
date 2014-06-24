package us.quizz.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Question;
import us.quizz.enums.QuestionKind;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Test functionality of QuestionSelector class. */
@RunWith(JUnit4.class)
public class QuestionSelectorTest extends QuizBaseTest {
  private final int maxQuestions = 1000;
  private final int maxBins = 1000;
  private QuestionSelector randomizedSelector;
  private QuestionSelector nonRandomizedSelector;
  private String today = new SimpleDateFormat("yyyMMdd").format(new Date());
  private Random rand = new Random(Integer.parseInt(today));

  @Before
  public void setUp() {
    super.setUp();
    initNonRandomizedQuestionSelector();
    initRandomizedQuestionSelector();
  }

  /**
   * Initialize a list of 5 questions with the following difficulties:
   * [0.0, 0.5, 0.5, 0.75, 1.0]
   */
  private void initNonRandomizedQuestionSelector() {
    // initialize static selector
    List<Question> staticQuestions = new ArrayList<>();
    Question q1 = new Question(
        QUIZ_ID1, new Text(""), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, 1L,
        "", true, false, 1.5);
    q1.setDifficultyPrior(0.0);
    staticQuestions.add(q1);

    Question q2 = new Question(
        QUIZ_ID1, new Text(""), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, 2L,
        "", true, false, 1.5);
    q2.setDifficultyPrior(0.5);
    staticQuestions.add(q2);

    Question q3 = new Question(
        QUIZ_ID1, new Text(""), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, 3L,
        "", true, false, 1.5);
    q3.setDifficultyPrior(1.0);
    staticQuestions.add(q3);

    Question q4 = new Question(
        QUIZ_ID1, new Text(""), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, 4L,
        "", true, false, 1.5);
    q4.setDifficultyPrior(0.5);
    staticQuestions.add(q4);

    Question q5 = new Question(
        QUIZ_ID1, new Text(""), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, 5L,
        "", true, false, 1.5);
    q5.setDifficultyPrior(0.75);
    staticQuestions.add(q5);

    nonRandomizedSelector = new QuestionSelector(staticQuestions);
  }

  /**
   * Create a random set of questions for randomized questions
   */
  void initRandomizedQuestionSelector() {
    // initialized randomized selector (all questions with different difficulty)
    List<Question> questions = new ArrayList<>();
    int numTotalQuestions = rand.nextInt(maxQuestions);
    Set<Double> difficultiesAdded = new HashSet<Double>();
    for (int i = 0; i < numTotalQuestions; ++i) {
      Question question = new Question(
          QUIZ_ID1, new Text(""), QuestionKind.MULTIPLE_CHOICE_COLLECTION, (long) i,
          String.valueOf(i), true, false, 1.5);
      double randomDifficulty = rand.nextDouble();
      // Require that no two questions have the same difficulty so we can test
      // proper binning (see below: testRandomizedBinQuestionsQuasiUniformlyByDifficulty)
      if (!difficultiesAdded.contains(randomDifficulty)) {
        difficultiesAdded.add(randomDifficulty);
        question.setDifficultyPrior(randomDifficulty);
        questions.add(question);
      }
    }
    randomizedSelector = new QuestionSelector(questions);
  }

  /**
   * Take two lists of questions and return true if all of the questions in the first
   * list have difficulties smaller than those in the second list
   *
   * @param bin1 the first list of questions
   * @param bin2 the second list of questions
   */
  private boolean islessDifficultBin(List<Question> bin1, List<Question> bin2) {
    for (Question question1 : bin1) {
      for (Question question2 : bin2) {
        if (question1.getDifficultyPrior() >= question2.getDifficultyPrior()){
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Check whether each question in a bin is less difficult that each question in the next bin
   * for all bins
   *
   * @param questionBins a list of lists of questions.
   */
  private boolean satisfiesLessDifficultBinForConsecutiveBins(List<List<Question>> questionBins) {
    int numBins = questionBins.size();
    for (int i = 0; i < numBins - 1; ++i) {
      if (!islessDifficultBin(questionBins.get(i), questionBins.get(i + 1))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether all questions with the same difficulty are in the same bin
   *
   * @param questionBins a list of lists of questions.
   */
  private boolean hasAllQuestionsWithSameDifficultyInSameBucket(
      List<List<Question>> questionBins) {
    Map<Double, Integer> difficultyToBin = new HashMap<>();
    for (int i = 0; i < questionBins.size(); ++i) {
      for (Question question : questionBins.get(i)) {
        double difficulty = question.getDifficultyPrior();
        if (difficultyToBin.containsKey(difficulty) && difficultyToBin.get(difficulty) != i) {
          return false;
        } else {
          difficultyToBin.put(difficulty, i);
        }
      }
    }
    return true;
  }

  /** Checks that questions are binned as expected: (0.0, 0.5, 0.5) | (0.75) | (1.0) */
  @Test
  public void testBinQuestionsQuasiUniformlyByDifficultyPriorNonRandomized() {
    // Standard case.
    int numBins = 3;
    List<List<Question>> questionBins = nonRandomizedSelector
        .binQuestionsQuasiUniformlyByDifficultyPrior(numBins);

    assertEquals(3, questionBins.size());
    assertEquals(3, questionBins.get(0).size());
    assertEquals(1, questionBins.get(1).size());
    assertEquals(1, questionBins.get(2).size());

    // Invariant: all questions in bin n have difficulty less than questions in bin n + 1
    assertTrue(satisfiesLessDifficultBinForConsecutiveBins(questionBins));

    // Invariant: all questions with the same difficulty are in the same bin
    assertTrue(hasAllQuestionsWithSameDifficultyInSameBucket(questionBins));
  }

  /**
   * Check binning with more bins than questions. It should be:
   * (0.0) | (0.5, 0.5) | (0.75) | (1.0) | EMPTY | ...| EMPTY
   */
  @Test
  public void testBinQuestionsQuasiUniformlyByDifficultyPriorNonRandomizedManyBins() {
    // Edge case: more bins than questions.
    int numBins = 10;
    List<List<Question>> questionBins = nonRandomizedSelector
        .binQuestionsQuasiUniformlyByDifficultyPrior(numBins);

    assertEquals(10, questionBins.size());
    assertEquals(1, questionBins.get(0).size());
    assertEquals(2, questionBins.get(1).size());  // Questions with the same difficulty
    assertEquals(1, questionBins.get(2).size());
    assertEquals(1, questionBins.get(3).size());
    for (int i = 4; i < numBins; ++i) {
      assertEquals(0, questionBins.get(i).size());
    }

    // Invariant: all questions in bin n have difficulty less than questions in bin n + 1.
    assertTrue(satisfiesLessDifficultBinForConsecutiveBins(questionBins));

    // Invariant: all questions with the same difficulty are in the same bin.
    assertTrue(hasAllQuestionsWithSameDifficultyInSameBucket(questionBins));
  }

  /**
   * Check binning with only 1 bin.  Should be:
   * (0.0, 0.5, 0.5, 0.75, 1.0)
   */
  @Test
  public void testBinQuestionsQuasiUniformlyByDifficultyPriorNonRandomizedOneBin() {
    // Edge case: 1 bin.
    int numBins = 1;
    List<List<Question>> questionBins = nonRandomizedSelector
        .binQuestionsQuasiUniformlyByDifficultyPrior(numBins);

    assertEquals(1, questionBins.size());
    assertEquals(5, questionBins.get(0).size());

    // Invariant: all questions in bin n have difficulty less than questions in bin n + 1.
    assertTrue(satisfiesLessDifficultBinForConsecutiveBins(questionBins));

    // Invariant: all questions with the same difficulty are in the same bin.
    assertTrue(hasAllQuestionsWithSameDifficultyInSameBucket(questionBins));
  }

  /**
   * Check that bin sizes are uniformly distributed.  We can check this exactly
   * because we generated the random questions by requiring than none of them
   * have the same difficulty.
   */
  @Test
  public void testBinQuestionsQuasiUniformlyByDifficultyRandomized(){
    int numBins = rand.nextInt(maxBins);
    List<List<Question>> questionBins = randomizedSelector
        .binQuestionsQuasiUniformlyByDifficultyPrior(numBins);

    // Invariant: all questions in bin n have difficulty less than questions in bin n + 1.
    assertTrue(satisfiesLessDifficultBinForConsecutiveBins(questionBins));

    // Precondition: all questions have different difficulty (guaranteed above)
    // Invariant: all bins have same number of questions (or off by 1)
    int theoreticalQuestionsPerBin = new Double(
        Math.ceil(new Double(randomizedSelector.getQuestions().size()) / numBins)).intValue();
    for (List<Question> bin : questionBins) {
      assertTrue(bin.size() == theoreticalQuestionsPerBin
          || bin.size() == theoreticalQuestionsPerBin - 1);
    }
  }

  /**
   * Check to make sure that we can get the least difficult questions correctly
   * Since we know the difficulties and we set the numBins to be 3, we know that
   * the difficulties per bin (denoted by pipe) look like this:
   * (0.0, 0.5, 0.5) | (0.75) | (1.0)
   */
  @Test
  public void testGetLeastDifficultQuestionsNonRandomized(){
    int numBins = 3;
    int numQuestions = 3;
    System.out.println(nonRandomizedSelector.getQuestions().size());
    List<Question> leastDifficultQuestions = nonRandomizedSelector.getLeastDifficultQuestions(
        new HashSet<Long>(), new HashSet<String>(), numQuestions, false, numBins);

    assertEquals(3, leastDifficultQuestions.size());

    // Check to make sure that we got the question with 0.0 difficulty
    // and the two questions with 0.5 difficulty.
    double difficulty0 = leastDifficultQuestions.get(0).getDifficultyPrior();
    double difficulty1 = leastDifficultQuestions.get(1).getDifficultyPrior();
    double difficulty2 = leastDifficultQuestions.get(2).getDifficultyPrior();
    assertTrue(difficulty0 == 0.0 || difficulty0 == 0.5);
    assertTrue(difficulty1 == 0.0 || difficulty1 == 0.5);
    assertTrue(difficulty2 == 0.0 || difficulty2 == 0.5);
    assertEquals(1.0, difficulty0 + difficulty1 + difficulty2, 0.0);
  }

  /**
   * Ensure that if a question from bin n has been selected all questions
   * from all bins from [0, n-1] have also been selected for the
   * randomized set of questions.
   */
  @Test
  public void testGetLeastDifficultQuestionsRandomized(){
    int numBins = rand.nextInt(maxBins);
    int numQuestions = rand.nextInt(maxQuestions);
    List<List<Question>> questionBins = randomizedSelector
        .binQuestionsQuasiUniformlyByDifficultyPrior(numBins);
    List<Question> leastDifficultQuestions = randomizedSelector.getLeastDifficultQuestions(
        new HashSet<Long>(), new HashSet<String>(), numQuestions, false, numBins);

    // Invariant: no question from bin n appear unless all questions from bin n-1 appear
    Map<Long, Integer> questionIdToBin = new HashMap<>();
    for (int i = 0; i < questionBins.size(); i++) {
      for (Question question : questionBins.get(i)) {
        questionIdToBin.put(question.getId(), i);
      }
    }

    // Create a set of all of the IDs in our selected questions
    // Then, find the largest bin that any question comes from and make sure
    // that all questions from the easier bins are also included
    Set<Long> questionIDs = new HashSet<>();
    int mostDifficultBin = 0;
    for (Question question : leastDifficultQuestions) {
      questionIDs.add(question.getId());
      if (questionIdToBin.get(question.getId()) > mostDifficultBin) {
        mostDifficultBin = questionIdToBin.get(question.getId());
      }
    }

    for (int i = 0; i < mostDifficultBin; i++) {
      for (Question question : questionBins.get(i)) {
        assertTrue(questionIDs.contains(question.getId()));
      }
    }
  }

  /**
   * Check to make sure that we can get the most difficult questions correctly
   * Since we know the difficulties and we set the numBins to be 5, we know that
   * the difficulties per bin (denoted by pipe) look like this:
   * (0.0) | (0.5, 0.5) | (0.75) | (1.0)
   */
  @Test
  public void testGetMostDifficultQuestionsNonRandomized(){
    int numBins = 5;
    int numQuestions = 3;
    List<Question> mostDifficultQuestions = nonRandomizedSelector.getMostDifficultQuestions(
        new HashSet<Long>(), new HashSet<String>(), numQuestions, false, numBins);

    assertEquals(3, mostDifficultQuestions.size());

    // Check to make sure that we got the question with 1.0 difficulty
    // the questions with 0.75 difficulty and one of the questions with
    // 0.5 difficulty.
    double difficulty0 = mostDifficultQuestions.get(0).getDifficultyPrior();
    double difficulty1 = mostDifficultQuestions.get(1).getDifficultyPrior();
    double difficulty2 = mostDifficultQuestions.get(2).getDifficultyPrior();
    Set<Double> difficulties = new HashSet<>();
    difficulties.add(difficulty0);
    difficulties.add(difficulty1);
    difficulties.add(difficulty2);

    assertTrue(difficulties.contains(1.0));
    assertTrue(difficulties.contains(0.5));
    assertTrue(difficulties.contains(0.75));
  }

  /**
   * Checks that if a question from bin n is returned, all questions from
   * all bins [n-1, n_last] have been returned as well.
   */
  @Test
  public void testGetMostDifficultQuestionsRandomized(){
    int numBins = rand.nextInt(maxBins);
    int numQuestions = rand.nextInt(maxQuestions);
    List<List<Question>> questionBins = randomizedSelector
        .binQuestionsQuasiUniformlyByDifficultyPrior(numBins);
    List<Question> mostDifficultQuestions = randomizedSelector.getMostDifficultQuestions(
        new HashSet<Long>(), new HashSet<String>(), numQuestions, false, numBins);

    // Invariant: no question from bin n appears unless all questions from bin n+1 appear
    Map<Long, Integer> questionIdToBin = new HashMap<>();
    for (int i = 0; i < questionBins.size(); i++) {
      for (Question question : questionBins.get(i)) {
        questionIdToBin.put(question.getId(), i);
      }
    }

    // Create a set of all of the IDs in our selected questions
    // Then, find the largest bin that any question comes from and make sure
    // that all questions from the harder bins are also included.
    Set<Long> questionIDs = new HashSet<>();
    int leastDifficultBin = numBins;
    for (Question question : mostDifficultQuestions) {
      questionIDs.add(question.getId());
      if (questionIdToBin.get(question.getId()) < leastDifficultBin) {
        leastDifficultBin = questionIdToBin.get(question.getId());
      }
    }

    for (int i = numBins - 1; i > leastDifficultBin; --i) {
      for (Question question : questionBins.get(i)) {
        assertTrue(questionIDs.contains(question.getId()));
      }
    }
  }

  /**
   * Checks that the correct number fo questions are being returned
   */
  @Test
  public void testGetRandomQuestionsRandomized(){
    int numQuestions = rand.nextInt(maxQuestions);
    List<Question> randomQuestions = randomizedSelector.getRandomQuestions(
        new HashSet<Long>(), new HashSet<String>(), numQuestions, false);
    assertEquals(randomQuestions.size(),
        Math.min(numQuestions, randomizedSelector.getQuestions().size()));
  }
}
