package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerAggregationStrategy;
import us.quizz.enums.QuestionKind;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.utils.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuizPerformanceService extends OfyBaseService<QuizPerformance> {
  private UserAnswerService userAnswerService;
  private QuestionService questionService;

  @Inject
  public QuizPerformanceService(QuizPerformanceRepository quizPerformanceRepository,
      UserAnswerService userAnswerService, QuestionService questionService) {
    super(quizPerformanceRepository);
    this.userAnswerService = userAnswerService;
    this.questionService = questionService;
  }

  public QuizPerformance get(String quizid, String userid) {
    return ((QuizPerformanceRepository) baseRepository).get(quizid, userid);
  }

  public QuizPerformance getNoCache(String quizid, String userid) {
    return ((QuizPerformanceRepository) baseRepository).getNoCache(quizid, userid);
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

  // Updates the QuizPerformance statistics of the given userId in the given
  // quizId.
  // This includes the correctness statistics and user rank statistics.
  public void updateStatistics(String quizId, String userId) {
    QuizPerformance qp = new QuizPerformance(quizId, userId);

    List<UserAnswer> userAnswerList = userAnswerService.getUserAnswers(quizId, userId);
    // This is used to get a set of unique questions answered by user.
    List<Long> ids = new ArrayList<Long>();
    for (UserAnswer userAnswer : userAnswerList) {
      ids.add(userAnswer.getQuestionID());
    }
    List<Question> questionList = questionService.listByIds(ids);
    qp = computeCorrect(qp, userAnswerList, questionList);

    List<QuizPerformance> quizPerformanceList = this.getQuizPerformancesByQuiz(quizId);
    qp = computeRank(qp, quizPerformanceList);
    this.save(qp);
  }

  public QuizPerformance computeCorrect(QuizPerformance qp, List<UserAnswer> results,
      List<Question> questions) {
    // We first compute the current quality of the user, and we use this value
    // when handling collection questions
    if (qp.getCorrectScore() == null) {
      qp.setCorrectScore(0d);
    }
    if (qp.getTotalScore() == null) {
      qp.setTotalScore(0d);
    }

    // TODO(panos): Check if the quiz is a multiple choice one
    int numberOfMultipleChoiceOptions = 4;

    // questionID -> Question.
    Map<Long, Question> questionsMap = new HashMap<Long, Question>();
    for (Question question : questions) {
      // TODO(panos): The task below is a hack. Should query QuizService.
      numberOfMultipleChoiceOptions = question.getAnswers().size();
      questionsMap.put(question.getId(), question);
    }

    // The calculation below uses Laplacian smoothing, to avoid division by 0
    // and big fluctuations early on in the calculations.
    double userProb = 1.0 * (qp.getCorrectScore() + 1)
        / (qp.getTotalScore() + numberOfMultipleChoiceOptions);

    // Sort UserAnswer result by increasing timestamp. This modifies results.
    Collections.sort(results, new Comparator<UserAnswer>() {
      public int compare(UserAnswer userAnswer1, UserAnswer userAnswer2) {
        return (int) (userAnswer1.getTimestamp() - userAnswer2.getTimestamp());
      }
    });

    int numCalibrationAnswers = 0;
    int numCorrectAnswers = 0;
    int numAnswers = 0;
    double scoreCorrect = 0;
    double scoreTotal = 0;
    for (UserAnswer ua : results) {
      // If we cannot find the original question for this answer, skip.
      if (!questionsMap.containsKey(ua.getQuestionID())) {
        continue;
      }
      if (ua.getAction().equals(UserAnswer.SUBMIT)) {
        numAnswers++;
      } else {
        // This is a "I don't know answer". Skip.
        continue;
      }

      // Only counts each question once, based on user's first answer.
      // TODO(chunhowt): Have a better way to take into account of answers to
      // the same question.
      Question question = questionsMap.remove(ua.getQuestionID());

      // TODO (panos): For now, we leave that statement outside of the
      // if statements below, because the numberCorrectAnswers is what
      // we display to the user.
      if (ua.getIsCorrect()) {
        numCorrectAnswers++;
      }

      if (question.getKind() == QuestionKind.MULTIPLE_CHOICE_CALIBRATION
          || question.getKind() == QuestionKind.FREETEXT_CALIBRATION) {
        numCalibrationAnswers++;
        scoreTotal++;
        if (ua.getIsCorrect()) {
          scoreCorrect++;
        }
      } else if (question.getKind() == QuestionKind.MULTIPLE_CHOICE_COLLECTION
          || question.getKind() == QuestionKind.FREETEXT_COLLECTION) {
        // We only update the estimate for the user quality when the
        // confidence about the question quality is higher than the
        // user quality. Otherwise, the collection questions are going
        // to bring down (on expectation) the quality of the users because
        // we are still not confident about which answer is correct.
        // (notice that eventually all collection questions will get high enough
        // confidence and will contribute in the estimation of user quality)
        if (userProb < question.getConfidence()) {
          scoreTotal++;
          scoreCorrect += question.getAnswer(ua.getAnswerID()).getProbCorrectForStrategy(AnswerAggregationStrategy.NAIVE_BAYES);
        }
      }
    }
    qp.setTotalanswers(numAnswers);
    qp.setCorrectanswers(numCorrectAnswers);
    qp.setTotalCalibrationAnswers(numCalibrationAnswers);
    qp.setIncorrectanswers(numAnswers - numCorrectAnswers);
    qp.setCorrectScore(scoreCorrect);
    qp.setTotalScore(scoreTotal);

    double meanInfoGainFrequentist = 0;
    double meanInfoGainBayes = 0;
    double varInfoGainBayes = 0;
    try {
      meanInfoGainFrequentist = Helper.getInformationGain(
          qp.getPercentageCorrect(), numberOfMultipleChoiceOptions);
      meanInfoGainBayes = Helper.getBayesianMeanInformationGain(
          qp.getCorrectanswers(), qp.getIncorrectanswers(), numberOfMultipleChoiceOptions);
      varInfoGainBayes = Helper.getBayesianVarianceInformationGain(
          qp.getCorrectanswers(), qp.getIncorrectanswers(), numberOfMultipleChoiceOptions);
    } catch (Exception e) {
      e.printStackTrace();
    }

    qp.setFreqInfoGain(qp.getTotalanswers() * meanInfoGainFrequentist);
    qp.setScore(qp.getFreqInfoGain());
    qp.setBayesInfoGain(qp.getTotalanswers() * meanInfoGainBayes);
    double lcbInfoGain = qp.getTotalanswers() * (meanInfoGainBayes - Math.sqrt(varInfoGainBayes));
    if (Double.isNaN(lcbInfoGain) || lcbInfoGain < 0) {
      qp.setLcbInfoGain(0.0);
    } else {
      qp.setLcbInfoGain(lcbInfoGain);
    }

    return qp;
  }

  public QuizPerformance computeRank(QuizPerformance qp, List<QuizPerformance> results) {
    qp.setTotalUsers(results.size());
    int higherScore = 0;
    for (QuizPerformance r : results) {
      if (r.getUserid().equals(qp.getUserid())) {
        continue;
      }
      if (r.getScore() > qp.getScore()) {
        higherScore++;
      }
    }
    qp.setRankScore(higherScore + 1);
    return qp;
  }
}
