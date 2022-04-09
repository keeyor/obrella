/* 
     Author: Michael Gatzonis - 4/2/2022 
     obrella
*/
package org.opendelos.control.services.youtube;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.opendelos.control.conf.YouTubeProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
public class EventsAuthorizationCode extends AbstractAuthorizationCodeServlet {

	private final YouTubeProperties youTubeProperties;

	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

	@Autowired
	public EventsAuthorizationCode(YouTubeProperties youTubeProperties) {
		this.youTubeProperties = youTubeProperties;
	}

/*	NOTE:: MOVED TO YOUTUBE PROPERTIES
	private static final Collection<String> SCOPES =
			 Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl","https://www.googleapis.com/auth/youtubepartner","https://www.googleapis.com/auth/youtube.upload");
*/



	@Override
	protected String getRedirectUri(HttpServletRequest httpServletRequest) throws ServletException, IOException {
		GenericUrl url = new GenericUrl(httpServletRequest.getRequestURL().toString());
		url.setRawPath(youTubeProperties.getCallback());			// .setRawPath("/youtube/insert_stream");
		return url.build();
	}

	@Override
	public AuthorizationCodeFlow initializeFlow() throws IOException {

		Collection<String> SCOPES = youTubeProperties.getScopes();

		// Load client secrets.
		File file = ResourceUtils.getFile(youTubeProperties.getClientSecretsFile());
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(file.getAbsolutePath()));

		return new GoogleAuthorizationCodeFlow.Builder(
				new NetHttpTransport(), GsonFactory.getDefaultInstance(),
				clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret(),
				SCOPES).setAccessType("offline").build();

/*				new NetHttpTransport(), GsonFactory.getDefaultInstance(),
				"765663939465-da4te8qs5dnqbg5n314gcdmbhfaqkopr.apps.googleusercontent.com", "a9mxhxrKK8ysihUUHGiLngqt",
				SCOPES).setAccessType("offline").build();*/
	}

	@Override
	protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
		return "sa";
	}

}
