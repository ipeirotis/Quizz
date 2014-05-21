package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;

import java.util.ArrayList;
import java.util.List;

public class UserQuizStatisticsService {
  private UserAnswerService userAnswerService;
  private QuizPerformanceService quizPerformanceService;
  private QuestionService questionService;

  @Inject
  public UserQuizStatisticsService(UserAnswerService userAnswerService,
      QuizPerformanceService quizPerformanceService,
      QuestionService questionService) {
    this.userAnswerService = userAnswerService;
    this.quizPerformanceService = quizPerformanceService;
    this.questionService = questionService;
  }

  public void updateStatistics(String quizId, String userId) {
    QuizPerformance qp = new QuizPerformance(quizId, userId);

    List<UserAnswer> userAnswerList = userAnswerService.getUserAnswers(quizId, userId);
    // This is used to get a set of unique questions answered by user.
    List<Long> ids = new ArrayList<Long>();
    for (UserAnswer userAnswer : userAnswerList) {
      ids.add(userAnswer.getQuestionID());
    }
    List<Question> questionList = questionService.listByIds(ids);

    qp.computeCorrect(userAnswerList, questionList);

    List<QuizPerformance> quizPerformanceList = quizPerformanceService
        .getQuizPerformancesByQuiz(quizId);
    qp.computeRank(quizPerformanceList);
    quizPerformanceService.save(qp);
  }
}
