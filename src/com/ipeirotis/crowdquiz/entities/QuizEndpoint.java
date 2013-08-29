package com.ipeirotis.crowdquiz.entities;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.ipeirotis.crowdquiz.utils.FreebaseSearch;
import com.ipeirotis.crowdquiz.utils.Helper;
import com.ipeirotis.crowdquiz.utils.PMF;

@Api(
		name = "quizz",
		description = "The API for Quizz.us",
		version = "v1",
		namespace = @ApiNamespace(
				ownerDomain = "www.quizz.us", 
				ownerName = "www.quizz.us", 
				packagePath = "crowdquiz.entities"))
public class QuizEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unused" })
	@ApiMethod(name = "listQuiz")
	public CollectionResponse<Quiz> listQuiz(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Quiz> execute = QuizRepository.getQuizzes();

		/*
		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Quiz.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Quiz>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Quiz obj : execute)
				;
		} finally {
			mgr.close();
		}
		*/

		return CollectionResponse.<Quiz> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getQuiz")
	public Quiz getQuiz(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		Quiz quiz = null;
		try {
			quiz = mgr.getObjectById(Quiz.class, Quiz.generateKeyFromID(id));
		} finally {
			mgr.close();
		}
		return quiz;
	}
	
	/**
	 * This method generates a next question for the passed quiz
	 *
	 * @param quiz the primary key of the quiz
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getNextQuestionInstance", path = "quizquestioninstance/quiz/{quiz}")
	public QuizQuestionInstance getNextQuestion(@Named("quiz") String quiz) {
		String mid = Helper.getNextQuizQuestion(quiz);
		return getQuestionInstance(quiz,mid);
	}

	/** This method generates a next question for the passed quiz
	 *
	 * @param quiz the primary key of the quiz
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getQuestionInstance", path = "quizquestioninstance/quiz/{quiz}/mid/{mid}")
	public QuizQuestionInstance getQuestionInstance(@Named("quiz") String quiz, @Named("mid") String mid) {
		String name = FreebaseSearch.getFreebaseAttribute(mid,"name");
		String questiontext = QuizRepository.getQuiz(quiz).getQuestionText();
		QuizQuestion question = QuizQuestionRepository.getQuizQuestion(quiz, mid);
		QuizQuestionInstance result = QuizQuestionRepository.getQuizQuestionInstanceWithGold(quiz, mid, name, 4);
		result.setMidname(name);
		result.setQuizquestion(questiontext);
		result.setCorrectanswers(question.getNumberOfCorrentUserAnswers());
		result.setTotalanswers(question.getNumberOfUserAnswers());
		return result;
	}


	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param quiz the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertQuiz")
	public Quiz insertQuiz(Quiz quiz) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsQuiz(quiz)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(quiz);
		} finally {
			mgr.close();
		}
		return quiz;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param quiz the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateQuiz")
	public Quiz updateQuiz(Quiz quiz) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsQuiz(quiz)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(quiz);
		} finally {
			mgr.close();
		}
		return quiz;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeQuiz")
	public void removeQuiz(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Quiz quiz = mgr.getObjectById(Quiz.class, Quiz.generateKeyFromID(id));
			mgr.deletePersistent(quiz);
		} finally {
			mgr.close();
		}
	}

	private boolean containsQuiz(Quiz quiz) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Quiz.class, quiz.getKey());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.getPM();
	}

}
