package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.ipeirotis.crowdquiz.utils.Helper;

/** 
 * The Quiz is the basic unit of the application. Each quiz contains 
 * a set of QuizQuestions. 
 * 
 * @author ipeirotis
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Quiz {

	public static Key generateKeyFromID(String relation) {

		return KeyFactory.createKey(Quiz.class.getSimpleName(), "id_" + relation);
	}

	// The category of the quiz. This allows the quizzes to be grouped by category in the
	// first page, instead of being a big list of quizzes.
	@Persistent
	private String	category;
	
	// The user-friendly name of the relation that we are targeting
	@Persistent
	private String	name;

	// The name of the relation that we are targeting. 
	// Typically, we assign the name of a KP attribute on this one
	// and serves as a defacto primary key for the quiz.
	@Persistent
	private String	relation;

	// The question that we will ask to the user
	@Persistent
	private String	questionText;

	// The type of entry for the answer that we expect
	// We do not use this for multiple choice questions
	// but it is used for the fill-in questions, to enable
	// autocompletion using the Freebase auto-suggest widget
	@Persistent
	private String	freebaseType;

	// The id of the AdWords ad campaign that brings visitors to the quiz
	@Persistent
	private Long	campaignid;
	
	// The following numbers are statistics about the quiz
	
	// The number of users that arrived in a Quiz page
	@Persistent
	private Integer totalUsers;
	
	// The number of users that answered at least one non-IDK question
	@Persistent
	private Integer contributingUsers;

	// The conversion rate = contributingUsers/totalUsers
	@Persistent
	private Double conversionRate;

	// The number of correct answers submitted
	@Persistent
	private Integer correctAnswers;

	// The total number of non-IDK answers submitted
	@Persistent
	private Integer totalAnswers;

	
	@Persistent
	private Double avgUserCorrectness;


	@Persistent
	private Double avgAnswerCorrectness;


	@Persistent
	private Double capacity;
	
	@Persistent
	private Integer questions;
	
	@Persistent
	private Integer gold;
	
	@Persistent
	private Integer silver;
	
	@Persistent
	private Integer submitted;


	public Integer getQuestions() {
		return questions;
	}


	public void setQuestions(Integer questions) {
		this.questions = questions;
	}


	public Integer getGold() {
		return gold;
	}


	public void setGold(Integer gold) {
		this.gold = gold;
	}


	public Integer getSilver() {
		return silver;
	}


	public void setSilver(Integer silver) {
		this.silver = silver;
	}


	public Integer getSubmitted() {
		return submitted;
	}


	public void setSubmitted(Integer submitted) {
		this.submitted = submitted;
	}

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;


	public Quiz(String name, String relation, String questionText) {

		this.name = name;
		this.questionText = questionText;
		this.relation = relation;
		this.freebaseType = null;


		this.key = generateKeyFromID(relation);
	}


	public Double getAvgUserCorrectness() {
		return avgUserCorrectness;
	}


	public Long getCampaignid() {
	
		return campaignid;
	}


	public Double getCapacity(Double error) {
		
		try {
			return capacity/ (1-Helper.entropy(1-error,2));
		} catch (Exception e) {
			e.printStackTrace();
			return capacity;
		}
	}


	public Integer getContributingUsers() {
		return contributingUsers;
	}


	public Double getConversionRate() {
		return conversionRate;
	}


	public Integer getCorrectAnswers() {
		return correctAnswers;
	}

	public String getFreebaseType() {

		return freebaseType;
	}
	

	public Key getKey() {

		return key;
	}
	
	public String getName() {
	
		return name;
	}
	

	public String getQuestionText() {

		return questionText;
	}


	public String getRelation() {

		return relation;
	}
	
	public Integer getTotalAnswers() {
		return totalAnswers;
	}
	
	public Integer getTotalUsers() {
		return totalUsers;
	}
	
	
	public void setAvgUserCorrectness(Double avgUserCorrectness) {
		this.avgUserCorrectness = avgUserCorrectness;
	}

	public void setCampaignid(Long campaignid) {
	
		this.campaignid = campaignid;
	}

	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}

	public void setContributingUsers(Integer contributingUsers) {
		this.contributingUsers = contributingUsers;
	}

	public void setConversionRate(Double conversionRate) {
		this.conversionRate = conversionRate;
	}

	public void setCorrectAnswers(Integer correctAnswers) {
		this.correctAnswers = correctAnswers;
	}

	
	public void setTotalAnswers(Integer totalAnswers) {
		this.totalAnswers = totalAnswers;
	}

	public void setTotalUsers(Integer totalUsers) {
		this.totalUsers = totalUsers;
	}

}
