package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Answer {

	public static String generateKeyID(Long questionID, Integer internalID) {
		return "id_" + questionID + "_" + internalID;
	}
	
	public static Key generateKeyFromID(Long questionID, Integer internalID) {
		return generateKeyFromKeyID(generateKeyID(questionID, internalID));
	}
	
	public static Key generateKeyFromKeyID(String keyID) {
		return KeyFactory.createKey(Answer.class.getSimpleName(), keyID);
	}
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;
	
	@Persistent
	private Integer internalID;

	@Persistent
	private String text;
	// contains html to put on the website?
	
	@Persistent
	private Double score;
	// represents value that is asociated with selecting this answer
	
	@Persistent
	private String kind;
	// used to identify what to expect in metadata and how to interpret score
	
	@Persistent
	private String source;
	
	@Persistent
	private JsonObject metadata;
	
	@Persistent
	private Long questionID;
	
	@Persistent
	private String quizID;
	
	@Persistent
	private Boolean isGold;
	
	@Persistent
	private Double probability;
	
	public Answer(Long questionID, String quizID, String text, Integer internalID){
		this.questionID = questionID;
		this.quizID = quizID;
		this.text = text;
		this.internalID = internalID;
		this.id = generateKeyFromID(questionID, internalID);
	}
	
	public Key getID(){
		return id;
	}	
	
	public Integer getInternalID() {
		return internalID;
	}

	public void setInternalID(Integer internalID) {
		this.internalID = internalID;
	}

	public String getText(){
		return text;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public Double getScore(){
		return score;
	}
	
	public void setScore(Double score){
		this.score = score;
	}
	
	public String getKind(){
		return kind;
	}
	
	public void setKind(String kind){
		this.kind = kind;
	}
	
	public void setMetadata(JsonObject metadata){
		this.metadata = metadata;
	}
	
	protected JsonPrimitive getPrimitiveMD(String key){
		return metadata.getAsJsonPrimitive(key);
	}
	
	public String getStringMetadata(String key){
		return getPrimitiveMD(key).getAsString();
	}
	
	public int getIntegerMetadata(String key){
		return getPrimitiveMD(key).getAsInt();
	}
	
	public boolean getBoolMetadata(String key){
		return getPrimitiveMD(key).getAsBoolean();
	}
	
	public long getLongMetadata(String key){
		return getPrimitiveMD(key).getAsLong();
	}
	
	public boolean isSilver() {
		return probability != null;
	}
	
	public Double getProbability(){
		return probability;
	}
	
	public void setProbability(Double probability){
		this.probability = probability;
	}
	
	public void setSource(String source){
		this.source = source;
	}
	
	public void setQuizID(String quizID){
		this.quizID = quizID;
	}
	
	public void setQuestionID(Long questionID){
		this.questionID = questionID;
	}

	public Boolean getIsGold() {
		return isGold;
	}

	public void setIsGold(Boolean isGold) {
		this.isGold = isGold;
	}

	public String getSource() {
		return source;
	}

	public JsonObject getMetadata() {
		return metadata;
	}

	public Long getQuestionID() {
		return questionID;
	}

	public String getQuizID() {
		return quizID;
	}

	public void setId(Key id) {
		this.id = id;
	}
}
