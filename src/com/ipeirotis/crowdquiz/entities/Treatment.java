package com.ipeirotis.crowdquiz.entities;

import java.lang.reflect.Field;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * The treatment object assigns a set of **boolean** variables that indicate
 * which treatments are active for the given user. At this point, we only
 * support boolean treatments. In the future we plan to add support for enum
 * fields as treatments.
 * 
 * 
 * @author ipeirotis
 * 
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Treatment {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	// TODO: Create a different table in datastore with the conditions, and assign a probability
	// to each of them. Then, when creating the treatment, query the datastore for the probabilities
	// assigned to each condition.
	
	// Should we show any popup with a message?
	@Persistent
	Boolean showMessage;
	
	// Should we show the correct answer in hte popup?
	@Persistent
	Boolean showCorrect;

	// Should we show the total number of correct answers so far?
	@Persistent
	Boolean showScore;

	// Should we show the percentage of correct answers?
	@Persistent
	Boolean showPercentageCorrect;

	// Should we show the answers given by other users?
	@Persistent
	Boolean showCrowdAnswers;

	// Should we show the rank among the other users in terms of % of correct answers?
	@Persistent
	Boolean showPercentageRank;

	// Should we show the rank among the other users in terms of # of correct answers?
	@Persistent
	Boolean showScoreRank;

	
	public Treatment() {

	}
	
	public static Key generateKeyFromID(Long setting) {

		return KeyFactory.createKey(Treatment.class.getSimpleName(), "id_" + setting);
	}
	

	public static Treatment assignTreatment() {
		Treatment result = new Treatment();

		Field[] fields = Treatment.class.getDeclaredFields();
		//System.out.println("Fields:"+fields.length);

		int cnt = 0;
		
		// Setting is effectively a bit vector, setting 0/1 the various treatments
		long setting = 0;
		for (Field f : fields) {
			if (f.getType().equals(Boolean.class)) {
				f.setAccessible(true);
				
				// Assign a random value to each condition
				Boolean value = (Math.random() < 0.5) ? false : true;
				//System.out.println("Setting " + f.getName() + " to " + value);
				try {
					f.set(result, value);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
				// Encode in the setting vector whether this treatment is on or off
				long add = Math.round(Math.pow(2, ++cnt));
				if (value==true) {
					setting += add;
				}
			}
		}
		//System.out.println("Setting:"+setting);
		result.setKey(Treatment.generateKeyFromID(setting));
		return result;
	}
	
	public String toString() {
		

		Field[] fields = Treatment.class.getDeclaredFields();

		StringBuffer sb = new StringBuffer();
		sb.append("key:"+this.key+"\n");
		for (Field f : fields) {
			if (f.getType().equals(Boolean.class)) {
				String name = f.getName();
				Boolean value = null;
				try {
					value = (Boolean) f.get(this);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				sb.append(name+ ":" + value+"\n");
				
			}
		}
			
		return sb.toString();
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Boolean getShowCorrect() {
		return showCorrect;
	}

	public void setShowCorrect(Boolean showCorrect) {
		this.showCorrect = showCorrect;
	}

	public Boolean getShowScore() {
		return showScore;
	}

	public void setShowScore(Boolean showScore) {
		this.showScore = showScore;
	}

	public Boolean getShowPercentageCorrect() {
		return showPercentageCorrect;
	}

	public void setShowPercentageCorrect(Boolean showPercentageCorrect) {
		this.showPercentageCorrect = showPercentageCorrect;
	}

	public Boolean getShowCrowdAnswers() {
		return showCrowdAnswers;
	}

	public void setShowCrowdAnswers(Boolean showCrowdAnswers) {
		this.showCrowdAnswers = showCrowdAnswers;
	}

	public Boolean getShowMessage() {
		return showMessage;
	}

	public void setShowMessage(Boolean showMessage) {
		this.showMessage = showMessage;
	}

	public Boolean getShowPercentageRank() {
		return showPercentageRank;
	}

	public void setShowPercentageRank(Boolean showPercentageRank) {
		this.showPercentageRank = showPercentageRank;
	}

	public Boolean getShowScoreRank() {
		return showScoreRank;
	}

	public void setShowScoreRank(Boolean showScoreRank) {
		this.showScoreRank = showScoreRank;
	}


}
