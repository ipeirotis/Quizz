package com.ipeirotis.crowdquiz.entities;

import com.ipeirotis.crowdquiz.utils.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "userreferalendpoint", namespace = @ApiNamespace(ownerDomain = "ipeirotis.com", ownerName = "ipeirotis.com", packagePath = "crowdquiz.entities"))
public class UserReferalEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listUserReferal")
	public CollectionResponse<UserReferal> listUserReferal(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<UserReferal> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(UserReferal.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<UserReferal>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (UserReferal obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<UserReferal> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getUserReferal")
	public UserReferal getUserReferal(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		UserReferal userreferal = null;
		try {
			userreferal = mgr.getObjectById(UserReferal.class, id);
		} finally {
			mgr.close();
		}
		return userreferal;
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
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsUserReferal(userreferal)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(userreferal);
		} finally {
			mgr.close();
		}
		return userreferal;
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
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsUserReferal(userreferal)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(userreferal);
		} finally {
			mgr.close();
		}
		return userreferal;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeUserReferal")
	public void removeUserReferal(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			UserReferal userreferal = mgr.getObjectById(UserReferal.class, id);
			mgr.deletePersistent(userreferal);
		} finally {
			mgr.close();
		}
	}

	private boolean containsUserReferal(UserReferal userreferal) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(UserReferal.class, userreferal.getKey());
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
