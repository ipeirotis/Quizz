package us.quizz.endpoints;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class QuizEndpoint {
	
	protected static int QUESTION_PACKAGE_SIZE = 10;
	
	private QuizRepository quizRepository;
	private QuizQuestionRepository quizQuestionRepository;
	
	@Inject
	public QuizEndpoint(QuizRepository quizRepository, QuizQuestionRepository quizQuestionRepository){
		this.quizRepository = quizRepository;
		this.quizQuestionRepository = quizQuestionRepository;
	}

	/**
	 * This method lists all the entities inserted in datastore. It uses HTTP
	 * GET method and paging support.
	 * 
	 * @return A CollectionResponse class containing the list of all entities
	 *         persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unused" })
	@ApiMethod(name = "listQuiz")
	public CollectionResponse<Quiz> listQuiz(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Quiz> execute = quizRepository.getQuizzes();

		/*
		 * try { mgr = getPersistenceManager(); Query query =
		 * mgr.newQuery(Quiz.class); if (cursorString != null && cursorString !=
		 * "") { cursor = Cursor.fromWebSafeString(cursorString);
		 * HashMap<String, Object> extensionMap = new HashMap<String, Object>();
		 * extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
		 * query.setExtensions(extensionMap); }
		 * 
		 * if (limit != null) { query.setRange(0, limit); }
		 * 
		 * execute = (List<Quiz>) query.execute(); cursor =
		 * JDOCursorHelper.getCursor(execute); if (cursor != null) cursorString
		 * = cursor.toWebSafeString();
		 * 
		 * // Tight loop for fetching all entities from datastore and accomodate
		 * // for lazy fetch. for (Quiz obj : execute) ; } finally {
		 * mgr.close(); }
		 */

		return CollectionResponse.<Quiz> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET
	 * method.
	 * 
	 * @param id
	 *            the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getQuiz")
	public Quiz getQuiz(@Named("id") String id) {
		return quizRepository.singleGetObjectByIdThrowing(Quiz.class,
				Quiz.generateKeyFromID(id));
	}

	/**
	 * This method generates a questions for quiz
	 */
	@ApiMethod(name = "listNextQuestions", path = "quizquestions/{quiz}")
	public Map<String, Set<Question>> getNextQuestions(@Named("quiz") String quiz,
			@Nullable @Named("num") Integer num) {
		if (num == null)
			num = QUESTION_PACKAGE_SIZE;
		
		return quizQuestionRepository.getNextQuizQuestions(quiz, num);
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity
	 * already exists in the datastore, an exception is thrown. It uses HTTP
	 * POST method.
	 * 
	 * @param quiz
	 *            the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertQuiz")
	public Quiz insertQuiz(Quiz quiz) {
		return quizRepository.insert(quiz);
	}

	/**
	 * This method is used for updating an existing entity. If the entity does
	 * not exist in the datastore, an exception is thrown. It uses HTTP PUT
	 * method.
	 * 
	 * @param quiz
	 *            the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateQuiz")
	public Quiz updateQuiz(Quiz quiz) {
		return quizRepository.update(quiz);
	}

	/**
	 * This method removes the entity with primary key id. It uses HTTP DELETE
	 * method.
	 * 
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeQuiz")
	public void removeQuiz(@Named("id") String id) {
		quizRepository.remove(Quiz.generateKeyFromID(id));
	}
	
	@ApiMethod(name = "addQuiz", path="addQuiz", httpMethod=HttpMethod.POST)
	public void addQuiz(@Named("name") String name, @Named("quizID") String quizID) {
		Quiz q = new Quiz(name, quizID);
		quizRepository.storeQuiz(q);
	}
	
}