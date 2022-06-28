/* 
     Author: Michael Gatzonis - 19/3/2021 
     obrella
*/
package org.opendelos.live.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.opendelos.live.repository.resource.ResourceQuery;
import org.opendelos.live.services.resource.ResourceService;
import org.opendelos.live.services.scheduler.LiveService;
import org.opendelos.live.services.structure.StreamingServerService;
import org.opendelos.live.services.wowza.WowzaRestService;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.structure.StreamingServer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.*;

@Service("RunScheduler")
@Slf4j
public class LiveRunner {

	private final ResourceService resourceService;
	private final LiveService liveService;
	private final WowzaRestService wowzaRestService;
	private final StreamingProperties streamingProperties;
	private final StreamingServerService streamingServerService;

	public LiveRunner(ResourceService resourceService, LiveService liveService, WowzaRestService wowzaRestService, StreamingProperties streamingProperties, StreamingServerService streamingServerService) {
		this.resourceService = resourceService;
		this.liveService = liveService;
		this.wowzaRestService = wowzaRestService;
		this.streamingProperties = streamingProperties;
		this.streamingServerService = streamingServerService;
	}

    @Scheduled(cron = "${scheduler.cron}")
	public void ScheduleRunner() {
		//> Get Today's Schedule.
		List<Resource> TodaySchedule = liveService.getTodaysScheduleFromDatabase();

		boolean useRecorderServer = streamingProperties.isUse_recorder();
		LocalDateTime localDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

		if (TodaySchedule.size()>0) {
			log.info("SCHEDULER RUN AT '{}'", localDateTime);

			Map<String, StreamingServer> streamingServersMap = liveService.getStreamingServersHM("true","ipcamera");
			Map<String, StreamingServer> recordingServersMap = liveService.getStreamingServersHM("true","recorder");

			if (useRecorderServer && recordingServersMap.isEmpty()) {
				log.warn("No valid Recorders found. Disabling RS use!");
				useRecorderServer = false;
			}

			int streams_passed = 0;
			int streams_live = 0;
			int streams_stop_now = 0;
			int streams_start_now = 0;
			int streams_future = 0;
			int streams_errors = 0;
			int streams_restart = 0;

				// Get current time
				Instant time_now = ZonedDateTime.now().toInstant().truncatedTo(ChronoUnit.MINUTES);

				for (Resource resource: TodaySchedule) {
					Instant resource_StartTime = resource.getDate();
					long duration_hours = Long.parseLong(resource.getRealDuration().substring(0,2));
					long duration_mins = Long.parseLong(resource.getRealDuration().substring(3,5));
					Instant resource_EndTime = resource.getDate().plus(duration_hours,ChronoUnit.HOURS).plus(duration_mins,ChronoUnit.MINUTES);

					StreamingServer streamingServer;
					String resource_ss_id = null;
					if ( resource.getStreamingServerId() != null) { resource_ss_id = resource.getStreamingServerId();}

					StreamingServer recorderServer;
					String resource_rs_id = null;
					if (resource.getRecordingServerId() != null) { resource_rs_id = resource.getRecordingServerId();}


					if (resource_StartTime.isBefore(time_now) && resource_EndTime.isAfter(time_now)) {
						//# LIVE RESOURCES
						if (resource.isBroadcast() && resource.isRecording()) {
							 if (useRecorderServer) {
							 	 //# CHECK LIVE_STATUS
							 	 streamingServer = streamingServersMap.get(resource_ss_id);
							 	 if (streamingServer != null) {
									 int stream_live_status_on_live_server = -3;
									 try {
										 stream_live_status_on_live_server = wowzaRestService.STREAM_LIVE_STATUS(resource, streamingServer);
									 }
									 catch (Exception ignored) {}
									 if (stream_live_status_on_live_server == 0) {
										 log.info("Stream {} Streaming OK on SS {}",
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_code", streamingServer.getCode()));
									 }
									 else if (stream_live_status_on_live_server == -1) {
										 log.warn("Stream {} Stream Warning on SS {} msg {}",
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_code", streamingServer.getCode()),
												 keyValue("server_code", "Camera error? Waiting..."));
									 }
									 else if (stream_live_status_on_live_server == -3) {
										 log.error("Stream {} Stream ERROR on SS {} msg {}",
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_code", streamingServer.getCode()),
												 keyValue("server_code", "Undefined Error!"));
									 }
								 }
							 	 else {
									 log.error("Stream {} Stream ERROR msg {}",
											 keyValue("stream_name", resource.getStreamName()),
											 keyValue("server_code", "Streaming Server NOT found in enabled servers!"));
								 }
								 //# CHECK RECORDING STATUS
								 recorderServer = recordingServersMap.get(resource_rs_id);
								 if (recorderServer != null) {
									 int stream_live_status_on_rec_server = -3;
									 try {
										 stream_live_status_on_rec_server = wowzaRestService.STREAM_STATUS(resource, recorderServer);
									 }
									 catch (Exception ignored) {}
									 if (stream_live_status_on_rec_server == 0) {
										 log.info("Stream {} Recording OK on RS {}",
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_code",recorderServer.getCode()));
									 }
									 if (stream_live_status_on_rec_server == -1) { 	//not connected: Wait!!!
										 streams_errors++;
										 Duration delay = Duration.between(time_now, resource_StartTime);
										 log.warn("Scheduler Log: {} {} {} {} {}",
												 keyValue("action", "STREAM_RECORDING_WARNING"),
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_type", recorderServer.getType()),
												 keyValue("server_code", recorderServer.getCode()),
												 keyValue("msg", "Delay:" + delay.toMinutes() + " min. Camera Error? Waiting..."));
										 streams_restart++;
									 }
									 if (stream_live_status_on_rec_server == -3) { 	//unknown error: clean and restart
										 streams_errors++;
										 Duration delay = Duration.between(time_now, resource_StartTime);
										 log.error("Scheduler Log: {} {} {} {} {}",
												 keyValue("action", "STREAM_RECORDING_ERROR"),
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_type",recorderServer.getType()),
												 keyValue("server_code",recorderServer.getCode()),
												 keyValue("msg","Delay:" + delay.toMinutes() + " min. Attempting Restart in a Minute"));
										 wowzaRestService.STREAM_CLEAN(resource,recorderServer);
										 wowzaRestService.STREAM_START(resource,recorderServer);
										 streams_restart++;
									 }
									 else if (stream_live_status_on_rec_server == -2) {		//started but not recording
										 streams_errors++;
										 Duration delay = Duration.between(time_now, resource_StartTime);
										 log.warn("Scheduler Log: {} {} {} {} {}",
												 keyValue("action", "STREAM_RECORDING_WARN"),
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_type",recorderServer.getType()),
												 keyValue("server_code",recorderServer.getCode()),
												 keyValue("msg","Delay:" + delay.toMinutes() + " min. Restarting RECORDER"));
										 wowzaRestService.STREAM_RECORD(resource,recorderServer);
									 }
								 }
								 else {
									 log.error("Stream {} Stream ERROR msg {}",
											 keyValue("stream_name", resource.getStreamName()),
											 keyValue("server_code", "REcorder Server NOT found in enabled servers!"));
								 }
							 }
							 else {
								 //# CHECK RECORDING STATUS
								 streamingServer = streamingServersMap.get(resource_ss_id);
								 if (streamingServer != null) {
									 int stream_live_status_on_rec_server = -3;
									 try {
										 stream_live_status_on_rec_server = wowzaRestService.STREAM_STATUS(resource, streamingServer);
									 }
									 catch (Exception ignored) {}
									 if (stream_live_status_on_rec_server == 0) {
										 log.info("Stream {} PLAY+RECORD OK on SS {}",
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_code",streamingServer.getCode()));
									 }
									 if (stream_live_status_on_rec_server == -1) { 	//not connected: Wait!!!
										 streams_errors++;
										 Duration delay = Duration.between(time_now, resource_StartTime);
										 log.warn("Scheduler Log: {} {} {} {} {}",
												 keyValue("action", "PLAY+RECORD_WARNING"),
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_type", streamingServer.getType()),
												 keyValue("server_code", streamingServer.getCode()),
												 keyValue("msg", "Delay:" + delay.toMinutes() + " min. Camera Error? Waiting..."));
										 streams_restart++;
									 }
									 else if (stream_live_status_on_rec_server == -2) {		//started but not recording
										 streams_errors++;
										 Duration delay = Duration.between(time_now, resource_StartTime);
										 log.warn("Scheduler Log: {} {} {} {} {}",
												 keyValue("action", "PLAY+RECORD_WARN"),
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_type",streamingServer.getType()),
												 keyValue("server_code",streamingServer.getCode()),
												 keyValue("msg","Delay:" + delay.toMinutes() + " min. Restarting RECORDER"));
										 wowzaRestService.STREAM_RECORD(resource,streamingServer);
									 }
									 if (stream_live_status_on_rec_server == -3) { 	//unknown error: clean and restart
										 streams_errors++;
										 Duration delay = Duration.between(time_now, resource_StartTime);
										 log.error("Scheduler Log: {} {} {} {} {}",
												 keyValue("action", "PLAY+RECORD_ERROR"),
												 keyValue("stream_name", resource.getStreamName()),
												 keyValue("server_type",streamingServer.getType()),
												 keyValue("server_code",streamingServer.getCode()),
												 keyValue("msg","Delay:" + delay.toMinutes() + " min. Attempting Restart in a Minute"));
										 wowzaRestService.STREAM_CLEAN(resource,streamingServer);
										 wowzaRestService.STREAM_START(resource,streamingServer);
										 streams_restart++;
									 }
								 }
								 else {
									 log.error("Stream {} Stream ERROR msg {}",
											 keyValue("stream_name", resource.getStreamName()),
											 keyValue("server_code", "Streaming Server NOT found in enabled servers!"));
								 }
							 }
						}
						else if (!resource.isRecording()) {		// BROADCAST ONLY
							// CHECK LIVE STATUS ON SS
							//# CHECK LIVE_STATUS
							streamingServer = streamingServersMap.get(resource_ss_id);
							if (streamingServer != null) {
								int stream_live_status_on_live_server = -3;
								try {
									stream_live_status_on_live_server = wowzaRestService.STREAM_LIVE_STATUS(resource, streamingServer);
								}
								catch (Exception ignored) {}
								if (stream_live_status_on_live_server == 0) {
									log.info("Stream {} Streaming OK on SS {}",
											keyValue("stream_name", resource.getStreamName()),
											keyValue("server_code", streamingServer.getCode()));
								}
								else if (stream_live_status_on_live_server == -1) {
									log.warn("Stream {} Stream Warning on SS {} msg {}",
											keyValue("stream_name", resource.getStreamName()),
											keyValue("server_code", streamingServer.getCode()),
											keyValue("server_code", "Camera error? Waiting..."));
								}
								else if (stream_live_status_on_live_server == -3) {
									log.error("Stream {} Stream ERROR on SS {} msg {}",
											keyValue("stream_name", resource.getStreamName()),
											keyValue("server_code", streamingServer.getCode()),
											keyValue("server_code", "Undefined Error!"));
								}
							}
							else {
								log.error("Stream {} Stream ERROR msg {}",
										keyValue("stream_name", resource.getStreamName()),
										keyValue("server_code", "Streaming Server NOT found in enabled servers!"));
							}
						}
						else if (!resource.isBroadcast()) { 	//RECORD ONLY
							// CHECK RECORD STATUS ON SS OR RS
							if (useRecorderServer) {
								recorderServer = recordingServersMap.get(resource_rs_id);
							}
							else {
								recorderServer = streamingServersMap.get(resource_ss_id);
							}
							//# CHECK RECORDING STATUS
							if (recorderServer != null) {
									int stream_live_status_on_rec_server = -3;
									try {
										stream_live_status_on_rec_server = wowzaRestService.STREAM_STATUS(resource, recorderServer);
									}
									catch (Exception ignored) {}
									if (stream_live_status_on_rec_server == 0) {
										log.info("Stream {} RECORD OK {}",
												keyValue("stream_name", resource.getStreamName()),
												keyValue("server_code",recorderServer.getCode()));
									}
									if (stream_live_status_on_rec_server == -1) { 	//not connected: Wait!!!
										streams_errors++;
										Duration delay = Duration.between(time_now, resource_StartTime);
										log.warn("Scheduler Log: {} {} {} {} {}",
												keyValue("action", "RECORD_WARNING"),
												keyValue("stream_name", resource.getStreamName()),
												keyValue("server_type", recorderServer.getType()),
												keyValue("server_code", recorderServer.getCode()),
												keyValue("msg", "Delay:" + delay.toMinutes() + " min. Camera Error? Waiting..."));
										streams_restart++;
									}
									if (stream_live_status_on_rec_server == -3) { 	//unknown error: clean and restart
										streams_errors++;
										Duration delay = Duration.between(time_now, resource_StartTime);
										log.error("Scheduler Log: {} {} {} {} {}",
												keyValue("action", "RECORD_ERROR"),
												keyValue("stream_name", resource.getStreamName()),
												keyValue("server_type",recorderServer.getType()),
												keyValue("server_code",recorderServer.getCode()),
												keyValue("msg","Delay:" + delay.toMinutes() + " min. Attempting Restart in a Minute"));
										wowzaRestService.STREAM_CLEAN(resource,recorderServer);
										wowzaRestService.STREAM_START(resource,recorderServer);
										streams_restart++;
									}
									else if (stream_live_status_on_rec_server == -2) {		//started but not recording
										streams_errors++;
										Duration delay = Duration.between(time_now, resource_StartTime);
										log.warn("Scheduler Log: {} {} {} {} {}",
												keyValue("action", "RECORD_WARN"),
												keyValue("stream_name", resource.getStreamName()),
												keyValue("server_type",recorderServer.getType()),
												keyValue("server_code",recorderServer.getCode()),
												keyValue("msg","Delay:" + delay.toMinutes() + " min. Restarting RECORDER"));
										wowzaRestService.STREAM_RECORD(resource,recorderServer);
									}
								}
								else {
									log.error("Stream {} Stream ERROR msg {}",
											keyValue("stream_name", resource.getStreamName()),
											keyValue("server_code", "Recording Server NOT found in enabled servers!"));
								}
						}
					}
					else if (resource_EndTime.equals(time_now)) {
						//# STOP LIVE (on time)
						streams_passed++;
						streams_stop_now++;

						String msg = "";
						if (resource.isBroadcast() && resource.isRecording()) {
							if (useRecorderServer) {
								streamingServer = streamingServerService.findById(resource_ss_id);
								if (streamingServer != null) {
									msg += "STREAM_STOP Stream @: " + streamingServer.getCode();
									wowzaRestService.STREAM_STOP(resource, streamingServer);
								}
								recorderServer = streamingServerService.findById(resource_rs_id);
								if (recorderServer != null) {
									msg += " :: STREAM_STOP (WGET) Record @: " + recorderServer.getCode();
									wowzaRestService.STREAM_STOP(resource, recorderServer);
								}
							}
							else {
								streamingServer = streamingServerService.findById(resource_ss_id);
								if (streamingServer != null) {
									msg += "STREAM_STOP  (WGET) Stream & Record @" + streamingServer.getCode();
									wowzaRestService.STREAM_STOP(resource, streamingServer);
								}
							}
						}
						else if (!resource.isRecording()) {
								streamingServer = streamingServerService.findById(resource_ss_id);
								if (streamingServer != null) {
									msg += "STREAM_STOP Stream @: " + streamingServer.getCode();
									wowzaRestService.STREAM_STOP(resource, streamingServer);
								}
						}
						else {
								if (useRecorderServer) {
									recorderServer = streamingServerService.findById(resource_rs_id);
								}
								else {
									recorderServer = streamingServerService.findById(resource_ss_id);
								}
								if (recorderServer != null) {
									msg += " :: STREAM_STOP  (WGET) RECORD @: " + recorderServer.getCode();
									wowzaRestService.STREAM_STOP(resource, recorderServer);
								}
						}
						log.info("Scheduler Log: {} {} {}",
								keyValue("action", "STREAM_STOP"),
								keyValue("stream_name", resource.getStreamName()),
								keyValue("msg",msg));

					}
					else if (resource_StartTime.equals(time_now)) {			//# START LIVE (on time)

						streams_live++;
						streams_start_now++;
						String msg;

						if (resource.isBroadcast() && resource.isRecording()) {
							msg = "BROADCAST & RECORD: " + resource.getStreamName();
							streamingServer = this.pickRandomStreamingServer(streamingServersMap);
							resource.setStreamingServerId(streamingServer.getId());
							wowzaRestService.STREAM_START(resource,streamingServer);
							if (!useRecorderServer) {
								msg += "- Stream & Record @: " + streamingServer.getCode();
								resource.setRecordingServerId(streamingServer.getId());
								wowzaRestService.STREAM_RECORD(resource, streamingServer);
							}
							else {
								recorderServer = this.pickRandomStreamingServer(recordingServersMap);
								resource.setRecordingServerId(recorderServer.getId());
								wowzaRestService.STREAM_START(resource, recorderServer);
								wowzaRestService.STREAM_RECORD(resource, recorderServer);
								msg += "- Stream @: " + streamingServer.getCode() + " / Recorder @: " + recorderServer.getCode();
							}
							log.info("Scheduler Log: {} {} {}",
									keyValue("action", "STREAM_START"),
									keyValue("stream_name", resource.getStreamName()),
									keyValue("msg",msg));
						}
						else if (!resource.isRecording()) { // BROADCAST ONLY
							msg = "BROADCAST ONLY: " + resource.getStreamName();
							streamingServer = this.pickRandomStreamingServer(streamingServersMap);
							resource.setStreamingServerId(streamingServer.getId());
							wowzaRestService.STREAM_START(resource,streamingServer);
							msg += "- Stream @: " + streamingServer.getCode();
							log.info("Scheduler Log: {} {} {}",
									keyValue("action", "STREAM_START"),
									keyValue("stream_name", resource.getStreamName()),
									keyValue("msg",msg));
						}
						else if (!resource.isBroadcast()) { // RECORD ONLY
							msg = "RECORD ONLY: " + resource.getStreamName();
							if (useRecorderServer) {
								recorderServer = this.pickRandomStreamingServer(recordingServersMap);	// picke from recorders
								resource.setRecordingServerId(recorderServer.getId());
								wowzaRestService.STREAM_START(resource, recorderServer);
								wowzaRestService.STREAM_RECORD(resource, recorderServer);
								msg += "- Recorder @: " + recorderServer.getCode();
							}
							else {
								recorderServer = this.pickRandomStreamingServer(streamingServersMap);  // picker from regular steraming servers
								resource.setRecordingServerId(recorderServer.getId());
								wowzaRestService.STREAM_START(resource, recorderServer);
								wowzaRestService.STREAM_RECORD(resource, recorderServer);
								msg += "- Record @: " + recorderServer.getCode();
							}
							log.info("Scheduler Log: {} {} {}",
									keyValue("action", "STREAM_START"),
									keyValue("stream_name", resource.getStreamName()),
									keyValue("msg",msg));
						}
						resourceService.updateToCollection(resource, "Scheduler.Live");
					}
					else if (resource_StartTime.isBefore(time_now) && resource_EndTime.isBefore(time_now)) {
						if (resource_ss_id != null) {
							streamingServer = streamingServerService.findById(resource_ss_id);
							if (streamingServer != null) {
								int stream_live_status_on_live_server = wowzaRestService.STREAM_STATUS(resource, streamingServer);
								if (stream_live_status_on_live_server != -3) {
									log.warn("clean-up zombie Stream @: " + streamingServer.getCode());
									wowzaRestService.STREAM_CLEAN(resource, streamingServer);
								}
							}
						}
						if (resource_rs_id != null && !resource_rs_id.equals(resource_ss_id)) { // covers the case resource_ss_id == null [ happens in record only ]
							recorderServer = streamingServerService.findById(resource_rs_id);
							if (recorderServer != null) {
								int stream_live_status_on_rec_server = wowzaRestService.STREAM_STATUS(resource, recorderServer);
								if (stream_live_status_on_rec_server != -3) {
									log.warn("clean-up zombie Stream @: " + recorderServer.getCode());
									wowzaRestService.STREAM_CLEAN(resource, recorderServer);
								}
							}
						}
						streams_passed++;
					} else if (resource_StartTime.isAfter(time_now)) {
						streams_future++;
					}
				}
				if (streams_stop_now >0 || streams_start_now >0 || streams_restart >0 || streams_passed >0 || streams_live >0 || streams_future > 0 || streams_errors >0) {
					log.info("SCHEDULER REPORT AT '{}'. Stopped: '{}'. Started: '{}'. ReStarted: '{}'. Passed: '{}'. Live Now: '{}'. Future Live: '{}'. Errors: '{}'",
							localDateTime, streams_stop_now, streams_start_now, streams_restart, streams_passed, streams_live, streams_future, streams_errors);
				}
		}
		else {
			log.trace("SCHEDULER REPORT AT '{}'. No Live Streams for today!", localDateTime);
			//TODO: Maybe you should clean Scheduler.Live Collection
		}
	}

	private StreamingServer pickRandomStreamingServer(Map<String, StreamingServer> mapOfEnabledStreamingServers ) {

		StreamingServer streamingServer = null;
		java.util.Random random = new java.util.Random();
		int random_server_index = random.nextInt(mapOfEnabledStreamingServers.size());
		int index = 0;
		for (Map.Entry<String, StreamingServer> entry : mapOfEnabledStreamingServers.entrySet()) {
			if (index == random_server_index) {
				 streamingServer = entry.getValue();
				 break;
			}
			else {
				index++;
			}
		}
		return streamingServer;
	}

}
