package com.ipeirotis.crowdquiz.entities;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.ipeirotis.crowdquiz.utils.PMF;

/**
 * Keeps track of the performance of a user within a Quiz
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
	
	// The rank across % correct
	@Persistent
	Integer rankPercentCorrect;
	
	// The rank across % correct
	@Persistent
	Integer rankTotalCorrect;
	
	// The number of other users that participated in the same quiz 
	@Persistent
	Integer totalUsers;
	
	public QuizPerformance(String quiz, String userid) {
		this.key = QuizPerformance.generateKeyFromID(quiz, userid);
		this.userid = userid;
		this.quiz = quiz;
		this.totalanswers = 0;
		this.correctanswers = 0;
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
	
	public void computePercentageRank() {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(QuizPerformance.class);
		q.setFilter("quiz == quizParam");
		q.declareParameters("String quizParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", this.quiz);

		List<QuizPerformance> results = (List<QuizPerformance>) q.executeWithMap(params);
		pm.close();
		
		int higherPercentage=0;
		int lowerPercentage=0;
		int equalPercentage=0;
		
		int higherCorrect=0;
		int lowerCorrect=0;
		int equalCorrect=0;
		
		for (QuizPerformance qp : results) {
			if (qp.userid.equals(this.userid)) continue;
			
			if (qp.getPercentageCorrect()>this.getPercentageCorrect()) {
				higherPercentage++;
			} else if (qp.getPercentageCorrect()<this.getPercentageCorrect()) {
				lowerPercentage++;
			} else {
				equalPercentage++;
			}
			
			if (qp.getCorrectanswers() > this.getCorrectanswers()) {
				higherCorrect++;
			} else if (qp.getCorrectanswers() < this.getCorrectanswers()) {
				lowerCorrect++;
			} else {
				equalCorrect++;
			}
		}
		this.rankPercentCorrect = higherPercentage+1;
		this.rankTotalCorrect = higherCorrect+1;
		this.totalUsers = higherCorrect + lowerCorrect + equalCorrect + 1;
	}
	
	public void computeCorrect() {
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("relation == quizParam && userid == useridParam");
		q.declareParameters("String quizParam, String useridParam");
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", this.quiz);
		params.put("useridParam", this.userid);

		List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
		
		int c = 0;
		for (UserAnswer ua : results) {
			Boolean correct = ua.getIsCorrect();
			if (correct == null) {
				ArrayList<String> gold = QuizQuestion.getGoldAnswers(ua.getRelation(), ua.getMid());
				correct = gold.contains(ua.getUseranswer());
				ua.setIsCorrect(correct);
				pm.makePersistent(ua);
			}
			if (correct) {
				c++;
			}
		}
		this.correctanswers = c;
		this.totalanswers = results.size();
		
		pm.close();
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
	
}
