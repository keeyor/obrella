package org.opendelos.vodapp.security.token;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import org.opendelos.model.security.TokenInfo;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;

public class JWTAuthenticationToken extends AbstractAuthenticationToken{


   private static final long serialVersionUID = 1L;
   private final Object principal;

   //Collection  authorities;
   public JWTAuthenticationToken(TokenInfo tokenInfo) {
	   super(null);
	   super.setAuthenticated(true); // must use super, as we override

	  try {

			   String secret =  tokenInfo.getSharedSecret();
			   String token  =  tokenInfo.getToken();
			   Map<String,Object> decodedPayload =  new JWTVerifier(secret).verify(token);

			   String token_domain_name = decodedPayload.get("url").toString();
			   String ref_domain_name = tokenInfo.getDomainName();
			   if (!ref_domain_name.equals(token_domain_name)) {
				   throw new AuthenticationServiceException("Invalid HOST");
			   }

		   } catch (SignatureException signatureException) {
			   throw new AuthenticationServiceException("Invalid Signature");
		   } catch (IllegalStateException illegalStateException) {
			   throw new AuthenticationServiceException("Invalid Token");
		   } catch (InvalidKeyException e) {
			   throw new AuthenticationServiceException("Invalid Key");
		   } catch (NoSuchAlgorithmException e) {
			   throw new AuthenticationServiceException("No such Algorithm");
		   } catch (IOException e) {
			   throw new AuthenticationServiceException("IO Exception");
		   } catch (JWTVerifyException e) {
			   throw new AuthenticationServiceException("JWT Exception");
		   }

	   this.principal = tokenInfo;

   }


   @Override
   public void setDetails(Object details) {
	   // TODO Auto-generated method stub
	   super.setDetails(details);
   }


   @Override
   public Object getCredentials() {
	   return "";
   }

   @Override
   public Object getPrincipal() {
	   return principal;
   }


}