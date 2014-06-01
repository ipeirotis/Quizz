package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import nl.bitwalker.useragentutils.Browser;

import us.quizz.service.BrowserStatsService;
import us.quizz.utils.QueueUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateBrowsersStatistics extends HttpServlet {
  private static final String BROWSER_PARAM = "browser";
  private static final String BROWSER_ALL = "all";

  private BrowserStatsService browserStatisticsService;

  @Inject
  public UpdateBrowsersStatistics(BrowserStatsService browserStatisticsService) {
    this.browserStatisticsService = browserStatisticsService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (BROWSER_ALL.equals(request.getParameter(BROWSER_PARAM))) {
      Queue queue = QueueUtils.getDefaultQueue();
      Set<Browser> browsers = new HashSet<Browser>();
      for (Browser browser : Browser.values()) {
        browsers.add(browser.getGroup());
      }

      for (Browser browser : browsers) {
        queue.add(Builder
            .withUrl("/api/updateBrowsersStatistics")
            .param(BROWSER_PARAM, browser.getGroup().toString())
            .retryOptions(RetryOptions.Builder.withTaskRetryLimit(0))
            .method(TaskOptions.Method.GET));
      }
    } else {
      browserStatisticsService.updateStatistics(request.getParameter(BROWSER_PARAM));
    }
  }
}
