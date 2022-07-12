package org.opendelos.live.services.wowza;

import java.util.Map;

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
import org.opendelos.live.services.scheduler.LiveService;
import org.opendelos.live.services.structure.ClassroomService;
import org.opendelos.live.services.structure.StreamingServerService;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.scheduler.wowza.HttpComponentsClientHttpRequestFactoryDigestAuth;
import org.opendelos.model.scheduler.wowza.StreamStatus;
import org.opendelos.model.scheduler.wowza.config.StreamFileAppConfig;
import org.opendelos.model.scheduler.wowza.config.StreamRecorderConfig;
import org.opendelos.model.scheduler.wowza.responses.IncomingStreamConfig;
import org.opendelos.model.structure.Classroom;
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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service("WowzaRestService")
public class WowzaRestService {

	private static final Logger logger = LoggerFactory.getLogger("schedulerLogger");
	private static final int INVALID_STREAM_ID = -5;
	private static final int STREAM_OK = 0;
	private static final int INVALID_STREAMINGSERVER = -1;


	private final ClassroomService classroomService;
	private final StreamingProperties streamingProperties;
	private final StreamingServerService streamingServerService;
	private final LiveService liveService;

	@Autowired
	public WowzaRestService(ClassroomService classroomService, StreamingProperties streamingProperties, StreamingServerService streamingServerService, LiveService liveService) {
		this.classroomService = classroomService;
		this.streamingProperties = streamingProperties;
		this.streamingServerService = streamingServerService;
		this.liveService = liveService;
	}


	public void STREAM_START(Resource resource, StreamingServer streamingServer) {

		String streamName = resource.getStreamName();
		String streamId = resource.getStreamId();
		if (streamingServer.getType().equals("recorder")) {
			streamId = streamId + "-rec";
		}

		Classroom classroom = classroomService.findById(resource.getClassroom());
		String classroom_uri = classroom.getDevices().get(0).getStreamAccessUrl();

		StreamFileAppConfig streamFileAppConfig = new StreamFileAppConfig();
		streamFileAppConfig.setName(streamId);
		streamFileAppConfig.setServerName("_defaultServer_");
		streamFileAppConfig.setUri(classroom_uri);

		try {
			this.AddStreamFile(streamingServer, streamFileAppConfig);
		}
		catch (RestClientException ignored) {}
		try {
			this.ConnectStreamFile(streamingServer, streamId);
		}
		catch (RestClientException rce) {
			logger.error("Error starting stream " + streamName + ". Error Message:" + rce.getMessage());
		}
	}

	public void STREAM_RECORD(Resource resource, StreamingServer streamingServer)  throws  RestClientException {

		String streamId = resource.getStreamId();
		String streamName = resource.getStreamName();
		if (streamingServer.getType().equals("recorder")) {
			streamId = streamId + "-rec";
		}
		//Config Recorder
		String recorder_name = streamId + ".stream";
		StreamRecorderConfig streamRecorderConfig = new StreamRecorderConfig();
		streamRecorderConfig.setRecoderName(recorder_name);
		streamRecorderConfig.setOutputPath(streamingProperties.getStorage());
		streamRecorderConfig.setSegmentationType("SegmentByDuration");
		int segmentationDuration = streamingProperties.getSegmentation_duration() * 60 * 1000;
		streamRecorderConfig.setSegmentDuration(segmentationDuration);
		streamRecorderConfig.setFileTemplate(resource.getId() + "_${SegmentNumber}_${SegmentTime}");
		streamRecorderConfig.setFileFormat("mp4");
		try {
			this.CreateRecorderForStream(streamingServer, recorder_name, streamRecorderConfig);
		}
		catch (RestClientException rce) {
			logger.error("Error starting Recorder for stream " + streamName + ". Error Message:" + rce.getMessage());
		}
	}

	public boolean STREAM_STOP(Resource resource, StreamingServer streamingServer) {

		String streamName = resource.getStreamName();
		String streamId = resource.getStreamId();
		if (streamingServer.getType().equals("recorder")) {
			streamId = streamId + "-rec";
		}
		if (resource.isRecording() && resource.getRecorderServerId().equals(streamingServer.getId())) {
			try {
				this.StopRecorderForStream(streamingServer, streamId + ".stream");
			}
			catch(RestClientException ignored){}
		}
		try {
			this.DisconnectStreamFile(streamingServer,streamId);
		}
		catch (RestClientException rce) {
			logger.warn("WARN: Could not Disconnect Stream File: " + streamId + " Stream Name:" + streamName);
		}
		try {
			this.RemoveStreamFile(streamingServer,streamId);
		}
		catch (RestClientException rce) {
			logger.warn("WARN: Could not Remove Stream File: " + streamId + ".stream" + " Stream Name:" + streamName);
		}
		return true;
	}

