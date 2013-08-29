package us.quizz.repository;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import com.ipeirotis.crowdquiz.entities.UserReferal;
import com.ipeirotis.crowdquiz.utils.PMF;

public class UserReferralRepository {

	public static Set<String> getUserIDsByQuiz(String quizid) {
		PersistenceManager pm = PMF.getPM();
		Query query = pm.newQuery(UserReferal.class);
		query.setFilter("quiz == quizParam");
		query.declareParameters("String quizParam");
		
		TreeSet<String> userids = new TreeSet<String>();
		int limit = 1000;
		int i=0;
		while (true) {
			query.setRange(i, i+limit);
			@SuppressWarnings("unchecked")
			List<UserReferal> results = (List<UserReferal>) query.execute(quizid);
			if (results.size()==0) break;
			for (UserReferal ur : results) {
				userids.add(ur.getUserid());
			}
			i+=limit;
		}
		pm.close();
		
		return userids;
	}
	
	public static void createAndStoreUserReferal(HttpServletRequest req, String userid) {

		UserReferal ur = new UserReferal(userid);
		ur.setQuiz(req.getParameter("relation"));
		ur.setReferer(req.getHeader("Referer"));
		ur.setIpaddress(req.getRemoteAddr());
		ur.setBrowser(req.getHeader("User-Agent"));
		
		PersistenceManager pm = PMF.getPM();
		pm.makePersistent(ur);
		pm.close();
	}
	
	
	
	
	
}
