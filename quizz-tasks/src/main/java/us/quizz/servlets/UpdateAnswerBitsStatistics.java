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

import org.apache.commons.math3.special.Gamma;

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
			
		// <userid, {correct, incorrect, information_gain}>
		Map<String, Double[]> userStatistics = getUserStatistics(quizID);
		
		// <questionid, <answerid, {userids}>>
		Map<Long, Map<Integer, List<String>>> questionsMap = getQuestionAnswerMap(userAnswers);
		// if(questionsMap.size() == 0) return;	
		Map<Long, List<String>> questionsUsers = new HashMap<Long, List<String>>();
		for (Long questionId : questionsMap.keySet()) {
			List<String> questionUsers = new ArrayList<String>();
			Map<Integer, List<String>> answerUsers = questionsMap.get(questionId);
			for (Integer answerId : answerUsers.keySet()) {
				questionUsers.addAll(answerUsers.get(answerId));
			}
			questionsUsers.put(questionId, questionUsers);
		}
		
		List<Key> keys = new ArrayList<Key>();
		for(Long questionId : questionsMap.keySet()) {
			keys.add(KeyFactory.createKey(Question.class.getSimpleName(), questionId));
		}
		
		List<Question> questions = quizQuestionRepository.getQuizQuestionsByKeys(keys);
		for(Question question : questions){
			Long questionId = question.getID();
			List<String> allUsersForQuestion = questionsUsers.get(questionId);
			Map<Integer, List<String>> answerUsers = questionsMap.get(questionId);
			for (Integer answerId : answerUsers.keySet()) {
				Answer answer = question.getAnswers().get(answerId);
				if(answer == null) {
					// TODO: This can happen only for user-submitted free text answers
					// We should create a new Answer object and store it in the datastore
					// and we should also add the answer object in the list of answers for the 
					// parent Question object.
					continue;
				}
				
				double bits = 0.0d;
				List<String> userIds = answerUsers.get(answerId);
				for(String userId : userIds){
					if(userStatistics.containsKey(userId + "_" + quizID)) {
						Double userBits = userStatistics.get(userId + "_" + quizID)[2];
						bits += userBits;
					}
				}
				answer.setBits(bits);
				answer.setNumberOfPicks(Long.valueOf(userIds.size()));
				
				// NOTE: The computation of correctness below is applicable ONLY
				// for multiple choice questions. For free-text answers, we need
				// to use the Chinese Table process described by Dan Weld.
				int n = answerUsers.keySet().size();
				double priorlogit = Gamma.digamma(1) - Gamma.digamma(n-1);
				double logit = priorlogit;
				for(String userId: allUsersForQuestion) {
					if(userStatistics.containsKey(userId + "_" + quizID)) {
						Double correct = userStatistics.get(userId + "_" + quizID)[0];
						Double incorrect = userStatistics.get(userId + "_" + quizID)[1];
						
						// Adding pseudocounts for Bayesian prior
						correct += 1;
						incorrect += n-1;
						
						double bayesianLogit = Gamma.digamma(correct) - Gamma.digamma(incorrect);
					
						// if the user selected this answer, we add the q/(1-q) logit.
						// otherwise we add the opposite 
						logit += (answerUsers.containsKey(userId))?  bayesianLogit  : -bayesianLogit;
					}
				}
				answer.setProbCorrect(Math.exp(logit) / (Math.exp(logit)+1) );
				
				
			}
		}			
	}
	
	
	// Returns <UserID, {correct, incorrect, information gain}>
	private Map<String, Double[]> getUserStatistics(String quizID) {
		Map<String, Double[]> result = new HashMap<String, Double[]>();
		List<QuizPerformance> quizPerfomances = quizPerformanceRepository.getQuizPerformances(quizID);
		for(QuizPerformance qp : quizPerfomances) {
			Double a = qp.getCorrectanswers().doubleValue();
			Double b = qp.getIncorrectanswers().doubleValue();
			Double bits = qp.getTotalanswers()==0 ? 0:qp.getScore()/qp.getTotalanswers();
			result.put(qp.getUserid() + "_" + qp.getQuiz(), new Double[]{a,b, bits});
		}
		return result;
	}
	
	// Returns <QuestionID, <AnswerID, {UserIDs}>>
	private Map<Long, Map<Integer, List<String>>> getQuestionAnswerMap(
			List<UserAnswer> userAnswers) {
		
		Map<Long, Map<Integer, List<String>>> result = new HashMap<Long, Map<Integer,List<String>>>();
		
		for(UserAnswer ua : userAnswers) {
			Long questionId = ua.getQuestionID();
			Integer answerId = ua.getAnswerID();
			String userId = ua.getUserid();
			Map<Integer, List<String>> answers = result.get(questionId);
			
			if(answers==null){
				answers = new HashMap<Integer, List<String>>();
			}
	
			if(answers.containsKey(answerId)){
				answers.get(answerId).add(userId);
			} else {
				ArrayList<String> userList = new ArrayList<String>(Arrays.asList(userId));
				answers.put(answerId, userList);
			}
			
			result.put(questionId, answers);
			
			
		}
		return result;
	}
	
}
