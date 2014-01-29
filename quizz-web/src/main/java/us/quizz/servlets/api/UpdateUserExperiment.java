package us.quizz.servlets.api;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Experiment;
import us.quizz.entities.User;
import us.quizz.repository.UserRepository;
import us.quizz.utils.ServletUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
@Singleton
public class UpdateUserExperiment extends HttpServlet {
	
	private UserRepository userRepository;
	
	@Inject
	public UpdateUserExperiment(UserRepository userRepository){
		this.userRepository = userRepository;
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");

		ServletUtils.ensureParameters(req, "userid");
		String userid = req.getParameter("userid");

		User user = null;
		try {
			user = userRepository.singleGetObjectByIdThrowing(User.class, User.generateKeyFromID(userid));
		} catch (Exception e) {
			user = new User(userid);
		} finally {
			Experiment exp = new Experiment();
			exp.assignTreatments();
			user.setExperiment(exp);
			userRepository.save(user);
		}
	}

}
