/* 
     Author: Michael Gatzonis - 15/12/2020 
     live
*/
package org.opendelos.control.conf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

//@Component
public class HttpsConfig implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// String requestedPort = request.getServerPort() if you're not behind a proxy
		String requestedPort = request.getHeader("X-Forwarded-Port"); // I'm behind a proxy on Heroku

		if (requestedPort != null && requestedPort.equals("8081")) { // This will still allow requests on :8080
			response.sendRedirect("https://" + request.getServerName() + request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
			return false;
		}
		return true;
	}

 	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

		String requestedPort = request.getHeader("X-Forwarded-Port"); // I'm behind a proxy on Heroku
		if (requestedPort != null && requestedPort.equals("8081")) { // This will still allow requests on :8080
			response.sendRedirect("https://" + request.getServerName() + request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));

		}

	}

}
