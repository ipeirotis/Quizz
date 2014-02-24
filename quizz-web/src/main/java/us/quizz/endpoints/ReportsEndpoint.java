package us.quizz.endpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import us.quizz.entities.BrowserStats;
import us.quizz.entities.DomainStats;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.repository.DomainStatsRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class ReportsEndpoint {
	
	private QuizRepository quizRepository;
	private QuizQuestionRepository quizQuestionRepository;
	private BrowserStatsRepository browserStatsRepository;
	private DomainStatsRepository domainStatsRepository;
	
	@Inject
	public ReportsEndpoint(QuizRepository quizRepository,
			QuizQuestionRepository quizQuestionRepository,
			BrowserStatsRepository browserStatsRepository,
			DomainStatsRepository domainStatsRepository){
		this.quizRepository = quizRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.browserStatsRepository = browserStatsRepository;
		this.domainStatsRepository = domainStatsRepository;
	}

	@ApiMethod(name = "reports.answers", path="reports/answers")
	public ArrayList<Question> getAnswerReport(@Named("quizID")String quizID) {
		return quizQuestionRepository.getQuizQuestions(quizID);
	}
	
	@ApiMethod(name = "reports.scoreByBrowser", path="reports/scoreByBrowser")
	public List<BrowserStats> getScoreByBrowserReport() {
		return browserStatsRepository.list();
	}
	
	@ApiMethod(name = "reports.scoreByDomain", path="reports/scoreByDomain")
	public List<DomainStats> getScoreByDomainReport() {
		return domainStatsRepository.list();
	}
	
	@ApiMethod(name = "reports.contributionQuality", path="reports/contributionQuality")
	public List<Map<String, Object>> getContributionQualityReport() {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<Quiz> quizzes = quizRepository.list();
		
		for(Quiz quiz : quizzes){
			Map<String, Object> item = new HashMap<String, Object>();
			
			item.put("quiz", quiz);
			item.put("capacity99", quiz.getCapacity(0.01));
			item.put("capacity95", quiz.getCapacity(0.05));
			item.put("capacity90", quiz.getCapacity(0.10));
			
			result.add(item);
		}
		return result;
	}

}