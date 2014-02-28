package us.quizz.endpoints;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import us.quizz.di.CommonModule;
import us.quizz.di.EndpointsModule;
import us.quizz.di.WebModule;
import us.quizz.entities.Question;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class QuestionEndpointTest {
	
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
	public void testInsertQuestion(){
		QuestionEndpoint questionEndpoint = i.getInstance(QuestionEndpoint.class);
		Question question = questionEndpoint.insertQuestion(new Question("testQuiz", "How old are you?", 1.0));
		
		Assert.assertEquals(question.getQuizID(), "testQuiz");
	}
}
