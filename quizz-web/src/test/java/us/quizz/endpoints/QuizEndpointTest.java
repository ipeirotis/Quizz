package us.quizz.endpoints;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import us.quizz.di.CommonModule;
import us.quizz.di.EndpointsModule;
import us.quizz.di.WebModule;
import us.quizz.entities.Quiz;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class QuizEndpointTest {
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());
	
	private Injector i = Guice.createInjector(new EndpointsModule(), new WebModule(), new CommonModule());
	
	@Before
	public void setUp() {
		helper.setUp();
	}
	
	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void testInsertQuiz(){
		QuizEndpoint quizEndpoint = i.getInstance(QuizEndpoint.class);
		Quiz quiz = quizEndpoint.insertQuiz(new Quiz("testName", "testQuiz"));
		
		Assert.assertEquals(quiz.getQuizID(), "testQuiz");
	}
}
