package us.quizz.di;

import com.google.api.server.spi.guice.GuiceSystemServiceServletModule;

import us.quizz.endpoints.ProcessUserAnswerEndpoint;
import us.quizz.endpoints.QuestionEndpoint;
import us.quizz.endpoints.QuizEndpoint;
import us.quizz.endpoints.QuizPerformanceEndpoint;
import us.quizz.endpoints.ReportsEndpoint;
import us.quizz.endpoints.UpdateQuizCountsEndpoint;
import us.quizz.endpoints.UserAnswerEndpoint;
import us.quizz.endpoints.UserEndpoint;

import java.util.HashSet;
import java.util.Set;

public class EndpointsModule extends GuiceSystemServiceServletModule {
  @Override
  protected void configureServlets() {
    super.configureServlets();

    Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
    serviceClasses.add(ProcessUserAnswerEndpoint.class);
    serviceClasses.add(QuestionEndpoint.class);
    serviceClasses.add(QuizEndpoint.class);
    serviceClasses.add(QuizPerformanceEndpoint.class);
    serviceClasses.add(ReportsEndpoint.class);
    serviceClasses.add(UpdateQuizCountsEndpoint.class);
    serviceClasses.add(UserAnswerEndpoint.class);
    serviceClasses.add(UserEndpoint.class);
    this.serveGuiceSystemServiceServlet("/_ah/spi/*", serviceClasses);
  }
}
