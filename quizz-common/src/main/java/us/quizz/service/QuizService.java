package us.quizz.service;

import java.util.List;

import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;

import com.google.inject.Inject;

public class QuizService {

  private UserReferralRepository userReferralRepository;
  private QuizPerformanceRepository quizPerformanceRepository;
  private QuizRepository quizRepository;
  private QuizQuestionRepository quizQuestionRepository;
  private UserAnswerRepository userAnswerRepository;
  
  @Inject
  public QuizService(UserReferralRepository userReferralRepository, 
      QuizPerformanceRepository quizPerformanceRepository, QuizRepository quizRepository,
      QuizQuestionRepository quizQuestionRepository, UserAnswerRepository userAnswerRepository){
    this.userReferralRepository = userReferralRepository;
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.quizRepository = quizRepository;
    this.quizQuestionRepository = quizQuestionRepository;
    this.userAnswerRepository = userAnswerRepository;
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

    /*Class<?>[] itemsClasses = new Class<?>[] { UserAnswer.class, Answer.class, Question.class };
    for (Class<?> cls : itemsClasses) {
      deleteAll(pm, quizID, cls);
    }*/
  }
  
  public Quiz updateQuizCounts(String quizID) {
    Quiz quiz = quizRepository.get(quizID);
    Integer count = quizQuestionRepository.getNumberOfQuizQuestions(quizID, false);
    quiz.setQuestions(count);

    count = userAnswerRepository.getNumberOfUserAnswers(quizID, false);
    quiz.setSubmitted(count);

    count = quizQuestionRepository.getNumberOfGoldQuestions(quizID, false);
    quiz.setGold(count);

    // TODO(chunhowt): UserReferral is broken now, so this will always return 0.
    count = userReferralRepository.getUserIDsByQuiz(quizID).size();
    quiz.setTotalUsers(count + 1);  // +1 for smoothing, ensuring no division by 0

    List<QuizPerformance> perf = quizPerformanceRepository
        .getQuizPerformancesByQuiz(quizID);

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
      totalCorrect += qp.getCorrectanswers();
      totalAnswers += qp.getTotalanswers();
      totalCalibrationAnswers += qp.getTotalCalibrationAnswers();
      avgCorrectness += qp.getPercentageCorrect();
      bits += qp.getScore();
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
