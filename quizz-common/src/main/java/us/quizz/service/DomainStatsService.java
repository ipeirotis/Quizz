package us.quizz.service;

import us.quizz.entities.DomainStats;
import us.quizz.repository.DomainStatsRepository;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

public class DomainStatsService {

  private DomainStatsRepository domainStatsRepository;
  
  @Inject
  public DomainStatsService(DomainStatsRepository domainStatsRepository){
    this.domainStatsRepository = domainStatsRepository;
  }

  public CollectionResponse<DomainStats> listWithCursor(String cursor, Integer limit){
    return domainStatsRepository.listWithCursor(cursor, limit);
  }
}