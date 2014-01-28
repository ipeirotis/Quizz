package us.quizz.repository;

import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.UserReferalCounter;
import us.quizz.utils.PMF;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.common.base.Strings;

public class UserReferalCounterRepository {

	public static UserReferalCounter get(String domain){
		return PMF.singleGetObjectById(UserReferalCounter.class, 
				UserReferalCounter.generateKey(domain));
	}
	
	public static void save(UserReferalCounter userReferalCounter) {
		PMF.singleMakePersistent(userReferalCounter);
	}
	
	@SuppressWarnings("unchecked")
	public static List<UserReferalCounter> list(String cursorString, Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<UserReferalCounter> result = null;
		try {
			mgr = PMF.getPM();
			Query query = mgr.newQuery(UserReferalCounter.class);
			query.setOrdering("count desc");
			if (!Strings.isNullOrEmpty(cursorString)) {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null && limit != 0) {
				query.setRange(0, limit);
			}

			result = (List<UserReferalCounter>) query.execute();
			cursor = JDOCursorHelper.getCursor(result);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();
			else
				cursorString = "";

		} finally {
			mgr.close();
		}
		
		return result;
	}
	
}
