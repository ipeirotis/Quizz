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

	// The Freebase mid for which we ask the question
	@Persistent
	private String mid;
	
	// Caching the Freebase entity name
	@Persistent
	private String midname;

	// The quiz id for this question
	@Persistent
	private String quiz;
	
	// The text of the question, from the Quiz entity
	@Persistent
	private String quizquestion;

	// The set of answers
	@Persistent
	private Set<String> answers;
	
	// The correct answer among the choices
	@Persistent
	private String correct;
	
	// Whether the correct one is a gold (if false, then it is a silver answer from the Knowledge Vault)
	@Persistent
	private Boolean correctIsGold;
	
	// Total number of times the question has been asked
	@Persistent
	private Integer totalanswers;
	
	// Number of times the given answer was correct
	@Persistent
	private Integer correctanswers;
	
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
