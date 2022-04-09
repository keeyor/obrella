package org.opendelos.control.services.wowza;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opendelos.control.conf.YouTubeProperties;
import org.opendelos.model.scheduler.wowza.HttpComponentsClientHttpRequestFactoryDigestAuth;
import org.opendelos.model.scheduler.wowza.config.StreamFileAppConfig;
import org.opendelos.model.scheduler.wowza.config.StreamRecorderConfig;
import org.opendelos.model.scheduler.wowza.config.StreamTargetConfig;
import org.opendelos.model.scheduler.wowza.responses.IncomingStreamConfig;
import org.opendelos.model.structure.StreamingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service("WowzaRestService")
public class WowzaRestService {

	private static final Logger logger = LoggerFactory.getLogger("WowzaRestService");

	private final YouTubeProperties youTubeProperties;

	@Autowired
	public WowzaRestService(YouTubeProperties youTubeProperties) {
		this.youTubeProperties = youTubeProperties;
	}


	public String getStatisticsForStream(String hostname, int port, String username, String password, String appName, String streamName)  {

		String status;

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);
		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

					/*
					curl -X GET \
					-H 'Accept:application/json; charset=utf-8' \
					-H 'Content-Type:application/json; charset=utf-8' \
					http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/live/instances/_definst_/incomingstreams/myStream/monitoring/current
					 */

		String postUrl = "http://"  + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName
									+ "/instances/_definst_/incomingstreams/"
									+ streamName +	"/monitoring/current";

		logger.trace("getStatisticsForStream URL:" + postUrl);
		ResponseEntity<String> response  = restTemplate.exchange(postUrl, HttpMethod.GET, null, String.class);
		status = response.getBody();

