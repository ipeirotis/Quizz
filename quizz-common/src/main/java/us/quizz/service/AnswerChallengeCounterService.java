package us.quizz.service;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.repository.AnswerChallengeCounterRepository;

import java.util.List;

public class AnswerChallengeCounterService {

  private AnswerChallengeCounterRepository answerChallengeCounterRepository;
  
  @Inject
  public AnswerChallengeCounterService(
      AnswerChallengeCounterRepository answerChallengeCounterRepository){
    this.answerChallengeCounterRepository = answerChallengeCounterRepository;
  }
  
  public AnswerChallengeCounter get(String quizID, Long questionID) {
    return answerChallengeCounterRepository.get(
        AnswerChallengeCounter.generateId(quizID, questionID));
  }
  
  public AnswerChallengeCounter save(AnswerChallengeCounter answerChallengeCounter){
    return answerChallengeCounterRepository.saveAndGet(answerChallengeCounter);
  }

  public CollectionResponse<AnswerChallengeCounter> list(String cursorString, Integer limit){
    return answerChallengeCounterRepository.listWithCursor(cursorString, limit);
  }

  public List<AnswerChallengeCounter> listAll(){
      return answerChallengeCounterRepository.listAll();
  }
}
