package us.quizz.servlets.api;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Question;
import us.quizz.repository.QuizQuestionRepository;

@SuppressWarnings("serial")
public class UpdateQuestionStatistics extends HttpServlet {



	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain;charset=utf-8");

		String questionID = req.getParameter("questionID");
		resp.getWriter().print("QuestionID:"+questionID+"\n");

		Question question = QuizQuestionRepository.getQuizQuestion(questionID);
		if (question==null) return;
		
		int u = getNumberOfUserAnswers(questionID);
		question.setHasUserAnswers((u>0));
		question.setNumberOfUserAnswers(u);
		resp.getWriter().print("Number of user answers:"+u+"\n");

		int c = getNumberOfCorrectUserAnswers(questionID);
		question.setNumberOfCorrentUserAnswers(c);
		resp.getWriter().print("Number of correct user answers:"+c+"\n");
		
		QuizQuestionRepository.storeQuizQuestion(question);
	}
	
	private int getNumberOfUserAnswers(String questionID) {
		return QuizQuestionRepository.getNumberOfUserAnswersExcludingIDK(questionID);
	}
	
	private int getNumberOfCorrectUserAnswers(String questionID) {
		return QuizQuestionRepository.getNumberOfCorrectUserAnswers(questionID);
	}
	
	
}
