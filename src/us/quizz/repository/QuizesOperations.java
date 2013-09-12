package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ipeirotis.crowdquiz.entities.Quiz;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.QuizQuestionInstance;
import com.ipeirotis.crowdquiz.utils.Helper;

public class QuizesOperations {
	
	protected static int ANSWERS_COUNT = 4;
	protected static int QUESTION_PACKAGE_SIZE = 10;

	public static String getNextQuizQuestion(String quiz) {
		return getNextQuizQuestions(quiz, 1).iterator().next();
	}

	public static Set<String> getNextQuizQuestions(String quiz, int n) {
		ArrayList<String> list = QuizQuestionRepository.getQuizQuestionsWithGold(quiz);
		return Helper.selectRandomElements(list, n);
	}

	public static QuizQuestionInstance getNextQuizQuestionInstance(String quizId) {
		return getNextQuizQuestionInstances(quizId, 1).get(0);
	}
	
	public static List<QuizQuestionInstance> getNextQuizQuestionInstances(String quizId) {
		return getNextQuizQuestionInstances(quizId, QUESTION_PACKAGE_SIZE);
	}
	
	/**
	 * Could be done a little bit better but it is much simpler that way ....
	 */
	protected static ArrayList<String> generateWrongAnswers(String quizId, Set<String> questions, int n) {
		ArrayList<String> randomGolds = QuizQuestionRepository.getSomeQuizGoldAnswers(quizId, n * 100);
		Set<String> wrongAnswers = new HashSet<String>(randomGolds);
		wrongAnswers.removeAll(questions);
		for (String questionMid: questions) {
			wrongAnswers.removeAll(
					QuizQuestionRepository.getGoldAnswers(quizId, questionMid));
			wrongAnswers.removeAll(
					QuizQuestionRepository.getSilverAnswers(quizId, questionMid, true, 0.5));
		}
		return new ArrayList<String>(wrongAnswers);
	}
	
	public static List<QuizQuestionInstance> getNextQuizQuestionInstances(String quizId, int n){
//		Quiz quiz = QuizRepository.getQuiz(quizId);
		Set<String> quizQuestionsWithGold = getNextQuizQuestions(quizId, n);
		ArrayList<String> wrongAnswers = generateWrongAnswers(quizId, quizQuestionsWithGold, n);
		Iterator<String> choosenWrongAnswers =
				Helper.selectRandomElements(wrongAnswers, n * (ANSWERS_COUNT - 1)).iterator();
		List<QuizQuestionInstance> questions = new ArrayList<QuizQuestionInstance>(n);
		for (String quizQuestionWithGold: quizQuestionsWithGold){
			questions.add(
					makeQuestionInstance(quizId, quizQuestionWithGold, choosenWrongAnswers));
		}
		return questions;
	}
	
	private static QuizQuestionInstance makeQuestionInstance(String quizId, String questionMid, Iterator<String> wrongAnswers) {
		// TODO
		String questiontext = QuizRepository.getQuiz(quizId).getQuestionText();
		QuizQuestion question = QuizQuestionRepository.getQuizQuestion(quizId, questionMid);
		QuizQuestionInstance result = QuizQuestionRepository.getQuizQuestionInstanceWithGold(quizId,
				questionMid, question.getName(), 4);
		result.setMidname(question.getName());
		result.setQuizquestion(questiontext);
		result.setCorrectanswers(question.getNumberOfCorrentUserAnswers());
		result.setTotalanswers(question.getNumberOfUserAnswers());
		return result;
	}
}
