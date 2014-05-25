package us.quizz.service;

import com.google.inject.Inject;

import com.googlecode.objectify.cmd.Query;

import us.quizz.entities.Question;
import us.quizz.entities.UserAnswer;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.QuestionRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.utils.CachePMF;
import us.quizz.utils.MemcacheKey;

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

  @Inject
  public QuestionService(QuestionRepository questionRepository, 
      UserAnswerRepository userAnswerRepository){
    super(questionRepository);
    this.userAnswerRepository = userAnswerRepository;
  }

  public List<Question> getQuizQuestions(String quizid) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", quizid);
    return listAll(params);
  }

  // Given a list of questionIDs, fetch the corresponding questions and extract and return the
  // client ids of those questions.
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

  // Returns the next numQuestions calibration and collection questions in the given quizID
  // for a given userID.
  // Here, we prioritizes questions that userID has never answered before (not the same
  // questionID and not the same clientID) and have the least questions' totalUserScore.
  // If there are not enough questions to fulfill the numQuestions questions desired, we will
  // reask answered questions for collection questions, but won't reask for calibration
  // questions. This is due to the assumption that if we repeat asking collection question,
  // we will gain extra information bit, but we won't get extra information bit from asking
  // calibration questions.
  // The map result returned will has two values, mapping from:
  //   - CALIBRATION_KEY -> set of calibration questions.
  //   - COLLECTION_KEY -> set of collection questions.
  public Map<String, Set<Question>> getNextQuizQuestions(
      String quizID, int numQuestions, String userID) {
    // First, we try to get a list of questions that the user has answered before for this quiz.
    List<UserAnswer> userAnswers = userAnswerRepository.getUserAnswers(quizID, userID);
    Set<Long> questionIDs = new HashSet<Long>();
    for (UserAnswer userAnswer : userAnswers) {
      questionIDs.add(userAnswer.getQuestionID());
    }
    Set<String> questionClientIDs = getQuestionClientIDs(questionIDs);

    // Then, we pick calibration and collection questions from datastore and filter the questions
    // previously asked from the results.
    Map<String, Set<Question>> result = new HashMap<String, Set<Question>>();
    result.put(CALIBRATION_KEY,
        new HashSet<Question>(getSomeQuizQuestionsWithCriteria(
            quizID, questionIDs, questionClientIDs, numQuestions, "hasGoldAnswer", false)));
    result.put(COLLECTION_KEY,
        new HashSet<Question>(getSomeQuizQuestionsWithCriteria(
            quizID, questionIDs, questionClientIDs, numQuestions, "hasSilverAnswers", true)));
    return result;
  }

  // Returns numQuestions of questions given the query criteria without repeating the questions
  // in the questionIDs set or question with client id in the questionClientIds set.
  // Params:
  //   quizID: Quiz id to choose candidate questions from.
  //   questionIDs: List of question ids that had been answered by the users.
  //   questionClientIDs: List of client ids of the questions that hat been answered by the user
  //                      OR that will be shown to the user. Thus, here, we modify it to
  //                      include those client ids of the questions chosen in this function.
  //   numQuestions: Number of questions attempted to be picked.
  //   repeatQuestions: Whether to repeat question already asked if not enough candidate questions
  //                    to reach the numQuestions questions desired.
  private List<Question> getSomeQuizQuestionsWithCriteria(
      String quizID, Set<Long> questionIDs, Set<String> questionClientIDs,
      int numQuestions, String criteria, boolean repeatQuestions) {
    Query<Question> query =
        baseRepository
        // We try to fetch extra questions proportional to # questions answered to ensure
        // that we have questions to ask after filtering away answered questions.
        .query(numQuestions + questionIDs.size() * QUESTION_FETCHING_MULTIPLIER, "totalUserScore")
        .filter("quizID", quizID)
        .filter(criteria, Boolean.TRUE);

    List<Question> questions = baseRepository.listAllByChunkForQuery(query);
    List<Question> kept = new ArrayList<Question>();
    List<Question> discarded = new ArrayList<Question>();
    for (Question question : questions) {
      // Question not asked before (not the same questionID and not the same clientID)
      // is selected first.
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
}
