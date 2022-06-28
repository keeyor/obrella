/* 
     Author: Michael Gatzonis - 9/2/2021 
     live
*/
package org.opendelos.liveapp.api.wowza;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendelos.liveapp.api.common.ApiUtils;
import org.opendelos.liveapp.services.resource.ResourceService;
import org.opendelos.liveapp.services.scheduler.LiveService;
import org.opendelos.liveapp.services.structure.StreamingServerService;
import org.opendelos.liveapp.services.wowza.WowzaRestService;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.scheduler.wowza.StreamFiles;
import org.opendelos.model.scheduler.wowza.StreamType;
import org.opendelos.model.scheduler.wowza.responses.IncomingStreamConfig;
import org.opendelos.model.scheduler.wowza.responses.IncomingStreamConfigExtended;
import org.opendelos.model.structure.StreamingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WowzaApi {

	private static final Logger logger = LoggerFactory.getLogger(WowzaApi.class.getName());

	private final StreamingServerService streamingServerService;
	private final WowzaRestService wowzaRestService;
	private final LiveService liveService;
	private final StreamingProperties streamingProperties;
	private final ResourceService resourceService;

	@Autowired
	public WowzaApi(StreamingServerService streamingServerService, WowzaRestService wowzaRestService, LiveService liveService, StreamingProperties streamingProperties, ResourceService resourceService) {
		this.streamingServerService = streamingServerService;
		this.wowzaRestService = wowzaRestService;
		this.liveService = liveService;
		this.streamingProperties = streamingProperties;
		this.resourceService = resourceService;
	}

	@RequestMapping(value = "/api/v1/wowza/{id}/stats", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public String getWowzaApplicationStatistics(@PathVariable("id") String id) {
		try {
			StreamingServer streamingServer = streamingServerService.findById(id);
			if (streamingServer != null) {
				String host = streamingServer.getServer();
				int rPort = Integer.parseInt(streamingServer.getRestPort());
				String app = streamingServer.getApplication();
				String username = streamingServer.getAdminUser();
				String password = streamingServer.getAdminPassword();

				logger.info("Server:" + streamingServer.getCode() + " app:" + app);
				ResponseEntity<String> responseEntity;
				responseEntity = wowzaRestService.GetApplicationStatistics(host, rPort, username, password, app);
				logger.info("Server:" + streamingServer.getCode() + " response:" + responseEntity.getBody());
				if (responseEntity.getBody() != null){
					return responseEntity.getBody();
				}
				else {
					logger.warn("Incompatible response:" + streamingServer.getCode() + " app:" + app);
					return "-";
				}
			}
			else {
				logger.error("Server NOT found: " + id);
				return "-";
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	@RequestMapping(value = "/api/v1/wowza/{id}/stream/{streamId}/stats", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public String getWowzaStreamStatistics(@PathVariable("id") String id,@PathVariable("streamId") String streamId) {
		try {
			StreamingServer streamingServer = streamingServerService.findById(id);
			String host = streamingServer.getServer();
			int rPort = Integer.parseInt(streamingServer.getRestPort());
			String app = streamingServer.getApplication();
			String username = streamingServer.getAdminUser();
			String password = streamingServer.getAdminPassword();

			//logger.info("Check Stream: " + streamId + " - " + response);
			return wowzaRestService.getStatisticsForStream(host,rPort,username,password,app,streamId);
		}
		catch(Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}


	@RequestMapping(value = "/api/v1/wowza/status_all", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public byte[] getWowzaStreamStatistics() {

		List<StreamingServer> streamingServers = streamingServerService.getAllByEnabledAndType("true","ipcamera");
		boolean useRecorderServer = streamingProperties.isUse_recorder();
		if (useRecorderServer) {
			StreamingServer recorderServer = liveService.getRecorderServer();
			if (recorderServer != null) {
				streamingServers.add(recorderServer);
			}
		}
		List<IncomingStreamConfigExtended> incomingStreamConfigs = new ArrayList<>();

		for (StreamingServer streamingServer: streamingServers) {
			try {
				String host = streamingServer.getServer();
				int rPort = Integer.parseInt(streamingServer.getRestPort());
				String app = streamingServer.getApplication();
				String username = streamingServer.getAdminUser();
				String password = streamingServer.getAdminPassword();

				ResponseEntity<String> response = wowzaRestService.GetListOfStreamFiles(host,rPort,username,password,app);

				StreamFiles streamFiles;
				ObjectMapper mapper = new ObjectMapper();

				streamFiles = mapper.readValue(response.getBody(), StreamFiles.class);
				logger.trace("SS status: " + streamingServer.getCode());
				for (StreamType streamType: streamFiles.getStreamFiles()) {
						 String stream_id = streamType.getId();
						 String stream_db_id = stream_id;
						 //DB stream is always without "-rec"
						  if (stream_db_id.endsWith("-rec")) {
						 	stream_db_id = stream_db_id.substring(0,stream_db_id.lastIndexOf("-rec"));
						  }
						 Resource live_resource = resourceService.findByStreamIdInCollection(stream_db_id,"Scheduler.Live");

						 //Stream Status CAN contain "-rec"
						 IncomingStreamConfig incomingStreamConfig = wowzaRestService.getIncomingStreamInformation(streamingServer,stream_id + ".stream");
						 if (incomingStreamConfig != null) {
							 IncomingStreamConfigExtended incomingStreamConfigExtended = new IncomingStreamConfigExtended();
							 BeanUtils.copyProperties(incomingStreamConfig, incomingStreamConfigExtended);
							 incomingStreamConfigExtended.setServerCode(streamingServer.getCode());
							 incomingStreamConfigExtended.setApplicationName(app);
							 String roomCode = "NA";
							 boolean isRecording = false;
							 if (live_resource != null && live_resource.getStreamName() != null) {
								 roomCode = live_resource.getStreamName();
								 isRecording = live_resource.isRecording();
							 }
							 incomingStreamConfigExtended.setRecording(isRecording);
							 incomingStreamConfigExtended.setRoomCode(roomCode);
							 incomingStreamConfigs.add(incomingStreamConfigExtended);
						 }
				}
			}
			catch(Exception e) {
				logger.error("Error reading status from Server {}. The msg: {}",  streamingServer.getCode(),e.getMessage());
			}
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(incomingStreamConfigs);
		return b;
	}
}
