package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizRepository;

import com.ipeirotis.crowdquiz.utils.Helper;

@SuppressWarnings("serial")
public class DeleteQuiz extends HttpServlet {

	final static Logger logger = Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String baseURL = Helper.getBaseURL(req);
		String url = baseURL + "/admin/";
		resp.sendRedirect(url);

		try {
			String relation = req.getParameter("relation");
			if (relation == null) {
				return;
			}

			QuizRepository.deleteQuiz(relation);

		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"Reached execution time limit. Press refresh to continue.",
					e);
		}
	}

}
