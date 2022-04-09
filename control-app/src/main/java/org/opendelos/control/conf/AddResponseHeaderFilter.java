/* 
     Author: Michael Gatzonis - 15/12/2020 
     live
*/
package org.opendelos.control.conf;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

//@Configuration
@WebFilter("/filter-response-header/*")
public class AddResponseHeaderFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		String redirectURLCC = httpServletResponse.getHeader("Location");
		if (redirectURLCC != null) {
			redirectURLCC = redirectURLCC.replace("http://", "https://");
			httpServletResponse.setHeader("Location", redirectURLCC);

		}
		httpServletResponse.setHeader(
				"Opendelos-Filter-Header", "Value-Filter");
		chain.doFilter(request, response);
	}


}