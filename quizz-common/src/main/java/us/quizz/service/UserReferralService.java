package us.quizz.service;

import com.google.inject.Inject;

import nl.bitwalker.useragentutils.Browser;

import us.quizz.entities.DomainStats;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserReferal;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.DomainStatsRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.utils.UrlUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

public class UserReferralService extends OfyBaseService<UserReferal> {
  private DomainStatsRepository domainStatsRepository;

  @Inject
  public UserReferralService(UserReferralRepository userReferralRepository,
      DomainStatsRepository domainStatsRepository){
    super(userReferralRepository);
    this.domainStatsRepository = domainStatsRepository;
  }

  // Get a list of userids participating for the given quizID.
  public Set<String> getUserIDsByQuiz(String quizID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quiz", quizID);

    TreeSet<String> userids = new TreeSet<String>();
    List<UserReferal> results = listAll(params);
    for (UserReferal ur : results) {
      userids.add(ur.getUserid());
    }
    return userids;
  }

  // Creates and stores a new UserReferal for the request, userid and url referer given.
  // This will generate a UserReferal object and potentially a DomainStats object, and then
  // they are saved asynchronously.
  // If quizID is null, then the user comes straight to the Quizz homepage, so we store a special
  // value for such cases.
  public void asyncCreateAndStoreUserReferal(
      HttpServletRequest req, String userid, String referer, String quizID) {
    UserReferal ur = new UserReferal(userid);
    ur.setQuiz(quizID != null ? quizID : UserReferal.QUIZ_LANDING_PAGE);
    ur.setIpaddress(req.getRemoteAddr());
    ur.setBrowser(Browser.parseUserAgentString(req.getHeader("User-Agent")));
    ur.setReferer(referer);
    ur.setDomain(UrlUtils.extractDomain(referer));
    asyncSave(ur);

    if (ur.getDomain() != null) {
      DomainStats domainStats = domainStatsRepository.get(ur.getDomain());
      if (domainStats == null) {
        domainStats = new DomainStats(ur.getDomain(), 0, 0);
      }
      domainStats.incUserCount();
      domainStatsRepository.asyncSave(domainStats);
    }
  }

  public List<UserReferal> getUserQuizReferal(String userID, String quizID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("userid", userID);
    params.put("quiz", quizID);
    return listAll(params);
  }

  // Returns UserReferralService.Result for the given browser.
  public Result getCountByBrowser(Browser browser) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("browser", browser);
    List<UserReferal> list = listAll(params);
    long count = list.size();

    Set<String> quizPerformanceIds = new HashSet<String>();
    for (UserReferal userReferal : list) {
      quizPerformanceIds.add(
          QuizPerformance.generateId(userReferal.getQuiz(), userReferal.getUserid()));
    }
    return new Result(count, quizPerformanceIds);
  }

  public class Result {
    // Number of UserReferal that comes to Quizz on this browser. This counts multiple visits from
    // the same userids.
    private long count;
    // Set of quiz performance ids representing (user, quiz) pairs for the browser given.
    private Set<String> quizPerformanceIds;

    public Result(long count, Set<String> quizPerformanceIds) {
      this.count = count;
      this.quizPerformanceIds = quizPerformanceIds;
    }

    public long getCount() {
      return count;
    }

    public Set<String> getQuizPerformanceIds() {
      return quizPerformanceIds;
    }
  }
}
