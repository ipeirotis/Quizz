package us.quizz.di;

import com.google.inject.servlet.ServletModule;

import us.quizz.servlets.CacheExploreExploit;
import us.quizz.servlets.CacheSurvivalProbability;
import us.quizz.servlets.GetQuizCounts;
import us.quizz.servlets.RemoveOrphanQuestions;
import us.quizz.servlets.RemoveOrphanUserAnswers;
import us.quizz.servlets.UpdateAllQuestionStatistics;
import us.quizz.servlets.UpdateAllUserStatistics;
import us.quizz.servlets.UpdateBrowsersStatistics;
import us.quizz.servlets.UpdateCountStatistics;
import us.quizz.servlets.UpdateQuestionStatistics;
import us.quizz.servlets.UpdateUserQuizStatistics;

public class TasksModule extends ServletModule {
  @Override
  protected void configureServlets() {
    super.configureServlets();
    serve("/api/cacheExploreExploit").with(CacheExploreExploit.class);
    serve("/api/cacheSurvivalProbability").with(CacheSurvivalProbability.class);
    serve("/api/getQuizCounts").with(GetQuizCounts.class);
    serve("/api/updateAllQuestionStatistics").with(UpdateAllQuestionStatistics.class);
    serve("/api/updateAllUserStatistics").with(UpdateAllUserStatistics.class);
    serve("/api/updateBrowsersStatistics").with(UpdateBrowsersStatistics.class);
    serve("/api/updateCountStatistics").with(UpdateCountStatistics.class);
    serve("/api/updateQuestionStatistics").with(UpdateQuestionStatistics.class);
    serve("/api/updateUserQuizStatistics").with(UpdateUserQuizStatistics.class);
    serve("/consistency/removeOrphanQuestions").with(RemoveOrphanQuestions.class);
    serve("/consistency/removeOrphanUserAnswers").with(RemoveOrphanUserAnswers.class);
  }
}
