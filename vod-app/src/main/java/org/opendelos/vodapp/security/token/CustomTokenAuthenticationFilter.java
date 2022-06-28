package org.opendelos.vodapp.security.token;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.security.TokenInfo;
import org.opendelos.vodapp.conf.LmsProperties;
import org.opendelos.vodapp.services.opUser.OpUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;


@Component
public class CustomTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private TokenAuthService tokenAuthService;
	private OpUserService opUserService;

	public final String HEADER_SECURITY_TOKEN = "X-CustomToken";

	@Autowired
	public CustomTokenAuthenticationFilter(String defaultFilterProcessesUrl, TokenAuthService tokenAuthService, OpUserService opUserService) {
        super(defaultFilterProcessesUrl);
		super.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(defaultFilterProcessesUrl));
        setAuthenticationManager(new NoOpAuthenticationManager());
        setAuthenticationSuccessHandler(new TokenSimpleUrlAuthenticationSuccessHandler());
		this.tokenAuthService = tokenAuthService;
		this.opUserService = opUserService;
	}


	/**
     * Attempt to authenticate request - basically just pass over to another method to authenticate request headers 
     */
    @Override public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        String token = request.getHeader(HEADER_SECURITY_TOKEN);

		String _access = "PUBLIC";

		String servlet_path = request.getServletPath();
		if (servlet_path.contains("/private/lms") || servlet_path.contains("private/check_auth") || servlet_path.contains("token_authentication")) {
			_access = "PRIVATE";
		}
        AbstractAuthenticationToken userAuthenticationToken = authUserByToken(token, _access);
        if(userAuthenticationToken == null) throw new AuthenticationServiceException(MessageFormat.format("Error | {0}", "Bad Token"));
        return userAuthenticationToken;
    }

    /**
     * authenticate request by token
     */
    private AbstractAuthenticationToken authUserByToken(String token, String access) {
        
    	if (token==null) {
            return null;
        }
    	TokenInfo tokenInfo = tokenAuthService.loadTokenDetails(token,access);
    	if (tokenInfo == null) {
    		logger.warn("NOT AUTHORIZED: BAD_TOKEN");
    		throw new AuthenticationServiceException("Not Authorized"); 
    	}
    	else  {
    		if (tokenInfo.getAccess().equalsIgnoreCase(("PRIVATE"))) {
				if (tokenInfo.getUserId() != null) {
					OpUser user = opUserService.findByToken(token);
					if (user != null) {
						logger.info("AUTHORIZED PRIVATE ACCESS FOR USER: " + user.getName() + " REF: " + tokenInfo.getDomainName());
						return new JWTAuthenticationToken(tokenInfo);
					}
					else {
						logger.warn("NOT AUTHORIZED: USER_NOT_FOUND");
						throw new AuthenticationServiceException("Not Authorized");
					}
				}
				else {
					logger.warn("NOT AUTHORIZED: USER_NOT_SET");
					throw new AuthenticationServiceException("Not Authorized");
				}
			}
			else {
				logger.info("AUTHORIZED PUBLIC ACCESS FOR COURSE: " + tokenInfo.getRId() + " REF: " + tokenInfo.getDomainName());
				return new JWTAuthenticationToken(tokenInfo);
			}
		}
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        super.doFilter(req, res, chain);
    }

}

