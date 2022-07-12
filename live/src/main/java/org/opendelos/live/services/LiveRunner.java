/* 
     Author: Michael Gatzonis - 19/3/2021 
     obrella
*/
package org.opendelos.live.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.opendelos.live.services.resource.ResourceService;
import org.opendelos.live.services.scheduler.LiveService;
import org.opendelos.live.services.structure.StreamingServerService;
import org.opendelos.live.services.wowza.WowzaRestService;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.scheduler.wowza.StreamStatus;
import org.opendelos.model.structure.StreamingServer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
			boolean servers_ready= true;
			log.info("SCHEDULER RUN AT '{}'", localDateTime);

			Map<String, StreamingServer> streamingServersMap = liveService.getStreamingServersHM("true","ipcamera");
			Map<String, StreamingServer> recordingServersMap = liveService.getStreamingServersHM("true","recorder");

			if (streamingServersMap.isEmpty()) {
				log.warn("No active Streaming Servers defined. Nothing to do!");
				servers_ready = false;
			}
			if (useRecorderServer && recordingServersMap.isEmpty()) {
				log.warn("No valid Recorders found. Disabling RS use!");
				useRecorderServer = false;
			}

			int streams_passed = 0;
			int streams_live = 0;
			int streams_recs = 0;
			int streams_stop_now = 0;
			int streams_start_now = 0;
			int streams_future = 0;
			int streams_live_errors = 0;
			int streams_rec_errors = 0;
			int streams_restart = 0;

			Instant time_now = ZonedDateTime.now().toInstant().truncatedTo(ChronoUnit.MINUTES);

			if (servers_ready) {
				for (Resource resource : TodaySchedule) {
					Instant resource_StartTime = resource.getDate();
					long duration_hours = Long.parseLong(resource.getRealDuration().substring(0, 2));
					long duration_mins = Long.parseLong(resource.getRealDuration().substring(3, 5));
					Instant resource_EndTime = resource.getDate().plus(duration_hours, ChronoUnit.HOURS)
							.plus(duration_mins, ChronoUnit.MINUTES);

					//A. STOP LIVE BROADCAST/RECORDING (ON TIME)
					if (resource_EndTime.equals(time_now)) {
						if (resource.isBroadcast()) {
							String resource_ss_id = resource.getStreamingServerId();
							StreamingServer streamingServer = streamingServerService.findById(resource_ss_id);
							if (streamingServer != null) {
								wowzaRestService.STREAM_STOP(resource, streamingServer);
								log.info(String.format("Stop Streaming of %s @ %s", resource.getStreamName(), streamingServer.getCode()));
							}
						}
						if (resource.isRecording() && !resource.getRecorderServerId().equals(resource.getStreamingServerId())) {
							String resource_rs_id = resource.getRecorderServerId();
							StreamingServer recorderServer = streamingServerService.findById(resource_rs_id);
							if (recorderServer != null) {
								wowzaRestService.STREAM_STOP(resource, recorderServer);
								log.info(String.format("Stop Streaming of %s @ %s", resource.getStreamName(), recorderServer.getCode()));
							}
						}
						streams_stop_now++;
					}
					//B. START NOW BROADCAST/RECORDINGS (CREATE LIVE NOW!)
					else if (resource_StartTime.equals(time_now)) {
						StreamingServer streamingServer = null;
						if (resource.isBroadcast()) {
							streamingServer = this.pickRandomStreamingServer(streamingServersMap);
							resource.setStreamingServerId(streamingServer.getId());
							resource.setStreamingServerInfo(streamingServer.getCode() + " / " + streamingServer.getCode());
							wowzaRestService.STREAM_START(resource,streamingServer);
							log.info(String.format("Start Streaming of %s @ %s", resource.getStreamName(), streamingServer.getCode()));
						}
						if (resource.isRecording()) {
							StreamingServer recorderServer;
							if (useRecorderServer) {
								recorderServer = this.pickRandomStreamingServer(recordingServersMap);
								resource.setRecorderServerId(recorderServer.getId());
								resource.setRecorderServerInfo(recorderServer.getCode() + " / " + recorderServer.getCode());
								resource.setUseRecorder(true);
								wowzaRestService.STREAM_START(resource, recorderServer);
								wowzaRestService.STREAM_RECORD(resource,recorderServer);
								log.info(String.format("Start Recording of %s @ %s", resource.getStreamName(), recorderServer.getCode()));
							}
							else {
								if (streamingServer != null) {
									resource.setRecorderServerId(streamingServer.getId());
									resource.setRecorderServerInfo(streamingServer.getCode() + " / " + streamingServer.getCode());
								}
								else {
									streamingServer = this.pickRandomStreamingServer(streamingServersMap);
									resource.setRecorderServerId(streamingServer.getId());
									resource.setRecorderServerInfo(streamingServer.getCode() + " / " + streamingServer.getCode());
								}
								resource.setUseRecorder(false);
								wowzaRestService.STREAM_START(resource, streamingServer);
								wowzaRestService.STREAM_RECORD(resource, streamingServer);
								log.info(String.format("Start Recording of %s @ %s", resource.getStreamName(), streamingServer.getCode()));
							}
						}
						resourceService.updateToCollection(resource, "Scheduler.Live");
						streams_live++;
						streams_start_now++;
					}
					//C. PASSED
					else if (resource_StartTime.isBefore(time_now) && resource_EndTime.isBefore(time_now)) {
						streams_passed++;
					}
					//B. FUTURE LIVE
					else if (resource_StartTime.isAfter(time_now)) {
						streams_future++;
					}
					//D. ALREADY LIVE (SHOULD BE!!!)
					if (resource_StartTime.isBefore(time_now) && resource_EndTime.isAfter(time_now)) {
						StreamStatus streamStatus = wowzaRestService.getStreamStatus(resource,streamingServersMap,recordingServersMap);
						if (resource.isBroadcast()) {
							if (streamStatus.isStreamAlive()) {
								streams_live++;
								log.info(String.format("Stream %s is alive @ %s",resource.getStreamName(), resource.getStreamingServerInfo()));
							}
							else {
								streams_live_errors++;
								log.error(String.format("Stream %s error @ %s. Error is: %s",resource.getStreamName(), resource.getStreamingServerInfo(), streamStatus.getStreamingError()));
								if (streamStatus.getFatalError() == null) {
									if (streamStatus.getStreamingErrorCode() ==  -1) {
										log.error(String.format("Stream %s NOT CONNECTED. IP Camera Error (?). Waiting...", resource.getStreamName()));
									}
									else if (streamStatus.getRecordingErrorCode() == -3) {
										log.error(String.format("Stream %s FAILED. Re-starting", resource.getStreamName()));
										//CLEAN FIRST! IGNORE ERRORS
										if (resource.getStreamingServerId() != null) {
											StreamingServer streamingServer = streamingServerService.findById(resource.getStreamingServerId());
											if (streamingServer != null) {
												wowzaRestService.STREAM_CLEAN(resource, streamingServer);
											}
										}
										//RE-START ON RANDOM SERVER
										StreamingServer streamingServer = this.pickRandomStreamingServer(streamingServersMap);
										resource.setStreamingServerId(streamingServer.getId());
										resource.setStreamingServerInfo(streamingServer.getCode() + " / " + streamingServer.getCode());
										wowzaRestService.STREAM_START(resource,streamingServer);
										log.info(String.format("Re-Start Streaming of %s @ %s", resource.getStreamName(), streamingServer.getCode()));

										resourceService.updateToCollection(resource, "Scheduler.Live");
										streams_restart++;
									}
								}
							}
						}
						if (resource.isRecording()) {
							if (streamStatus.isRecAlive()) {
								streams_recs++;
								log.info(String.format("Stream %s is Recording @ %s",resource.getStreamName(), resource.getRecorderServerInfo()));
							}
							else {
								streams_rec_errors++;
								log.error(String.format("Stream %s Recording error @ %s. Error is: %s",resource.getStreamName(), resource.getRecorderServerInfo(), streamStatus.getRecordingError()));
								if (streamStatus.getFatalError() == null) {
									if (streamStatus.getStreamingErrorCode() == -1) {
										log.error(String.format("Stream %s NOT CONNECTED. IP Camera Error (?). Waiting...", resource.getStreamName()));
									}
									if (streamStatus.getStreamingErrorCode() == -2) {
										log.error(String.format("Stream %s NOT RECORDING.", resource.getStreamName()));
										StreamingServer recordingServer = recordingServersMap.get(resource.getRecorderServerId());
										wowzaRestService.STREAM_RECORD(resource, recordingServer);
										log.info(String.format("Re-Init Recording of %s @ %s", resource.getStreamName(), recordingServer.getCode()));
									}
									else if (streamStatus.getRecordingErrorCode() == -3) {
										log.error(String.format("Stream %s RECORDING FAILED. Re-starting", resource.getStreamName()));
										//CLEAN FIRST! IGNORE ERRORS
										if (resource.getRecorderServerId() != null) {
											StreamingServer recordingServer = streamingServerService.findById(resource.getRecorderServerId());
											if (recordingServer != null) {
												wowzaRestService.STREAM_CLEAN(resource, recordingServer);
											}
										}
										if (useRecorderServer) {
											//RE-START ON RANDOM RECORDING SERVER
											StreamingServer recorderServer = this.pickRandomStreamingServer(recordingServersMap);
											resource.setRecorderServerId(recorderServer.getId());
											resource.setRecorderServerInfo(recorderServer.getCode() + " / " + recorderServer.getCode());
											resource.setUseRecorder(true);
											wowzaRestService.STREAM_START(resource, recorderServer);
											wowzaRestService.STREAM_RECORD(resource,recorderServer);
											log.info(String.format("Re-Start Recording of %s @ %s", resource.getStreamName(), recorderServer.getCode()));
											streams_restart++;
										}
										else {
											StreamingServer recordingServer;
											//IF SCHEDULE IS STREAMING TOO, THIS COULD NOT BE NULL
											if (resource.getStreamingServerId() != null) {
												//USE SAME SERVER AS STREAMING
												recordingServer = streamingServersMap.get(resource.getStreamingServerId());
												resource.setRecorderServerId(recordingServer.getId());
												resource.setRecorderServerInfo(recordingServer.getCode() + " / " + recordingServer.getCode());
												wowzaRestService.STREAM_RECORD(resource, recordingServer);
											}
											else {
												//USE NEW SERVER (FROM STREAM LIST. REMINDER=NOT USING RECORDER) AS RECORDER (CASE OF ONLY RECORDING SCHEDULES)
												recordingServer = this.pickRandomStreamingServer(streamingServersMap);
												resource.setRecorderServerId(recordingServer.getId());
												resource.setRecorderServerInfo(recordingServer.getCode() + " / " + recordingServer.getCode());
												wowzaRestService.STREAM_START(resource, recordingServer);
												wowzaRestService.STREAM_RECORD(resource, recordingServer);
												streams_restart++;
											}
											log.info(String.format("Re-Start Recording of %s @ %s", resource.getStreamName(), recordingServer.getCode()));
										}
										resourceService.updateToCollection(resource, "Scheduler.Live");
									}
								}
							}
						}
					}
				}

				//TODO :: STOP AND DISCARD ANY STREAM ON SERVERS THAT SHOULD NOT BE THERE!!! MAYBE EVERY 1 HOUR???
			}

			if (streams_stop_now >0 || streams_start_now >0 || streams_restart >0 || streams_live >0 || streams_future > 0 || streams_live_errors >0) {
					log.info("SCHEDULER REPORT AT '{}'. Stopped: '{}'. Started: '{}'. ReStarted: '{}'. Passed: '{}'. Live Now: '{}'. Recs Now: '{}'. Future: '{}'. Live Errors: '{}' . Rec Errors: '{}'",
							localDateTime, streams_stop_now, streams_start_now, streams_restart, streams_passed, streams_live, streams_recs,  streams_future, streams_live_errors, streams_rec_errors);
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
