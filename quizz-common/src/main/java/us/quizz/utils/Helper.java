package us.quizz.utils;

import org.apache.commons.math3.special.Gamma;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

// TODO(chunhowt): Figure out the calculation details and write unit tests for this class.
public class Helper {
  public static String getBaseURL(HttpServletRequest req) {
    String baseURL = req.getScheme() + "://" + req.getServerName() + ":"
        + req.getServerPort();
    return baseURL;
  }

  // Computes the entropy of an answer given by a user with quality q (= probability of correct)
  // and n available options in a multiple choice question.
  // @throws Exception
  public static Double entropy(double q, int n) {
    // There is no uncertainty, so entropy is 0.
    if (q == 1.0 || n == 1) {
      return 0.0d;
    }
    if (q == 0.0) {
      return -Math.log(1.0 / (n - 1)) / Math.log(2);
    }
    if (n < 1) {
      return Double.NaN;
    }
    if (q < 0.0 || q > 1.0) {
      return Double.NaN;
    }
    double entropy =
        (1 - q) * Math.log((1 - q) / (n - 1)) / Math.log(2) +
        q * Math.log(q) / Math.log(2);
    return -entropy;
  }

  // Returns the information gain contributed by the user given its quality q and the number
  // of options, n, in a multiple choice question.
  // @throws Exception
  public static double getInformationGain(double q, int n) {
    return Helper.entropy(1.0 / n, n) - Helper.entropy(q, n);
  }

  // Computes a Bayesian version of Information Gain
  // @param a The number of correct answers (plus the prior)
  // @param b The number of incorrect answers (plus the prior)
  // @param n The number of multiple options in the quiz
  // @throws Exception
  public static double getBayesianMeanInformationGain(double a, double b, int n)
      throws Exception {
    // We have that limit(x * Psi(x), x = 0) = -1
    // Based on this, for the Bayesian Information Gain (BIG):
    // BIG(0,b,n) = lg(n) - lg(n-1)
    // BIG(a,0,n) = lg(n)
    // This means that BIG(0,0) does not have a value when the prior it
    // Beta(0,0)

    if (a < 0 || b < 0 || n <= 1) {
      throw new Exception("Unsupported parameters values");
    }
    if (a == 0 && b == 0 && n > 2) {
      throw new Exception("Undefined value");
    }
    if (a == 0 && b == 0 && n == 2) {
      return Math.log(2) / Math.log(2); // The only case where we have
                                        // convergence as a-->0, b-->0
    }
    if (a == 0 && b > 0) {
      return Math.log(n) / Math.log(2) - Math.log(n - 1) / Math.log(2);
    }
    if (a > 0 && b == 0) {
      return Math.log(n) / Math.log(2);
    }

    return Math.log(n) - (b / (a + b)) * Math.log(n - 1) - getBayesianEntropy(a, b);
  }

  // Computes the variance of the Bayesian version of Information Gain
  // @param a The number of correct answers (plus the prior)
  // @param b The number of incorrect answers (plus the prior)
  // @param n The number of multiple options in the quiz
  // @throws Exception
  public static double getBayesianVarianceInformationGain(double a, double b, int n)
      throws Exception {
    return getBayesianInformationGainSquare(a, b, n)
        - Math.pow(getBayesianMeanInformationGain(a, b, n), 2);
  }

  // Computes the Bayesian version of Entropy
  // @param a The number of correct answers (plus the prior)
  // @param b The number of incorrect answers (plus the prior)
  // @throws Exception
  public static double getBayesianEntropy(double a, double b) throws Exception {
    double nominator = (a + b) * Gamma.digamma(a + b + 1) - a
        * Gamma.digamma(a + 1) - b * Gamma.digamma(b + 1);
    double denominator = (a + b);

    return nominator / denominator;
  }

  // Computes the square of the Bayesian version of Entropy.
  // @param a The number of correct answers (plus the prior)
  // @param b The number of incorrect answers (plus the prior)
  // @throws Exception
  public static double getBayesianEntropySquare(double a, double b) throws Exception {
    double coef_ab = 2 * (a * b) / ((a + b) * (a + b + 1));
    double coef_a = a * (a + 1) / ((a + b + 1) * (a + b));
    double coef_b = b * (b + 1) / ((a + b + 1) * (a + b));

    double Ja = Math.pow(Gamma.digamma(a + 2) - Gamma.digamma(a + b + 2), 2)
        + Gamma.trigamma(a + 2) - Gamma.trigamma(a + b + 2);
    double Jb = Math.pow(Gamma.digamma(b + 2) - Gamma.digamma(a + b + 2), 2)
        + Gamma.trigamma(b + 2) - Gamma.trigamma(a + b + 2);
    double Iab = (Gamma.digamma(a + 1) - Gamma.digamma(a + b + 2))
        * (Gamma.digamma(b + 1) - Gamma.digamma(a + b + 2))
        - Gamma.trigamma(a + b + 2);

    return coef_a * Ja + coef_b * Jb + coef_ab * Iab;
  }

  // Computes the square of the Bayesian version of Information Gain.
  // @param a The number of correct answers (plus the prior)
  // @param b The number of incorrect answers (plus the prior)
  // @param n The number of multiple options in the quiz
  // @throws Exception
  public static double getBayesianInformationGainSquare(
      double a, double b, int n) throws Exception {
    double A2 = Math.log(n) * Math.log(n);
    double B2 = getBayesianEntropySquare(a, b);
    double C2 = b * (b + 1) * Math.log(n - 1) * Math.log(n - 1)
        / ((a + b) * (a + b + 1));
    double AB = -2 * Math.log(n) * getBayesianEntropy(a, b);
    double AC = -2 * Math.log(n) * Math.log(n - 1) * b / (a + b);
    double BC_1 = (2 * Math.log(n - 1) * b) / ((a + b) * (a + b + 1));
    double BC_2 = (a + b + 1) * Gamma.digamma(a + b + 1) - (b + 1)
        * Gamma.digamma(b + 1) - a * Gamma.digamma(a + 1);

    return A2 + B2 + C2 + AB + AC + BC_1 * BC_2;
  }
}
