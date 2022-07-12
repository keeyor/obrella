/* 
     Author: Michael Gatzonis - 9/2/2021 
     live
*/
package org.opendelos.live.api;

import org.opendelos.live.services.resource.ResourceService;
import org.opendelos.live.services.scheduler.LiveService;
import org.opendelos.live.services.structure.StreamingServerService;
import org.opendelos.live.services.wowza.WowzaRestService;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.structure.StreamingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LiveApi {

	private static final Logger logger = LoggerFactory.getLogger(LiveApi.class.getName());

	private final WowzaRestService wowzaRestService;
	private final ResourceService resourceService;
	private final StreamingServerService streamingServerService;
	private final StreamingProperties streamingProperties;
	private final LiveService liveService;

	public LiveApi(WowzaRestService wowzaRestService, ResourceService resourceService, StreamingServerService streamingServerService, StreamingProperties streamingProperties, LiveService liveService) {
		this.wowzaRestService = wowzaRestService;
		this.resourceService = resourceService;
		this.streamingServerService = streamingServerService;
		this.streamingProperties = streamingProperties;
		this.liveService = liveService;
	}

	@RequestMapping(value = "/api/v1/status", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> pingApp() {
		return new ResponseEntity<>("ALIVE", HttpStatus.ACCEPTED);
	}

	@RequestMapping(value = "/api/v1/live/{id}/stop/{keepFile}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> stopLiveStream(@PathVariable("id") String id,@PathVariable("keepFile") boolean keepFile) {
		boolean res;
		boolean res_record;
		logger.info("Ακύρωση Μετάδοσης/Καταγραφής :: " + id);
		Resource resource = resourceService.findByIdInCollection(id,"Scheduler.Live");
		boolean useRecorderServer = streamingProperties.isUse_recorder();
		StreamingServer recorderServer = liveService.getRecorderServer();
		if (recorderServer == null) {
			useRecorderServer = false;
		}
		if (resource != null) {
			try {
				//# First: Remove from Live Collection
				resourceService.removeByIdInCollection(id, "Scheduler.Live");
				if (keepFile && !useRecorderServer) {
					String streaming_server_id = resource.getStreamingServerId();
					StreamingServer streamingServer = streamingServerService.findById(streaming_server_id);
					res = wowzaRestService.STREAM_STOP(resource, streamingServer);
					if (resource.isRecording()) {
						logger.info("Η μετάδοση & η εγγραφή σταμάτησαν (με εγγραφή) :: " + resource.getStreamName() + ". DateTime:" + resource.getDate());
					}
					else {
						logger.info("Η μετάδοση & η εγγραφή ακυρώθηκαν (χωρίς εγγραφή) :: " + resource.getStreamName() + ". DateTime:" + resource.getDate());
					}
				}
				else {
					res = wowzaRestService.STREAM_STOP_NOFILE(resource);
					logger.info("Η μετάδοση ακυρώθηκε :: " + resource.getStreamName() + ". DateTime:" + resource.getDate());
				}
			}
			catch (Exception e) {
				logger.error("Πρόβλημα στην ακύρωση της μετάδοσης:" + e.getMessage() + " :: " + resource.getStreamName()+ ". DateTime:" + resource.getDate());
				res = false;
			}
			try {
				if (keepFile && useRecorderServer && resource.isRecording()) {

					res_record = wowzaRestService.STREAM_STOP(resource, recorderServer);
					logger.info("Η εγγραφή της μετάδοσης σταμάτησε (με εγγραφή) :: " + resource.getStreamName() + ". DateTime:" + resource.getDate());
				}
				else {
					if (useRecorderServer && resource.isRecording()) {
						res_record = wowzaRestService.STREAM_STOP_NOFILE_FROM_RECORDER(resource);
						logger.info("Η εγγραφή της μετάδοσης ακυρώθηκε  (χωρίς εγγραφή) :: " + resource.getStreamName() + ". DateTime:" + resource.getDate());
					}
					else {
						res_record = true;
					}
				}
			}
			catch (Exception e) {
				logger.error("Πρόβλημα στην ακύρωση της εγγραφής:" + e.getMessage() + " :: " + resource.getStreamName()+ ". DateTime:" + resource.getDate());
				res_record = false;
			}
			String result = "";
			if (!res) {
				result += "(Ακύρωση) Η ακύρωση της μετάδοσης απέτυχε " + " :: " + resource.getStreamName() + ". DateTime:" + resource.getDate();
			}
			if (!res_record) {
				result += "(Ακύρωση) Η ακύρωση της εγγραφής απέτυχε " +  " :: " + resource.getStreamName() + ". DateTime:" + resource.getDate();
			}
			return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>("(Ακύρωση) Το Stream δεν βρέθηκε :: " + id, HttpStatus.BAD_REQUEST);
	}
}
