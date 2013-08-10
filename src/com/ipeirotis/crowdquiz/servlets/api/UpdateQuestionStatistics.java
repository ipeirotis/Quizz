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

		String relation = req.getParameter("relation");
		String mid = req.getParameter("mid");
		
		resp.getWriter().print("Relation:"+relation+"\n");
		resp.getWriter().print("Mid:"+mid+"\n");

		QuizQuestion question = QuizQuestionRepository.getQuizQuestion(relation, mid);
		if (question==null) return;
		
		int g = getNumberOfGoldAnswers(relation, mid);
		question.setHasGoldAnswer((g>0));
		question.setNumberOfGoldAnswers(g);
		resp.getWriter().print("Number of gold answers:"+g+"\n");
		
		int s = getNumberOfSilverAnswers(relation, mid);
		question.setHasSilverAnswers((s>0));
		question.setNumberOfSilverAnswers(s);
		resp.getWriter().print("Number of silver answers:"+s+"\n");
		
		int u = getNumberOfUserAnswers(relation, mid);
		question.setHasUserAnswers((u>0));
		question.setNumberOfUserAnswers(u);
		resp.getWriter().print("Number of user answers:"+u+"\n");

		int c = getNumberOfCorrectUserAnswers(relation, mid);
		question.setNumberOfCorrentUserAnswers(c);
		resp.getWriter().print("Number of correct user answers:"+c+"\n");
		
		QuizQuestionRepository.storeQuizQuestion(question);
		
		

	}
	
	private int getNumberOfGoldAnswers(String quiz, String mid) {
		return QuizQuestionRepository.getGoldAnswers(quiz,mid).size();
	}

	private int getNumberOfSilverAnswers(String quiz, String mid) {
		return QuizQuestionRepository.getSilverAnswers(quiz, mid, true, 0.0).size();
	}
	private int getNumberOfUserAnswers(String quiz, String mid) {
		return QuizQuestionRepository.getNumberOfUserAnswersExcludingIDK(quiz, mid);
	}
	private int getNumberOfCorrectUserAnswers(String quiz, String mid) {
		return QuizQuestionRepository.getNumberOfCorrectUserAnswers(quiz,mid);
	}
	
	
}
