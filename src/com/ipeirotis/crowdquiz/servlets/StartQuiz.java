package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.UserReferralRepository;

import com.ipeirotis.crowdquiz.entities.User;
import com.ipeirotis.crowdquiz.utils.Helper;

@SuppressWarnings("serial")
public class StartQuiz extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String relation = req.getParameter("relation");
		User user = User.getUseridFromCookie(req, resp);
		String userid = user.getUserid();
		UserReferralRepository.createAndStoreUserReferal(req, userid);

		String nextURL = Helper.getNextMultipleChoiceURL(req, relation, userid, null);

		resp.sendRedirect(nextURL);
	}



}
