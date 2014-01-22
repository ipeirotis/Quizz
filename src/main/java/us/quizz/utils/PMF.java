package us.quizz.utils;

import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public final class PMF {

	final static Logger logger = Logger.getLogger(PMF.class.getCanonicalName());

	private static final PersistenceManagerFactory pmfInstance = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	private PMF() {
	}

	public static PersistenceManagerFactory get() {
		return pmfInstance;
	}

	public static PersistenceManager getPM() {
		return pmfInstance.getPersistenceManager();
	}

	public static void singleMakePersistent(Object... items) {
		PersistenceManager pm = getPM();
		try {
			pm.makePersistentAll(items);
		} finally {
			pm.close();
		}
	}

	public static <T> T singleMakePersistent(T item) {
		PersistenceManager pm = getPM();
		try {
			return pm.makePersistent(item);
		} finally {
			pm.close();
		}
	}

	public static void makePersistentIterative(Object... items) {
		PersistenceManager pm = getPM();
		try {
			for (Object item : items) {
				pm.makePersistent(item);
			}
		} finally {
			pm.close();
		}
	}

	public static <T> T singleGetObjectById(Class<T> cls, Object key) {
		try {
			return singleGetObjectByIdThrowing(cls, key);
		} catch (JDOObjectNotFoundException ex) {
			logger.warning("PM: Didn't found object: " + cls.getCanonicalName()
					+ " , key: " + key.toString());
			return null;
		}
	}

	public static <T> T singleGetObjectByIdThrowing(Class<T> cls, Object key) {
		PersistenceManager pm = getPM();
		try {
			return pm.getObjectById(cls, key);
		} finally {
			pm.close();
		}
	}

	public static <T> T singleGetObjectByIdWithCaching(String cacheKey,
			Class<T> cls, Object key) {
		T item = CachePMF.get(cacheKey, cls);
		if (item != null)
			return item;
		item = singleGetObjectById(cls, key);
		if (item != null)
			CachePMF.put(cacheKey, item);
		return item;
	}
}