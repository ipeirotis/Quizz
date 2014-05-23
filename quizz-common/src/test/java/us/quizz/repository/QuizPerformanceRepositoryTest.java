package us.quizz.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.QuizPerformance;

import java.util.ArrayList;
import java.util.List;

public class QuizPerformanceRepositoryTest {
  private static final String QUIZ_ID = "quizid";

  private QuizPerformanceRepository quizPerformanceRepository = null;
  private List<QuizPerformance> quizPerformances = null;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
          new LocalTaskQueueTestConfig(),
          new LocalMemcacheServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
    quizPerformanceRepository = new QuizPerformanceRepository();
    quizPerformances = new ArrayList<QuizPerformance>();

    for (int i = 1; i <= 100; ++i) {
      QuizPerformance quizPerformance = new QuizPerformance(QUIZ_ID, "userid_" + i);
      quizPerformanceRepository.save(quizPerformance);
      quizPerformances.add(quizPerformance);
    }
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testGet() {
    assertEquals(quizPerformances.get(4),
                 quizPerformanceRepository.get(QUIZ_ID, "userid_5"));
    assertNull(quizPerformanceRepository.get(QUIZ_ID, "fake_userid"));
    assertNull(quizPerformanceRepository.get("fake_quiz_id", "userid_5"));
  }

  @Test
  public void testDelete() {
    assertNotNull(quizPerformanceRepository.get(QUIZ_ID, "userid_5"));
    quizPerformanceRepository.delete(QUIZ_ID, "userid_5");
    quizPerformanceRepository.flush();
    assertNull(quizPerformanceRepository.get(QUIZ_ID, "userid_5"));
  }
}
