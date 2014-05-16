package us.quizz.service;

import java.util.List;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.repository.AnswerChallengeCounterRepository;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

public class AnswerChallengeCounterService {

  private AnswerChallengeCounterRepository answerChallengeCounterRepository;
  
  @Inject
  public AnswerChallengeCounterService(AnswerChallengeCounterRepository answerChallengeCounterRepository){
    this.answerChallengeCounterRepository = answerChallengeCounterRepository;
  }
  
  public AnswerChallengeCounter get(String quizID, Long questionID) {
    return answerChallengeCounterRepository.get(AnswerChallengeCounter.generateId(quizID, questionID));
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