	public StreamStatus getStreamStatus(Resource resource,
			Map<String, StreamingServer> streamingServersMap, Map<String, StreamingServer> recordingServersMap) {

		StreamStatus streamStatus = new StreamStatus();
		String res_stream_id = resource.getStreamId();
		if (res_stream_id == null || res_stream_id.trim().equals("")) {
			streamStatus.setFatalError("INVALID_STREAM");
			return streamStatus;
		}
		streamStatus.setStreamAlive(true);
		streamStatus.setRecAlive(true);
		if (resource.isBroadcast() && !resource.isRecording()) {
			streamStatus.setScheduled2Stream(true);
			String ss_id = resource.getStreamingServerId();
			StreamingServer streamingServer = streamingServersMap.get(ss_id);
			streamStatus.setStreamingServerId(ss_id);
			if (streamingServer == null) {
				streamStatus.setStreamAlive(false);
				streamStatus.setStreamingError("INVALID/INACTIVE STREAMING_SERVER");
				streamStatus.setStreamingErrorCode(-4);
			}
			else {
				   int status = this.STREAM_STATUS(resource,streamingServer,false);
				   if (status == 0) {
				   		streamStatus.setStreamAlive(true);
				   }
				   else {
				   		streamStatus.setStreamAlive(false);
					    streamStatus.setStreamingError(this.getStreamStatusMsg(status));
					    streamStatus.setStreamingErrorCode(status);
				   }
			}
		}
		else if (resource.isRecording() && !resource.isBroadcast()) {
			streamStatus.setScheduled2Record(true);
			String rs_id = resource.getRecorderServerId();
			StreamingServer recordingServer = recordingServersMap.get(rs_id);
			streamStatus.setRecordingServerId(rs_id);
			if (recordingServer == null) {
				streamStatus.setRecAlive(false);
				streamStatus.setRecordingError("INVALID/INACTIVE RECORDING_SERVER");
				streamStatus.setRecordingErrorCode(-4);
			}
			else {
					int status = this.STREAM_STATUS(resource,recordingServer,true);
					if (status == 0) {
						streamStatus.setRecAlive(true);
					}
					else {
						streamStatus.setRecAlive(false);
						streamStatus.setRecordingError(this.getStreamStatusMsg(status));
						streamStatus.setRecordingErrorCode(status);
					}
			}
		}
		else if (resource.isBroadcast() && resource.isRecording()){
			//SAME SERVER
			if (resource.getStreamingServerId().equals(resource.getRecorderServerId())) {
				streamStatus.setScheduled2Stream(true);
				streamStatus.setScheduled2Record(true);
				String s_id = resource.getStreamingServerId();
				StreamingServer streamingServer = streamingServersMap.get(s_id);
				streamStatus.setStreamingServerId(s_id);
				streamStatus.setRecordingServerId(s_id);
				if (streamingServer == null) {
					streamStatus.setStreamAlive(false);
					streamStatus.setRecAlive(false);
					streamStatus.setStreamingError("INVALID/INACTIVE STREAMING+RECORDING SERVER");
					streamStatus.setStreamingErrorCode(-4);
				}
				else {
					int status = this.STREAM_STATUS(resource,streamingServer,true);
					if (status == 0) {
						streamStatus.setStreamAlive(true);
					}
					else {
						streamStatus.setStreamAlive(false);
						streamStatus.setStreamingError(this.getStreamStatusMsg(status));
						streamStatus.setStreamingErrorCode(status);
					}
				}
			}
			//DIFFERENT SERVERS
			else {
				streamStatus.setScheduled2Stream(true);
				String ss_id = resource.getStreamingServerId();
				StreamingServer streamingServer = streamingServersMap.get(ss_id);
				streamStatus.setStreamingServerId(ss_id);
				if (streamingServer == null) {
					streamStatus.setStreamAlive(false);
					streamStatus.setStreamingError("INVALID/INACTIVE STREAMING SERVER");
					streamStatus.setStreamingErrorCode(-4);
				}
				else {
					int live_status = this.STREAM_STATUS(resource,streamingServer,false);
					if (live_status == 0) {
						streamStatus.setStreamAlive(true);
					}
					else {
						streamStatus.setStreamAlive(false);
						streamStatus.setStreamingError(this.getStreamStatusMsg(live_status));
						streamStatus.setStreamingErrorCode(live_status);
					}
				}
				streamStatus.setScheduled2Record(true);
				String rs_id = resource.getRecorderServerId();
				StreamingServer recordingServer = recordingServersMap.get(rs_id);
				streamStatus.setRecordingServerId(rs_id);
				if (recordingServer == null) {
					streamStatus.setRecAlive(false);
					streamStatus.setRecordingError("INVALID/INACTIVE RECORDING_SERVER");
					streamStatus.setStreamingErrorCode(-4);
				}
				else {
					int rec_status = this.STREAM_STATUS(resource,recordingServer,true);
					if (rec_status == 0) {
						streamStatus.setRecAlive(true);
					}
					else {
						streamStatus.setRecAlive(false);
						streamStatus.setRecordingError(this.getStreamStatusMsg(rec_status));
						streamStatus.setRecordingErrorCode(rec_status);
					}
				}
			}
		}
		return streamStatus;
	}

