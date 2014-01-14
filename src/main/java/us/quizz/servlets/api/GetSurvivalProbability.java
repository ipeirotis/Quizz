package us.quizz.servlets.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.servlets.Utils;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class GetSurvivalProbability extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Utils.ensureParameters(req, "a_from", "a_to", "b_from", "b_to");
		String quizID = req.getParameter("quizID");
		Integer a_from = Integer.valueOf(req.getParameter("a_from"));
		Integer a_to = Integer.valueOf(req.getParameter("a_to"));
		Integer b_from = Integer.valueOf(req.getParameter("b_from"));
		Integer b_to = Integer.valueOf(req.getParameter("b_to"));
		
		long u_from = QuizPerformanceRepository
				.getNumberOfAnswers(quizID, a_from, b_from);
		long u_to = QuizPerformanceRepository
				.getNumberOfAnswers(quizID, a_to, b_to);
		double psurvival = (u_from == 0) ? u_from : u_to/u_from;
		
		resp.setContentType("application/json;charset=UTF-8");
		new Gson().toJson(new Response(u_from, u_to, psurvival), resp.getWriter());
	}
	
	class Response {
		long u_from;
		long u_to;
		double psurvival;
		
		public Response(long u_from, long u_to, double psurvival) {
			this.u_from = u_from;
			this.u_to = u_to;
			this.psurvival = psurvival;
		}
	}
	
}
