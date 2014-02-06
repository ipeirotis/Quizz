package us.quizz.di;

import us.quizz.servlets.DownloadUserAnswers;
import us.quizz.servlets.FacebookLogin;
import us.quizz.servlets.api.UpdateUserQuizStatistics;

import com.google.inject.servlet.ServletModule;

public class WebModule extends ServletModule {

	  @Override
	  protected void configureServlets() { 
		  super.configureServlets();

		  serve("/admin/downloadUserAnswers").with(DownloadUserAnswers.class);
		  serve("/api/updateUserQuizStatistics").with(UpdateUserQuizStatistics.class);
		  serve("/fblogin").with(FacebookLogin.class);
	  }
}
