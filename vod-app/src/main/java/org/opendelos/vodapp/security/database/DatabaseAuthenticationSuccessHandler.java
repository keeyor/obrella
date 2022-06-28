/* 
     Author: Michael Gatzonis - 10/26/2018 
     OpenDelosDAC
*/
package org.opendelos.vodapp.security.database;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opendelos.model.delos.OpUser;
import org.opendelos.model.security.TokenInfo;
import org.opendelos.model.users.ActiveUserStore;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.vodapp.services.opUser.OpUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class DatabaseAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private final OpUserService opUserService;

    private final Logger log = LoggerFactory.getLogger(DatabaseAuthenticationSuccessHandler.class);

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public DatabaseAuthenticationSuccessHandler(OpUserService opUserService) {
        this.opUserService = opUserService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {

        handle(httpServletRequest, httpServletResponse, authentication);
        clearAuthenticationAttributes(httpServletRequest);
    }
    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        String targetUrl = "/admin";

        TokenInfo tokenInfo = (TokenInfo) request.getSession().getAttribute("token_details");
        String ref = request.getHeader(HttpHeaders.REFERER);

        OoUserDetails loggedIn_user = (OoUserDetails) authentication.getPrincipal();

        if ( ref.contains("rlogin") && tokenInfo != null) {

            //Get logged-on User details

            String user_id 		    = loggedIn_user.getId();
            String host				= tokenInfo.getDomainName();
            String token			= tokenInfo.getToken();

            OpUser user = opUserService.findById(user_id);
            user.setToken(token);
            opUserService.update(user);
            //_dlmService.putTokenForDlmUser(institution_id, user_id, host, token);
            targetUrl = tokenInfo.getRedirect_url();
            targetUrl = targetUrl.replace("&amp;","&");

        }
        else {

            if (loggedIn_user.getDepartmentId().equalsIgnoreCase("ASK")) {
                targetUrl = "/admin/user_profile";
            }
            if (response.isCommitted()) {
                log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
                return;
            }
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

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }
    protected RedirectStrategy getRedirectStrategy() {
        return redirectStrategy;
    }
}
