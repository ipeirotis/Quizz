package com.ipeirotis.crowdquiz.entities;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.ipeirotis.crowdquiz.utils.PMF;

@Api(
		name = "quizz",
		description = "The API for Quizz.us",
		version = "v1",
		namespace = @ApiNamespace(
				ownerDomain = "www.quizz.us", 
				ownerName = "www.quizz.us", 
				packagePath = "crowdquiz.entities"))
public class UserAnswerFeedbackEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listUserAnswerFeedback")
	public CollectionResponse<UserAnswerFeedback> listUserAnswerFeedback(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<UserAnswerFeedback> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(UserAnswerFeedback.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<UserAnswerFeedback>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (UserAnswerFeedback obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<UserAnswerFeedback> builder()
				.setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getUserAnswerFeedback")
	public UserAnswerFeedback getUserAnswerFeedback(@Named("quiz") String quiz, @Named("userid")  String userid, @Named("mid") String mid) {
		PersistenceManager mgr = getPersistenceManager();
		UserAnswerFeedback useranswerfeedback = null;
		try {
			useranswerfeedback = mgr
					.getObjectById(UserAnswerFeedback.class, UserAnswerFeedback.generateKeyFromID(quiz, userid, mid));
		} finally {
			mgr.close();
		}
		return useranswerfeedback;
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
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsUserAnswerFeedback(useranswerfeedback)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(useranswerfeedback);
		} finally {
			mgr.close();
		}
		return useranswerfeedback;
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
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsUserAnswerFeedback(useranswerfeedback)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(useranswerfeedback);
		} finally {
			mgr.close();
		}
		return useranswerfeedback;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeUserAnswerFeedback")
	public void removeUserAnswerFeedback(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			UserAnswerFeedback useranswerfeedback = mgr.getObjectById(
					UserAnswerFeedback.class, id);
			mgr.deletePersistent(useranswerfeedback);
		} finally {
			mgr.close();
		}
	}

	private boolean containsUserAnswerFeedback(
			UserAnswerFeedback useranswerfeedback) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(UserAnswerFeedback.class,
					useranswerfeedback.getKey());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
