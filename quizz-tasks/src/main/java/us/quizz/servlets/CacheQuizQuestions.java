package us.quizz.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class CacheQuizQuestions extends HttpServlet {
  // Number of questions to cache in Memcache for each quiz.
  private static final int NUM_CACHED_QUESTIONS = 10;

  private QuizQuestionRepository quizQuestionRepository;
  private QuizRepository quizRepository;

  @Inject
  public CacheQuizQuestions(QuizQuestionRepository quizQuestionRepository,
      QuizRepository quizRepository){
    this.quizQuestionRepository = quizQuestionRepository;
    this.quizRepository = quizRepository;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    List<Quiz> list = quizRepository.getQuizzes();
    for (Quiz quiz : list) {
      resp.getWriter().println("Updating quiz: " + quiz.getName());
      quizQuestionRepository.getNextQuizQuestionsWithoutCaching(
          quiz.getQuizID(), NUM_CACHED_QUESTIONS);
    }
  }
}
