package us.quizz.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.repository.DomainStatsRepository;
import us.quizz.repository.UserReferralRepository;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateDomainsStatistics extends HttpServlet {
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(UpdateDomainsStatistics.class.getName());

  private DomainStatsRepository domainStatsRepository;
  private UserReferralRepository userReferralRepository;

  @Inject
  public UpdateDomainsStatistics(
      DomainStatsRepository domainStatsRepository,
      UserReferralRepository userReferralRepository) {
    this.domainStatsRepository = domainStatsRepository;
    this.userReferralRepository = userReferralRepository;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException { 
  }
}
