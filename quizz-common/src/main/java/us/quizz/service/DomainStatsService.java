package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.DomainStats;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.DomainStatsRepository;

public class DomainStatsService extends OfyBaseService<DomainStats> {
  @Inject
  public DomainStatsService(DomainStatsRepository domainStatsRepository){
    super(domainStatsRepository);
  }
}
