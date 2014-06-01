package us.quizz.servlets;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.service.QuizService;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class GetQuizCounts extends HttpServlet {
  protected static final String QUIZ_ID_PARAM = "quizID";
  protected static final String CACHE_PARAM = "cache";
  protected static final String CACHE_NO = "no";

  private QuizService quizService;

  @Inject
  public GetQuizCounts(QuizService quizService) {
    this.quizService = quizService;
  }

  // Given the HttpServletRequest, extracts the quizId param and cache param, then prints
  // the quiz counts as json results.
  // If cache = CACHE_NO, updates the quiz counts before printing..
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String quiz = req.getParameter(QUIZ_ID_PARAM);
    String cache = req.getParameter(CACHE_PARAM);
    if (cache != null && cache.equals(CACHE_NO)) {
      quizService.updateQuizCounts(quiz);
    }

    Quiz q = quizService.get(quiz);
    Preconditions.checkArgument(q != null, "Unknown quiz ID: " + quiz);

    resp.setContentType("application/json;charset=utf-8");
    Gson gson = new Gson();
    Response result = new Response(quiz, q.getQuestions(), q.getGold(), q.getSubmitted());
    String json = gson.toJson(result);
    resp.getWriter().println(json);
  }

  class Response {
    String quiz;
    Integer questions;
    Integer gold;
    Integer submitted;

    Response(String quiz, Integer questions, Integer gold, Integer submitted) {
      this.quiz = quiz;
      this.questions = questions;
      this.gold = gold;
      this.submitted = submitted;
    }
  }
}
