package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.repository.AnswersRepository;

import java.util.List;

public class AnswerService {
  private AnswersRepository answerRepository;
  
  @Inject
  public AnswerService(AnswersRepository answerRepository) {
    this.answerRepository = answerRepository;
  }

  public Answer save(Answer answer){
    return answerRepository.saveAndGet(answer);
  }

  public List<Answer> list() {
    return answerRepository.listAllByCursor();
  }
}
