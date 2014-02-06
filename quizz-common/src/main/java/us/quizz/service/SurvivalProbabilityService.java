package us.quizz.service;

import us.quizz.repository.QuizPerformanceRepository;

import com.google.inject.Inject;

public class SurvivalProbabilityService {

	private QuizPerformanceRepository quizPerformanceRepository;
	
	@Inject
	public SurvivalProbabilityService(QuizPerformanceRepository quizPerformanceRepository){
		this.quizPerformanceRepository = quizPerformanceRepository;
	}

	public Result getSurvivalProbability(String quizID, Integer a_from, Integer a_to,
			Integer b_from, Integer b_to) {
		long u_from = quizPerformanceRepository.getNumberOfAnswers(quizID, a_from, b_from);
		long u_to = quizPerformanceRepository.getNumberOfAnswers(quizID, a_to, b_to);
		double psurvival = (u_from == 0) ? 1.0 : 1.0*u_to/u_from;

		return new Result(u_from, u_to, psurvival);
	}

	public class Result {
		private long u_from;
		private long u_to;
		private double psurvival;

		public Result(long u_from, long u_to, double psurvival) {
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
