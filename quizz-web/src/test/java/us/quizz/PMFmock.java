package us.quizz;

import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public final class PMFmock {
  private static Properties newProperties = new Properties();

  static {
      newProperties.put("javax.jdo.PersistenceManagerFactoryClass",
                        "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
      newProperties.put("javax.jdo.option.ConnectionURL", "appengine");
      newProperties.put("javax.jdo.option.NontransactionalRead", "true");
      newProperties.put("javax.jdo.option.NontransactionalWrite", "true");
      newProperties.put("javax.jdo.option.RetainValues", "true");
      newProperties.put("datanucleus.appengine.autoCreateDatastoreTxns", "true");
      newProperties.put("datanucleus.appengine.allowMultipleRelationsOfSameType", "true");
  }

  private static final PersistenceManagerFactory pmfInstance = JDOHelper
      .getPersistenceManagerFactory(newProperties);

  private PMFmock() {}

  public static PersistenceManager getPM() {
    return pmfInstance.getPersistenceManager();
  }
}
