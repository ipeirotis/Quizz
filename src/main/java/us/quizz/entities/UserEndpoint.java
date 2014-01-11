package us.quizz.entities;

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;

import us.quizz.utils.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Key;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.entities"))
public class UserEndpoint extends BaseCollectionEndpoint<User> {

	public UserEndpoint() {
		super(User.class, "User");
	}

	/**
	 * This method lists all the entities inserted in datastore. It uses HTTP
	 * GET method and paging support.
	 * 
	 * @return A CollectionResponse class containing the list of all entities
	 *         persisted and a cursor to the next page.
	 */
	@ApiMethod(name = "listUser")
	public CollectionResponse<User> listUser(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {
		return listItems(cursorString, limit);
	}

	@Override
	protected void fetchItem(User user) {
		user.getExperiment();
		user.getTreatments();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET
	 * method.
	 * 
	 * @param id
	 *            the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getUser")
	public User getUser(@Named("userid") String userid) {
		User user = PMF.singleGetObjectByIdThrowing(User.class,
				User.generateKeyFromID(userid));
		Experiment e = user.getExperiment();
		for (String s : e.getTreatments().keySet())
			e.getTreatments().get(s);
		Map<String, Boolean> treatments = user.getTreatments();
		for (String s : treatments.keySet())
			treatments.get(s);
		return user;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity
	 * already exists in the datastore, an exception is thrown. It uses HTTP
	 * POST method.
	 * 
	 * @param user
	 *            the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertUser")
	public User insertUser(User user) {
		return insert(user);
	}

	/**
	 * This method is used for updating an existing entity. If the entity does
	 * not exist in the datastore, an exception is thrown. It uses HTTP PUT
	 * method.
	 * 
	 * @param user
	 *            the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateUser")
	public User updateUser(User user) {
		return update(user);
	}

	/**
	 * This method removes the entity with primary key id. It uses HTTP DELETE
	 * method.
	 * 
	 * @param id
	 *            the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeUser")
	public void removeUser(@Named("userid") String userid) {
		remove(User.generateKeyFromID(userid));
	}

	@Override
	protected Key getKey(User item) {
		return item.getKey();
	}
}
