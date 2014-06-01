package us.quizz.utils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;

// Util class to manage queues.
public final class QueueUtils {
  // Queue to update quiz counts during UpdateCountStatistics cron job.
  // Queue to update browser statistics during UpdateBrowsersStatistics cron job.
  public static Queue getDefaultQueue() {
    return QueueFactory.getDefaultQueue();
  }

  // Queue to set up an AdGroup.
  public static Queue getAdGroupQueue() {
    return QueueFactory.getQueue("adgroup");
  }

  // Queue to set up an AdCampaign.
  public static Queue getAdCampaignQueue() {
    return QueueFactory.getQueue("adcampaign");
  }

  // Queue to cache survival probabilities during CacheSurvivalProbability cron job.
  // Queue to cache explore-exploit during CacheExploreExploit cron job.
  public static Queue getSurvivalQueue() {
    return QueueFactory.getQueue("survival");
  }

  // Queue to update QuizPerformance entity for a user in a quiz when user contributes a new answer.
  // and during UpdateAllUserStatistics cron job.
  public static Queue getUserStatisticsQueue() {
    return QueueFactory.getQueue("userStatistics");
  }

  // Queue to update Question and Answer entities during UpdateAllQuestionStatistics cron job.
  public static Queue getQuestionStatisticsQueue() {
    return QueueFactory.getQueue("questionStatistics");
  }
}
