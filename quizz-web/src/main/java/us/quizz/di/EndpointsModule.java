package us.quizz.di;

import java.util.HashSet;
import java.util.Set;

import us.quizz.endpoints.CampaignManagementEndpoint;
import us.quizz.endpoints.ProcessUserAnswerEndpoint;
import us.quizz.endpoints.QuestionEndpoint;
import us.quizz.endpoints.QuizEndpoint;
import us.quizz.endpoints.QuizPerformanceEndpoint;
import us.quizz.endpoints.ReportsEndpoint;
import us.quizz.endpoints.UpdateQuizCountsEndpoint;
import us.quizz.endpoints.UserActionEndpoint;
import us.quizz.endpoints.UserAnswerEndpoint;
import us.quizz.endpoints.UserEndpoint;

import com.google.api.server.spi.guice.GuiceSystemServiceServletModule;

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
    serviceClasses.add(UserActionEndpoint.class);
    serviceClasses.add(UserAnswerEndpoint.class);
    serviceClasses.add(UserEndpoint.class);
    serviceClasses.add(CampaignManagementEndpoint.class);
    this.serveGuiceSystemServiceServlet("/_ah/spi/*", serviceClasses);
  }
}
