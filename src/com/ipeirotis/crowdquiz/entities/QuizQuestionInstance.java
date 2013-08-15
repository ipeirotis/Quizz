package com.ipeirotis.crowdquiz.entities;

import java.util.Set;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.ipeirotis.crowdquiz.utils.FreebaseSearch;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class QuizQuestionInstance {

	public static Key generateKeyFromID(String relation, String mid) {

		return KeyFactory.createKey(QuizQuestionInstance.class.getSimpleName(), "id_"
				+ relation + "_" + mid);
	}

	@Persistent
	private String mid;
	
	@Persistent
	private String midname;

	@Persistent
	private String quiz;
	
	@Persistent
	private String quizquestion;

	@Persistent
	private Set<String> answers;
	
	@Persistent
	private String correct;
	
	@Persistent
	private Boolean correctIsGold;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	public String getMidname() {
		return midname;
	}

	public void setMidname(String midname) {
		this.midname = midname;
	}

	public String getQuizquestion() {
		return quizquestion;
	}

	public void setQuizquestion(String quizquestion) {
		this.quizquestion = quizquestion;
	}

	public QuizQuestionInstance(String quiz, String mid, Set<String> answers, String correct, Boolean correctIsGold) {

		this.mid = mid;
		this.quiz = quiz;
		this.answers = answers;
		this.correct = correct;
		this.correctIsGold = correctIsGold;

		this.key = generateKeyFromID(quiz, mid);
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getQuiz() {
		return quiz;
	}

	public void setQuiz(String quiz) {
		this.quiz = quiz;
	}

	public Set<String> getAnswers() {
		return answers;
	}

	public void setAnswers(Set<String> answers) {
		this.answers = answers;
	}

	public String getCorrect() {
		return correct;
	}

	public void setCorrect(String correct) {
		this.correct = correct;
	}

	public Boolean getCorrectIsGold() {
		return correctIsGold;
	}

	public void setCorrectIsGold(Boolean correctIsGold) {
		this.correctIsGold = correctIsGold;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}
	

}
