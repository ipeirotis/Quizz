package us.quizz.servlets;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.service.BrowserStatisticsService;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import eu.bitwalker.useragentutils.Browser;

@SuppressWarnings("serial")
@Singleton
public class UpdateBrowsersStatistics extends HttpServlet {
  private BrowserStatisticsService browserStatisticsService;

  @Inject
  public UpdateBrowsersStatistics(BrowserStatisticsService browserStatisticsService) {
    this.browserStatisticsService = browserStatisticsService;
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
      browserStatisticsService.updateStatistics(request.getParameter("browser"));
    }
  }
}
