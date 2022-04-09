/* 
     Author: Michael Gatzonis - 13/2/2022 
     Balloon
*/
package org.opendelos.liveapp.api.scheduler;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.liveapp.api.common.ApiUtils;
import org.opendelos.liveapp.services.resource.ResourceService;
import org.opendelos.liveapp.services.scheduler.LiveService;
import org.opendelos.liveapp.services.structure.ClassroomService;
import org.opendelos.liveapp.services.structure.StreamingServerService;
import org.opendelos.liveapp.services.wowza.WowzaRestService;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LiveApi {

	private static final Logger logger = LoggerFactory.getLogger(LiveApi.class.getName());

	private final LiveService liveService;
	private final ResourceService resourceService;
	private final ClassroomService classroomService;
	private final StreamingProperties streamingProperties;
	private final StreamingServerService streamingServerService;
	private final WowzaRestService wowzaRestService;

	public LiveApi(LiveService liveService, ResourceService resourceService, ClassroomService classroomService, StreamingProperties streamingProperties, StreamingServerService streamingServerService, WowzaRestService wowzaRestService) {
		this.liveService = liveService;
		this.resourceService = resourceService;
		this.classroomService = classroomService;
		this.streamingProperties = streamingProperties;
		this.streamingServerService = streamingServerService;

		this.wowzaRestService = wowzaRestService;
	}

	@RequestMapping(value="/api/v1/live/liveLectures",method = RequestMethod.GET)
	public byte[] getLiveLectures(HttpServletRequest request,
			@RequestParam(value = "d", required = false) String d) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setCollectionName("Scheduler.Live");
		resourceQuery.setDepartmentId(d);
		resourceQuery.setSort("date");
		resourceQuery.setDirection("asc");
		resourceQuery.setResourceType("c");

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);

		//> Get Live List
		QueryResourceResults liveResources = liveService.getLiveResourcesByQuery(resourceQuery);
		byte[] b1;
		b1 = ApiUtils.TransformResultsForDataTable(liveResources.getSearchResultList());

		return b1;
	}
	@RequestMapping(value="/api/v1/live/liveScheduledEvents",method = RequestMethod.GET)
	public byte[] getLiveScheduledEvent(HttpServletRequest request,
			@RequestParam(value = "d", required = false) String d) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setCollectionName("Scheduler.Live");
		resourceQuery.setDepartmentId(d);
		resourceQuery.setSort("date");
		resourceQuery.setDirection("asc");
		resourceQuery.setResourceType("e");

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);

		//> Get Live List
		QueryResourceResults liveResources = liveService.getLiveResourcesByQuery(resourceQuery);
		byte[] b1;
		b1 = ApiUtils.TransformResultsForDataTable(liveResources.getSearchResultList());

		return b1;
	}

	@RequestMapping(value="/api/v1/live/liveToday/{type}",method = RequestMethod.GET)
	public byte[] getTodayEvent(HttpServletRequest request,
			@PathVariable(value = "type", required = false) String type) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setCollectionName("Scheduler.Live");
		if (!type.equals("all")) {
			resourceQuery.setResourceType(type);
		}
		resourceQuery.setSort("date");
		resourceQuery.setDirection("asc");

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);

		//> Get Live List
		QueryResourceResults todayResources = resourceService.searchPageableLectures(resourceQuery);
		classroomService.setClassroomNameToResults(todayResources);
		byte[] b1;
		b1 = ApiUtils.TransformResultsForDataTable(todayResources.getSearchResultList());

		return b1;
	}

	@RequestMapping(value = "/api/v1/live/{id}/stop/{keepFile}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> stopLiveStream(@PathVariable("id") String id,@PathVariable("keepFile") boolean keepFile) {
		boolean res;
		boolean res_record;
		logger.info("Ακύρωση Μετάδοσης/Καταγραφής :: " + id);
		Resource resource = resourceService.findByIdInCollection(id,"Scheduler.Live");
		boolean useRecorderServer = streamingProperties.isUse_recorder();
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
					StreamingServer recorderServer = liveService.getRecorderServer();
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

	@RequestMapping(value="/api/v1/live/liveNowLecturesCounter",method = RequestMethod.GET)
	public int getLiveLecturesCounter(HttpServletRequest request) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		return liveService.getLiveLecturesCounter();
	}

	@RequestMapping(value="/api/v1/live/ScheduledLecturesForTodayCounter",method = RequestMethod.GET)
	public long getScheduledForTodayCounter(HttpServletRequest request) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		return liveService.getScheduledForTodayCoursesCount();
	}
}
