package us.quizz.servlets;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.BrowserStats;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import eu.bitwalker.useragentutils.Browser;

@SuppressWarnings("serial")
@Singleton
public class UpdateBrowsersStatistics extends HttpServlet {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(UpdateBrowsersStatistics.class.getName());
	
	private QuizRepository quizRepository;
	private BrowserStatsRepository browserStatsRepository;
	private QuizQuestionRepository quizQuestionRepository;
	private UserAnswerRepository userAnswerRepository;
	private QuizPerformanceRepository quizPerformanceRepository;
	private UserReferralRepository userReferralRepository;
	
	@Inject
	public UpdateBrowsersStatistics(QuizRepository quizRepository, 
			QuizQuestionRepository quizQuestionRepository, UserAnswerRepository userAnswerRepository,
			QuizPerformanceRepository quizPerformanceRepository, UserReferralRepository userReferralRepository,
			BrowserStatsRepository browserStatsRepository){
		this.quizRepository = quizRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.userAnswerRepository = userAnswerRepository;
		this.quizPerformanceRepository = quizPerformanceRepository;
		this.userReferralRepository = userReferralRepository;
		this.browserStatsRepository = browserStatsRepository;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if("true".equals(request.getParameter("all"))){
			Queue queue = QueueFactory.getDefaultQueue();
			
			Set<Browser> browsers = new HashSet<Browser>();
			for (Browser browser : Browser.values()) {
				browsers.add(browser);
			}
	
			for (Browser browser : browsers) {
				queue.add(Builder
						.withUrl("/api/updateBrowsersStatistics")
						.param("browser", browser.getGroup().toString())
						.retryOptions(RetryOptions.Builder.withTaskRetryLimit(1))
						.method(TaskOptions.Method.GET));
			}
		}else{
			updateStatistics(request.getParameter("browser"));
		}
	}
	
	private void updateStatistics(String browser){
		Browser b = Browser.valueOf(browser);
		long count = userReferralRepository.getCountByBrowser(b);
		if(count > 0){
			BrowserStats bs = new BrowserStats(b, userReferralRepository.getCountByBrowser(b), 0.0d);
			browserStatsRepository.save(bs);
		}
	}
}
