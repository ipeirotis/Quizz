package us.quizz.endpoints;

import java.util.List;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;

import us.quizz.service.SurvivalProbabilityService;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1",
     namespace = @ApiNamespace(ownerDomain = "crowd-power.appspot.com",
                               ownerName = "crowd-power.appspot.com",
                               packagePath = "us.quizz.endpoints"))
public class SurvivalProbabilityEndpoint {
  private SurvivalProbabilityService survivalProbabilityService;

  @Inject
  public SurvivalProbabilityEndpoint(SurvivalProbabilityService survivalProbabilityService) {
    this.survivalProbabilityService = survivalProbabilityService;
  }

  @ApiMethod(name = "getSurvivalProbability", path = "getSurvivalProbability")
  public SurvivalProbabilityService.Result getSurvivalProbability(
                    @Nullable @Named("quizID") String quizID,
                    @Named("a_from") Integer a_from,
                    @Named("a_to") Integer a_to,
                    @Named("b_from") Integer b_from,
                    @Named("b_to") Integer b_to) {
    return survivalProbabilityService
        .getSurvivalProbability(quizID, a_from, a_to, b_from, b_to);
  }
  
  @ApiMethod(name = "getSurvivalProbabilities", path = "getSurvivalProbabilities")
  public List<SurvivalProbabilityService.Result> getSurvivalProbabilities(
                    @Nullable @Named("quizID") String quizID) {
    return survivalProbabilityService.getSurvivalProbabilities(quizID);
  }
  
}
