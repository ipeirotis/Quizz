package us.quizz.service;

import us.quizz.entities.Question;
import us.quizz.repository.QuizQuestionRepository;

import com.google.inject.Inject;

public class QuestionStatisticsService {
  private QuizQuestionRepository quizQuestionRepository;

  @Inject
  public QuestionStatisticsService(QuizQuestionRepository quizQuestionRepository) {
    this.quizQuestionRepository = quizQuestionRepository;
  }

  public Question updateStatistics(String questionID) {
    Question question = quizQuestionRepository.getQuizQuestion(questionID);
    if (question == null) {
      throw new IllegalArgumentException("Question with id=" + questionID + " is not exist");
    }
    int u = getNumberOfUserAnswers(questionID);
    question.setHasUserAnswers((u > 0));
    question.setNumberOfUserAnswers(u);

    int c = getNumberOfCorrectUserAnswers(questionID);
    question.setNumberOfCorrentUserAnswers(c);

    quizQuestionRepository.storeQuizQuestion(question);
    
    return question;
  }

  private int getNumberOfUserAnswers(String questionID) {
    return quizQuestionRepository
        .getNumberOfUserAnswersExcludingIDK(questionID);
  }

  private int getNumberOfCorrectUserAnswers(String questionID) {
    return quizQuestionRepository.getNumberOfCorrectUserAnswers(questionID);
  }
}
