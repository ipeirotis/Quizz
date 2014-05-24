package us.quizz.service;

import com.google.inject.Inject;

import com.googlecode.objectify.cmd.Query;

import us.quizz.entities.Question;
import us.quizz.entities.UserAnswer;
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

public class QuestionService {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(QuestionService.class.getName());
  
  // Keys used to annotate the question results in the map returned by getNextQuizQuestions.
  public static final String CALIBRATION_KEY = "calibration";
  public static final String COLLECTION_KEY = "collection";

  // Multiplier used to fetch more questions than being asked when choosing the next questions
  // to allow us to fetch enough candidate questions for each user.
  private static final int QUESTION_FETCHING_MULTIPLIER = 3;

  private QuestionRepository questionRepository;
  private UserAnswerRepository userAnswerRepository;

  @Inject
  public QuestionService(QuestionRepository questionRepository, 
      UserAnswerRepository userAnswerRepository){
    this.questionRepository = questionRepository;
    this.userAnswerRepository = userAnswerRepository;
  }

  public Question get(Long questionId) {
    return questionRepository.get(questionId);
  }

  public List<Question> listAll() {
    return questionRepository.listAllByCursor();
  }

  private List<Question> listAll(Map<String, Object> params) {
    return questionRepository.listAllByCursor(params);
  }

  public Question save(Question question){
    return questionRepository.saveAndGet(question);
  }

  public void delete(Long questionId){
    questionRepository.delete(questionId);
  }

  public void deleteAll(List<Question> questions) {
    questionRepository.delete(questions);
  }

  public List<Question> listByIds(Iterable<Long> questionIDs) {
    return questionRepository.listByIds(questionIDs);
  }

  public List<Question> getQuizQuestions(String quizid) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", quizid);
    return questionRepository.listAllByCursor(params);
  }

  private Set<String> getQuestionClientIDs(Set<Long> questionIDs) {
    // Pre-empt fast because listByIds doesn't work for empty container.
    if (questionIDs.isEmpty()) {
      return new HashSet<String>();
    }

    List<Question> questions = listByIds(questionIDs);
    Set<String> questionClientIDs = new HashSet<String>();
    for (Question question : questions) {
      if (question.getClientID() != null) {
        questionClientIDs.add(question.getClientID());
      }
    }
    return questionClientIDs;
  }

  // Returns the next n calibration and collection questions in the given quizID for a given userID.
  // The map result will has two values, mapping from:
  // - CALIBRATION_KEY -> set of calibration questions.
  // - COLLECTION_KEY -> set of collection questions.
  public Map<String, Set<Question>> getNextQuizQuestions(String quizID, int n, String userID) {
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
    List<Question> goldQuestions =
        getSomeQuizQuestionsWithGold(quizID, n, questionIDs, questionClientIDs);
    result.put(CALIBRATION_KEY, new HashSet<Question>(goldQuestions));

    List<Question> silverQuestions =
        getSomeQuizQuestionsWithSilver(quizID, n, questionIDs, questionClientIDs);
    result.put(COLLECTION_KEY, new HashSet<Question>(silverQuestions));
    return result;
  }

  // Returns a list of calibration questions that is at least size min_amount
  // if feasible (eg. enough candidate questions).
  private List<Question> getSomeQuizQuestionsWithGold(
      String quizID, int min_amount, Set<Long> questionIDs, Set<String> questionClientIDs) {
    Query<Question> query =
        questionRepository
        .query(min_amount + questionIDs.size() * QUESTION_FETCHING_MULTIPLIER, "totalUserScore")
        .filter("quizID", quizID)
        .filter("hasGoldAnswer", Boolean.TRUE);

    List<Question> result = questionRepository.listAllByChunkForQuery(query);
    return pickQuestionsAsked(result, questionIDs, questionClientIDs, min_amount,
        false  /* don't repeat if not enough questions. */);
  }

  // Returns a list of collection questions that is at least size min_amount
  // if feasible (eg. enough candidate questions).
  private List<Question> getSomeQuizQuestionsWithSilver(
      String quizId, int min_amount, Set<Long> questionIDs, Set<String> questionClientIDs) {
    Query<Question> query =
        questionRepository
        .query(min_amount + questionIDs.size() * QUESTION_FETCHING_MULTIPLIER, "totalUserScore")
        .filter("quizID", quizId)
        .filter("hasSilverAnswers", Boolean.TRUE);

    List<Question> result = questionRepository.listAllByChunkForQuery(query);
    return pickQuestionsAsked(result, questionIDs, questionClientIDs, min_amount,
        true  /* repeat if not enough questions. */);
  }

  // Filters questions to prefer choosing n questions that are not contained in the set.
  // If repeatQuestions is true, allows duplicate questions when there is not enough candidate
  // questions that have never been answered before.
  // Params:
  //   questions: Candidate questions to choose from.
  //   questionIDs: List of questions ids that had been answered by the users.
  //   questionClientIDs: Contains client ids of the questions that had been answered by the user
  //                      or that will be shown to the user. Thus, here, we modify it to
  //                      include those client ids chosen in this method.
  //   numQuestions: Number of questions attempted to be picked.
  //   repeatQuestions: Repeat question already asked if not enough candidate questions to reach
  //                    the numQuestions questions desired.
  // TODO(chunhowt): This is essentially an attempt to do a join query of Question and
  // UserAnswer entities but it seems like datastore doesn't allow such a join query. This
  // works for now, but there should be a better solution.
  private List<Question> pickQuestionsAsked(
      List<Question> questions, Set<Long> questionIDs, Set<String> questionClientIDs,
      int numQuestions, boolean repeatQuestions) {
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

    Integer count = questionRepository.countByProperties(params);
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

    Integer count = questionRepository.countByProperty("quizID", quizID);
    CachePMF.put(key, count);
    return count;
  }
}
