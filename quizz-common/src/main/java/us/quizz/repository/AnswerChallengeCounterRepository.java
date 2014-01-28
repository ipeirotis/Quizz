package us.quizz.repository;

import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.utils.PMF;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.common.base.Strings;

public class AnswerChallengeCounterRepository {

	public static AnswerChallengeCounter get(String quizID, Long questionID){
		return PMF.singleGetObjectById(AnswerChallengeCounter.class, 
				AnswerChallengeCounter.generateKey(quizID, questionID));
	}
	
	public static void save(AnswerChallengeCounter challengeCounter) {
		PMF.singleMakePersistent(challengeCounter);
	}
	
	@SuppressWarnings("unchecked")
	public static List<AnswerChallengeCounter> list(String cursorString, Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<AnswerChallengeCounter> result = null;
		try {
			mgr = PMF.getPM();
			Query query = mgr.newQuery(AnswerChallengeCounter.class);
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

			result = (List<AnswerChallengeCounter>) query.execute();
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
