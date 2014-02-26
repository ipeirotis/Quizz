package us.quizz.di;

import us.quizz.servlets.CacheQuizQuestions;
import us.quizz.servlets.CacheSurvivalProbability;
import us.quizz.servlets.GetQuizCounts;
import us.quizz.servlets.UpdateAllQuestionStatistics;
import us.quizz.servlets.UpdateAllUserStatistics;
import us.quizz.servlets.UpdateAnswerBitsStatistics;
import us.quizz.servlets.UpdateAnswerCountsStatistics;
import us.quizz.servlets.UpdateBrowsersStatistics;
import us.quizz.servlets.UpdateCountStatistics;
import us.quizz.servlets.UpdateQuestionStatistics;
import us.quizz.servlets.UpdateUserQuizStatistics;

import com.google.inject.servlet.ServletModule;

public class TasksModule extends ServletModule {

	  @Override
	  protected void configureServlets() { 
		  super.configureServlets();
		  
		  serve("/api/updateBrowsersStatistics").with(UpdateBrowsersStatistics.class);
		  serve("/api/updateAnswerBitsStatistics").with(UpdateAnswerBitsStatistics.class);
		  serve("/api/updateAnswerCountsStatistics").with(UpdateAnswerCountsStatistics.class);
		  serve("/api/updateAllQuestionStatistics").with(UpdateAllQuestionStatistics.class);
		  serve("/api/updateQuestionStatistics").with(UpdateQuestionStatistics.class);
		  serve("/api/updateUserQuizStatistics").with(UpdateUserQuizStatistics.class);
		  serve("/api/updateAllUserStatistics").with(UpdateAllUserStatistics.class);
		  serve("/api/cacheSurvivalProbability").with(CacheSurvivalProbability.class);
		  serve("/api/cacheQuizQuestions").with(CacheQuizQuestions.class);
		  serve("/api/updateCountStatistics").with(UpdateCountStatistics.class);
		  serve("/api/getQuizCounts").with(GetQuizCounts.class);
	  }
}