	private String getStreamStatusMsg(int status) {

		String msg = "STREAM_OK";
		if (status == -1) {
				msg = "NOT_CONNECTED";
		}
		else if (status == -2) {
				msg = "NOT_RECORDING";
		}
		else if (status == -3) {
				msg = "UKNOWN_ERROR";
		}
		return msg;
	}

	//# Check Live,REC status on same server
	public int STREAM_STATUS(Resource resource, StreamingServer streamingServer, boolean checkRecording) {

		int status = 0;
		String streamId = resource.getStreamId();

		if (streamingServer.getType().equals("recorder")) {
					streamId = streamId + "-rec";
		}
		IncomingStreamConfig incomingStreamConfig = this.getIncomingStreamInformation(streamingServer,streamId + ".stream");
		if (incomingStreamConfig != null) {
			if (incomingStreamConfig.isConnected() && checkRecording) {
				if (resource.isRecording() && !incomingStreamConfig.isRecordingSet()) {
					status = -2; // connected but NOT recording
				}
			}
			else if (!incomingStreamConfig.isConnected()){
				status = -1; // not connected
			}
		}
		else {
			status = -3; // unknown error >> invalid stream returned from wowza
		}

		return status;
	}

/*    public int STREAM_LIVE_STATUS(Resource resource, StreamingServer streamingServer) {

		int status = 0;
		String streamId = resource.getStreamId();

		IncomingStreamConfig incomingStreamConfig = this.getIncomingStreamInformation(streamingServer, streamId + ".stream");
		if (incomingStreamConfig != null) {
			if (!incomingStreamConfig.isConnected()) {
				status = -1; // not connected
			}
		}
		else {
			status = -3; // unknown error >> invalid stream returned from wowza
		}
		return status;
	}*/

	public void STREAM_CLEAN(Resource resource, StreamingServer streamingServer) {

		String server_type = streamingServer.getType();
		String streamId = resource.getStreamId();
		if (server_type.equals("recorder")) {
			streamId = streamId + "-rec";
		}
		try {
				this.DisconnectStreamFile(streamingServer, streamId);
				this.RemoveStreamFile(streamingServer, streamId);
		}
		catch (RestClientException ignored) {}
	}

	public boolean STREAM_STOP_NOFILE(Resource resource) {
		String streamingServerId = resource.getStreamingServerId();
		StreamingServer streamingServer = streamingServerService.findById(streamingServerId);
		return this.STREAM_STOP(resource,streamingServer);
	}

