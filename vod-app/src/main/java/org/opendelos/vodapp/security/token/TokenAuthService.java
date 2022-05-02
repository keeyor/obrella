/* 
     Author: Michael Gatzonis - 27/4/2022 
     obrella
*/
package org.opendelos.vodapp.security.token;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.security.TokenInfo;
import org.opendelos.vodapp.conf.LmsProperties;
import org.opendelos.vodapp.services.opUser.OpUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenAuthService {

	private static final Log logger = LogFactory.getLog(TokenAuthService.class);

	private final LmsProperties lmsProperties;
	private final OpUserService opUserService;

	@Autowired
	public TokenAuthService(LmsProperties lmsProperties, OpUserService opUserService) {
		this.lmsProperties = lmsProperties;
		this.opUserService = opUserService;
	}

	public TokenInfo loadTokenDetails(String token, String access) {

		TokenInfo tokenInfo = null;

		List<String> lms_urls = lmsProperties.getUrl();
		List<String> lms_names = lmsProperties.getName();
		List<String> lms_secrets = lmsProperties.getSecret();

		for (int i=0; i < lms_urls.size(); i++) {

			if (lms_urls.get(i) != null && lms_secrets.get(i) != null && lms_names.get(i) != null) {			//just-in-case

				String lms_url = lms_urls.get(i);
				String lms_secret = lms_secrets.get(i);

				try {
					Map<String,Object> decodedPayload =  new JWTVerifier(lms_secret).verify(token);
					String tokenDomainName = decodedPayload.get("url").toString();

					//#Mandatory if access == PRIVATE
					String tokenUserId = null;
					if (access.equals("PRIVATE")) {
						String tokenSsoUserUId = decodedPayload.get("user_id").toString();
						OpUser tokenUser = opUserService.findByUid(tokenSsoUserUId);
						tokenUserId = tokenUser.getId();
					}
					//# Not sure if it is used!!!
					String tokenRedirectUrl = null;
					try {
						tokenRedirectUrl = decodedPayload.get("redirect_url").toString();
					}
					catch (Exception ignored) {}

					//# Not sure if it is used!!!
					String tokenResourceRid = null;
					try {
						tokenResourceRid = decodedPayload.get("rid").toString();
					}
					catch (Exception ignored) {}

					if (lms_url.equals(tokenDomainName)) {
						tokenInfo = new TokenInfo();
						tokenInfo.setDomainName(tokenDomainName);
						tokenInfo.setRId(tokenResourceRid);
						tokenInfo.setRedirect_url(tokenRedirectUrl);
						tokenInfo.setSharedSecret(lms_secret);
						tokenInfo.setAccess(access);
						tokenInfo.setUserId(tokenUserId); // is null if access == 'PUBLIC'
						tokenInfo.setToken(token);
						break;
					}
				}
				catch (NoSuchAlgorithmException | InvalidKeyException | IOException | SignatureException | JWTVerifyException e) {
					logger.error("TokenAuthService: Error decoding token:" + e.getMessage());
				}
			}
		}
		if (tokenInfo == null) {
			logger.warn("TokenAuthService: No Matched LMS found for provided token");
		}

		return tokenInfo;

	}
}
