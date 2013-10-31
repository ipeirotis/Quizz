package us.quizz.repository;

import java.util.ArrayList;
import java.util.Set;

import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.utils.Helper;

public class QuizesOperations {
	
	public static Question getNextQuizQuestion(String quiz) {
		return getNextQuizQuestions(quiz, 1).iterator().next();
	}

	public static Set<Question> getNextQuizQuestions(String quiz, int n) {
		int N = n * 50;
		ArrayList<Question> list = QuizQuestionRepository.getSomeQuizQuestionsWithGold(quiz, N);
		return Helper.trySelectingRandomElements(list, n);
	}
}
