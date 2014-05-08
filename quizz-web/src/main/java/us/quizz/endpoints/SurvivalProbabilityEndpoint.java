package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;

import us.quizz.entities.SurvivalProbabilityResult;
import us.quizz.service.SurvivalProbabilityService;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class SurvivalProbabilityEndpoint {
  private SurvivalProbabilityService survivalProbabilityService;

  @Inject
  public SurvivalProbabilityEndpoint(SurvivalProbabilityService survivalProbabilityService) {
    this.survivalProbabilityService = survivalProbabilityService;
  }

  @ApiMethod(name = "getSurvivalProbability", path = "getSurvivalProbability")
  public SurvivalProbabilityResult getSurvivalProbability(
      @Nullable @Named("quizID") String quizID,
      @Named("a_from") Integer a_from,
      @Named("a_to") Integer a_to,
      @Named("b_from") Integer b_from,
      @Named("b_to") Integer b_to,
      @Nullable @Named("c_from") Integer c_from,
      @Nullable @Named("c_to") Integer c_to) {
    return survivalProbabilityService
        .getSurvivalProbability(quizID, a_from, a_to, b_from, b_to, c_from, c_to);
  }

  @ApiMethod(name = "getSurvivalProbabilities", path = "getSurvivalProbabilities")
  public List<SurvivalProbabilityResult> getSurvivalProbabilities(
      @Nullable @Named("quizID") String quizID) {
    return survivalProbabilityService.getSurvivalProbabilities(quizID);
  }
}
