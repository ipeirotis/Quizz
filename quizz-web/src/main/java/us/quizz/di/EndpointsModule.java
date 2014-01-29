package us.quizz.di;

import java.util.HashSet;
import java.util.Set;

import us.quizz.endpoints.QuestionEndpoint;
import us.quizz.endpoints.QuizEndpoint;
import us.quizz.endpoints.QuizPerformanceEndpoint;
import us.quizz.endpoints.SurvivalProbabilityEndpoint;
import us.quizz.endpoints.UserAnswerEndpoint;
import us.quizz.endpoints.UserAnswerFeedbackEndpoint;
import us.quizz.endpoints.UserEndpoint;
import us.quizz.endpoints.UserReferalEndpoint;
import us.quizz.endpoints.UtilEndpoint;

import com.google.api.server.spi.guice.GuiceSystemServiceServletModule;

/**
 * @author nkislitsin
 *
 */
public class EndpointsModule extends GuiceSystemServiceServletModule {
	  @Override
	  protected void configureServlets() {
	    super.configureServlets();

	    Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
	    serviceClasses.add(QuestionEndpoint.class);
	    serviceClasses.add(QuizEndpoint.class);
	    serviceClasses.add(QuizPerformanceEndpoint.class);
	    serviceClasses.add(SurvivalProbabilityEndpoint.class);
	    serviceClasses.add(UserAnswerEndpoint.class);
	    serviceClasses.add(UserAnswerFeedbackEndpoint.class);
	    serviceClasses.add(UserEndpoint.class);
	    serviceClasses.add(UserReferalEndpoint.class);
	    serviceClasses.add(UtilEndpoint.class);
	    this.serveGuiceSystemServiceServlet("/_ah/spi/*", serviceClasses);
	  }
}
