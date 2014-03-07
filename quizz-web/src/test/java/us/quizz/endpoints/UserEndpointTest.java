package us.quizz.endpoints;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import us.quizz.di.CommonModule;
import us.quizz.di.EndpointsModule;
import us.quizz.di.WebModule;
import us.quizz.entities.User;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.bitwalker.useragentutils.Browser;

public class UserEndpointTest {
	
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
	public void testGetUser(){
		UserEndpoint userEndpoint = i.getInstance(UserEndpoint.class);
		HttpServletRequest req = mock(HttpServletRequest.class);
		
		when(req.getRemoteAddr()).thenReturn("192.168.0.1");
		when(req.getParameter("quizID")).thenReturn("testQuizID");
		when(req.getHeader("User-Agent"))
		.thenReturn("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
		
		Map<String, Object> map = userEndpoint.getUser(req, "testId");
		User user = (User)map.get("user");
		System.out.println(user.getUserid());
	}
}
