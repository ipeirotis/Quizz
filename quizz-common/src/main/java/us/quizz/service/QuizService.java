package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.repository.QuestionRepository;
import us.quizz.repository.QuizRepository;

import java.util.List;

public class QuizService {
  private UserReferralService userReferralService;
  private QuizPerformanceService quizPerformanceService;
  private QuizRepository quizRepository;
  private QuestionService questionService;
  private UserAnswerService userAnswerService;
  
  @Inject
  public QuizService(UserReferralService userReferralService, 
      QuizPerformanceService quizPerformanceService, QuizRepository quizRepository,
      QuestionService questionService, UserAnswerService userAnswerService) {
    this.userReferralService = userReferralService;
    this.quizPerformanceService = quizPerformanceService;
    this.quizRepository = quizRepository;
    this.questionService = questionService;
    this.userAnswerService = userAnswerService;
  }
  
  public List<Quiz> list(){
    return quizRepository.list();
  }
  
  public Quiz get(String quizID){
    return quizRepository.get(quizID);
  }
  
  public Quiz save(Quiz quiz){
    return quizRepository.saveAndGet(quiz);
  }
  
  public void delete(String quizID) {
    quizRepository.delete(quizID);
  }

  // Deletes all entities associated with the given quizID.
  public void deleteAll(String quizID) {
    delete(quizID);

    /* TODO(chunhowt): Implement this.
    Class<?>[] itemsClasses = new Class<?>[] { UserAnswer.class, Answer.class, Question.class };
    for (Class<?> cls : itemsClasses) {
      deleteAll(pm, quizID, cls);
    }
    */
  }

  public Quiz updateQuizCounts(String quizID) {
    Quiz quiz = quizRepository.get(quizID);
    Integer count = questionService.getNumberOfQuizQuestions(quizID, false);
    quiz.setQuestions(count);

    count = userAnswerService.getNumberOfUserAnswers(quizID);
    quiz.setSubmitted(count);

    count = questionService.getNumberOfGoldQuestions(quizID, false);
    quiz.setGold(count);

    // TODO(chunhowt): UserReferral is broken now, so this will always return 0.
    count = userReferralService.getUserIDsByQuiz(quizID).size();
    quiz.setTotalUsers(count + 1);  // +1 for smoothing, ensuring no division by 0

    List<QuizPerformance> perf = quizPerformanceService.getQuizPerformancesByQuiz(quizID);

    int contributingUsers = perf.size();
    // +1 for smoothing, ensuring no division by 0
    quiz.setContributingUsers(contributingUsers + 1);
    quiz.setConversionRate(1.0 * quiz.getContributingUsers() / quiz.getTotalUsers());
 
    // +1 for smoothing, ensuring no division by 0
    int totalCorrect = 1;
    int totalAnswers = 1;
    int totalCalibrationAnswers = 1;
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
    quiz.setCapacity(bits / quiz.getContributingUsers());
    quiz.setAvgUserCorrectness(avgCorrectness / quiz.getContributingUsers());
    quiz.setAvgAnswerCorrectness(
        1.0 * quiz.getCorrectAnswers() / quiz.getTotalCalibrationAnswers());
    return quizRepository.saveAndGet(quiz);
  }
}
