package com.ipeirotis.crowdquiz.utils;

import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public final class PMF {

	private static final PersistenceManagerFactory	pmfInstance	= JDOHelper.getPersistenceManagerFactory("transactions-optional");

	private PMF() {
	}

	public static PersistenceManagerFactory get() {
		return pmfInstance;
	}
	
	public static PersistenceManager getPM() {
		return pmfInstance.getPersistenceManager();
	}
	
	public static void singleStore(Object... items) {
		PersistenceManager pm = getPM();
		pm.makePersistentAll(items);
		pm.close();
	}
	
	public static void singleStore(Collection<?> items) {
		PersistenceManager pm = getPM();
		pm.makePersistentAll(items);
		pm.close();
	}
}