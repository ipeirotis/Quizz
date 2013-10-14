package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizRepository;

import com.ipeirotis.crowdquiz.utils.Helper;

@SuppressWarnings("serial")
public class DeleteQuiz extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String baseURL = Helper.getBaseURL(req);
		String url = baseURL + "/admin/";
		resp.sendRedirect(url);
		Utils.ensureParameters(req, "relation");

		String relation = req.getParameter("relation");
		QuizRepository.deleteQuiz(relation);
	}

}
