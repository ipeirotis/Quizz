package com.ipeirotis.crowdquiz.utils;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import java.util.logging.Logger;

public final class PMF {
	
	final static Logger logger = Logger.getLogger(PMF.class.getCanonicalName());

	private static final PersistenceManagerFactory	pmfInstance	= JDOHelper.getPersistenceManagerFactory("transactions-optional");

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
		pm.makePersistentAll(items);
		pm.close();
	}
	
	public static <T> T singleGetObjectById(Class<T> cls, Object key){
		PersistenceManager pm = getPM();
		try {
			return pm.getObjectById(cls, key);
		} catch (JDOObjectNotFoundException ex) {
			logger.warning("PM: Didn't found object: " + cls.getCanonicalName() +
					" , key: " + key.toString());
			return null;
		} finally {
			pm.close();
		}
	}
}