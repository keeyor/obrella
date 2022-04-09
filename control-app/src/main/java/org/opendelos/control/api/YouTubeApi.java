/* 
     Author: Michael Gatzonis - 5/2/2022 
     obrella
*/
package org.opendelos.control.api;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.opendelos.control.services.youtube.EventsAuthorizationCode;
import org.opendelos.control.services.youtube.YouTubePublicationService;
import org.opendelos.control.conf.YouTubeProperties;
import org.opendelos.model.users.OoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YouTubeApi {

	@Value("${app.url}")
	String app_url;

	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

	private final Logger logger = LoggerFactory.getLogger(YouTubeApi.class);
	
	private final YouTubePublicationService youTubePublicationService;
	private final EventsAuthorizationCode eventsAuthorizationCode;
	private final YouTubeProperties youTubeProperties;

	@Autowired
	public YouTubeApi(YouTubePublicationService youTubePublicationService, EventsAuthorizationCode eventsAuthorizationCode, YouTubeProperties youTubeProperties) {
		this.youTubePublicationService = youTubePublicationService;
		this.eventsAuthorizationCode = eventsAuthorizationCode;
		this.youTubeProperties = youTubeProperties;
	}

	@RequestMapping(value= "/api/youtube/store", method= RequestMethod.POST, headers = {"Accept=text/html, text/xml, application/json"})
	public String getAuthCodePage(final Model model,@RequestBody String code) throws IOException, GeneralSecurityException {

		String msg;
		DataStoreFactory DATA_STORE_FACTORY=new FileDataStoreFactory(new File(youTubeProperties.getDatastore()));

		logger.info("CAllback to:" + app_url + youTubeProperties.getCallback());
		if (code != null) {
			final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			// Load client secrets.
			File file = ResourceUtils.getFile(youTubeProperties.getClientSecretsFile()); //
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(file.getAbsolutePath()));

			model.addAttribute("authcode", code);
			GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
					new NetHttpTransport(),
					new GsonFactory(),
					"https://oauth2.googleapis.com/token",
					clientSecrets.getDetails().getClientId(),
					clientSecrets.getDetails().getClientSecret(),
					code,
					"https://dimos.med.uoa.gr").execute();
			//app_url + youTubeProperties.getCallback()).execute();

			String accessToken = tokenResponse.getAccessToken();
			String refreshToken = tokenResponse.getRefreshToken();
			model.addAttribute("accessToken", accessToken);
			model.addAttribute("refreshToken", refreshToken);

			//store credentials
			Collection<String> SCOPES = youTubeProperties.getScopes();
			try {
				OoUserDetails ooUserDetails = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).build();			  flow.createAndStoreCredential(tokenResponse,ooUserDetails.getUid());
				logger.info("YouTube credentials for user {} stored!",ooUserDetails.getUid());
				msg = "YouTube credentials for user " + ooUserDetails.getUid() + " stored!!";
			}
			catch (IOException   e) {
				logger.error((" ERROR IN SCHEDULE SEQUENCE:" + e.getMessage()));
				msg = "Παρουσιάστηκε πρόβλημα: " + e.getMessage();
			}
		}
		else {
			logger.warn("AuthCode is null. Cannot Authenticate to YouTube");
			msg = "Παρουσιάστηκε πρόβλημα.. Ο κωδικός αυθεντικοποίησης είναι κενός!!!";
		}
		model.addAttribute("msg",msg);

		return msg;
	}

	@RequestMapping(value= "/api/youtube/setBroadcast", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> setYouTubeBroadcast(@RequestBody String scheduleId)  {

		String liveBroadcastId;
		Credential credential;
		try {
			credential = youTubePublicationService.getSingedInUserGoogleCredentials();
			if (credential == null) {
				logger.info("No previous credentials found. Generating new!!!");
				//Request an access token
				AuthorizationCodeFlow authorizationCodeFlow = eventsAuthorizationCode.initializeFlow();
				String redirect_url = app_url + youTubeProperties.getCallback(); //"/youtube/insert_stream";
				String url = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(redirect_url).build();
				//TODO: Redirect does not work on Server. Return the url and find another way to redirect user to google authentication page
				//response.sendRedirect(url);
				return new ResponseEntity<>(url, HttpStatus.ACCEPTED);
			}
			else {
				logger.info("Youtube: User's Credentials to YouTube Found!");
				liveBroadcastId = youTubePublicationService.ScheduleYoutubeLiveBroadcast(credential,scheduleId);
			}
			return new ResponseEntity<>(liveBroadcastId, HttpStatus.ACCEPTED);
		}
		catch (IOException | GeneralSecurityException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value= "/api/youtube/unsetBroadcast/{scheduleId}/{broadcastId}", method = RequestMethod.POST,produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> unsetYouTubeBroadcast(@PathVariable String scheduleId, @PathVariable String broadcastId,HttpServletResponse response) throws IOException, GeneralSecurityException {

		Credential credential;
		try {
			credential = youTubePublicationService.getSingedInUserGoogleCredentials();
			 if (credential == null) {
				logger.info("Youtube: No previous credentials found. Generating new!!!");
				//Request an access token
				AuthorizationCodeFlow authorizationCodeFlow = eventsAuthorizationCode.initializeFlow();
				String redirect_url = app_url + youTubeProperties.getCallback(); //"/youtube/insert_stream";
				String url = authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(redirect_url).build();
				response.sendRedirect(url);
			}
			else {
				youTubePublicationService.UnScheduleYoutubeLiveBroadcast(credential,broadcastId, scheduleId);
			}
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch (IOException | GeneralSecurityException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
