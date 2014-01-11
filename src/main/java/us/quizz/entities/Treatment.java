package us.quizz.entities;

import java.util.HashSet;
import java.util.Set;

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

	// The name of the treatment
	@Persistent
	String name;

	// The probability of assigning this treatment to a user
	@Persistent
	Double probability;

	// This is to allow for the treatment to block other treatments
	// within the same experiment
	@Persistent
	Set<String> blocks;

	// This is to allow for the treatment to be blocked by other treatments
	// within the same experiment
	@Persistent
	Set<String> blockedBy;

	// If this treatment needs to run by itself
	@Persistent
	Boolean blocksAll;

	public Treatment(String name, Double probability) {
		this.key = Treatment.generateKeyFromID(name);
		this.name = name;
		this.probability = probability;
		this.blocks = new HashSet<String>();
		this.blockedBy = new HashSet<String>();
		this.blocksAll = false;
	}

	public static Key generateKeyFromID(String name) {
		return KeyFactory.createKey(Treatment.class.getSimpleName(), "id_"
				+ name);
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
	}

	public Set<String> getBlocks() {
		return blocks;
	}

	public void setBlocks(Set<String> blocks) {
		this.blocks = blocks;
	}

	public Set<String> getBlockedBy() {
		return blockedBy;
	}

	public void setBlockedBy(Set<String> blockedBy) {
		this.blockedBy = blockedBy;
	}

	public Boolean getBlocksAll() {
		return blocksAll;
	}

	public void setBlocksAll(Boolean blocksAll) {
		this.blocksAll = blocksAll;
	}

}
