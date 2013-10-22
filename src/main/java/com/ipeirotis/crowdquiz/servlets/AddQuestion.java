package com.ipeirotis.crowdquiz.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
		
		PMF.singleMakePersistent(question);
		Status status = new Status(quizID, text, weight, question.getID());

		resp.setContentType("application/json");
		resp.getWriter().println(gson.toJson(status));
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