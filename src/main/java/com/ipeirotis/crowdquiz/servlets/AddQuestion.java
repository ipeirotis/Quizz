package com.ipeirotis.crowdquiz.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ipeirotis.crowdquiz.entities.Answer;
import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddQuestion extends HttpServlet {

	final static Logger	logger	= Logger.getLogger("com.ipeirotis.quizz");
	
	static protected JsonParser jParser = new JsonParser();
	static protected Gson gson = new Gson();

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		BufferedReader reader = req.getReader();
		JsonObject jobject = jParser.parse(reader).getAsJsonObject();
		
		String quizID = jobject.get("quizID").getAsString();
		String text = jobject.get("text").getAsString();
		Double weight = jobject.get("weight").getAsDouble();
		Question question = new Question(quizID, text, weight);
		PersistenceManager pm = PMF.getPM();
		try{
			pm.makePersistent(question); // To generate key
			for (JsonElement je: jobject.get("answers").getAsJsonArray()) {
				Integer internalID = question.getAnswers().size();
				Answer answer = parseAnswer(je.getAsJsonObject(), question, internalID);
				question.addAnswer(answer);
			}
			pm.makePersistentAll(question.getAnswers());
			pm.makePersistent(question);
		} finally {
			pm.close();
		}
		Status status = new Status(quizID, text, weight, question.getID());

		resp.setContentType("application/json");
		resp.getWriter().println(gson.toJson(status));
	}
	
	protected Answer parseAnswer(JsonObject jAnswer, Question question, Integer internalID){
		Answer answer = new Answer(question.getID(), question.getQuizID(),
				jAnswer.get("text").getAsString(), internalID);
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
	
	protected static class Status {
		protected String quizID;
		protected String text;
		protected Double weight;
		protected Long questionID;
		
		public Status(String quizID, String text, Double weight, Long questionID) {
			this.quizID = quizID;
			this.text = text;
			this.weight = weight;
			this.questionID = questionID;
		}
	}
}
