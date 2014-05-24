package us.quizz.ofy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Quiz;
import us.quizz.enums.QuizKind;
import us.quizz.repository.QuizRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class OfyBaseRepositoryTest {
  // We are testing OfyBaseRepository using the Quiz entity.
  private QuizRepository quizRepository = null;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
          new LocalTaskQueueTestConfig(),
          new LocalMemcacheServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
    quizRepository = new QuizRepository();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private List<Quiz> saveAllQuizzes(int count) {
    List<Quiz> quizzes = new ArrayList<Quiz>();
    for (int i = 0; i < count; ++i) {
      Quiz quiz = new Quiz("Test quiz " + i, "test_quiz_" + i, QuizKind.MULTIPLE_CHOICE);
      quizzes.add(quiz);
    }
    quizRepository.saveAll(quizzes);
    return quizzes;
  }

  @Test
  public void testSave() {
    Quiz quiz = new Quiz("Test quiz", "test_quiz", QuizKind.MULTIPLE_CHOICE);
    assertEquals(0, quizRepository.count());

    Key<Quiz> quizKey = quizRepository.save(quiz);
    assertEquals(1, quizRepository.count());
    assertEquals(Key.create(Quiz.class, "test_quiz"), quizKey);
  }

  @Test
  public void testSaveAndGet() {
    Quiz quiz = new Quiz("Test quiz", "test_quiz", QuizKind.MULTIPLE_CHOICE);
    assertEquals(0, quizRepository.count());

    Quiz actualQuiz = quizRepository.saveAndGet(quiz);
    assertEquals(1, quizRepository.count());
    assertEquals(quiz, actualQuiz);
  }

  @Test
  public void testSaveAllAndGet() {
    int numEntities = 10;
    List<Quiz> quizzes = saveAllQuizzes(numEntities);
    assertEquals(numEntities, quizRepository.count());
    for (int i = 0; i < numEntities; ++i) {
      assertEquals(quizzes.get(i), quizRepository.get("test_quiz_" + i));
    }
  }

  @Test
  public void testDeleteSingle() {
    Quiz quiz = new Quiz("Test quiz", "test_quiz", QuizKind.MULTIPLE_CHOICE);
    assertEquals(0, quizRepository.count());
    
    quizRepository.save(quiz);
    assertEquals(1, quizRepository.count());

    quizRepository.delete(quiz);
    quizRepository.flush();  // delete is asynchronous, this forces it to complete.

    assertEquals(0, quizRepository.count());
    assertNull(quizRepository.get("test_quiz"));
  }

  @Test
  public void testDeleteMultiple() {
    int numEntities = 10;
    List<Quiz> quizzes = saveAllQuizzes(numEntities);
    assertEquals(numEntities, quizRepository.count());

    quizRepository.delete(quizzes.subList(0, 5));
    quizRepository.flush();  // delete is asynchronous, this forces it to complete.

    assertEquals(5, quizRepository.count());
    assertNotNull(quizRepository.get("test_quiz_6"));
    assertNull(quizRepository.get("test_quiz_4"));
  }

  @Test
  public void testDeleteKey() {
    Quiz quiz = new Quiz("Test quiz", "test_quiz", QuizKind.MULTIPLE_CHOICE);
    assertEquals(0, quizRepository.count());

    Key<Quiz> quizKey = quizRepository.save(quiz);
    assertEquals(1, quizRepository.count());

    quizRepository.delete(quizKey);
    quizRepository.flush();  // delete is asynchronous, this forces it to complete.

    assertNull(quizRepository.get(quizKey));
    assertEquals(0, quizRepository.count());
  }

  @Test
  public void testDeleteId() {
    Quiz quiz = new Quiz("Test quiz", "test_quiz", QuizKind.MULTIPLE_CHOICE);
    assertEquals(0, quizRepository.count());

    quizRepository.save(quiz);
    assertEquals(1, quizRepository.count());

    quizRepository.delete("test_quiz");
    quizRepository.flush();  // delete is asynchronous, this forces it to complete.

    assertNull(quizRepository.get("test_quiz"));
    assertEquals(0, quizRepository.count());
  }

  @Test
  public void testDeleteAllKeys() {
    int numEntities = 10;
    saveAllQuizzes(numEntities);
    assertEquals(numEntities, quizRepository.count());

    List<Key<Quiz>> quizzesKey = new ArrayList<Key<Quiz>>();
    for (int i = 0; i < 5; ++i) {
      quizzesKey.add(Key.create(Quiz.class, "test_quiz_" + i));
    }
    quizRepository.deleteAll(quizzesKey);
    quizRepository.flush();  // delete is asynchronous, this forces it to complete.

    assertNull(quizRepository.get("test_quiz_1"));
    assertNotNull(quizRepository.get("test_quiz_8"));
    assertEquals(5, quizRepository.count());
  }

  @Test(expected = NotFoundException.class)
  public void testSafeGet() {
    quizRepository.safeGet("test_quiz");
  }

  @Test
  public void testGetAll() {
    saveAllQuizzes(10);

    List<Key<Quiz>> quizzesKey = new ArrayList<Key<Quiz>>();
    for (int i = 0; i < 5; ++i) {
      quizzesKey.add(Key.create(Quiz.class, "test_quiz_" + i));
    }
    Collection<Quiz> newQuizzes = quizRepository.getAll(quizzesKey);
    assertEquals(5, newQuizzes.size());

    quizzesKey = new ArrayList<Key<Quiz>>();
    for (int i = 0; i < 5; ++i) {
      quizzesKey.add(Key.create(Quiz.class, "fake_quiz_" + i));
    } 
    newQuizzes = quizRepository.getAll(quizzesKey);
    assertEquals(0, newQuizzes.size());
  }

  @Test
  public void testGetByProperty() {
    saveAllQuizzes(10);
    assertNotNull(quizRepository.getByProperty("name", "Test quiz 5"));
    assertNotNull(quizRepository.getByProperty("numChoices", 4));
    assertNull(quizRepository.getByProperty("showOnDefault", true));
  }

  @Test
  public void testCount() {
    saveAllQuizzes(1234);
    assertEquals(1234, quizRepository.count());
  }

  @Test
  public void testCountByProperty() {
    for (int i = 0; i < 100; ++i) {
      Quiz quiz = new Quiz("Test quiz " + i,
                           "test_quiz_" + i,
                           i % 2 == 0 ? QuizKind.MULTIPLE_CHOICE : QuizKind.FREE_TEXT);
      if (i % 4 == 0) {
        quiz.setNumChoices(2);
      }
      quiz.setShowOnDefault(i % 9 == 0 ? true : false);
      quizRepository.save(quiz);
    }

    assertEquals(50, quizRepository.countByProperty("kind", QuizKind.FREE_TEXT));
    assertEquals(50, quizRepository.countByProperty("kind", QuizKind.MULTIPLE_CHOICE));

    assertEquals(25, quizRepository.countByProperty("numChoices", 4));
    assertEquals(25, quizRepository.countByProperty("numChoices", 2));
    assertEquals(50, quizRepository.countByProperty("numChoices", null));

    assertEquals(12, quizRepository.countByProperty("showOnDefault", true));
    assertEquals(88, quizRepository.countByProperty("showOnDefault", false));
  }

  @Test
  public void testListByIds() {
    saveAllQuizzes(25);
    List<String> ids = new ArrayList<String>();
    for (int i = 0; i < 20; ++i) {
      ids.add("test_quiz_" + i);
      ids.add("fake_quiz_" + i);
    }

    List<Quiz> quizzes = quizRepository.listByStringIds(ids);
    assertEquals(20, quizzes.size());
  }

  @Test
  public void testListAllByChunk() {
    saveAllQuizzes(1200);
    List<Quiz> quizzes = quizRepository.listAllByChunk();
    // Note(chunhowt): .chunk(size) doesn't limit the # results returned.
    assertEquals(1200, quizzes.size());
  }

  @Test
  public void testListAllByCursor() {
    saveAllQuizzes(1200);
    List<Quiz> quizzes = quizRepository.listAllByCursor();
    assertEquals(1200, quizzes.size());
  }

  @Test
  public void testListAllByCursorWithParams() {
    for (int i = 0; i < 5000; ++i) {
      Quiz quiz = new Quiz("Test quiz " + i,
                           "test_quiz_" + i,
                           QuizKind.MULTIPLE_CHOICE);
      if (i % 4 == 0) {
        quiz.setNumChoices(2);
      }
      quiz.setShowOnDefault(i % 2 == 0 ? true : false);
      quizRepository.save(quiz);
    }

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("showOnDefault", true);
    params.put("numChoices", 2);

    List<Quiz> quizzes = quizRepository.listAllByCursor(params);
    assertEquals(1250, quizzes.size());
    for (Quiz quiz : quizzes) {
      assertEquals((Integer)2, quiz.getNumChoices());
      assertTrue(quiz.getShowOnDefault());
    }
  }

  @Test
  public void testListAllByCursorWithNotEqualParams() {
    saveAllQuizzes(1200);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("showOnDefault !=", true);

    List<Quiz> quizzes = quizRepository.listAllByCursor(params);
    // Note(chunhowt): For filter with "not equal" operator, cursor doesn't work
    // and thus we only fetch the first 1000 results.
    assertEquals(1000, quizzes.size());
  }

  @Test
  public void testListAllByChunkWithNotEqualParams() {
    saveAllQuizzes(1200);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("showOnDefault !=", true);

    List<Quiz> quizzes = quizRepository.listAllByChunk(params);
    // For filter with "not equal" operator, list by chunking will work and get all the results.
    assertEquals(1200, quizzes.size());
  }

  @Test
  public void testListByCursor() {
    saveAllQuizzes(1200);
    String cursor = null;
    Integer limit = 1000;
    List<Quiz> results = new ArrayList<Quiz>();

    while (true) {
      CollectionResponse<Quiz> response = quizRepository.listByCursor(cursor, limit);
      results.addAll(response.getItems());
      cursor = response.getNextPageToken();
      if (cursor == null) {
        break;
      }
    }
    assertEquals(1200, results.size());
  }

  @Test
  public void testListByProperty() {
    for (int i = 0; i < 2500; ++i) {
      Quiz quiz = new Quiz("Test quiz " + i,
                           "test_quiz_" + i,
                           QuizKind.MULTIPLE_CHOICE);
      quiz.setShowOnDefault(i % 2 == 0 ? true : false);
      quizRepository.save(quiz);
    }
    List<Quiz> quizzes = quizRepository.listAllByProperty("showOnDefault", true);
    assertEquals(1250, quizzes.size());
    for (Quiz quiz : quizzes) {
      assertTrue(quiz.getShowOnDefault());
    }
  }
}
