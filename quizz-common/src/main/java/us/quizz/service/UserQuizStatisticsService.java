package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.UserAnswerRepository;

import java.util.List;

public class UserQuizStatisticsService {
  private UserAnswerRepository userAnswerRepository;
  private QuizPerformanceRepository quizPerformanceRepository;

  @Inject
  public UserQuizStatisticsService(UserAnswerRepository userAnswerRepository,
      QuizPerformanceRepository quizPerformanceRepository) {
    this.userAnswerRepository = userAnswerRepository;
    this.quizPerformanceRepository = quizPerformanceRepository;
  }

  public void updateStatistics(String quizId, String userId) {
    QuizPerformance qp = new QuizPerformance(quizId, userId);

    List<UserAnswer> userAnswerList = userAnswerRepository.getUserAnswers(quizId, userId);
    qp.computeCorrect(userAnswerList);

    List<QuizPerformance> quizPerformanceList = quizPerformanceRepository
        .getQuizPerformancesByQuiz(quizId);
    qp.computeRank(quizPerformanceList);
    quizPerformanceRepository.storeQuizPerformance(qp);
  }
}
