package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizQuestionRepository;

import com.google.common.base.Strings;
import com.ipeirotis.crowdquiz.entities.Answer;
import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddAnswer extends HttpServlet {
	final static Logger					logger	= Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		Utils.ensureParameters(req, "questionID", "answer");

		String strQuestionID = req.getParameter("questionID").trim();
		Long questionID = Long.parseLong(strQuestionID);
		
		Question question = QuizQuestionRepository.getQuizQuestion(questionID);
		if (question == null) {
			throw new IllegalArgumentException("Unknown question: " + questionID);
		}
		
		Answer answer = new Answer();
		answer.setText(req.getParameter("answer").trim());
		answer.setQuizID(question.getQuizID());
		question.addAnswer(answer);
		
		if (Strings.isNullOrEmpty(req.getParameter("probability"))) {
			goldAnswer(answer, question, req);
		} else {
			silverAnswer(answer, question, req);
		}

		QuizQuestionRepository.storeQuizQuestion(question);
		PMF.singleMakePersistent(answer);
		resp.getWriter().println("OK");
	}

	private void silverAnswer(Answer answer, Question qq,
			HttpServletRequest req) {
		
		String source = req.getParameter("source").trim();
		
		String prob = req.getParameter("probability");
		Double probability = -1.0;
		if (prob != null) {
			try {
			probability = Double.parseDouble(prob);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		} else {
			return;
		}
		answer.setKind("silver");
		answer.setSource(source);
		answer.setProbability(probability);
		qq.setHasSilverAnswers(true);
	}

	private void goldAnswer(Answer answer, Question qq,
			HttpServletRequest req) {
		String gold = req.getParameter("isGold");
		if (!Strings.isNullOrEmpty(gold) && Boolean.parseBoolean(gold)) {
			qq.setHasGoldAnswer(true);
			answer.setGold(true);
			answer.setKind("gold");
		} else {
			answer.setKind("normal_from_golds");
		}
	}
}
