package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.QuizQuestionInstance;
import com.ipeirotis.crowdquiz.utils.Helper;

public class QuizesOperations {
	
	protected static int ANSWERS_COUNT = 4;

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
	
	/**
	 * Could be done a little bit better but it is much simpler that way ....
	 */
	protected static ArrayList<String> generateWrongAnswers(String quizId, Set<String> questions, int n) {
		ArrayList<String> randomGolds = QuizQuestionRepository.getSomeQuizGoldAnswers(quizId, n * 150);
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
		String questionText = QuizRepository.getQuiz(quizId).getQuestionText();
		for (String quizQuestionWithGold: quizQuestionsWithGold){
			questions.add(
					makeQuestionInstance(questionText, quizId, quizQuestionWithGold, choosenWrongAnswers));
		}
		return questions;
	}
	
	private static QuizQuestionInstance makeQuestionInstance(String questionText, String quizId,
					String questionMid, Iterator<String> wrongAnswers) {
		QuizQuestion question = QuizQuestionRepository.getQuizQuestion(quizId, questionMid);
		
		String goldAnswer = QuizQuestionRepository.getRandomGoldAnswer(quizId, questionMid);
		Set<String> choices = generateChoices(quizId, questionMid, goldAnswer, wrongAnswers);
		
		QuizQuestionInstance result = new QuizQuestionInstance(quizId, questionMid, choices,
					goldAnswer, true);
		
		result.setMidname(question.getName());
		result.setQuizquestion(questionText);
		result.setCorrectanswers(question.getNumberOfCorrentUserAnswers());
		result.setTotalanswers(question.getNumberOfUserAnswers());
		return result;
	}
	
	protected static Set<String> generateChoices(String quizId, String questionMid, String goldAnswer,
					Iterator<String> wrongAnswers){
		Set<String> choices = new HashSet<String>();
		for (int i=0;i<ANSWERS_COUNT - 1;i++) {
			choices.add(wrongAnswers.next());
		}
		choices.add(goldAnswer);
		return choices;
	}
}
