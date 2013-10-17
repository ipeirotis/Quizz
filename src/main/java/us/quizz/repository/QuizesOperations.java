package us.quizz.repository;

import java.util.ArrayList;
import java.util.Set;

import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.utils.Helper;

public class QuizesOperations {
	
	protected static int ANSWERS_COUNT = 4;

	public static QuizQuestion getNextQuizQuestion(String quiz) {
		return getNextQuizQuestions(quiz, 1).iterator().next();
	}

	public static Set<QuizQuestion> getNextQuizQuestions(String quiz, int n) {
		ArrayList<QuizQuestion> list = QuizQuestionRepository.getQuizQuestionsWithGold(quiz);
		return Helper.selectRandomElements(list, n);
	}

	public static QuizQuestion getNextQuizQuestionInstance(String quizId) {
		return getNextQuizQuestions(quizId, 1).iterator().next();
	}

}
