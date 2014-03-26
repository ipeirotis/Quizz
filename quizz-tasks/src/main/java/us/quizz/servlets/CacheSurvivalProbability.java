package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.service.SurvivalProbabilityService;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class CacheSurvivalProbability extends HttpServlet {
  private SurvivalProbabilityService survivalProbabilityService;

  @Inject
  public CacheSurvivalProbability(
      SurvivalProbabilityService survivalProbabilityService) {
    this.survivalProbabilityService = survivalProbabilityService;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if ("true".equals(req.getParameter("sched"))) {
      sched();
    } else {
      survivalProbabilityService.cacheValuesInMemcache(null);
    }
  }

  private void sched() {
    Queue queue = QueueFactory.getDefaultQueue();
    queue.add(Builder.withUrl("/api/cacheSurvivalProbability").method(
        TaskOptions.Method.GET));
  }
}
