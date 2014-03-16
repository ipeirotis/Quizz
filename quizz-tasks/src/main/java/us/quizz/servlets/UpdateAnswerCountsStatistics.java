package us.quizz.servlets;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@Singleton
public class UpdateAnswerCountsStatistics extends HttpServlet {
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(UpdateAnswerCountsStatistics.class.getName());

  private QuizRepository quizRepository;
  private QuizQuestionRepository quizQuestionRepository;
  private UserAnswerRepository userAnswerRepository;

  @Inject
  public UpdateAnswerCountsStatistics(
      QuizRepository quizRepository,
      QuizQuestionRepository quizQuestionRepository,
      UserAnswerRepository userAnswerRepository) {
    this.quizRepository = quizRepository;
    this.quizQuestionRepository = quizQuestionRepository;
    this.userAnswerRepository = userAnswerRepository;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if ("true".equals(request.getParameter("all"))) {
      List<Quiz> list = quizRepository.getQuizzes();
      Queue queue = QueueFactory.getDefaultQueue();
      for (Quiz quiz : list) {
        queue.add(Builder
            .withUrl("/api/updateAnswerCountsStatistics")
            .param("quizID", quiz.getQuizID().toString())
            .retryOptions(RetryOptions.Builder.withTaskRetryLimit(1))
            .method(TaskOptions.Method.GET));
      }
    } else {
      updateStatistics(request.getParameter("quizID"));
    }
  }

  private void updateStatistics(String quizID) {
    if (quizID != null && !quizID.isEmpty()) {
      List<UserAnswer> userAnswers = userAnswerRepository.getUserAnswers(quizID);
      if (userAnswers != null) {
        Map<Long, Map<Integer, Long>> questionsMap = new HashMap<Long, Map<Integer,Long>>();
        for (UserAnswer ua : userAnswers) {
          Map<Integer, Long> answersMap;
          if (questionsMap.containsKey(ua.getQuestionID())) {
            answersMap = questionsMap.get(ua.getQuestionID());
            if (answersMap.containsKey(ua.getAnswerID())) {
              answersMap.put(ua.getAnswerID(), answersMap.get(ua.getAnswerID()) + 1);
            } else {
              answersMap.put(ua.getAnswerID(), 1L);
            }
          } else {
            answersMap = new HashMap<Integer, Long>();
            answersMap.put(ua.getAnswerID(), 1L);

            questionsMap.put(ua.getQuestionID(), answersMap);
          }
        }

        List<Key> keys = new ArrayList<Key>();
        for (Long questionId : questionsMap.keySet()) {
          keys.add(KeyFactory.createKey(Question.class.getSimpleName(), questionId));
        }
        if (keys.size() != 0) {
          List<Question> questions = quizQuestionRepository.getQuizQuestionsByKeys(keys);
          for (Question question : questions) {
            Map<Integer, Long> answersMap = questionsMap.get(question.getID());
            for (Map.Entry<Integer, Long> entry : answersMap.entrySet()) {
              if (question.getAnswers() != null &&
                  entry.getKey() >= 0 && 
                  question.getAnswers().size() > entry.getKey()) {
                Answer answer = question.getAnswers().get(entry.getKey());
                if (answer != null) {
                  answer.setNumberOfPicks(entry.getValue());
                }
              }
            }
          }
        }
      }
    }
  }
}
