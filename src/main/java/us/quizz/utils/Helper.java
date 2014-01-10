package us.quizz.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.math3.special.Gamma;


public class Helper {


	public static String getBaseURL(HttpServletRequest req) {
		String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
		return baseURL;
	}

	/**
	 * Computing the entropy of an answer given by a user with quality q (quality=probability of correct)
	 * and n available options in the multiple choice question
	 * @throws Exception 
	 * 
	 */
	public static double entropy(double q, int n) throws Exception {
		if (q==1.0 || n==1) return 0;
		if (q==0.0) return -Math.log(1.0/(n-1))/Math.log(2);
		if (n<1) throw new Exception("Invalid value for n in entropy calculation");
		if (q<0.0 || q>1.0) throw new Exception("Invalid value for q in entropy calculation");
		double entropy = (1-q) * Math.log((1-q)/(n-1))/Math.log(2)+ q*Math.log(q)/Math.log(2);
		return -entropy;
	}
	
	/**
	 * The score is the total amount of information contributed by the user.
	 * 
	 * We compute the information gain for a single answer of quality q,
	 * and multiply with the total number of answers given
	 * @throws Exception 
	 * 
	 * 
	 */
	public static double getInformationGain(double q, int n) throws Exception {
		double informationGain = Helper.entropy(1.0/n, n) - Helper.entropy(q,n);
		return informationGain;
	}
	
	/**
	 * Computes a Bayesian version of Information Gain
	 * 
	 * @param a The number of correct answers (plus the prior)
	 * @param b The number of incorrect answers (plus the prior)
	 * @param n The number of multiple options in the quiz
	 * @return
	 * @throws Exception
	 */
	public static double getBayesianMeanInformationGain(double a, double b, int n) throws Exception {
	
		// We have that limit(x*Psi(x), x=0) = -1
		// Based on this, for the Bayesian Information Gain (BIG): 
		// BIG(0,b,n) = lg(n) - lg(n-1) 
		// BIG(a,0,n) = lg(n)
		// This means that BIG(0,0) does not have a value when the prior it Beta(0,0)
		
		if (a<0 || b<0 || n<=1) throw new Exception("Unsupported parameters values");
		if (a==0 && b==0 && n>2) throw new Exception("Undefined value");
		if (a==0 && b==0 && n==2) return Math.log(2)/Math.log(2); // The only case where we have convergence as a-->0, b-->0
		if (a==0 && b>0) return Math.log(n)/Math.log(2)-Math.log(n-1)/Math.log(2);
		if (a>0 && b==0) return Math.log(n)/Math.log(2);
		
		double result = Math.log(n) - (b/(a+b)) * Math.log(n-1) - getBayesianEntropy(a,b);
		
		return result;
		
	}
	
	
	/**
	 * Computes a Bayesian version of Information Gain
	 * 
	 * @param a The number of correct answers (plus the prior)
	 * @param b The number of incorrect answers (plus the prior)
	 * @param n The number of multiple options in the quiz
	 * @return
	 * @throws Exception
	 */
	public static double getBayesianVarianceInformationGain(double a, double b, int n) throws Exception {
	
		double result = getBayesianInformationGainSquare(a, b, n) - Math.pow(getBayesianMeanInformationGain(a, b, n), 2);
		
		return result;
		
	}
	
	/**
	 * Computes a Bayesian version of Entropy
	 * 
	 * @param a The number of correct answers (plus the prior)
	 * @param b The number of incorrect answers (plus the prior)
	 * @return
	 * @throws Exception
	 */
	public static double getBayesianEntropy(double a, double b) throws Exception {
	
		double nominator = (a+b) * Gamma.digamma(a+b+1) - a*Gamma.digamma(a+1) - b*Gamma.digamma(b+1);
		double denominator = (a+b) ;
		
		return nominator/denominator;
		
	}
	
	
	/**
	 * Computes a Bayesian version of Information Gain
	 * 
	 * @param a The number of correct answers (plus the prior)
	 * @param b The number of incorrect answers (plus the prior)
	 * @param n The number of multiple options in the quiz
	 * @return
	 * @throws Exception
	 */
	public static double getBayesianEntropySquare(double a, double b) throws Exception {
	

		double coef_ab = 2*(a*b)/((a+b)*(a+b+1));
		double coef_a = a*(a+1)/((a+b+1)*(a+b));
		double coef_b = b*(b+1)/((a+b+1)*(a+b));
		
		double Ja = Math.pow(Gamma.digamma(a+2) - Gamma.digamma(a+b+2), 2) + Gamma.trigamma(a+2) - Gamma.trigamma(a+b+2); 
		double Jb = Math.pow(Gamma.digamma(b+2) - Gamma.digamma(a+b+2), 2) + Gamma.trigamma(b+2) - Gamma.trigamma(a+b+2); 
		double Iab = (Gamma.digamma(a+1) - Gamma.digamma(a+b+2)) * (Gamma.digamma(b+1) - Gamma.digamma(a+b+2)) - Gamma.trigamma(a+b+2);
		
		double result = coef_a * Ja + coef_b * Jb + coef_ab * Iab;
		
		return result;
		
	}

	
	/**
	 * Computes a Bayesian version of Information Gain
	 * 
	 * @param a The number of correct answers (plus the prior)
	 * @param b The number of incorrect answers (plus the prior)
	 * @param n The number of multiple options in the quiz
	 * @return
	 * @throws Exception
	 */
	public static double getBayesianInformationGainSquare(double a, double b, int n) throws Exception {
	

		double A2 = Math.log(n)*Math.log(n);
		double B2 = getBayesianEntropySquare(a,b);
		double C2 = b*(b+1)*Math.log(n-1)*Math.log(n-1) / ( (a+b) * (a+b+1) );
		double AB = -2*Math.log(n)*getBayesianEntropy(a,b);
		double AC = -2*Math.log(n)*Math.log(n-1)*b/(a+b);
		double BC_1 = (2 * Math.log(n-1) * b)/ ((a+b) * (a+b+1));
		double BC_2 = (a+b+1)*Gamma.digamma(a+b+1) -(b+1)*Gamma.digamma(b+1) -a*Gamma.digamma(a+1);
		
		double result = A2 + B2 + C2 + AB + AC + BC_1 * BC_2;
		
		return result;
		
	}
	
	/**
	 * Selects random elements without replacement, modifies elements order in given array!
	 */
	public static <T> Set<T> selectRandomElements(ArrayList<T> elements, int n){
		Set<T> randomEls = trySelectingRandomElements(elements, n);
		if (randomEls.size() < n) {
			throw new IllegalArgumentException("Not enough elements to select uniq " + n +
					" random ones from " + elements.size());
		}
		return randomEls;
	}
	
	public static <T> Set<T> trySelectingRandomElements(ArrayList<T> elements, int n){
		Set<T> randomEls = new HashSet<T>(n);
		int size = elements.size();
		Random r = new Random();
		while (randomEls.size() < n && size > 0) {
			int rndIdx = r.nextInt(size);
			T randElement = elements.get(rndIdx);
			randomEls.add(randElement);
			size--;
			elements.set(rndIdx, elements.get(size));
			elements.set(size, randElement);
			// swapping with last element to avoid costly remove
		}
		return randomEls;
	}

}