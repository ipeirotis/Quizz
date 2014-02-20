package us.quizz.repository;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import us.quizz.entities.UserReferal;
import us.quizz.entities.UserReferalCounter;
import us.quizz.utils.PMF;
import us.quizz.utils.UrlUtils;

import com.google.appengine.api.datastore.Key;

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
			UserReferalCounter referalCounter = 
					PMF.singleGetObjectById(UserReferalCounter.class, ur.getDomain());
			
			if(referalCounter == null)
				referalCounter = new UserReferalCounter(ur.getDomain());
			
			referalCounter.incCount();
			PMF.singleMakePersistent(referalCounter);
		}
	}

}
