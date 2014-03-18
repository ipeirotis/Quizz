package us.quizz.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.repository.DomainStatsRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerFeedbackRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserRepository;
import us.quizz.service.AnswerBitsStatisticsService;
import us.quizz.service.AnswerCountsStatisticsService;
import us.quizz.service.BrowserStatisticsService;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.QuestionStatisticsService;
import us.quizz.service.SurvivalProbabilityService;
import us.quizz.service.UserQuizStatisticsService;

public class CommonModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AnswerChallengeCounterRepository.class).in(Singleton.class);
    bind(AnswersRepository.class).in(Singleton.class);
    bind(BadgeRepository.class).in(Singleton.class);
    bind(BrowserStatsRepository.class).in(Singleton.class);
    bind(DomainStatsRepository.class).in(Singleton.class);
    bind(QuizPerformanceRepository.class).in(Singleton.class);
    bind(QuizQuestionRepository.class).in(Singleton.class);
    bind(QuizRepository.class).in(Singleton.class);
    bind(UserAnswerFeedbackRepository.class).in(Singleton.class);
    bind(UserAnswerRepository.class).in(Singleton.class);
    bind(UserReferralRepository.class).in(Singleton.class);
    bind(UserRepository.class).in(Singleton.class);

    bind(ExplorationExploitationService.class).in(Singleton.class);
    bind(UserQuizStatisticsService.class).in(Singleton.class);
    bind(SurvivalProbabilityService.class).in(Singleton.class);
    bind(QuestionStatisticsService.class).in(Singleton.class);
    bind(AnswerCountsStatisticsService.class).in(Singleton.class);
    bind(AnswerBitsStatisticsService.class).in(Singleton.class);
    bind(BrowserStatisticsService.class).in(Singleton.class);
  }
}
