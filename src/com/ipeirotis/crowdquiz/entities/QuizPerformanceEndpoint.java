package com.ipeirotis.crowdquiz.entities;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import us.quizz.repository.QuizPerformanceRepository;

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
public class QuizPerformanceEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listQuizPerformance")
	public CollectionResponse<QuizPerformance> listQuizPerformance(@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<QuizPerformance> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(QuizPerformance.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<QuizPerformance>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (QuizPerformance obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<QuizPerformance> builder().setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@ApiMethod(name = "listQuizPerformanceByUser", path = "quizperformance/user/{user}")
	public CollectionResponse<QuizPerformance> listQuizPerformanceByUser(@Named("user") String userid) {

		List<QuizPerformance> execute = QuizPerformanceRepository.getQuizPerformancesByUser(userid);
		return CollectionResponse.<QuizPerformance> builder().setItems(execute).build();
	}

	
	
	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getQuizPerformance", path = "quizperformance/quiz/{quiz}/user/{user}")
	public QuizPerformance getQuizPerformance(@Named("quiz") String quiz, @Named("user") String userid) {

		QuizPerformance quizperformance = QuizPerformanceRepository.getQuizPerformance(quiz, userid);
		if (quizperformance==null) quizperformance = new QuizPerformance(quiz, userid);

		return quizperformance;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param quizperformance the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertQuizPerformance")
	public QuizPerformance insertQuizPerformance(QuizPerformance quizperformance) {

		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsQuizPerformance(quizperformance)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(quizperformance);
		} finally {
			mgr.close();
		}
		return quizperformance;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param quizperformance the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateQuizPerformance")
	public QuizPerformance updateQuizPerformance(QuizPerformance quizperformance) {

		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsQuizPerformance(quizperformance)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(quizperformance);
		} finally {
			mgr.close();
		}
		return quizperformance;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeQuizPerformance")
	public void removeQuizPerformance(@Named("quizid") String quiz, @Named("userid") String userid) {

		PersistenceManager mgr = getPersistenceManager();
		try {
			QuizPerformance quizperformance = mgr.getObjectById(QuizPerformance.class, QuizPerformance.generateKeyFromID(quiz, userid));
			mgr.deletePersistent(quizperformance);
		} finally {
			mgr.close();
		}
	}

	private boolean containsQuizPerformance(QuizPerformance quizperformance) {

		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(QuizPerformance.class, quizperformance.getKey());
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
