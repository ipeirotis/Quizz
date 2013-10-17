package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizQuestionRepository;

import com.ipeirotis.crowdquiz.entities.QuizQuestion;

@SuppressWarnings("serial")
public class UpdateQuestionStatistics extends HttpServlet {



	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		String strQuestionId = req.getParameter("questionID");
		Long questionID = Long.parseLong(strQuestionId);
		resp.getWriter().print("QuestionID:"+questionID+"\n");

		QuizQuestion question = QuizQuestionRepository.getQuizQuestion(questionID);
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
	
	private int getNumberOfUserAnswers(Long questionID) {
		return QuizQuestionRepository.getNumberOfUserAnswersExcludingIDK(questionID);
	}
	
	private int getNumberOfCorrectUserAnswers(Long questionID) {
		return QuizQuestionRepository.getNumberOfCorrectUserAnswers(questionID);
	}
	
	
}
