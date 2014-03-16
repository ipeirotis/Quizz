package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import eu.bitwalker.useragentutils.Browser;

import us.quizz.entities.BrowserStats;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserReferralRepository.Result;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateBrowsersStatistics extends HttpServlet {
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(UpdateBrowsersStatistics.class.getName());

  private BrowserStatsRepository browserStatsRepository;
  private QuizPerformanceRepository quizPerformanceRepository;
  private UserReferralRepository userReferralRepository;

  @Inject
  public UpdateBrowsersStatistics(
      QuizPerformanceRepository quizPerformanceRepository,
      UserReferralRepository userReferralRepository,
      BrowserStatsRepository browserStatsRepository) {
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.userReferralRepository = userReferralRepository;
    this.browserStatsRepository = browserStatsRepository;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if ("true".equals(request.getParameter("all"))) {
      Queue queue = QueueFactory.getDefaultQueue();

      Set<Browser> browsers = new HashSet<Browser>();
      for (Browser browser : Browser.values()) {
        browsers.add(browser.getGroup());
      }

      for (Browser browser : browsers) {
        queue.add(Builder
            .withUrl("/api/updateBrowsersStatistics")
            .param("browser", browser.getGroup().toString())
            .retryOptions(RetryOptions.Builder.withTaskRetryLimit(0))
            .method(TaskOptions.Method.GET));
      }
    } else {
      updateStatistics(request.getParameter("browser"));
    }
  }

  private void updateStatistics(String browser) {
    Browser b = Browser.valueOf(browser);
    Result res = userReferralRepository.getCountByBrowser(b);

    Double userScores = quizPerformanceRepository.getScoreSumByIds(res.getUsers());
    if (res.getCount() > 0) {
      BrowserStats bs = new BrowserStats(b, res.getCount(), userScores);
      browserStatsRepository.save(bs);
    }
  }
}
