package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.AnswersRepository;

import java.util.List;

public class AnswerService extends OfyBaseService<Answer> {
  @Inject
  public AnswerService(AnswersRepository answerRepository) {
    super(answerRepository);
  }
}
