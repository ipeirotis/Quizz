package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizQuestionRepository;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ipeirotis.crowdquiz.entities.Answer;
import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddAnswer extends HttpServlet {
	final static Logger	logger	= Logger.getLogger("com.ipeirotis.crowdquiz");
	
	static protected JsonParser jParser = new JsonParser();
	static protected Gson gson = new Gson();
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		JsonObject jobject = jParser.parse(req.getReader()).getAsJsonObject();

		Long questionID = jobject.get("questionID").getAsLong();
		Question question = QuizQuestionRepository.getQuizQuestion(questionID);
		if (question == null) {
			throw new IllegalArgumentException("Unknown question: " + questionID);
		}
		Answer answer = parseAnswer(jobject, question);
		question.addAnswer(answer);

		PMF.singleMakePersistent(answer);
		PMF.singleMakePersistent(question);

		resp.setContentType("application/json");
		resp.getWriter().println("{ \"answerID: \"" + answer.getID() + "\"}");
	}
	
	protected Answer parseAnswer(JsonObject jAnswer, Question question){
		Answer answer = new Answer(question.getID(), question.getQuizID(),
				jAnswer.get("text").getAsString());
		if (jAnswer.has("probability")) {
			parseSilverAnswer(jAnswer, answer, question);
		} else {
			parseGoldAnswer(jAnswer, answer, question);
		}
		return answer;
	}
	
	private void parseSilverAnswer(JsonObject jAnswer, Answer answer, Question question) {
		
		String source = jAnswer.get("source").getAsString();
		Double probability = jAnswer.get("probability").getAsDouble();
		
		answer.setKind("silver");
		answer.setSource(source);
		answer.setProbability(probability);
		question.setHasSilverAnswers(true);
	}

	private void parseGoldAnswer(JsonObject jAnswer, Answer answer, Question question) {
		boolean isGold = false;
		if (jAnswer.has("isGold")) {
			String gold = jAnswer.get("isGold").getAsString();
			isGold = !Strings.isNullOrEmpty(gold) && Boolean.parseBoolean(gold);
		}
		if (isGold) {
			question.setHasGoldAnswer(true);
			answer.setIsGold(true);
			answer.setKind("gold");
		} else {
			answer.setKind("normal_from_golds");
		}
	}
}
