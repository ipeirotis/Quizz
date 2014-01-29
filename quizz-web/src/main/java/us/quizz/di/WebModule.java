package us.quizz.di;

import us.quizz.ads.CampaignManagement;
import us.quizz.servlets.AddBadge;
import us.quizz.servlets.AddQuestion;
import us.quizz.servlets.AddQuiz;
import us.quizz.servlets.AddTreatment;
import us.quizz.servlets.AddUserAnswer;
import us.quizz.servlets.DeleteQuiz;
import us.quizz.servlets.DownloadUserAnswers;
import us.quizz.servlets.FacebookLogin;
import us.quizz.servlets.ProcessUserAnswer;
import us.quizz.servlets.StartQuiz;
import us.quizz.servlets.api.GetNumberOfSubmittedAnswers;
import us.quizz.servlets.api.UpdateUserExperiment;
import us.quizz.servlets.api.UpdateUserQuizStatistics;

import com.google.inject.servlet.ServletModule;

public class WebModule extends ServletModule {

	  @Override
	  protected void configureServlets() { 
		  super.configureServlets();
		  
		  serve("/addQuiz").with(AddQuiz.class);
		  serve("/startQuiz").with(StartQuiz.class);
		  serve("/addQuestion").with(AddQuestion.class);
		  serve("/addTreatment").with(AddTreatment.class);
		  serve("/addBadge").with(AddBadge.class);
		  serve("/addUserAnswer").with(AddUserAnswer.class);
		  serve("/processUserAnswer").with(ProcessUserAnswer.class);
		  serve("/admin/downloadUserAnswers").with(DownloadUserAnswers.class);
		  serve("/api/getNumberOfSubmittedAnswers").with(GetNumberOfSubmittedAnswers.class);
		  serve("/api/deleteQuiz").with(DeleteQuiz.class);
		  serve("/api/updateUserQuizStatistics").with(UpdateUserQuizStatistics.class);
		  serve("/api/updateUserExperiment").with(UpdateUserExperiment.class);
		  serve("/fblogin").with(FacebookLogin.class);
		  serve("/campaignManagement").with(CampaignManagement.class);
	  }
}
