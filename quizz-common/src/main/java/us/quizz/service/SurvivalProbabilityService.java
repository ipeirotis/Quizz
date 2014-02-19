package us.quizz.service;

import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.utils.CachePMF;

import com.google.inject.Inject;

public class SurvivalProbabilityService {

	private QuizPerformanceRepository quizPerformanceRepository;
	
	@Inject
	public SurvivalProbabilityService(QuizPerformanceRepository quizPerformanceRepository){
		this.quizPerformanceRepository = quizPerformanceRepository;
	}

	public Result getSurvivalProbability(String quizID, Integer a_from, Integer a_to,
			Integer b_from, Integer b_to) {
		Long users_from = CachePMF.get(getKey(a_from, b_from), Long.class);
		Long users_to = CachePMF.get(getKey(a_to, b_to), Long.class);
		
		if(users_from == null || users_from == 0)
			return new Result(1L, 1L, 0.5d); // We assume a default survival probability of 0.5
		
		if(users_to == null)
			return new Result(1L, 1L, 0.5d); // We assume a default survival probability of 0.5

		double psurvival = 1.0*users_to/users_from;

		return new Result(users_from, users_to, psurvival);
	}
	
	public void cacheValue(Integer a, Integer b){
		long value = quizPerformanceRepository.getNumberOfAnswers(null, a, b);
		CachePMF.put(getKey(a, b), value, 60*60*25 );
	}
	
	private String getKey(Integer a, Integer b){
		return "survivalProbability_" + a + "_" + b;
	}

	public class Result {
		private long users_from;
		private long users_to;
		private double psurvival;

		public Result(long u_from, long u_to, double psurvival) {
			this.users_from = u_from;
			this.users_to = u_to;
			this.psurvival = psurvival;
		}

		public long getU_from() {
			return users_from;
		}

		public void setU_from(long u_from) {
			this.users_from = u_from;
		}

		public long getU_to() {
			return users_to;
		}

		public void setU_to(long u_to) {
			this.users_to = u_to;
		}

		public double getPsurvival() {
			return psurvival;
		}

		public void setPsurvival(double psurvival) {
			this.psurvival = psurvival;
		}
	}
}