	public boolean STREAM_STOP_NOFILE_FROM_RECORDER(Resource resource) {
		boolean useRecorderServer = streamingProperties.isUse_recorder();
		if (useRecorderServer) {
			StreamingServer recorderServer = liveService.getRecorderServer();
			return this.STREAM_STOP(resource,recorderServer);
		}
		return true;
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
	public void AddStreamFile(StreamingServer streamingServer, StreamFileAppConfig streamFileAppConfig) throws RestClientException {

		String hostname = streamingServer.getServer();
		int restPort 	= Integer.parseInt(streamingServer.getRestPort());
		String username = streamingServer.getAdminUser();
		String password = streamingServer.getAdminPassword();
		String appName  = streamingServer.getApplication();
		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, restPort, username, password);

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

		String postUrl = "http://" + hostname + ":" + restPort + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName + "/streamfiles";
		logger.trace("AddStreamFile URL:" + postUrl);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(json2, headers);

		restTemplate.exchange(postUrl,  HttpMethod.POST, request, String.class);
	}
	public void ConnectStreamFile(StreamingServer streamingServer, String streamName) throws RestClientException {

		String hostname = streamingServer.getServer();
		int restPort 	= Integer.parseInt(streamingServer.getRestPort());
		String username = streamingServer.getAdminUser();
		String password = streamingServer.getAdminPassword();
		String appName  = streamingServer.getApplication();
		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, restPort, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		String postUrl = "http://" + hostname + ":" + restPort + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName + "/streamfiles/" + streamName
				+ "/actions/connect?connectAppName=" + appName + "&appInstance=_definst_&mediaCasterType=rtp";

		logger.trace("ConnectStreamFile URL:" + postUrl);
		restTemplate.exchange(postUrl, HttpMethod.PUT, null, String.class);
	}
	public void CreateRecorderForStream(StreamingServer streamingServer, String recorderName, StreamRecorderConfig streamRecorderConfig) throws RestClientException  {

		String hostname = streamingServer.getServer();
		int restPort 	= Integer.parseInt(streamingServer.getRestPort());
		String username = streamingServer.getAdminUser();
		String password = streamingServer.getAdminPassword();
		String appName  = streamingServer.getApplication();
		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, restPort, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json2 = gson.toJson(streamRecorderConfig);

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
		String postUrl = "http://" + hostname + ":" + restPort + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/"
				+ appName + "/instances/_definst_/streamrecorders/" + recorderName;

		logger.trace("CreateRecorderForStream URL:" + postUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(json2, headers);

		restTemplate.exchange(postUrl,  HttpMethod.POST, request, String.class);
	}
	public ResponseEntity<String> StopRecorderForStream(StreamingServer streamingServer, String streamName) throws RestClientException  {

		String hostname = streamingServer.getServer();
		int restPort 	= Integer.parseInt(streamingServer.getRestPort());
		String username = streamingServer.getAdminUser();
		String password = streamingServer.getAdminPassword();
		String appName  = streamingServer.getApplication();
		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, restPort, username, password);

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
		String postUrl = "http://" + hostname + ":" + restPort + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/"
				+ appName + "/instances/_definst_/streamrecorders/" + streamName + ".stream"
				+ "/actions/stopRecording";

		logger.trace("StopRecorderForStream URL:" + postUrl);
		return restTemplate.exchange(postUrl, HttpMethod.PUT, null, String.class);
	}
	public void DisconnectStreamFile(StreamingServer streamingServer, String streamName) throws RestClientException {

		String hostname = streamingServer.getServer();
		int restPort 	= Integer.parseInt(streamingServer.getRestPort());
		String username = streamingServer.getAdminUser();
		String password = streamingServer.getAdminPassword();
		String appName  = streamingServer.getApplication();
		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, restPort, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		if (streamName.endsWith(".stream")) {
			streamName = streamName.substring(0, streamName.lastIndexOf(".stream"));
		}
		String postUrl = "http://" + hostname + ":" + restPort + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/"
				+ appName + "/instances/_definst_/incomingstreams/" + streamName + ".stream" + "/actions/disconnectStream";
		logger.trace("DisconnectStreamFile URL:" + postUrl);

		restTemplate.exchange(postUrl, HttpMethod.PUT, null, String.class);
	}
	public void RemoveStreamFile(StreamingServer streamingServer, String streamName) throws  RestClientException {

		String hostname = streamingServer.getServer();
		int restPort 	= Integer.parseInt(streamingServer.getRestPort());
		String username = streamingServer.getAdminUser();
		String password = streamingServer.getAdminPassword();
		String appName  = streamingServer.getApplication();

		HttpComponentsClientHttpRequestFactory requestFactory = getRequestFactory(hostname, restPort, username, password);

		// Put the factory in the template
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);

		if (streamName.endsWith(".stream")) {
			streamName = streamName.substring(0, streamName.lastIndexOf(".stream"));
		}
		String postUrl = "http://" + hostname + ":" + restPort + "/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/" + appName + "/streamfiles/" + streamName;
		logger.trace("RemoveStreamFile URL:" + postUrl);

		restTemplate.exchange(postUrl, HttpMethod.DELETE, null, String.class);
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

		logger.info("getStatisticsForStream URL:" + postUrl);
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
}
