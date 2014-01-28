package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import us.quizz.entities.Question;
import us.quizz.utils.CachePMF;
import us.quizz.utils.Helper;

public class QuizesOperations {

	public static Map<String, Set<Question>> getNextQuizQuestions(String quiz, int n) {
		String key = "getquizquestion_" + quiz + n;
		@SuppressWarnings("unchecked")
		Map<String, Set<Question>> result = CachePMF.get(key, Map.class);
		if (result != null)
			return result;
		else
			result = new HashMap<String, Set<Question>>();

		int N = n * 5;
		ArrayList<Question> goldQuestions = QuizQuestionRepository
				.getSomeQuizQuestionsWithGold(quiz, N);
		result.put("gold", Helper.trySelectingRandomElements(goldQuestions, n));
		ArrayList<Question> silverQuestions = QuizQuestionRepository
				.getSomeQuizQuestionsWithSilver(quiz, N);
		result.put("silver", Helper.trySelectingRandomElements(silverQuestions, n));
		
		int cached_lifetime = 5 * 60; // 10 minutes
		CachePMF.put(key, result, cached_lifetime);

		return result;
	}
	
}
