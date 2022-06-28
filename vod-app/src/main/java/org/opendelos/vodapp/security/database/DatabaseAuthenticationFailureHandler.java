/* 
     Author: Michael Gatzonis - 10/26/2018 
     NDSS
*/
package org.opendelos.vodapp.security.database;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opendelos.model.security.TokenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
public class DatabaseAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final Logger log = LoggerFactory.getLogger(DatabaseAuthenticationFailureHandler.class);
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException exception) throws IOException, ServletException {

        handle(httpServletRequest, httpServletResponse, exception);
        clearAuthenticationAttributes(httpServletRequest);
    }
    protected void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        TokenInfo tokenInfo = (TokenInfo) request.getSession().getAttribute("token_details");
        String targetUrl;
        if ( tokenInfo != null) {
            targetUrl = "/rlogin?login_error=t&token=" + tokenInfo.getToken();
        }
        else {
            targetUrl = "/rlogin?login_error=t";
        }
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }



}
