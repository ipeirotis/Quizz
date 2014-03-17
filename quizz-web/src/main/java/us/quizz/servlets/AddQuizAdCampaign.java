package us.quizz.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.enums.QuestionKind;
import us.quizz.repository.QuizRepository;
import us.quizz.utils.ServletUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class AddQuizAdCampaign extends HttpServlet {
  private HttpServletResponse r;
  final static Logger logger = Logger.getLogger("com.ipeirotis.adcrowdkg");

  private QuizRepository quizRepository;

  @Inject
  public AddQuizAdCampaign(QuizRepository quizRepository) {
    this.quizRepository = quizRepository;
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    r = resp;
    r.setContentType("text/plain");
    ServletUtils.ensureParameters(req, "quizID", "name", "fbtype");

    try {
      String quizID = req.getParameter("quizID");
      resp.getWriter().println("Adding Relation: " + quizID);

      String name = req.getParameter("name");
      resp.getWriter().println("Name: " + name);

      String freebasetype = req.getParameter("fbtype");
      resp.getWriter().println("Freebase Type: " + freebasetype);

      String budget = req.getParameter("budget");
      if (budget != null) {
        resp.getWriter().println("Budget: " + budget);
      }

      String cpcbid = req.getParameter("cpcbid");
      if (cpcbid != null) {
        resp.getWriter().println("CPC bid: " + cpcbid);
      }

      String keywords = req.getParameter("keywords");
      if (keywords != null) {
        resp.getWriter().println("AdKeywords: " + keywords);
      }

      String adheadline = req.getParameter("adheadline");
      if (adheadline != null) {
        resp.getWriter().println("adText: " + adheadline);
      }

      String adline1 = req.getParameter("adline1");
      if (adline1 != null) {
        resp.getWriter().println("adText: " + adline1);
      }

      String adline2 = req.getParameter("adline2");
      if (adline2 != null) {
        resp.getWriter().println("adText: " + adline2);
      }

      Quiz q = new Quiz(name, quizID, QuestionKind.MULTIPLE_CHOICE);
      quizRepository.singleMakePersistent(q);

      Queue queueAdCampaign = QueueFactory.getQueue("adcampaign");

      queueAdCampaign.add(Builder.withUrl("/addCampaign")
          .param("quizID", quizID).param("budget", budget)
          .method(TaskOptions.Method.GET));

      Queue queueAdgroup = QueueFactory.getQueue("adgroup");

      for (;;) {
        // We introduce a delay of a few secs to allow the ad campaign
        // to be created and for the entries to be uploaded and stored
        long delay = 10; // in seconds
        long etaMillis = System.currentTimeMillis() + delay * 1000L;
        queueAdgroup.add(Builder
            .withUrl("/addAdGroup")
            .param("quizID", quizID)
            .param("questionID", "mid")
            // this is not used in AddAdGroup
            .param("cpcbid", cpcbid).param("keywords", keywords)
            .param("adheadline", adheadline)
            .param("adline1", adline1).param("adline2", adline2)
            .method(TaskOptions.Method.GET).etaMillis(etaMillis));
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE,
          "Reached execution time limit. Press refresh to continue.", e);
    }
  }
}
