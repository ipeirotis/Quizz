package us.quizz.di;

import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerFeedbackRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferalCounterRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserRepository;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class CommonModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AnswerChallengeCounterRepository.class).in(Singleton.class);
		bind(AnswersRepository.class).in(Singleton.class);
		bind(BadgeRepository.class).in(Singleton.class);
		bind(QuizPerformanceRepository.class).in(Singleton.class);
		bind(QuizQuestionRepository.class).in(Singleton.class);
		bind(QuizRepository.class).in(Singleton.class);
		bind(UserAnswerRepository.class).in(Singleton.class);
		bind(UserAnswerFeedbackRepository.class).in(Singleton.class);
		bind(UserReferalCounterRepository.class).in(Singleton.class);
		bind(UserReferralRepository.class).in(Singleton.class);
		bind(UserRepository.class).in(Singleton.class);
	}

}
