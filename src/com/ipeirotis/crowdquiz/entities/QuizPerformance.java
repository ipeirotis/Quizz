package com.ipeirotis.crowdquiz.entities;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.UserAnswerRepository;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.ipeirotis.crowdquiz.utils.Helper;

/**
 * Keeps track of the performance of a user within a Quiz. This is a "caching" object that aggregates the results
 * from the underlying UserAnswer objects. In this object, we keep track of the number of total and correct answers that 
 * a given user submitted for the quiz, the "score" of the user (the Bayesian Information Gain compared to random choice)
 * and the relative rank of the user compared to other users.
 * 
 * The two key functions are the compute and computeRank. The first one is a relatively lightweight function that goes
 * through all the "UserAnswer" objects for the user-quiz combination, and examine the number of correct and incorrect
 * answers, and the computes the user score. The computeRank performs a comparison of the user scores against the scores
 * of all the other users that participated in the quiz, and computes the relative rank of the user within the group.
 * 
 * @author ipeirotis
 * 
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class QuizPerformance {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	// The userid of the user
	@Persistent
	String userid;
	
	// The id of the quiz
	@Persistent
	String quiz;

	// The number of answers given by the user
	@Persistent
	Integer totalanswers;

	// The number of correct answers given by the user
	@Persistent
	Integer correctanswers;
	
	// The total information gain by this user. This is the total number of answers given (excluding the "I do not know" answers)
	// multiplied with the Bayesian Information Gain.
	@Persistent
	Double score;
	
	// The rank across % correct
	@Persistent
	Integer rankPercentCorrect;
	
	// The rank across % correct
	@Persistent
	Integer rankTotalCorrect;
	
	// The rank across the IG score
	@Persistent
	Integer rankScore;
	
	// The number of other users that participated in the same quiz 
	@Persistent
	Integer totalUsers;
	
	public QuizPerformance(String quiz, String userid) {
		this.key = QuizPerformance.generateKeyFromID(quiz, userid);
		this.userid = userid;
		this.quiz = quiz;
		this.totalanswers = 0;
		this.correctanswers = 0;
		this.score = 0.0;
	}
	
	public static Key generateKeyFromID(String quiz, String userid) {
		return KeyFactory.createKey(QuizPerformance.class.getSimpleName(), "id_" + userid + "_" + quiz);
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public void increaseTotal() {
		this.totalanswers++;
	}
	
	public void increaseCorrect() {
		this.correctanswers++;
	}
	
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getQuiz() {
		return quiz;
	}

	public void setQuiz(String quiz) {
		this.quiz = quiz;
	}

	public Integer getTotalanswers() {
		return totalanswers;
	}

	public void setTotalanswers(Integer totalanswers) {
		this.totalanswers = totalanswers;
	}

	public Integer getCorrectanswers() {
		return correctanswers;
	}

	public void setCorrectanswers(Integer correctanswers) {
		this.correctanswers = correctanswers;
	}

	public Double getPercentageCorrect() {
		if (this.totalanswers!=null && this.correctanswers!=null && this.totalanswers > 0)
			return Math.round(100.0* this.correctanswers / this.totalanswers) / 100.0;
		else 
			return 0.0;
	}
	
	public void computeRank() {
		
		List<QuizPerformance> results = QuizPerformanceRepository.getQuizPerformancesByQuiz(this.quiz);
		this.totalUsers = results.size();
		
		int higherPercentage=0;
		int higherScore=0;
		int higherCorrect=0;

		
		for (QuizPerformance qp : results) {
			if (qp.userid.equals(this.userid)) continue;
			
			if (qp.getPercentageCorrect()>this.getPercentageCorrect()) {
				higherPercentage++;
			} else if (qp.getPercentageCorrect()<this.getPercentageCorrect()) {
				//lowerPercentage++;
			} else {
				//equalPercentage++; // Just in case we want to be more conservative in reporting rank, taking ties into account
			}
			
			if (qp.getScore()>this.getScore()) {
				higherScore++;
			} else if (qp.getScore()<this.getScore()) {
				//lowerScore++;
			} else {
				//equalScore++; // Just in case we want to be more conservative in reporting rank, taking ties into account
			}
			
			
			if (qp.getCorrectanswers() > this.getCorrectanswers()) {
				higherCorrect++;
			} else if (qp.getCorrectanswers() < this.getCorrectanswers()) {
				//lowerCorrect++;
			} else {
				//equalCorrect++; // Just in case we want to be more conservative in reporting rank, taking ties into account
			}
		}
		this.rankPercentCorrect = higherPercentage+1;
		this.rankTotalCorrect = higherCorrect+1;
		this.rankScore=higherScore+1;
		
	}
	
	public void computeCorrect() {

		List<UserAnswer> results = UserAnswerRepository.getUserAnswers(this.quiz, this.userid);

		int c = 0;
		int t = 0;
		for (UserAnswer ua : results) {
			Boolean correct = ua.getIsCorrect();
			if (ua.getAction().equals("Submit")) {
				t++;
			}
			if (correct == null) {
				ArrayList<String> gold = QuizQuestionRepository.getGoldAnswers(ua.getRelation(), ua.getMid());
				correct = gold.contains(ua.getUseranswer());
				ua.setIsCorrect(correct);
				UserAnswerRepository.storeUserAnswer(ua);
			}
			if (correct) {
				c++;
			}
		}
		this.correctanswers = c;
		this.totalanswers = t;
		
		int numberOfMultipleChoiceOptions = 4;
		try {
			
			// We do not want to give any positive score to someone who is "too wrong" so that their
			// answers become accidentally informative. So, if the quality drops below random
			// we set it at a level equal to random.
			double quality = this.getPercentageCorrect();
			if (quality<1.0/numberOfMultipleChoiceOptions) {
				this.score=0.0;
				return;
			}
			
			double meanInfoGain = Helper.getBayesianInformationGain(this.correctanswers, this.totalanswers-this.correctanswers, numberOfMultipleChoiceOptions);
			//double varInfoGain = Helper.getBayesianVarianceInformationGain(this.correctanswers, this.totalanswers-this.correctanswers, numberOfMultipleChoiceOptions);
			// this.score = this.totalanswers * meanInfoGain-Math.sqrt(varInfoGain);
			this.score = this.totalanswers * meanInfoGain;
			if (this.score<0) this.score=0.0;
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}


	public Integer getRankPercentCorrect() {
		return rankPercentCorrect;
	}

	public void setRankPercentCorrect(Integer rankPercentCorrect) {
		this.rankPercentCorrect = rankPercentCorrect;
	}

	public Integer getRankTotalCorrect() {
		return rankTotalCorrect;
	}

	public void setRankTotalCorrect(Integer rankTotalCorrect) {
		this.rankTotalCorrect = rankTotalCorrect;
	}

	public Integer getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(Integer totalUsers) {
		this.totalUsers = totalUsers;
	}
	
	public String displayPercentageCorrect() {
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(0);
		return percentFormat.format(this.getPercentageCorrect());
		 
	}
	
	public String displayRankPercentageCorrect() {
		if (this.getRankPercentCorrect()==null || this.getTotalUsers()==null || this.getTotalUsers()==0) {
			return "--";
		}
		
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(0);
		return percentFormat.format(1.0*this.getRankPercentCorrect()/this.getTotalUsers());
	}

	
	public String displayRankTotalCorrect() {
		if (this.getRankTotalCorrect()==null || this.getTotalUsers()==null || this.getTotalUsers()==0) {
			return "--";
		}
		
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(0);
		return percentFormat.format(1.0*this.getRankTotalCorrect()/this.getTotalUsers());
	}
	
	public String displayScore() {
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(0);
		return format.format(100*this.getScore());
	}
	

	public Double getScore() {
		if (this.score==null) return 0.0;
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Integer getRankScore() {
		return rankScore;
	}

	public void setRankScore(Integer rankScore) {
		this.rankScore = rankScore;
	}

	
}