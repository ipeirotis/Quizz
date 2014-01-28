package us.quizz.servlets.api;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Experiment;
import us.quizz.entities.User;
import us.quizz.utils.PMF;
import us.quizz.utils.ServletUtils;

/**
 * 
 * Takes as input a userid and a quiz, updates the user scores for the quiz, and
 * then computes the rank of the user within the set of all other users.
 * Finally, it puts the QuizPerformance object in the memcache for quick
 * retrieval.
 * 
 * @author ipeirotis
 * 
 */
@SuppressWarnings("serial")
public class UpdateUserExperiment extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");

		ServletUtils.ensureParameters(req, "userid");
		String userid = req.getParameter("userid");

		PersistenceManager pm = PMF.getPM();
		User user = null;
		try {
			user = pm.getObjectById(User.class, User.generateKeyFromID(userid));
		} catch (Exception e) {
			user = new User(userid);
		} finally {
			Experiment exp = new Experiment();
			exp.assignTreatments();
			user.setExperiment(exp);
			pm.makePersistent(user);
			pm.close();
		}
	}

}
