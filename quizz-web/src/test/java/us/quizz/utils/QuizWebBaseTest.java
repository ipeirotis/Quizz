package us.quizz.utils;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;

import org.junit.Before;

public class QuizWebBaseTest extends QuizBaseTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
          new LocalTaskQueueTestConfig()
              .setQueueXmlPath("src/main/webapp/WEB-INF/queue.xml")
              .setDisableAutoTaskExecution(true),
          new LocalMemcacheServiceTestConfig(),
          new LocalUserServiceTestConfig().setOAuthIsAdmin(true));

  @Before
  public void setUp() {
    super.setUp();
    helper.setUp();
  }
}
