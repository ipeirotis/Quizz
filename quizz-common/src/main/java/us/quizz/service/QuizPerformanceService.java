package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.QuizPerformance;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.QuizPerformanceRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuizPerformanceService extends OfyBaseService<QuizPerformance> {
  @Inject
  public QuizPerformanceService(QuizPerformanceRepository quizPerformanceRepository){
    super(quizPerformanceRepository);
  }

  public QuizPerformance get(String quizid, String userid) {
    return ((QuizPerformanceRepository) baseRepository).get(quizid, userid);
  }

  public void delete(String quizid, String userid) {
    delete(QuizPerformance.generateId(quizid, userid));
  }

  public List<QuizPerformance> getQuizPerformancesByQuiz(String quizid) {
    Map<String, Object> params = new HashMap<String, Object>();
    if (quizid != null) {
      params.put("quiz", quizid);
    }
    return listAll(params);
  }

  public List<QuizPerformance> getQuizPerformancesByUser(String userid) {
    Map<String, Object> params = new HashMap<String, Object>();
    if (userid != null) {
      params.put("userid", userid);
    }
    return listAll(params);
  }

  // Calculates the number of users that have at least "a" correct answers and "b" incorrect
  // answers for a given quiz (or calculate the stats across all quizzes if quizID == null).
  // Returns a map from (# correct -> (# incorrect -> count)).
  public Map<Integer, Map<Integer, Integer>> getCountsForSurvivalProbability(String quizID) {
    List<QuizPerformance> list = getQuizPerformancesByQuiz(quizID);
    // # correct -> (# incorrect -> count)
    Map<Integer, Map<Integer, Integer>> result = new HashMap<Integer, Map<Integer, Integer>>();

    for (QuizPerformance quizPerformance : list) {
      Integer correct = quizPerformance.getCorrectanswers();
      Integer incorrect = quizPerformance.getIncorrectanswers();
      if (correct == null || incorrect == null) continue;
      increaseCounts(result, correct, incorrect);
    }
    return result;
  }

  // Increments the count in result for all pairs of [0, correct] and [0, incorrect].
  // Params:
  //   result: (# correct -> (# incorrect -> count)).
  private void increaseCounts(Map<Integer, Map<Integer, Integer>> result,
      Integer correct, Integer incorrect) {
    for (int a = 0; a <= correct; ++a)  {
      Map<Integer, Integer> cntA = result.get(a);
      if (cntA == null) {
        cntA = new HashMap<Integer, Integer>();
        result.put(a, cntA);
      }

      for (int b = 0; b <= incorrect; ++b)  {
        Integer cntAB = cntA.get(b);
        if (cntAB == null) {
          cntAB = 0;
        }
        cntA.put(b, cntAB + 1);
      }
      result.put(a, cntA);
    }
  }

  // Returns the sum of quiz performance score for the set of ids given.
  public double getScoreSumByIds(Set<String> ids) {
    if (ids.size() == 0) {
      return 0d;
    }
    double result = 0d;
    List<String> list = new ArrayList<String>(ids);
    for (int i = 0; i < list.size(); i += 1000) {
      List<String> sublist = list.subList(i, Math.min(i + 1000, list.size()));
      List<QuizPerformance> results = listByStringIds(sublist);
      for (QuizPerformance qp : results) {
        result += qp.getScore();
      }
    }
    return result;
  }
}
