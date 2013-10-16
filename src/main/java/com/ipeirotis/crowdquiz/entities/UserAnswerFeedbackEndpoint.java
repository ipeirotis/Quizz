package com.ipeirotis.crowdquiz.entities;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Key;
import com.ipeirotis.crowdquiz.utils.PMF;

@Api(
		name = "quizz",
		description = "The API for Quizz.us",
		version = "v1",
		namespace = @ApiNamespace(
				ownerDomain = "www.quizz.us", 
				ownerName = "www.quizz.us", 
				packagePath = "crowdquiz.entities"))
public class UserAnswerFeedbackEndpoint extends BaseCollectionEndpoint<UserAnswerFeedback>{

	public UserAnswerFeedbackEndpoint(){
		super(UserAnswerFeedback.class, "UserAnswerFeedback");
	}
	
	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@ApiMethod(name = "listUserAnswerFeedback")
	public CollectionResponse<UserAnswerFeedback> listUserAnswerFeedback(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {
		return listItems(cursorString, limit);
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getUserAnswerFeedback")
	public UserAnswerFeedback getUserAnswerFeedback(@Named("quiz") String quiz, @Named("userid")  String userid, @Named("mid") String mid) {
		return PMF.singleGetObjectByIdThrowing(UserAnswerFeedback.class, UserAnswerFeedback.generateKeyFromID(quiz, userid, mid));
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param useranswerfeedback the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertUserAnswerFeedback")
	public UserAnswerFeedback insertUserAnswerFeedback(
			UserAnswerFeedback useranswerfeedback) {
		return insert(useranswerfeedback);
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param useranswerfeedback the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateUserAnswerFeedback")
	public UserAnswerFeedback updateUserAnswerFeedback(
			UserAnswerFeedback useranswerfeedback) {
		return update(useranswerfeedback);
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeUserAnswerFeedback")
	public void removeUserAnswerFeedback(@Named("id") Long id) {
		UserAnswerFeedback uaf = PMF.singleGetObjectByIdThrowing(UserAnswerFeedback.class, id);
		remove(uaf.getKey());
	}

	@Override
	protected Key getKey(UserAnswerFeedback item) {
		return item.getKey();
	}
}