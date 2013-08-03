package com.ipeirotis.crowdquiz.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.math3.special.Gamma;

import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.UserAnswer;


public class Helper {


	public static String getBaseURL(HttpServletRequest req) {
		String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
		return baseURL;
	}
	

	
	/**
	 * Returns the next question for the user. Checks all the previously given answers by the user
	 * to avoid returning a question for which we already have an answer from the user. The parameter
	 * justAddedMid ensures that we do not return the currently asked question, even if the relation
	 * has not persisted in the datastore yet.
	 * 
	 * 
	 * @param relation
	 * @param userid
	 * @param justAddedMid
	 * @param pm
	 * @return
	 */
	public static String getNextMultipleChoiceURL(HttpServletRequest req, String relation, String userid, String justAddedMid) {
		
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		
		String key = "quizquestions_"+relation;
		Set<String> availableQuestions = CachePMF.get(key, Set.class);
		if (availableQuestions==null) {
			String query = "SELECT FROM " + QuizQuestion.class.getName() 
								+ " WHERE relation=='" + relation + "'"
								+ " && hasGoldAnswer==true";
	
			@SuppressWarnings("unchecked")
			List<QuizQuestion> questions = (List<QuizQuestion>) pm.newQuery(query).execute();
			availableQuestions = new HashSet<String>();
			for (QuizQuestion q : questions) {
				availableQuestions.add(q.getFreebaseEntityId());
			}
			CachePMF.put(key,availableQuestions);
		}

		/*
		String queryGivenAnswers = "SELECT FROM " + UserAnswer.class.getName() + " WHERE userid=='" + userid
				+ "' && relation=='" + relation + "'";

		@SuppressWarnings("unchecked")
		List<UserAnswer> answers = (List<UserAnswer>) pm.newQuery(queryGivenAnswers).execute();
		Set<String> alreadyAnswered = new HashSet<String>();
		for (UserAnswer ue : answers) {
			alreadyAnswered.add(ue.getMid());
		}
		if (justAddedMid!=null) {
			alreadyAnswered.add(justAddedMid);
		}
		availableQuestions.removeAll(alreadyAnswered);
		*/
		
		pm.close();
		
		
		
		String nextURL = "/";
		if (availableQuestions.isEmpty()) {
			return nextURL;
		}
		
		
			ArrayList<String> list = new ArrayList<String>(availableQuestions);
			int rnd = (int)Math.round(Math.random()*availableQuestions.size());
			if (rnd<0) rnd=0;
			if (rnd>=availableQuestions.size()) rnd = availableQuestions.size()-1;
			String mid = list.get(rnd);
			
			try {
				nextURL = "/multChoice.jsp?" 
						+ "&numoptions=4" 
						+ "&relation=" + URLEncoder.encode(relation, "UTF-8")  
						+ "&mid=" + URLEncoder.encode(mid, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return getBaseURL(req) + nextURL;
			

	}


	/**
	 * Computing the entropy of an answer given by a user with quality q (quality=probability of correct)
	 * and n available options in the multiple choice question
	 * @throws Exception 
	 * 
	 */
	public static double entropy(double q, int n) throws Exception {
		if (q==1.0) return 0;
		if (q==0.0) return Math.log(1.0/(n-1))/Math.log(2);
		if (n==1) return 0;
		if (n<1) throw new Exception("Invalid value for n in entropy calculation");
		if (q<0.0 || q>1.0) throw new Exception("Invalid value for q in entropy calculation");
		double entropy = (1-q) * Math.log((1-q)/(n-1))/Math.log(2)+ q*Math.log(q)/Math.log(2);
		return entropy;
	}
	
	/**
	 * The score is the total amount of information contributed by the user.
	 * 
	 * We compute the information gain for a single answer of quality q,
	 * and multiply with the total number of answers given
	 * @throws Exception 
	 * 
	 * 
	 */
	public static double getInformationGain(double q, int n) throws Exception {
		double informationGain = Helper.entropy(q,n) - Helper.entropy(1.0/n, n);
		return informationGain;
	}
	
	/**
	 * Computes a Bayesian version of Information Gain
	 * 
	 * @param a The number of correct answers (plus the prior)
	 * @param b The number of incorrect answers (plus the prior)
	 * @param n The number of multiple options in the quiz
	 * @return
	 * @throws Exception
	 */
	public static double getBayesianInformationGain(double a, double b, int n) throws Exception {
	
		// We have that limit(x*Psi(x), x=0) = -1
		// Based on this, for the Bayesian Information Gain (BIG): 
		// BIG(0,b,n) = lg2(n) - lg2(n-1) 
		// BIG(a,0,n) = lg2(n)
		// This means that BIG(0,0) does not have a value when the prior it Beta(0,0)
		
		if (a<0 || b<0 || n<=1) throw new Exception("Unsupported parameters values");
		if (a==0 && b==0 && n>2) throw new Exception("Undefined value");
		if (a==0 && b==0 && n==2) return 1;
		if (a==0 && b>0) return Math.log(n/(n-1))/Math.log(2);
		if (a>0 && b==0) return Math.log(n)/Math.log(2);
		
		double nominator = a*Gamma.digamma(a) + b*Gamma.digamma(b) - (a+b) * Gamma.digamma(a+b) + (a+b)*Math.log(n) - b* Math.log(n-1) + 1;
		double denominator = (a+b) * Math.log(2);
		
		return nominator / denominator;
		
	}
	
	/**
	 * Computes a Bayesian version of Information Gain
	 * 
	 * @param a The number of correct answers (plus the prior)
	 * @param b The number of incorrect answers (plus the prior)
	 * @param n The number of multiple options in the quiz
	 * @return
	 * @throws Exception
	 */
	public static double getBayesianVarianceInformationGain(double a, double b, int n) throws Exception {
	

		double coef_ab = (a*b)/((a+b)*(a+b+1));
		double coef_a = a*(a+1)/((a+b+1)*(a+b));
		double coef_b = b*(b+1)/((a+b+1)*(a+b));
		
		double Ja = Math.pow(Gamma.digamma(a+2) - Gamma.digamma(a+b+2), 2) + Gamma.trigamma(a+2) - Gamma.trigamma(a+b+2); 
		double Jb = Math.pow(Gamma.digamma(b+2) - Gamma.digamma(a+b+2), 2) + Gamma.trigamma(b+2) - Gamma.trigamma(a+b+2); 
		double Iab = (Gamma.digamma(a+1) - Gamma.digamma(a+b+2)) * (Gamma.digamma(b+1) - Gamma.digamma(a+b+2)) - Gamma.trigamma(a+b+2);
		
		//double f1 = Math.log(n-1)*Math.log(n-1)*b*(b+1)/((a+b)*(a+b+1));
		
		double h2 = coef_a * Ja + coef_b * Jb + coef_ab * Iab; // + f1;
		double ig = getBayesianInformationGain(a, b, n);
		double result = h2 - ig*ig;
		
		return result;
		
	}
	
	

}
