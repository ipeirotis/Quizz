package us.quizz.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.UserAnswerRepository;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.inject.Inject;

public class AnswerCountsStatisticsService {
  private QuizQuestionRepository quizQuestionRepository;
  private UserAnswerRepository userAnswerRepository;

  @Inject
  public AnswerCountsStatisticsService(
      QuizQuestionRepository quizQuestionRepository,
      UserAnswerRepository userAnswerRepository) {
    this.quizQuestionRepository = quizQuestionRepository;
    this.userAnswerRepository = userAnswerRepository;
  }

  public void updateStatistics(String quizID) {
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
