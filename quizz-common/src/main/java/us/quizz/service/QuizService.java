package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.enums.AnswerAggregationStrategy;
import us.quizz.enums.AnswerKind;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.QuizRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizService extends OfyBaseService<Quiz> {
  private UserReferralService userReferralService;
  private QuizPerformanceService quizPerformanceService;
  private QuestionService questionService;
  private UserAnswerService userAnswerService;

  @Inject
  public QuizService(UserReferralService userReferralService,
      QuizPerformanceService quizPerformanceService, QuizRepository quizRepository,
      QuestionService questionService, UserAnswerService userAnswerService) {
    super(quizRepository);
    this.userReferralService = userReferralService;
    this.quizPerformanceService = quizPerformanceService;
    this.questionService = questionService;
    this.userAnswerService = userAnswerService;
  }

  // Deletes all entities associated with the given quizID.
  public void deleteRecursively(String quizID) {
    delete(quizID);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", quizID);
    userAnswerService.deleteAll(userAnswerService.listAll(params));
    questionService.deleteAll(questionService.listAll(params));
  }

  private double computeQuizQuality(String quizID, AnswerAggregationStrategy strategy) {
    int total = 0;
    int correct = 0;
    List<Question> questions = questionService.getQuizQuestions(quizID);
    for (Question question : questions) {
      if (!question.getHasGoldAnswer()) {
        continue;
      }

      ++total;
      Integer bestAnswerID = -1;
      bestAnswerID = strategy.getBestProbAnswerID(question);
      /*
      switch (strategy) {
        case NAIVE_BAYES:
          bestAnswerID = question.getBestBayesProbAnswerID();
          break;
        case WEIGHTED_VOTE:
          bestAnswerID = question.getBestWeightedVoteProbAnswerID();
          break;
        case MAJORITY_VOTE:
          bestAnswerID = question.getBestMajorityVoteProbAnswerID();
          break;
        default:
          break;
      }
      */
      if (bestAnswerID == null) {
        continue;
      }
      if (question.getAnswer(bestAnswerID) == null) {
        continue;
      }
      if (AnswerKind.GOLD.equals(question.getAnswer(bestAnswerID).getKind())) {
        ++correct;
      }
    }
    return total == 0 ? 0.0 : correct * 1.0 / total;
  }

  public Quiz updateQuizCounts(String quizID) {
    Quiz quiz = get(quizID);
    Integer count = questionService.getNumberOfQuizQuestions(quizID, false);
    quiz.setQuestions(count);

    // Number of UserAnswers for the given quizID, including "skip" answers.
    count = userAnswerService.getNumberOfUserAnswers(quizID);
    quiz.setSubmitted(count);

    count = questionService.getNumberOfGoldQuestions(quizID, false);
    quiz.setGold(count);

    // Number of total users for the given quizID, whether they contribute or not.
    // TODO(chunhowt): UserReferral is broken now, so this will always return 0.
    count = userReferralService.getUserIDsByQuiz(quizID).size();
    quiz.setTotalUsers(count);

    List<QuizPerformance> perf = quizPerformanceService.getQuizPerformancesByQuiz(quizID);
    quiz.setContributingUsers(perf.size());
    if (quiz.getTotalUsers() > 0) {
      quiz.setConversionRate(1.0 * quiz.getContributingUsers() / quiz.getTotalUsers());
    }
    int totalCorrect = 0;
    int totalAnswers = 0;
    int totalCalibrationAnswers = 0;
    double bits = 0;
    double avgCorrectness = 0;
    for (QuizPerformance qp : perf) {
      Integer t = qp.getCorrectanswers();
      totalCorrect += (t == null) ? 0 : t;
      t = qp.getTotalanswers();
      totalAnswers += (t == null) ? 0 : t;
      t = qp.getTotalCalibrationAnswers();
      totalCalibrationAnswers += (t == null) ? 0 : t; 
      Double d = qp.getPercentageCorrect(); 
      avgCorrectness += (d == null) ? 0 : d;
      d = qp.getScore();
      bits +=  (d == null) ? 0 : d;
    }
    quiz.setCorrectAnswers(totalCorrect);
    quiz.setTotalAnswers(totalAnswers);
    quiz.setTotalCalibrationAnswers(totalCalibrationAnswers);
    quiz.setTotalCollectionAnswers(totalAnswers - totalCalibrationAnswers);
    if (quiz.getContributingUsers() > 0) {
      quiz.setCapacity(bits / quiz.getContributingUsers());
      quiz.setAvgUserCorrectness(avgCorrectness / quiz.getContributingUsers());
    }
    if (quiz.getTotalAnswers() > 0) {
      quiz.setAvgAnswerCorrectness(
          1.0 * quiz.getCorrectAnswers() / quiz.getTotalAnswers());
    }

    quiz.setBayesProbQuizQuality(
        computeQuizQuality(quizID, AnswerAggregationStrategy.NAIVE_BAYES));
    quiz.setWeightedVoteProbQuizQuality(
        computeQuizQuality(quizID, AnswerAggregationStrategy.WEIGHTED_VOTE));
    quiz.setMajorityVoteProbQuizQuality(
        computeQuizQuality(quizID, AnswerAggregationStrategy.MAJORITY_VOTE));
    return save(quiz);
  }
  
}
