package us.quizz.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerRepository;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UpdateAnswerBitsStatistics extends HttpServlet {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(UpdateAnswerBitsStatistics.class.getName());
	
	private QuizRepository quizRepository;
	private QuizQuestionRepository quizQuestionRepository;
	private UserAnswerRepository userAnswerRepository;
	private QuizPerformanceRepository quizPerformanceRepository;
	
	@Inject
	public UpdateAnswerBitsStatistics(QuizRepository quizRepository, 
			QuizQuestionRepository quizQuestionRepository, UserAnswerRepository userAnswerRepository,
			QuizPerformanceRepository quizPerformanceRepository){
		this.quizRepository = quizRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.userAnswerRepository = userAnswerRepository;
		this.quizPerformanceRepository = quizPerformanceRepository;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if("true".equals(request.getParameter("all"))){
			List<Quiz> list = quizRepository.getQuizzes();
			Queue queue = QueueFactory.getDefaultQueue();
	
			for (Quiz quiz : list) {
				queue.add(Builder
						.withUrl("/api/updateAnswerBitsStatistics")
						.param("quizID", quiz.getQuizID().toString())
						.retryOptions(RetryOptions.Builder.withTaskRetryLimit(1))
						.method(TaskOptions.Method.GET));
			}
		}else{
			updateStatistics(request.getParameter("quizID"));
		}
	}
	
	private void updateStatistics(String quizID){
		if(quizID == null || quizID.isEmpty()) return;
		
		List<UserAnswer> userAnswers = userAnswerRepository.getUserAnswers(quizID);
		if(userAnswers == null) return;
			
		Map<String, Double> userBitsPerAnswer = getAvgBitsPerUserId(quizID);
		
		Map<Long, Map<Integer, List<String>>> questionsMap = getQuestionAnswerMap(userAnswers);
		// if(questionsMap.size() == 0) return;	
		
		List<Key> keys = new ArrayList<Key>();
		for(Long questionId : questionsMap.keySet()) {
			keys.add(KeyFactory.createKey(Question.class.getSimpleName(), questionId));
		}
		
		List<Question> questions = quizQuestionRepository.getQuizQuestionsByKeys(keys);
		for(Question question : questions){
			Map<Integer, List<String>> answersMap = questionsMap.get(question.getID());
			for(Map.Entry<Integer, List<String>> entry : answersMap.entrySet()){
				if(question.getAnswers() != null && entry.getKey()>=0 && question.getAnswers().size()>entry.getKey()){
					Answer answer = question.getAnswers().get(entry.getKey());
					
					if(answer == null) {
						// TODO: This can happen only for user-submitted free text answers
						// We should create a new Answer object and store it in the datastore
						// and we should also add the answer object in the list of answers for the 
						// parent Question object.
						continue;
					}
					
					double bits = 0.0d;
					for(String userId : entry.getValue()){
						if(userBitsPerAnswer.containsKey(userId + "_" + quizID))
							bits += userBitsPerAnswer.get(userId + "_" + quizID);
					}
					answer.setBits(bits);
					answer.setNumberOfPicks(Long.valueOf(entry.getValue().size()));
				
				}
			}
		}			
	}

	private Map<String, Double> getAvgBitsPerUserId(String quizID) {
		Map<String, Double> avgUserBitsMap = new HashMap<String, Double>();
		List<QuizPerformance> quizPerfomances = quizPerformanceRepository.getQuizPerformances(quizID);
		for(QuizPerformance qp : quizPerfomances) {
			double bits = qp.getTotalanswers()==0 ? 0:qp.getScore()/qp.getTotalanswers();
			avgUserBitsMap.put(qp.getUserid() + "_" + qp.getQuiz(), bits);
		}
		return avgUserBitsMap;
	}

	private Map<Long, Map<Integer, List<String>>> getQuestionAnswerMap(
			List<UserAnswer> userAnswers) {
		Map<Long, Map<Integer, List<String>>> questionsMap = new HashMap<Long, Map<Integer,List<String>>>();
		
		for(UserAnswer ua : userAnswers) {
			Map<Integer, List<String>> answersMap;
			if(questionsMap.containsKey(ua.getQuestionID())){
				answersMap = questionsMap.get(ua.getQuestionID());
				if(answersMap.containsKey(ua.getAnswerID())){
					answersMap.get(ua.getAnswerID()).add(ua.getUserid());
				}else
					answersMap.put(ua.getAnswerID(), new ArrayList<String>(Arrays.asList(ua.getUserid())));
			}else{
				answersMap = new HashMap<Integer, List<String>>();
				answersMap.put(ua.getAnswerID(), new ArrayList<String>(Arrays.asList(ua.getUserid())));
				
				questionsMap.put(ua.getQuestionID(), answersMap);
			}
		}
		return questionsMap;
	}
	
}
