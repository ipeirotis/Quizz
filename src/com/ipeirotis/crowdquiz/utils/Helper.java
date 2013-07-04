package com.ipeirotis.crowdquiz.utils;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Helper {

	public static String getUseridFromCookie(HttpServletRequest req, HttpServletResponse resp) {

		// Get an array of Cookies associated with this domain

		String userid = null;
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals("username")) {
					userid = c.getValue();
					break;
				}
			}
		}

		if (userid == null) {
			userid = UUID.randomUUID().toString();
			;
		}

		Cookie username = new Cookie("username", userid);
		resp.addCookie(username);
		return userid;
	}

	
}
