package us.quizz.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.inject.Inject;

public class UserQuizStatisticsService {
  private UserAnswerService userAnswerService;
  private QuizPerformanceRepository quizPerformanceRepository;
  private QuizQuestionRepository quizQuestionRepository;

  @Inject
  public UserQuizStatisticsService(UserAnswerService userAnswerService,
      QuizPerformanceRepository quizPerformanceRepository,
      QuizQuestionRepository quizQuestionRepository) {
    this.userAnswerService = userAnswerService;
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.quizQuestionRepository = quizQuestionRepository;
  }

  public void updateStatistics(String quizId, String userId) {
    QuizPerformance qp = new QuizPerformance(quizId, userId);

    List<UserAnswer> userAnswerList = userAnswerService.getUserAnswers(quizId, userId);
    // This is used to get a set of unique questions answered by user.
    Set<Key> keys = new HashSet<Key>();
    for (UserAnswer userAnswer : userAnswerList) {
      keys.add(KeyFactory.createKey(Question.class.getSimpleName(), userAnswer.getQuestionID()));
    }
    List<Question> questionList = quizQuestionRepository.getQuizQuestionsByKeys(
        new ArrayList<Key>(keys));

    qp.computeCorrect(userAnswerList, questionList);

    List<QuizPerformance> quizPerformanceList = quizPerformanceRepository
        .getQuizPerformancesByQuiz(quizId);
    qp.computeRank(quizPerformanceList);
    quizPerformanceRepository.storeQuizPerformance(qp);
  }
}
