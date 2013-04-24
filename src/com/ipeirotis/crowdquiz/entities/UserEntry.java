package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserEntry {

	@Persistent
	private String	userid;

	/**
	 * @return the userid
	 */
	public String getUserid() {

		return userid;
	}

	@Persistent
	private String	useranswer;

	@Persistent
	private String	freebaseanswer;

	@Persistent
	private String	relation;

	@Persistent
	private String	mid;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	public UserEntry(String userid, String relation, String mid, String useranswer, String freebaseanswer) {

		this.freebaseanswer = freebaseanswer;
		this.relation = relation;
		this.mid = mid;
		this.useranswer = useranswer;
		this.userid = userid;

		Key k = generateKeyFromID(userid, relation, mid);
		this.key = k;
	}

	public static Key generateKeyFromID(String userid, String relation, String mid) {

		return KeyFactory.createKey(UserEntry.class.getSimpleName(), "id_" + userid + "_" + relation + "_" + mid);
	}

	/**
	 * @return the useranswer
	 */
	public String getUseranswer() {

		return useranswer;
	}

	/**
	 * @param useranswer
	 *          the useranswer to set
	 */
	public void setUseranswer(String useranswer) {

		this.useranswer = useranswer;
	}

	/**
	 * @return the freebaseanswer
	 */
	public String getFreebaseanswer() {

		return freebaseanswer;
	}

	/**
	 * @param freebaseanswer
	 *          the freebaseanswer to set
	 */
	public void setFreebaseanswer(String freebaseanswer) {

		this.freebaseanswer = freebaseanswer;
	}

	/**
	 * @return the relation
	 */
	public String getRelation() {

		return relation;
	}

	/**
	 * @param relation
	 *          the relation to set
	 */
	public void setRelation(String relation) {

		this.relation = relation;
	}

	/**
	 * @return the mid
	 */
	public String getMid() {

		return mid;
	}

	/**
	 * @param mid
	 *          the mid to set
	 */
	public void setMid(String mid) {

		this.mid = mid;
	}

	/**
	 * @return the key
	 */
	public Key getKey() {

		return key;
	}

	/**
	 * @param key
	 *          the key to set
	 */
	public void setKey(Key key) {

		this.key = key;
	}

}
