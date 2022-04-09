/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.liveapp.mvc;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;

@Controller
public class AdminController {

	@GetMapping("logout")
	public String logout(HttpServletRequest request,HttpServletResponse response, SecurityContextLogoutHandler logoutHandler) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CookieClearingLogoutHandler cookieClearingLogoutHandler = new CookieClearingLogoutHandler(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
		cookieClearingLogoutHandler.logout(request, response, auth);
		logoutHandler.logout(request, response, auth);

		return "redirect:/";
	}


	@RequestMapping(value = "cas/secure")
	public String cashomeredirect() {

		return "redirect:admin";
	}
	@RequestMapping(value = {"/cas/login"})
	public String casLoginIndex(final Model model,Locale locale) {

		model.addAttribute("localeData", locale.getDisplayName());
		return"redirect:admin";
	}

}
