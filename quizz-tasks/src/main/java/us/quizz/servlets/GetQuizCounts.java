package us.quizz.servlets;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class GetQuizCounts extends HttpServlet {
  private QuizRepository quizRepository;

  @Inject
  public GetQuizCounts(QuizRepository quizRepository) {
    this.quizRepository = quizRepository;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String quiz = req.getParameter("quizID");
    String cache = req.getParameter("cache");
    if (cache != null && cache.equals("no")) {
      quizRepository.updateQuizCounts(quiz);
    }

    Quiz q = quizRepository.get(quiz);
    Preconditions.checkArgument(q != null, "Unknown quiz ID: " + quiz);

    resp.setContentType("application/json;charset=utf-8");
    Gson gson = new Gson();
    Response result = new Response(quiz, q.getQuestions(), q.getGold(),
        q.getSubmitted());
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
