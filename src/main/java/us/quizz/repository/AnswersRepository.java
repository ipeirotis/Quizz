package us.quizz.repository;

import com.ipeirotis.crowdquiz.entities.Answer;

public class AnswersRepository {
	
	static public Answer getAnswer(Long questionID, Integer answerID) {
		return QuizQuestionRepository.getQuizQuestion(questionID).getAnswer(answerID);
	}

}
