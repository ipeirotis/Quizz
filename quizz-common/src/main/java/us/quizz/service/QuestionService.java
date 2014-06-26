package us.quizz.service;

import com.google.inject.Inject;
import com.googlecode.objectify.cmd.Query;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuestionSelectionStrategy;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.QuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.utils.CachePMF;
import us.quizz.utils.LevenshteinAlgorithm;
import us.quizz.utils.MemcacheKey;
import us.quizz.utils.QuestionSelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class QuestionService extends OfyBaseService<Question> {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(QuestionService.class.getName());

  // Keys used to annotate the question results in the map returned by getNextQuizQuestions.
  public static final String CALIBRATION_KEY = "calibration";
  public static final String COLLECTION_KEY = "collection";

  // Multiplier used to fetch more questions than being asked when choosing the next questions
  // to allow us to fetch enough candidate questions for each user.
  private static final int QUESTION_FETCHING_MULTIPLIER = 3;

  private UserAnswerRepository userAnswerRepository;

  private QuizRepository quizRepository;

  private UserService userService;

  @Inject
  public QuestionService(QuestionRepository questionRepository, 
      UserAnswerRepository userAnswerRepository,
      QuizRepository quizRepository,
      UserService userService) {
    super(questionRepository);
    this.userAnswerRepository = userAnswerRepository;
    this.quizRepository = quizRepository;
    this.userService = userService;
  }

  public List<Question> getQuizQuestions(String quizid) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", quizid);
    return listAll(params);
  }

  /**
   * Given a list of questionIDs, fetches the corresponding questions and extracts and returns
   * the client ids of those questions.
   *
   * @param questionIDs a list of questionIDs
   */
  protected Set<String> getQuestionClientIDs(Set<Long> questionIDs) {
    // Pre-empt fast because listByIds doesn't work for empty container.
    if (questionIDs.isEmpty()) {
      return new HashSet<String>();
    }

    List<Question> questions = listByIds(questionIDs);
    Set<String> questionClientIDs = new HashSet<String>();
    for (Question question : questions) {
      if (question.getClientID() != null && !question.getClientID().isEmpty()) {
        questionClientIDs.add(question.getClientID());
      }
    }
    return questionClientIDs;
  }

  /**
   * Returns the next numQuestions calibration and collection questions in the given quizID
   * for a given userID.
   * Here, we prioritizes questions that userID has never answered before (not the same
   * questionID and not the same clientID) and have the least questions' totalUserScore.
   * If there are not enough questions to fulfill the numQuestions questions desired, we will
   * reask answered questions for collection questions, but won't reask for calibration
   * questions. This is due to the assumption that if we repeat asking collection question,
   * we will gain extra information bit, but we won't get extra information bit from asking
   * calibration questions.
   *
   * @return The map result returned will has two values, mapping from:
   *         CALIBRATION_KEY -> set of calibration questions.
   *         COLLECTION_KEY -> set of collection questions.

   * @param quizID identifier for quiz from which to get questions
   * @param numQuestions the number of questions to try and return
   * @param userID identifier for the user for whom these questions are intended
   */
  public Map<String, Set<Question>> getNextQuizQuestions(
      String quizID, int numQuestions, String userID) {
    // First, we try to get a list of questions that the user has answered before for this quiz.
    List<UserAnswer> userAnswers = userAnswerRepository.getUserAnswers(quizID, userID);
    Set<Long> questionIDs = new HashSet<Long>();
    for (UserAnswer userAnswer : userAnswers) {
      questionIDs.add(userAnswer.getQuestionID());
    }
    Set<String> questionClientIDs = getQuestionClientIDs(questionIDs);

    // Some quizzes use a question selection strategy and others do not.
    // If this quiz uses such a strategy, select questions according to the strategy.
    // Otherwise, use the default strategy;
    // TODO(kobren): refactor this if statement, it's a bit ugly.
    Quiz quiz = quizRepository.get(quizID);
    Map<String, Set<Question>> result = new HashMap<String, Set<Question>>();
    // We do not yet use question selection strategies.
    if (quiz.getUseQuestionSelectionStrategy() == null || !quiz.getUseQuestionSelectionStrategy()) {
      result.put(CALIBRATION_KEY,
          new HashSet<Question>(getSomeQuizQuestionsWithCriteria(
              // TODO(kobren): refactor magic strings.
              quizID, questionIDs, questionClientIDs, numQuestions, "hasGoldAnswer", false)));
      result.put(COLLECTION_KEY,
          new HashSet<Question>(getSomeQuizQuestionsWithCriteria(
              // TODO(kobren): refactor magic strings.
              quizID, questionIDs, questionClientIDs, numQuestions, "hasSilverAnswers", true)));
      return result;
    }

    // If this quiz allows for the use of question selection, use it here.
    Map<String, Object> calibrationParams = new HashMap<String, Object>();
    // TODO(kobren): refactor magic strings.
    calibrationParams.put("quizID", quizID);
    calibrationParams.put("hasGoldAnswer", Boolean.TRUE);

    Map<String, Object> collectionParams = new HashMap<String, Object>();
    // TODO(kobren): refactor magic strings.
    collectionParams.put("quizID", quizID);
    collectionParams.put("hasSilverAnswers", Boolean.TRUE);

    List<Question> calibrationQuestions = listAll(calibrationParams);
    List<Question> collectionQuestions  = listAll(collectionParams);

    // Then, we pick calibration and collection questions from datastore and filter the questions
    // previously asked from the results.  Questions are chosen via a QuestionSelector (which
    // selects questions based on a randomly selected strategy).
    QuestionSelector calibrationSelector = new QuestionSelector(calibrationQuestions);
    QuestionSelector collectionSelector  = new QuestionSelector(collectionQuestions);

    // TODO(kobren): find a way to pick a better parameter
    User user = userService.get(userID);
    // TODO(kobren): think about assigning a question selection strategy upon user creation
    QuestionSelectionStrategy strategy = user.pickQuestionSelectionStrategy();
    userService.asyncSave(user);

    int numBins = 10;
    result.put(CALIBRATION_KEY,
        // TODO(kobren): eventually we want to pick this strategy in a smart way
        new HashSet<Question>(calibrationSelector.questionsByStrategy(
            strategy, questionIDs, questionClientIDs, numQuestions, false, numBins)));
    result.put(COLLECTION_KEY,
        // TODO(kobren): eventually we want to pick this strategy in a smart way
        new HashSet<Question>(collectionSelector.questionsByStrategy(
            strategy, questionIDs, questionClientIDs, numQuestions, true, numBins)));
    return result;
  }

  /**
   * Returns numQuestions of questions given the query criteria without repeating the questions
   * in the questionIDs set or question with client id in the questionClientIds set
   *
   * @param numQuestions number of questions attempted to be picked.
   * @param questionIDs list of question ids that had been answered by the users.
   * @param questionClientIDs list of client ids of the questions that have been answered by the
   *                          user OR that will be shown to the user. Thus, here, we modify it to
   *                          include those client ids of the questions chosen in this function.
   * @param repeatQuestions whether to repeat question already asked if not enough candidate
   *                        questions to reach the numQuestions questions desired.
   * TODO(chunhowt): calibration and collection questions could have same clientID. If only few
   *                 calibration questions, some collection questions could never be asked
   */
  private List<Question> getSomeQuizQuestionsWithCriteria(
      String quizID, Set<Long> questionIDs, Set<String> questionClientIDs,
      int numQuestions, String criteria, boolean repeatQuestions) {
    Query<Question> query =
        baseRepository
            // We try to fetch extra questions proportional to # questions answered to ensure
            // that we have questions to ask after filtering away answered questions.
            .query(numQuestions + questionIDs.size() * QUESTION_FETCHING_MULTIPLIER,
                "totalUserScore")
            .filter("quizID", quizID)
            .filter(criteria, Boolean.TRUE);

    List<Question> questions = baseRepository.listAllByChunkForQuery(query);
    return validQuestionSetForQuizz(
        questions, numQuestions, questionIDs, questionClientIDs, repeatQuestions);
  }

  /**
   * Returns a valid subset of potentialQuestions. A valid subset may not repeat questions or
   * contain any questions with client id in questionClientIds set.  This method is static because
   * it is used by the question selector.
   * TODO(kobren): think about putting this method elsewhere
   *
   * @param potentialQuestions list of questions from which the a valid subset will be chosen.
   * @param numQuestions number of questions attempted to be picked.
   * @param questionIDs list of question ids that had been answered by the users.
   * @param questionClientIDs list of client ids of the questions that have been answered by the
   *                          user OR that will be shown to the user. Thus, here, we modify it to
   *                          include those client ids of the questions chosen in this function.
   * TODO(chunhowt): calibration and collection questions could have same clientID. If only few
   *                 calibration questions, some collection questions could never be asked
   * @param repeatQuestions whether to repeat question already asked if not enough candidate
   *                        questions to reach the numQuestions questions desired.
   */
  public static List<Question> validQuestionSetForQuizz(
      List<Question> potentialQuestions, int numQuestions,
      Set<Long> questionIDs, Set<String> questionClientIDs, boolean repeatQuestions) {
    List<Question> kept = new ArrayList<Question>();
    List<Question> discarded = new ArrayList<Question>();
    for (Question question : potentialQuestions) {
      // Questions previously asked are skipped.
      if (questionIDs.contains(question.getId())) {
        discarded.add(question);
        continue;
      }
      if (question.getClientID() == null ||
          question.getClientID().isEmpty() ||
          !questionClientIDs.contains(question.getClientID())) {
        kept.add(question);
        // Now that we selected this question, we need to blacklist the client id for the question.
        if (question.getClientID() != null && !question.getClientID().isEmpty()) {
          questionClientIDs.add(question.getClientID());
        }
        if (kept.size() == numQuestions) {
          break;
        }
      } else {
        discarded.add(question);
      }
    }

    // However, some quizz might be too small and a user has answered all its questions, here
    // we will just randomly choose some questions users answered before.
    // TODO(chunhowt): Instead of doing this, we can send a message to user to redirect him
    // to another quizz.
    if (kept.size() < numQuestions && repeatQuestions) {
      int numToChoose = Math.min(numQuestions - kept.size(), discarded.size());
      kept.addAll(discarded.subList(0, numToChoose));
    }
    return kept;
  }

  public Integer getNumberOfGoldQuestions(String quizID, boolean useCache) {
    String key = MemcacheKey.getNumGoldQuestions(quizID);
    if (useCache) {
      Integer result = CachePMF.get(key, Integer.class);
      if (result != null)
        return result;
    }

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", quizID); 
    params.put("hasGoldAnswer", Boolean.TRUE);

    Integer count = baseRepository.countByProperties(params);
    CachePMF.put(key, count);
    return count;
  }

  public Integer getNumberOfQuizQuestions(String quizID, boolean useCache) {
    String key = MemcacheKey.getNumQuizQuestions(quizID);
    if (useCache) {
      Integer result = CachePMF.get(key, Integer.class);
      if (result != null)
        return result;
    }

    Integer count = baseRepository.countByProperty("quizID", quizID);
    CachePMF.put(key, count);
    return count;
  }

  // There is no bestAnswer (this question was never answered before) if bestAnswer is null
  // or probability is negative.
  private String constructCollectionFeedback(
      Answer bestAnswer, double probability, boolean isCorrect, Integer answerID) {
    // There is no best answer, the user is the first user.
    if (bestAnswer == null) {
      if (answerID == -1) {
        return "We are not 100% sure about the correct answer either and you are the " +
            "first user to see this question!";
      } else {
        return "Great! We are not 100% sure about the correct answer and you are the " +
            "first user to answer!";
      }
    }

    String feedback = "";
    long roundedProbability = Math.round(probability * 100);

    if (answerID != -1) {
      // If the user is correct, gives encouraging message.
      feedback += isCorrect ? "Great! " : "Sorry! ";
    } else {
      // If the user skips, he will learn something new today.
      feedback += "Learn something new today! ";
    }

    feedback += "We are not 100% sure about the correct answer " +
        "but we believe " + bestAnswer.getText() + " to be correct and " +
        roundedProbability + "% of the users agree.";
    return feedback;
  }

  /**
   * Checks whether the answer given is the best answer for the given question and returns
   * the Result.
   * The answer given is either the answerID if it is a multiple choice question or the
   * userInput if it is a free text question.
   *
   * @param question a question whose answer will be verified.
   * @param answerID the id of the answer.
   * @param userInput the user answer in the case of a free text question.
   * @return
   */
  public Result verifyAnswer(Question question, Integer answerID, String userInput) {
    Answer bestAnswer = null;
    Boolean isCorrect = false;
    String message = "";

    switch (question.getKind()) {
      case MULTIPLE_CHOICE_CALIBRATION:
        for (Answer answer : question.getAnswers()) {
          if (answer.getKind() == AnswerKind.GOLD) {
            bestAnswer = answer;
            break;
          }
        }
        if (answerID == -1) {
          // User skips.
          isCorrect = false;
          message = "Learn something new today! The correct answer is " + bestAnswer.getText();
        } else if (bestAnswer.getInternalID() == answerID) {
          isCorrect = true;
          message = "Great! The correct answer is " + bestAnswer.getText();
        } else {
          isCorrect = false;
          message = "Sorry! The correct answer is " + bestAnswer.getText();
        }
        break;
      case MULTIPLE_CHOICE_COLLECTION:
        double maxProbability = -1;
        for (Answer answer : question.getAnswers()) {
          // Skip this answer, if it is never picked.
          if (answer.getNumberOfPicks() == null || answer.getNumberOfPicks() == 0) {
            continue;
          }
          Double prob = answer.getProbCorrect();
          if (prob == null) prob = 0.0;
          if (prob > maxProbability) {
            maxProbability = prob;
            bestAnswer = answer;
          }
        }

        // then the answer is correct. Else it is not.
        isCorrect = answerID != -1 &&
            (bestAnswer == null || bestAnswer.getInternalID() == answerID);
        message = constructCollectionFeedback(
            bestAnswer, maxProbability, isCorrect, answerID);
        break;
      case FREETEXT_CALIBRATION:
      case FREETEXT_COLLECTION:
        
        //TODO(panos): Need to handle "Skip"
        
        Result r;
        
        // Check if submitted answer matches a gold or silver answer
        r = checkFreeTextAgainstGoldSilver(question, userInput);
        if (r!=null) return r;
        
        // If it does not match a gold/silver, then check if it matches a gold/silver with a typo
        r = checkFreeTextAgainstGoldSilverWithTypos(question, userInput);
        if (r!=null) return r;
        
        // If it does not match a gold/silver (without a typo), check if it matches 
        // exactly answers submitted by other users 
        r = checkFreeTextAgainstUserAnswers(question, userInput);
        if (r!=null) return r;
        
        // Check if the answer submitted by the user matches an answer submitted by other users,
        // even with a typo
        r = checkFreeTextAgainstUserAnswersWithTypos(question, userInput);
        if (r!=null) return r;
        
        // At this point, it seems that the user answer does not match gold 
        // or any other user answer (even with a typo)
        r = generateFreeTextIncorrectResponse(question, userInput);
        return r;

      default:
        break;
    }

    return new Result(bestAnswer, isCorrect, message);
  }

  private Result generateFreeTextIncorrectResponse(Question question, String userInput) {
    Boolean isCorrect = false;
    Answer bestAnswer = null;
    for (Answer ans : question.getAnswers()) {
      AnswerKind ak = ans.getKind();
      if (ak == AnswerKind.GOLD) {
        bestAnswer = ans;
        break;
      }
    }
    String message = "Sorry! The correct answer is " + bestAnswer.getText();
    return new Result(bestAnswer, isCorrect, message);
  }
  
  
  private Result checkFreeTextAgainstUserAnswersWithTypos(Question question, String userInput) {
    Boolean isCorrect;
    String message;
    for (Answer ans : question.getAnswers()) {
      AnswerKind ak = ans.getKind();
      if (ak == AnswerKind.USER_SUBMITTED) {
        if (LevenshteinAlgorithm.getLevenshteinDistance(userInput, ans.getText()) <= 1) {
          isCorrect = true;
          message = "We did not know about this one, but other users submitted almost the same answer, so we will count it as correct.";
          return new Result(ans, isCorrect, message);
        }
      } 
    }
    return null;
  }

  private Result checkFreeTextAgainstUserAnswers(Question question, String userInput) {
    Boolean isCorrect;
    String message;
    for (Answer ans : question.getAnswers()) {
      AnswerKind ak = ans.getKind();
      if (ak == AnswerKind.USER_SUBMITTED) {
        if (ans.getText().equalsIgnoreCase(userInput)) {
          isCorrect = true;
          message = "We did not know about this one, but other users submitted the same answer, so we will count it as correct.";
          return new Result(ans, isCorrect, message);
        }
      } 
    }
    return null;
  }

  private Result checkFreeTextAgainstGoldSilverWithTypos(Question question, String userInput) {
    Boolean isCorrect;
    String message;
    QuestionKind qk = question.getKind();
    for (Answer ans : question.getAnswers()) {
      AnswerKind ak = ans.getKind();
      if ((qk == QuestionKind.FREETEXT_CALIBRATION && ak == AnswerKind.GOLD)
          || (qk == QuestionKind.FREETEXT_COLLECTION && ak == AnswerKind.SILVER)) {
        if (LevenshteinAlgorithm.getLevenshteinDistance(userInput, ans.getText()) <= 1) {
          isCorrect = true;
          message = "Nice! Be careful of typos next time. The correct answer is " + ans.getText();
          return new Result(ans, isCorrect, message);
        }
      }
    }
    return null;
  }

  private Result checkFreeTextAgainstGoldSilver(Question question, String userInput) {
    Boolean isCorrect;
    String message;
    QuestionKind qk = question.getKind();
    for (Answer ans : question.getAnswers()) {
      AnswerKind ak = ans.getKind();
      if ((qk == QuestionKind.FREETEXT_CALIBRATION && ak == AnswerKind.GOLD)
          || (qk == QuestionKind.FREETEXT_COLLECTION && ak == AnswerKind.SILVER)) {
       if (ans.getText().equalsIgnoreCase(userInput)) {
          isCorrect = true;
          message = "Great! The correct answer is " + ans.getText();
          return new Result(ans, isCorrect, message);
        }
      }
    }
    return null;
    
  }

  public class Result {
    // The best answer for the question asked.
    private Answer bestAnswer;
    // Whether the user's answer is correct for the question asked.
    private Boolean isCorrect;
    // The feedback message to be given to the user.
    private String message;

    public Result(Answer bestAnswer, Boolean isCorrect, String message) {
      this.bestAnswer = bestAnswer;
      this.isCorrect = isCorrect;
      this.message = message;
    }

    public Answer getBestAnswer() {
      return bestAnswer;
    }

    public Boolean getIsCorrect() {
      return isCorrect;
    }

    public String getMessage() {
      return message;
    }
  }
}
