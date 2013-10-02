package com.ipeirotis.crowdquiz.entities;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Key;
import com.ipeirotis.crowdquiz.utils.PMF;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(
		name = "quizz",
		description = "The API for Quizz.us",
		version = "v1",
		namespace = @ApiNamespace(
				ownerDomain = "www.quizz.us", 
				ownerName = "www.quizz.us", 
				packagePath = "crowdquiz.entities"))
public class UserReferalEndpoint extends BaseCollectionEndpoint<UserReferal> {

	public UserReferalEndpoint() {
		super(UserReferal.class, "User referal");
	}

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@ApiMethod(name = "listUserReferal")
	public CollectionResponse<UserReferal> listUserReferal(
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
	@ApiMethod(name = "getUserReferal")
	public UserReferal getUserReferal(@Named("id") Long id) {
		return PMF.singleGetObjectByIdThrowing(UserReferal.class, id);
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param userreferal the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertUserReferal")
	public UserReferal insertUserReferal(UserReferal userreferal) {
		return insert(userreferal);
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param userreferal the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateUserReferal")
	public UserReferal updateUserReferal(UserReferal userreferal) {
		return update(userreferal);
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeUserReferal")
	public void removeUserReferal(@Named("id") Long id) {
		UserReferal ur = PMF.singleGetObjectByIdThrowing(UserReferal.class, id);
		remove(ur.getKey());
	}

	@Override
	protected Key getKey(UserReferal item) {
		return item.getKey();
	}

}
