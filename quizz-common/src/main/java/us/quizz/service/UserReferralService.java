package us.quizz.service;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

import nl.bitwalker.useragentutils.Browser;

import us.quizz.entities.DomainStats;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserReferal;
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

public class UserReferralService {
  private UserReferralRepository userReferralRepository;
  private DomainStatsRepository domainStatsRepository;

  @Inject
  public UserReferralService(UserReferralRepository userReferralRepository,
      DomainStatsRepository domainStatsRepository){
    this.userReferralRepository = userReferralRepository;
    this.domainStatsRepository = domainStatsRepository;
  }

  public List<UserReferal> list(){
    return userReferralRepository.list();
  }

  public CollectionResponse<UserReferal> listWithCursor(String cursor, Integer limit){
    return userReferralRepository.listWithCursor(cursor, limit);
  }

  public UserReferal get(Long id){
    return userReferralRepository.get(id);
  }

  public UserReferal save(UserReferal userReferal){
    return userReferralRepository.saveAndGet(userReferal);
  }

  public void delete(Long id) {
    userReferralRepository.delete(id);
  }

  public Set<String> getUserIDsByQuiz(String quizID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quiz", quizID);

    TreeSet<String> userids = new TreeSet<String>();
    List<UserReferal> results = userReferralRepository.listAll(params);
    for (UserReferal ur : results) {
      userids.add(ur.getUserid());
    }
    return userids;
  }

  public void createAndStoreUserReferal(HttpServletRequest req, String userid) {
    UserReferal ur = new UserReferal(userid);
    ur.setQuiz(req.getParameter("quizID"));
    ur.setIpaddress(req.getRemoteAddr());
    ur.setBrowser(Browser.parseUserAgentString(req.getHeader("User-Agent")));
    String referer = UrlUtils.extractUrl(req.getHeader("Referer"));
    ur.setReferer(referer);
    ur.setDomain(UrlUtils.extractDomain(referer));
    userReferralRepository.save(ur);

    if (ur.getDomain() != null) {
      DomainStats domainStats = domainStatsRepository.get(ur.getDomain());
      if (domainStats == null) {
        domainStats = new DomainStats(ur.getDomain(), 0, 0);
      }
      domainStats.incUserCount();
      domainStatsRepository.save(domainStats);
    }
  }

  public Result getCountByBrowser(Browser browser) {
    Set<String> users = new HashSet<String>();
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("browserParam", browser);

    List<UserReferal> list = userReferralRepository.listAll(params);
    long count = list.size();
    for (UserReferal ref : list) {
      users.add(QuizPerformance.generateId(ref.getQuiz(), ref.getUserid()));
    }

    return new Result(count, users);
  }

  public class Result {
    private long count;
    private Set<String> users;

    public Result(long count, Set<String> users) {
      this.count = count;
      this.users = users;
    }

    public long getCount() {
      return count;
    }

    public Set<String> getUsers() {
      return users;
    }
  }
}
