package us.quizz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Question;
import us.quizz.repository.QuizQuestionRepository;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UpdateQuestionStatistics extends HttpServlet {

	private QuizQuestionRepository quizQuestionRepository;
	
	@Inject
	public UpdateQuestionStatistics(QuizQuestionRepository quizQuestionRepository){
		this.quizQuestionRepository = quizQuestionRepository;
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain;charset=utf-8");

		String questionID = req.getParameter("questionID");
		resp.getWriter().print("QuestionID:" + questionID + "\n");

		Question question = quizQuestionRepository.getQuizQuestion(questionID);
		if (question == null)
			return;

		int u = getNumberOfUserAnswers(questionID);
		question.setHasUserAnswers((u > 0));
		question.setNumberOfUserAnswers(u);
		resp.getWriter().print("Number of user answers:" + u + "\n");

		int c = getNumberOfCorrectUserAnswers(questionID);
		question.setNumberOfCorrentUserAnswers(c);
		resp.getWriter().print("Number of correct user answers:" + c + "\n");

		quizQuestionRepository.storeQuizQuestion(question);
	}

	private int getNumberOfUserAnswers(String questionID) {
		return quizQuestionRepository
				.getNumberOfUserAnswersExcludingIDK(questionID);
	}

	private int getNumberOfCorrectUserAnswers(String questionID) {
		return quizQuestionRepository.getNumberOfCorrectUserAnswers(questionID);
	}

}
