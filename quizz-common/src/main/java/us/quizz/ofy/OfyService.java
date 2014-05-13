package us.quizz.ofy;

import us.quizz.entities.BrowserStats;
import us.quizz.entities.DomainStats;
import us.quizz.entities.ExplorationExploitationResult;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.SurvivalProbabilityResult;
import us.quizz.entities.Treatment;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.entities.UserReferal;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
  static {
    register(BrowserStats.class);
    register(DomainStats.class);
    register(ExplorationExploitationResult.class);
    register(Quiz.class);
    register(QuizPerformance.class);
    register(SurvivalProbabilityResult.class);
    register(Treatment.class);
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
