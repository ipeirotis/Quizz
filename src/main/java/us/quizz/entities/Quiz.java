package us.quizz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import us.quizz.utils.Helper;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/** 
 * The Quiz is the basic unit of the application. Each quiz contains 
 * a set of Questions. The Quiz object is essentially a placeholder
 * for storing overall statistics about the quiz, and for storing 
 * the id and the title of the quiz.
 * 
 * @author ipeirotis
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Quiz {

	public static Key generateKeyFromID(String quizID) {

		return KeyFactory.createKey(Quiz.class.getSimpleName(), "id_" + quizID);
	}

	// CURRENTLY UNUSED
	// The category of the quiz. This allows the quizzes to be grouped by category in the
	// first page, instead of being a big list of quizzes.
	@Persistent
	private String	category;
	
	// The user-friendly name of the quiz that we are targeting
	@Persistent
	private String	name;

	// The name of the quiz that we are targeting. 
	// Typically, we assign the name of a KP attribute on this one
	// and serves as a defacto primary key for the quiz.
	@Persistent
	private String quizID;

	// CURRENTLY UNUSED
	// The type of entry for the answer that we expect
	// We do not use this for multiple choice questions
	// but we may use it in the future for the fill-in questions, to enable
	// autocompletion using the Freebase auto-suggest widget
	@Persistent
	private String	freebaseType;

	// The id of the AdWords ad campaign that brings visitors to the quiz
	@Persistent
	private Long	campaignid;

	// All the variables below are aggregate statistics about the quiz.
	// We update these using the QuizRepository.updateQuizCounts() call
	
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
	
	// The total number of all answers submitted
	@Persistent
	private Integer submitted;


	// The average correctness of the users
	@Persistent
	private Double avgUserCorrectness;

	// The probability that a submitted answer is correct
	@Persistent
	private Double avgAnswerCorrectness;

	// The average number of bits submitted by each user
	@Persistent
	private Double capacity;

	// The number of questions for the quiz
	@Persistent
	private Integer questions;

	// The number of questions for the quiz that have gold answers
	@Persistent
	private Integer gold;


	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	public Quiz(String name, String quizID) {

		this.name = name;
		this.quizID = quizID;
		this.freebaseType = null;

		this.key = generateKeyFromID(quizID);
	}

	
	public Double getAvgAnswerCorrectness() {
		return avgAnswerCorrectness;
	}


	public Double getAvgUserCorrectness() {
		return avgUserCorrectness;
	}


	public Long getCampaignid() {
		return campaignid;
	}
	
	public Double getCapacity() {
		return capacity;
	}
	
	public Double getCapacity(Double error) {
		try {
			return capacity/ (1-Helper.entropy(1-error,2));
		} catch (Exception e) {
			e.printStackTrace();
			return capacity;
		}
	}
	
	public String getCategory() {
		return category;
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


	public Integer getGold() {
		return gold;
	}


	public Key getKey() {

		return key;
	}


	public String getName() {
	
		return name;
	}


	public Integer getQuestions() {
		return questions;
	}


	public String getQuizID() {

		return quizID;
	}



	public Integer getSubmitted() {
		return submitted;
	}


	public Integer getTotalAnswers() {
		return totalAnswers;
	}


	public Integer getTotalUsers() {
		return totalUsers;
	}


	public void setAvgAnswerCorrectness(Double avgAnswerCorrectness) {
		this.avgAnswerCorrectness = avgAnswerCorrectness;
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
	

	public void setCategory(String category) {
		this.category = category;
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
	
	public void setFreebaseType(String freebaseType) {
		this.freebaseType = freebaseType;
	}
	
	public void setGold(Integer gold) {
		this.gold = gold;
	}
	
	
	public void setKey(Key key) {
		this.key = key;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setQuestions(Integer questions) {
		this.questions = questions;
	}

	public void setQuizID(String quizID) {
		this.quizID = quizID;
	}
	
	public void setSubmitted(Integer submitted) {
		this.submitted = submitted;
	}

	public void setTotalAnswers(Integer totalAnswers) {
		this.totalAnswers = totalAnswers;
	}

	public void setTotalUsers(Integer totalUsers) {
		this.totalUsers = totalUsers;
	}

}
