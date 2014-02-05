package us.quizz.di;

import us.quizz.ads.CampaignManagement;
import us.quizz.servlets.AddBadge;
import us.quizz.servlets.AddQuestion;
import us.quizz.servlets.AddQuiz;
import us.quizz.servlets.AddTreatment;
import us.quizz.servlets.DownloadUserAnswers;
import us.quizz.servlets.FacebookLogin;
import us.quizz.servlets.StartQuiz;
import us.quizz.servlets.api.UpdateUserExperiment;
import us.quizz.servlets.api.UpdateUserQuizStatistics;

import com.google.inject.servlet.ServletModule;

public class WebModule extends ServletModule {

	  @Override
	  protected void configureServlets() { 
		  super.configureServlets();
		  
		  serve("/addQuiz").with(AddQuiz.class);//TODO:remove, use endpoint
		  serve("/startQuiz").with(StartQuiz.class);//TODO:remove
		  serve("/addQuestion").with(AddQuestion.class);
		  serve("/addTreatment").with(AddTreatment.class);//TODO: remove, use endpoint
		  serve("/addBadge").with(AddBadge.class);//TODO: remove, use endpoint
		  serve("/admin/downloadUserAnswers").with(DownloadUserAnswers.class);
		  serve("/api/updateUserQuizStatistics").with(UpdateUserQuizStatistics.class);
		  serve("/api/updateUserExperiment").with(UpdateUserExperiment.class);
		  serve("/fblogin").with(FacebookLogin.class);
		  serve("/campaignManagement").with(CampaignManagement.class);
	  }
}
