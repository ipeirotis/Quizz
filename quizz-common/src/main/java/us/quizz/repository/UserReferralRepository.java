package us.quizz.repository;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import eu.bitwalker.useragentutils.Browser;

import us.quizz.entities.DomainStats;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserReferal;
import us.quizz.utils.UrlUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

public class UserReferralRepository extends BaseRepository<UserReferal> {
  public UserReferralRepository() {
    super(UserReferal.class);
  }

  @Override
  protected Key getKey(UserReferal item) {
    return item.getKey();
  }

  public Set<String> getUserIDsByQuiz(String quizID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = pm.newQuery(UserReferal.class);
      query.setFilter("quiz == quizParam");
      query.declareParameters("String quizParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("quizParam", quizID);

      TreeSet<String> userids = new TreeSet<String>();
      List<UserReferal> results = fetchAllResults(query, params);
      for (UserReferal ur : results) {
        userids.add(ur.getUserid());
      }
      return userids;
    } finally {
      pm.close();
    }
  }

  public void createAndStoreUserReferal(HttpServletRequest req, String userid) {
    UserReferal ur = new UserReferal(userid);
    ur.setQuiz(req.getParameter("quizID"));
    ur.setIpaddress(req.getRemoteAddr());
    ur.setBrowser(Browser.parseUserAgentString(req.getHeader("User-Agent")));
    String referer = UrlUtils.extractUrl(req.getHeader("Referer"));
    ur.setReferer(referer);
    ur.setDomain(UrlUtils.extractDomain(referer));

    singleMakePersistent(ur);

    if (ur.getDomain() != null) {
      DomainStats domainStats = singleGetObjectById(DomainStats.class, ur.getDomain());
      if (domainStats == null) {
        domainStats = new DomainStats(ur.getDomain(), 0, 0);
      }
      domainStats.incUserCount();
      singleMakePersistent(domainStats);
    }
  }

  @SuppressWarnings("unchecked")
  public Result getCountByBrowser(Browser browser) {
    PersistenceManager mgr = getPersistenceManager();
    long count = 0;
    Set<Key> users = new HashSet<Key>();
    List<UserReferal> list = null;

    try {
      Query q = mgr.newQuery(UserReferal.class);
      q.setFilter("browser == browserParam");
      q.declareParameters("String browserParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("browserParam", browser);

      list = fetchAllResults(q, params);
      count = list.size();
      for (UserReferal ref : list) {
        users.add(KeyFactory.createKey(QuizPerformance.class.getSimpleName(),
            "id_" + ref.getUserid() + "_" + ref.getQuiz()));
      }
    } finally {
      mgr.close();
    }
    return new Result(count, users);
  }

  public class Result {
    private long count;
    private Set<Key> users;

    public Result(long count, Set<Key> users) {
      this.count = count;
      this.users = users;
    }

    public long getCount() {
      return count;
    }

    public Set<Key> getUsers() {
      return users;
    }
  }
}
