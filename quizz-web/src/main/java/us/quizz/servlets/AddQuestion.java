package us.quizz.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.QuizQuestionRepository;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class AddQuestion extends HttpServlet {

	final static Logger logger = Logger.getLogger("com.ipeirotis.quizz");
	
	private QuizQuestionRepository questionRepositary;
	private AnswersRepository answersRepository;
	
	@Inject
	public AddQuestion(QuizQuestionRepository questionRepositary, AnswersRepository answersRepository){
		this.questionRepositary = questionRepositary;
		this.answersRepository = answersRepository;
	}

	static protected JsonParser jParser = new JsonParser();
	static protected Gson gson = new Gson();

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		BufferedReader reader = req.getReader();
		JsonObject jobject = jParser.parse(reader).getAsJsonObject();

		String quizID = jobject.get("quizID").getAsString();
		String text = jobject.get("text").getAsString();
		Double weight = jobject.get("weight").getAsDouble();
		Question question = new Question(quizID, text, weight);

		questionRepositary.singleMakePersistent(question);
		for (JsonElement je : jobject.get("answers").getAsJsonArray()) {
			Integer internalID = question.getAnswers().size();
			Answer answer = parseAnswer(je.getAsJsonObject(), question,
					internalID);
			question.addAnswer(answer);
		}
		answersRepository.saveAll(question.getAnswers());
		questionRepositary.singleMakePersistent(question);

		Status status = new Status(quizID, text, weight, question.getID());

		resp.setContentType("application/json");
		resp.getWriter().println(gson.toJson(status));
	}

	protected Answer parseAnswer(JsonObject jAnswer, Question question,
			Integer internalID) {
		Answer answer = new Answer(question.getID(), question.getQuizID(),
				jAnswer.get("text").getAsString(), internalID);
		String kind = jAnswer.get("kind").getAsString().toLowerCase();
		Preconditions.checkArgument(ANSWERS_PARSERS.containsKey(kind),
				"Unknown answer type: " + kind);
		answer.setKind(kind);
		ANSWERS_PARSERS.get(kind).parseIntoAnswer(jAnswer, answer, question);
		return answer;
	}

	protected interface AnswerParser {
		void parseIntoAnswer(JsonObject jAnswer, Answer answer,
				Question question);
	}

	protected static Map<String, AnswerParser> ANSWERS_PARSERS = new HashMap<String, AnswerParser>();
	static {
		ANSWERS_PARSERS.put("silver", new SilverAnswerParser());
		ANSWERS_PARSERS.put("selectable_gold", new GoldAnswerParser());
		ANSWERS_PARSERS.put("selectable_not_gold", new EmptyParser());
		ANSWERS_PARSERS.put("input_text", new GoldAnswerParser());
	}

	protected static class SilverAnswerParser implements AnswerParser {


		public void parseIntoAnswer(JsonObject jAnswer, Answer answer,
				Question question) {
			String source = jAnswer.get("source").getAsString();
			Double probability = jAnswer.get("probability").getAsDouble();
			answer.setSource(source);
			answer.setProbability(probability);
			question.setHasSilverAnswers(true);
		}
	}

	protected static class GoldAnswerParser implements AnswerParser {

		public void parseIntoAnswer(JsonObject jAnswer, Answer answer,
				Question question) {
			question.setHasGoldAnswer(true);
			answer.setIsGold(true);
		}
	}

	protected static class EmptyParser implements AnswerParser {

		public void parseIntoAnswer(JsonObject jAnswer, Answer answer,
				Question question) {
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