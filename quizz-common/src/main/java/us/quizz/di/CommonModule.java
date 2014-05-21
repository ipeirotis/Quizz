package us.quizz.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.BadgeAssignmentRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.repository.DomainStatsRepository;
import us.quizz.repository.ExperimentRepository;
import us.quizz.repository.ExplorationExploitationResultRepository;
import us.quizz.repository.QuestionRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.SurvivalProbabilityResultRepository;
import us.quizz.repository.TreatmentRepository;
import us.quizz.repository.UserAnswerFeedbackRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserRepository;
import us.quizz.service.AnswerChallengeCounterService;
import us.quizz.service.AnswerService;
import us.quizz.service.BadgeAssignmentService;
import us.quizz.service.BadgeService;
import us.quizz.service.BrowserStatsService;
import us.quizz.service.DomainStatsService;
import us.quizz.service.ExperimentService;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.QuestionService;
import us.quizz.service.QuestionStatisticsService;
import us.quizz.service.QuizPerformanceService;
import us.quizz.service.QuizService;
import us.quizz.service.SurvivalProbabilityService;
import us.quizz.service.TreatmentService;
import us.quizz.service.UserAnswerFeedbackService;
import us.quizz.service.UserAnswerService;
import us.quizz.service.UserQuizStatisticsService;
import us.quizz.service.UserReferralService;
import us.quizz.service.UserService;

public class CommonModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AnswerChallengeCounterRepository.class).in(Singleton.class);
    bind(AnswersRepository.class).in(Singleton.class);
    bind(BadgeAssignmentRepository.class).in(Singleton.class);
    bind(BadgeRepository.class).in(Singleton.class);
    bind(BrowserStatsRepository.class).in(Singleton.class);
    bind(DomainStatsRepository.class).in(Singleton.class);
    bind(ExperimentRepository.class).in(Singleton.class);
    bind(QuizPerformanceRepository.class).in(Singleton.class);
    bind(QuestionRepository.class).in(Singleton.class);
    bind(QuizRepository.class).in(Singleton.class);
    bind(SurvivalProbabilityResultRepository.class).in(Singleton.class);
    bind(ExplorationExploitationResultRepository.class).in(Singleton.class);
    bind(UserAnswerFeedbackRepository.class).in(Singleton.class);
    bind(UserAnswerRepository.class).in(Singleton.class);
    bind(UserReferralRepository.class).in(Singleton.class);
    bind(UserRepository.class).in(Singleton.class);
    bind(TreatmentRepository.class).in(Singleton.class);

    bind(AnswerChallengeCounterService.class).in(Singleton.class);
    bind(AnswerService.class).in(Singleton.class);
    bind(QuestionService.class).in(Singleton.class);
    bind(ExperimentService.class).in(Singleton.class);
    bind(UserService.class).in(Singleton.class);
    bind(BadgeAssignmentService.class).in(Singleton.class);
    bind(BadgeService.class).in(Singleton.class);
    bind(QuizPerformanceService.class).in(Singleton.class);
    bind(UserAnswerFeedbackService.class).in(Singleton.class);
    bind(ExplorationExploitationService.class).in(Singleton.class);
    bind(UserQuizStatisticsService.class).in(Singleton.class);
    bind(SurvivalProbabilityService.class).in(Singleton.class);
    bind(QuestionStatisticsService.class).in(Singleton.class);
    bind(BrowserStatsService.class).in(Singleton.class);
    bind(QuizService.class).in(Singleton.class);
    bind(UserReferralService.class).in(Singleton.class);
    bind(DomainStatsService.class).in(Singleton.class);
    bind(TreatmentService.class).in(Singleton.class);
    bind(UserAnswerService.class).in(Singleton.class);
  }
}
