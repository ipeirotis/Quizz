package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class QuizAnswer {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
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
	
	
	public Long getId(){
		return id;
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
}
