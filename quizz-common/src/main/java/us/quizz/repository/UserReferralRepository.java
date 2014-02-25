package us.quizz.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import us.quizz.entities.DomainStats;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserReferal;
import us.quizz.utils.PMF;
import us.quizz.utils.UrlUtils;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import eu.bitwalker.useragentutils.Browser;

public class UserReferralRepository extends BaseRepository<UserReferal>{
	
	public UserReferralRepository() {
		super(UserReferal.class);
	}
	
	@Override
	protected Key getKey(UserReferal item) {
		return item.getKey();
	}

	public Set<String> getUserIDsByQuiz(String quizid) {
		PersistenceManager pm = PMF.getPM();
		Query query = pm.newQuery(UserReferal.class);
		query.setFilter("quiz == quizParam");
		query.declareParameters("String quizParam");

		TreeSet<String> userids = new TreeSet<String>();
		int limit = 1000;
		int i = 0;
		while (true) {
			query.setRange(i, i + limit);
			@SuppressWarnings("unchecked")
			List<UserReferal> results = (List<UserReferal>) query
					.execute(quizid);
			if (results.size() == 0)
				break;
			for (UserReferal ur : results) {
				userids.add(ur.getUserid());
			}
			i += limit;
		}
		pm.close();

		return userids;
	}

	public void createAndStoreUserReferal(HttpServletRequest req,
			String userid) {

		UserReferal ur = new UserReferal(userid);
		ur.setQuiz(req.getParameter("quizID"));
		ur.setIpaddress(req.getRemoteAddr());
		ur.setBrowser(Browser.parseUserAgentString(req.getHeader("User-Agent")));
		String referer = UrlUtils.extractUrl(req.getHeader("Referer"));
		ur.setReferer(referer);
		ur.setDomain(UrlUtils.extractDomain(referer));

		PMF.singleMakePersistent(ur);
		
		if(ur.getDomain() != null){
			DomainStats domainStats = 
					PMF.singleGetObjectById(DomainStats.class, ur.getDomain());
			
			if(domainStats == null)
				domainStats = new DomainStats(ur.getDomain(), 0, 0);
			
			domainStats.incUserCount();
			PMF.singleMakePersistent(domainStats);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Result getCountByBrowser(Browser browser){

		PersistenceManager mgr = null;
		long count = 0;
		Set<Key> users = new HashSet<Key>();
		Cursor cursor = null;
		List<UserReferal> list = null;
		
		try {
			mgr = PMF.getPM();
			while (true) {
				Query q = mgr.newQuery(UserReferal.class);
				q.setFilter("browser == browserParam");
				q.declareParameters("String browserParam");
				if (cursor != null) {
					HashMap<String, Object> extensionMap = new HashMap<String, Object>();
					extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
					q.setExtensions(extensionMap);
				}
	
				q.setRange(0, 1000);
				list = (List<UserReferal>) q.execute(browser);
				cursor = JDOCursorHelper.getCursor(list);
				
				if (list.size() == 0)
					break;
	
				count += list.size();
				for(UserReferal ref : list){
					users.add(KeyFactory.createKey(QuizPerformance.class.getSimpleName(), 
						"id_" + ref.getUserid() + "_" + ref.getQuiz()));
				}
			}

		} finally {
			mgr.close();
		}

		return new Result(count, users);
	}
	
	public class Result{
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
