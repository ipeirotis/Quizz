package us.quizz.endpoints;

import javax.annotation.Nullable;
import javax.inject.Named;

import us.quizz.repository.QuizPerformanceRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class SurvivalProbabilityEndpoint {
	
	private QuizPerformanceRepository quizPerformanceRepository;
	
	@Inject
	public SurvivalProbabilityEndpoint(QuizPerformanceRepository quizPerformanceRepository){
		this.quizPerformanceRepository = quizPerformanceRepository;
	}

	@ApiMethod(name = "getSurvivalProbability", path="getSurvivalProbability")
	public Response getSurvivalProbability(@Nullable @Named("quizID") String quizID, 
										@Named("a_from") Integer a_from, 
										@Named("a_to") Integer a_to,
										@Named("b_from") Integer b_from,
										@Named("b_to") Integer b_to) {
		long u_from = quizPerformanceRepository
				.getNumberOfAnswers(quizID, a_from, b_from);
		long u_to = quizPerformanceRepository
				.getNumberOfAnswers(quizID, a_to, b_to);
		double psurvival = (u_from == 0) ? 1.0 : 1.0*u_to/u_from;
		
		return new Response(u_from, u_to, psurvival);
	}

	class Response {
		private long u_from;
		private long u_to;
		private double psurvival;

		public Response(long u_from, long u_to, double psurvival) {
			this.u_from = u_from;
			this.u_to = u_to;
			this.psurvival = psurvival;
		}

		public long getU_from() {
			return u_from;
		}

		public void setU_from(long u_from) {
			this.u_from = u_from;
		}

		public long getU_to() {
			return u_to;
		}

		public void setU_to(long u_to) {
			this.u_to = u_to;
		}

		public double getPsurvival() {
			return psurvival;
		}

		public void setPsurvival(double psurvival) {
			this.psurvival = psurvival;
		}
	}

}