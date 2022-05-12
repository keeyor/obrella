/* 
     Author: Michael Gatzonis - 10/26/2018 
     OpenDelosDAC
*/
package org.opendelos.control.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.opendelos.model.users.ActiveUserStore;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.model.users.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

@Configuration
public class OdSimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    ActiveUserStore activeUserStore;

    private final Logger log = LoggerFactory.getLogger(OdSimpleUrlAuthenticationSuccessHandler.class);

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {

        handle(httpServletRequest, httpServletResponse, authentication);
        clearAuthenticationAttributes(httpServletRequest);
    }
    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        //# Track user
        HttpSession session = request.getSession(false);
        if (session != null) {
            LoggedUser user = new LoggedUser(authentication.getName(), activeUserStore);
            session.setAttribute("user", user);
        }

        String targetUrl = "/admin";
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        String requested_url = savedRequest.getRedirectUrl();
        if (requested_url.contains("/cas/live_player")) {
            targetUrl = "/cas/live_player?" + StringUtils.substringAfter(requested_url,"?");
            log.info("redirect to:" + targetUrl);
        }
        OoUserDetails loggedIn_user = (OoUserDetails) authentication.getPrincipal();
        if (
                (!loggedIn_user.getUserAuthorities().contains(UserAccess.UserAuthority.MANAGER)) &&
                (!loggedIn_user.getUserAuthorities().contains(UserAccess.UserAuthority.STAFFMEMBER)) &&
                (!loggedIn_user.getUserAuthorities().contains(UserAccess.UserAuthority.SUPPORT))
            ) {
            targetUrl = "/403";
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
