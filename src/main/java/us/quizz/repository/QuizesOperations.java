package us.quizz.repository;

import java.util.ArrayList;
import java.util.Set;

import us.quizz.entities.Question;
import us.quizz.utils.CachePMF;
import us.quizz.utils.Helper;

public class QuizesOperations {
	
	public static Question getNextQuizQuestion(String quiz) {
		return getNextQuizQuestions(quiz, 1).iterator().next();
	}

	public static Set<Question> getNextQuizQuestions(String quiz, int n) {
		String key  = "getquizquestion_" + quiz + n;
		Set<Question> result = CachePMF.get(key, Set.class);
		if (result != null) return result;
		
		int N = n * 5;
		ArrayList<Question> list = QuizQuestionRepository.getSomeQuizQuestionsWithGold(quiz, N);
		result = Helper.trySelectingRandomElements(list, n);
		int cached_lifetime = 5 * 60; // 10 minutes
		CachePMF.put(key, result, cached_lifetime);
		
		return result;
	}
}
