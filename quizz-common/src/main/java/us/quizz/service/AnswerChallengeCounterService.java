package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.AnswerChallengeCounterRepository;

import java.util.List;

public class AnswerChallengeCounterService extends OfyBaseService<AnswerChallengeCounter> {
  @Inject
  public AnswerChallengeCounterService(
      AnswerChallengeCounterRepository answerChallengeCounterRepository) {
    super(answerChallengeCounterRepository);
  }

  public AnswerChallengeCounter get(String quizID, Long questionID) {
    return get(AnswerChallengeCounter.generateId(quizID, questionID));
  }
}