		return  status;
	}

	public ResponseEntity<String> GetListOfStreamFiles(String hostname, int port, String username, String password, String appName) {

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);
		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		 /*
 			curl -X GET \
			-H "Accept:application/json; charset=utf-8" \
			-H "Content-Type:application/json; charset=utf-8" \
			http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/live/streamfiles
		 */

		String postUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName + "/streamfiles";

		ResponseEntity<String> response  = restTemplate.exchange(postUrl, HttpMethod.GET, null, String.class);

		logger.trace("GetListOfStreamFiles:" + response);
		return  response;
	}

	public ResponseEntity<String> ViewDetailsOfStreamFile(String hostname, int port, String username, String password, String appName, String streamName) {

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		 /*
 			curl -X GET \
			-H 'Accept:application/json; charset=utf-8' \
			-H 'Content-type:application/json; charset=utf-8' \
			http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/live/streamfiles/metallica
		 */

		String postUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" 
								   + appName + "/streamfiles/" + streamName;
		logger.trace("ViewDetailsOfStreamFile URL:" + postUrl);

		return restTemplate.exchange(postUrl, HttpMethod.GET, null, String.class);
	}

	public ResponseEntity<String> AddStreamFile(String hostname, int port, String username, String password, String appName,
			StreamFileAppConfig streamFileAppConfig) {

		   HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

			// Put the factory in the template
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(requestFactory);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String json2 = gson.toJson(streamFileAppConfig);

			/*
				curl -X POST \
				-H 'Accept:application/json; charset=utf-8' \
				-H 'Content-Type:application/json; charset=utf-8'  \
				http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/live/streamfiles \
				-d'
				{
					"name": "creedence",
						"serverName": "_defaultServer_",
						"uri": "udp://1.2.3.4:10000"
				}'
		    */

			String postUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName + "/streamfiles";
			logger.trace("AddStreamFile URL:" + postUrl);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<>(json2, headers);

			return restTemplate.exchange(postUrl,  HttpMethod.POST, request, String.class);
	}

	public ResponseEntity<String> ConnectStreamFile(String hostname, int port, String username, String password, String appName, String streamName) {

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		// API Call: PUT
		/*
			curl -X PUT \
			-H 'Accept:application/json; charset=utf-8' \
			-H 'Content-type:application/json; charset=utf-8' \
			"http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/live/streamfiles/creedence/actions/connect?connectAppName=live&appInstance				=_definst_&mediaCasterType=rtp"
		 */
		// !Important: NO .stream at the streamName....
		String postUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName + "/streamfiles/" + streamName
				+ "/actions/connect?connectAppName=" + appName + "&appInstance=_definst_&mediaCasterType=rtp";

		logger.trace("ConnectStreamFile URL:" + postUrl);
		return restTemplate.exchange(postUrl, HttpMethod.PUT, null, String.class);
	}

	public ResponseEntity<String> DisconnectStreamFile(String hostname, int port, String username, String password, String appName, String streamName) {

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		/*
		curl -X PUT \
		-H 'Accept:application/json; charset=utf-8' \
		-H 'Content-Type:application/json; charset=utf-8' \
		http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/live/instances/_definst_/incomingstreams/creedence.stream/actions/disconnectStream
		 */
		if (streamName.endsWith(".stream")) {
			streamName = streamName.substring(0, streamName.lastIndexOf(".stream"));
		}
		String postUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/"
							+ appName + "/instances/_definst_/incomingstreams/" + streamName + ".stream" + "/actions/disconnectStream";
		logger.trace("DisconnectStreamFile URL:" + postUrl);

		return restTemplate.exchange(postUrl, HttpMethod.PUT, null, String.class);
	}

	public ResponseEntity<String> RemoveStreamFile(String hostname, int port, String username, String password, String appName, String streamName) {

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		/*
		curl -X DELETE \
		-H 'Accept:application/json; charset=utf-8' \
		http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/live/streamfiles/creedence
		 */

		if (streamName.endsWith(".stream")) {
			streamName = streamName.substring(0, streamName.lastIndexOf(".stream"));
		}
		String postUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName + "/streamfiles/" + streamName;
		logger.trace("RemoveStreamFile URL:" + postUrl);

		return restTemplate.exchange(postUrl, HttpMethod.DELETE, null, String.class);
	}

	public ResponseEntity<String>  CreateRecorderForStream(String hostname, int port, String username, String password, String appName,
			String streamName, StreamRecorderConfig streamRecorderConfig) {

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json2 = gson.toJson(streamRecorderConfig);

		logger.trace("Recorder params:" + json2);

		/*
		curl -X POST \
		-H 'Accept:application/json; charset=utf-8' \
		-H 'Content-Type:application/json; charset=utf-8' \
		http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/live/instances/_definst_/streamrecorders/myStream \
		-d '
		{
		  "instanceName": "",
		  "fileVersionDelegateName": "",
		  "serverName": "",
		  "recorderName": "myStream",
		  "currentSize": 0,
		  "segmentSchedule": "",
		  "startOnKeyFrame": true,
		  "outputPath": "",
		  "currentFile": "",
		  "saveFieldList": [
			""
		  ],
		  "recordData": false,
		  "applicationName": "",
		  "moveFirstVideoFrameToZero": false,
		  "recorderErrorString": "",
		  "segmentSize": 0,
		  "defaultRecorder": false,
		  "splitOnTcDiscontinuity": false,
		  "version": "",
		  "baseFile": "",
		  "segmentDuration": 0,
		  "recordingStartTime": "",
		  "fileTemplate": "",
		  "backBufferTime": 0,
		  "segmentationType": "",
		  "currentDuration": 0,
		  "fileFormat": "",
		  "recorderState": "",
		  "option": ""
		}'
		 */
		String postUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/"
								   + appName + "/instances/_definst_/streamrecorders/" + streamName;

		logger.trace("CreateRecorderForStream URL:" + postUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(json2, headers);

		return restTemplate.exchange(postUrl,  HttpMethod.POST, request, String.class);
	}

	public ResponseEntity<String> StopRecorderForStream(String hostname, int port, String username, String password, String appName, String streamName) {

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		/*
		curl -X PUT \
		-H 'Accept:application/json; charset=utf-8' \
		-H 'Content-Type:application/json; charset=utf-8' \
		http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/live/instances/_definst_/streamrecorders/myStream/actions/stopRecording
		 */
		if (streamName.endsWith(".stream")) {
			streamName = streamName.substring(0, streamName.lastIndexOf(".stream"));
		}
		String postUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/"
				 					+ appName + "/instances/_definst_/streamrecorders/" + streamName + ".stream"
									+ "/actions/stopRecording";

		logger.trace("StopRecorderForStream URL:" + postUrl);

		return restTemplate.exchange(postUrl, HttpMethod.PUT, null, String.class);
	}

	public ResponseEntity<String> GetApplicationStatistics(String hostname, int port, String username, String password, String appName) {
		//URL
		//http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/appName/monitoring/current

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		String getUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName + "/monitoring/current";
		logger.trace("GetApplicationStatistics URL:" + getUrl);

		ResponseEntity<String> wowza_response;
		try {
			wowza_response = restTemplate.exchange(getUrl, HttpMethod.GET, null, String.class);
		}
		catch (RestClientException rce) {
			wowza_response = new ResponseEntity<>("-", HttpStatus.FAILED_DEPENDENCY);
		}
		return wowza_response;
	}

	public IncomingStreamConfig getIncomingStreamInformation(StreamingServer streamingServer, String streamName) {

		String hostname = streamingServer.getServer();
		int restPort 	= Integer.parseInt(streamingServer.getRestPort());
		String username = streamingServer.getAdminUser();
		String password = streamingServer.getAdminPassword();
		String appName  = streamingServer.getApplication();
		//i.e.
		//http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/openDelosLive/instances/_definst_/incomingstreams/foscam
		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, restPort, username, password);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		String getUrl = "http://"  + hostname + ":" + restPort + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName
				+ "/instances/_definst_/incomingstreams/"  + streamName;

		logger.trace("getInformationForStream URL:" + getUrl);
		IncomingStreamConfig incomingStreamConfig = null;
		try {
			ResponseEntity<String> response = restTemplate.exchange(getUrl, HttpMethod.GET, null, String.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				incomingStreamConfig = mapper.readValue(response.getBody(), IncomingStreamConfig.class);
			}
		}
		catch (RestClientException rce) {
			logger.trace("Rest Client Exception reading Stream Information from Wowza: " + streamName);
		}
		catch (JsonProcessingException e) {
			logger.trace("Bad response reading Stream Information from Wowza: " + streamName);
		}
		return incomingStreamConfig;
	}

	private HttpComponentsClientHttpRequestFactory getRequestFactory(String hostname, int port, String username, String password) {

		// Build a client with a credentials provider
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new  UsernamePasswordCredentials(username,password);
		provider.setCredentials(AuthScope.ANY,credentials);

		// Create request factory with our super power client
		final HttpHost httpHost = new HttpHost(hostname, port, "http");
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).useSystemProperties().build();

		return new HttpComponentsClientHttpRequestFactoryDigestAuth(httpHost, client);
	}

 	public ResponseEntity<String> CreateStreamTarget(String hostname, int port, String username, String password, String appName, StreamTargetConfig streamTargetConfig)
	throws RestClientException {

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json2 = gson.toJson(streamTargetConfig);

		logger.trace("Target params:" + json2);
/*
				curl -X POST \
			-H 'Accept:application/json; charset=utf-8' \
			-H 'Content-type:application/json; charset=utf-8' \
			http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/testlive/pushpublish/mapentries/ppsource \
			-d'
			{
			   "serverName": "_defaultServer_",
			   "sourceStreamName": "myStream",
			   "entryName": "ppsource",
			   "profile": "rtmp",
			   "host": "localhost",
			   "application": "testlive",
			   "userName": "testUser",
			   "password": "pass",
			   "streamName": "myStream"
			}'
*/

		String postUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/"
				+ appName + "/pushpublish/mapentries/" + streamTargetConfig.getEntryName();

		logger.trace("CreateRecorderForStream URL:" + postUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(json2, headers);

		return restTemplate.exchange(postUrl,  HttpMethod.POST, request, String.class);
	}

	public void createStreamTargetInWowza(String streamName, String youtube_streamName) throws RestClientException {

		// Οι αίθουσες που μπορούν να κάνουν stream στο YoutubeController ίσως πρέπει να είναι επιλεγμένες και συγκεκριμένες...
		StreamTargetConfig streamTargetConfig = new StreamTargetConfig();
		streamTargetConfig.setServerName("_defaultServer_");
		streamTargetConfig.setSourceStreamName(streamName + ".stream");
		streamTargetConfig.setEntryName(streamName + youTubeProperties.getAppend2Target());
		streamTargetConfig.setProfile(youTubeProperties.getProfile());
		streamTargetConfig.setHost(youTubeProperties.getHost());
		streamTargetConfig.setApplication(youTubeProperties.getApplication());
		streamTargetConfig.setUserName(youTubeProperties.getUsername());
		streamTargetConfig.setPassword(youTubeProperties.getPassword());
		streamTargetConfig.setStreamName(youtube_streamName);

		String host = youTubeProperties.getWowzaHost();
		int port = youTubeProperties.getWowzaRestPort();
		String user = youTubeProperties.getWowzaUser();
		String password = youTubeProperties.getWowzaPassword();
		String appName = youTubeProperties.getApplication();

		ResponseEntity<String> wowza_response =  this.CreateStreamTarget(host, port, user, password, appName, streamTargetConfig);
		logger.info("createStreamTargetInWowza Response {} ", wowza_response.toString());
	}

	public void deleteStreamTargetFromWowza(String wowza_target_name) {

		String host = youTubeProperties.getWowzaHost();
		int port = youTubeProperties.getWowzaRestPort();
		String user = youTubeProperties.getWowzaUser();
		String password = youTubeProperties.getWowzaPassword();
		String appName = youTubeProperties.getApplication();

		try {
			ResponseEntity<String> wowza_response = this.RemoveStreamTarget(host, port, user, password, appName, wowza_target_name);
			System.out.println(wowza_response.toString());
		}
		catch (Exception ignored) {}
	}

	public ResponseEntity<String> RemoveStreamTarget(String hostname, int port, String username, String password, String appName, String entryName) {

/*
		curl -X DELETE \
		-H 'Accept:application/json; charset=utf-8' \
		-H 'Content-Type:application/json; charset=utf-8' \
		http://localhost:8087/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/testlive/pushpublish/mapentries/ppsource
*/

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, port, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		String getUrl = "http://" + hostname + ":" + port + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName + "/pushpublish/mapentries/" + entryName;

		ResponseEntity<String> wowza_response;
		try {
			wowza_response = restTemplate.exchange(getUrl, HttpMethod.DELETE, null, String.class);
		}
		catch (RestClientException rce) {
			wowza_response = new ResponseEntity<>("-", HttpStatus.FAILED_DEPENDENCY);
		}
		return wowza_response;
	}

}
