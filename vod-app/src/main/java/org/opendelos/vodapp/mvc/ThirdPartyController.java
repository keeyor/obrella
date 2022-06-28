package org.opendelos.vodapp.mvc;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendelos.model.delos.OpUser;
import org.opendelos.model.security.TokenInfo;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.vodapp.security.token.TokenAuthService;
import org.opendelos.vodapp.services.opUser.OpUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles requests: Home Pages
 */ 
@Controller
public class ThirdPartyController {

	private final TokenAuthService tokenAuthService;
	private final OpUserService opUserService;

	private static final Logger logger = LoggerFactory.getLogger(ThirdPartyController.class);

	public ThirdPartyController(TokenAuthService tokenAuthService, OpUserService opUserService) {
		this.tokenAuthService = tokenAuthService;
		this.opUserService = opUserService;
	}


	@RequestMapping(value = "/rlogin", method = RequestMethod.GET)
	public String SignInr(HttpServletRequest request, Map<String, Object> model,
			 @RequestParam(value = "login_error", required = false, defaultValue="f") String login_error,
			 @RequestParam(value = "reset", required = false, defaultValue="n") String reset,
			 @RequestParam(value = "debug", required = false, defaultValue="0") String debug,
			 @RequestParam(value = "token") String token) throws IOException {

		TokenInfo tokenInfo = tokenAuthService.loadTokenDetails(token, "PUBLIC");

		if (tokenInfo != null) {

			request.getSession().setAttribute("token_details",tokenInfo);
			model.put("login_error", login_error);

			Object _temp_principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (_temp_principal instanceof OoUserDetails && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null)
 			{
 				String user_id = ((OoUserDetails) _temp_principal).getId();
 				OpUser user = opUserService.findById(user_id);
 				logger.info("Authenticated Remote User:" + user.getName());
 				user.setToken(token);
 				opUserService.update(user);
 			}
		}
		else {
			return "cauthAccessDenied";
		}

		return "rlogin";
	}

	@RequestMapping(value = "/admin/cauth/cas", method = RequestMethod.GET)
    public String cauth(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "token") String token) {
		
		String redirect_url;
		TokenInfo tokenInfo = tokenAuthService.loadTokenDetails(token, "PUBLIC");

		if (tokenInfo !=null) {
			  redirect_url = tokenInfo.getRedirect_url();

			  redirect_url = redirect_url.replace("&amp;", "&");
			  request.getSession().setAttribute("token_details",tokenInfo);

			  Object _temp_principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			  if (_temp_principal instanceof OoUserDetails && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null)
			  {
					String user_id = ((OoUserDetails) _temp_principal).getId();
					OpUser user = opUserService.findById(user_id);
					logger.info("(CAS) Authenticated Remote User:" + user.getName());
					user.setToken(token);
					opUserService.update(user);
			  }
	     }
		 else {
				return "cauthAccessDenied";
		 }
		logger.trace("CAS Success:Redirecting to:" + redirect_url);
		try {
			response.sendRedirect(redirect_url);
		} catch (IOException e) {
			logger.error("Error in Response Redirect:" + e.getMessage());
		}
		return null;
 	}

	@RequestMapping(value = "/cauth/cas", method = RequestMethod.GET)
	public String cauthtest(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "token") String token) {

		String redirect_url;
		TokenInfo tokenInfo = tokenAuthService.loadTokenDetails(token, "PUBLIC"); //_dlmService.loadTokenDetails(token, "PUBLIC");


		if (tokenInfo !=null && tokenInfo.getDomainName() != null) {
			String host = tokenInfo.getDomainName();
			redirect_url = tokenInfo.getRedirect_url();

			redirect_url = redirect_url.replace("&amp;", "&");
			logger.info("NEW Redirect_URL:" + redirect_url);
			request.getSession().setAttribute("token_details",tokenInfo);
			Object _temp_principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

			if (_temp_principal instanceof OpUser && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null)
			{
				OpUser user = (OpUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				String user_id = user.getId();
				logger.info("Authenticated ThirdParty User:" + user.getName());
				opUserService.update(user); ///_dlmService.putTokenForDlmUser(institution_id, user_id, host, token);
			}
		}
		else {
			return "cauthAccessDenied";
		}

		logger.trace("CAS Success:Redirecting to:" + redirect_url);
		try {
			response.sendRedirect(redirect_url);
		} catch (IOException e) {
			logger.error("Error in Response Redirect:" + e.getMessage());
		}
		return null;
	}
	@RequestMapping(value = "/rauth", method = RequestMethod.GET)
    public String rauth() {
		return "rauth";
 		
 	}

	@RequestMapping(value = "/restt", method = RequestMethod.GET)

    public String test() {
		return "restt";
 		
 	}
	@RequestMapping(value = "/retr", method = RequestMethod.GET)
    public String test1() {
		return "retr";
 		
 	}	

}
