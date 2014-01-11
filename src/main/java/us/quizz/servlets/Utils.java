package us.quizz.servlets;

import javax.servlet.http.HttpServletRequest;

public class Utils {

	public static void ensureParameters(HttpServletRequest request,
			String... params) {
		for (String param : params) {
			if (request.getParameter(param) == null) {
				throw new IllegalArgumentException("Missing parameter: "
						+ param);
			}
		}
	}
}
