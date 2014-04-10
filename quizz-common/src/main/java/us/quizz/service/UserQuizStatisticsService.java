package us.quizz.service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.inject.Inject;

import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.UserAnswerRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserQuizStatisticsService {
  private UserAnswerRepository userAnswerRepository;
  private QuizPerformanceRepository quizPerformanceRepository;
  private QuizQuestionRepository quizQuestionRepository;

  @Inject
  public UserQuizStatisticsService(UserAnswerRepository userAnswerRepository,
      QuizPerformanceRepository quizPerformanceRepository,
      QuizQuestionRepository quizQuestionRepository) {
    this.userAnswerRepository = userAnswerRepository;
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.quizQuestionRepository = quizQuestionRepository;
  }

  public void updateStatistics(String quizId, String userId) {
    QuizPerformance qp = new QuizPerformance(quizId, userId);

    List<UserAnswer> userAnswerList = userAnswerRepository.getUserAnswers(quizId, userId);
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
