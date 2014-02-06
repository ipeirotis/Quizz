package us.quizz.di;

import us.quizz.ads.CampaignManagement;
import us.quizz.servlets.DownloadUserAnswers;
import us.quizz.servlets.FacebookLogin;
import us.quizz.servlets.api.UpdateUserQuizStatistics;

import com.google.inject.servlet.ServletModule;

public class WebModule extends ServletModule {

	  @Override
	  protected void configureServlets() { 
		  super.configureServlets();

		  //serve("/addQuestion").with(AddQuestion.class); TODO:remove
		  serve("/admin/downloadUserAnswers").with(DownloadUserAnswers.class);
		  serve("/api/updateUserQuizStatistics").with(UpdateUserQuizStatistics.class);
		  serve("/campaignManagement").with(CampaignManagement.class);
		  serve("/fblogin").with(FacebookLogin.class);
	  }
}
