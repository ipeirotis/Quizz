package us.quizz.ofy;

import us.quizz.entities.Answer;
import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.Badge;
import us.quizz.entities.BadgeAssignment;
import us.quizz.entities.BrowserStats;
import us.quizz.entities.DomainStats;
import us.quizz.entities.Experiment;
import us.quizz.entities.ExplorationExploitationResult;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.SurvivalProbabilityResult;
import us.quizz.entities.Treatment;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.entities.UserReferal;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
  static {
    register(Answer.class);
    register(AnswerChallengeCounter.class);
    register(Badge.class);
    register(BadgeAssignment.class);
    register(BrowserStats.class);
    register(DomainStats.class);
    register(Experiment.class);
    register(ExplorationExploitationResult.class);
    register(Question.class);
    register(Quiz.class);
    register(QuizPerformance.class);
    register(SurvivalProbabilityResult.class);
    register(Treatment.class);
    register(User.class);
    register(UserAnswer.class);
    register(UserAnswerFeedback.class);
    register(UserReferal.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }

  public static void register(Class<?> clazz) {
    factory().register(clazz);
  }
}
