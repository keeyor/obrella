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
import org.opendelos.live.services.scheduler.LiveService;
import org.opendelos.live.services.wowza.WowzaRestService;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.structure.StreamingServer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.*;

@Service("RunScheduler")
@Slf4j
public class LiveRunner {

	private final LiveService liveService;
	private final WowzaRestService wowzaRestService;
	private final StreamingProperties streamingProperties;

	public LiveRunner(LiveService liveService, WowzaRestService wowzaRestService, StreamingProperties streamingProperties) {
		this.liveService = liveService;
		this.wowzaRestService = wowzaRestService;
		this.streamingProperties = streamingProperties;
	}

	@Scheduled(cron = "${scheduler.cron}")
	public void ScheduleRunner() {
		//> Get Today's Schedule.
		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setCollectionName("Scheduler.Live");
		resourceQuery.setSort("date");
		resourceQuery.setDirection("asc");
		List<Resource> TodaySchedule = liveService.searchTodaysSchedule(resourceQuery);

		StreamingServer recorderServer = liveService.getRecorderServer();
		boolean useRecorderServer = streamingProperties.isUse_recorder();
		LocalDateTime localDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

		if (TodaySchedule.size()>0) {
			log.info("SCHEDULER RUN AT '{}'", localDateTime);
			//get enabled streaming servers
			Map<String, StreamingServer> mapOfStreamingServers = liveService.getMapOfStreamingServers();
			int streams_passed = 0;
			int streams_live = 0;
			int streams_stop_now = 0;
			int streams_start_now = 0;
			int streams_future = 0;
			int streams_errors = 0;
			int streams_restart = 0;

			if (mapOfStreamingServers.size()>0) {
				// Get current time
				Instant time_now = ZonedDateTime.now().toInstant().truncatedTo(ChronoUnit.MINUTES);

				for (Resource resource: TodaySchedule) {

					Instant resource_StartTime = resource.getDate();
					long duration_hours = Long.parseLong(resource.getRealDuration().substring(0,2));
					long duration_mins = Long.parseLong(resource.getRealDuration().substring(3,5));
					Instant resource_EndTime = resource.getDate().plus(duration_hours,ChronoUnit.HOURS).plus(duration_mins,ChronoUnit.MINUTES);

					String streamingServerId = resource.getStreamingServerId();
					StreamingServer streamingServer = mapOfStreamingServers.get(streamingServerId);

					if (resource_StartTime.isBefore(time_now) && resource_EndTime.isAfter(time_now)) {
						//# SHOULD BE LIVE- > check if stream is alive and (if required) is recording. Re-start on error
						/* STREAM_STATUS RETURNS:
							 status = -1; // not connected
							 status = -2; // connected not recording ( if required )
							 status = -3; // unknown error
						*/
						if (resource.isRecording()) {
							if (recorderServer != null && useRecorderServer) {
								//RECORDER SERVER
								int stream_live_status_on_rec_server = wowzaRestService.STREAM_STATUS(resource, recorderServer);
								if (stream_live_status_on_rec_server == -1 || stream_live_status_on_rec_server == -3) { 	//not started
									streams_errors++;
									Duration delay = Duration.between(time_now, resource_StartTime);
									log.warn("Scheduler Log: {} {} {} {} {}",
											keyValue("action", "STREAM_ERROR"),
											keyValue("stream_name", resource.getStreamName()),
											keyValue("server_type",recorderServer.getType()),
											keyValue("server_code",recorderServer.getCode()),
											keyValue("msg","Delay:" + delay.toMinutes() + " min. Attempting Restart"));;
									wowzaRestService.STREAM_CLEAN(resource,recorderServer);
									wowzaRestService.STREAM_START(resource,recorderServer);
									//No need to restart  recorder! Starts by itself if originally initiated
									//wowzaRestService.STREAM_RECORD(resource,recorderServer);
									streams_restart++;
								}
								else if (stream_live_status_on_rec_server == -2) {		//started but not recording
									streams_errors++;
									Duration delay = Duration.between(time_now, resource_StartTime);
									log.warn("Scheduler Log: {} {} {} {} {}",
											keyValue("action", "STREAM_RECORD_ERROR"),
											keyValue("stream_name", resource.getStreamName()),
											keyValue("server_type",recorderServer.getType()),
											keyValue("server_code",recorderServer.getCode()),
											keyValue("msg","Delay:" + delay.toMinutes() + " min. Attempting Restart Recorder"));
									wowzaRestService.STREAM_RECORD(resource,recorderServer);
								}
								//LIVE SERVER
								int stream_live_status_on_live_server = wowzaRestService.STREAM_LIVE_STATUS(resource, streamingServer);
								if (stream_live_status_on_live_server != 0) {
									streams_errors++;
									Duration delay = Duration.between(time_now, resource_StartTime);
									log.warn("Scheduler Log: {} {} {} {} {}",
											keyValue("action", "STREAM_ERROR"),
											keyValue("stream_name", resource.getStreamName()),
											keyValue("server_type",streamingServer.getType()),
											keyValue("server_code",streamingServer.getCode()),
											keyValue("msg","Delay:" + delay.toMinutes() + " min. Attempting Restart"));
									wowzaRestService.STREAM_CLEAN(resource,streamingServer);
									wowzaRestService.STREAM_START(resource,streamingServer);
									streams_restart++;
								}
								else {
									streams_live++;
								}
							}
							else {
								int stream_live_status_on_live_server = wowzaRestService.STREAM_STATUS(resource, streamingServer);
								if (stream_live_status_on_live_server == -1 || stream_live_status_on_live_server == -3) { 	//not started
									streams_errors++;
									Duration delay = Duration.between(time_now, resource_StartTime);
									log.warn("Scheduler Log: {} {} {} {} {}",
											keyValue("action", "STREAM_ERROR"),
											keyValue("stream_name", resource.getStreamName()),
											keyValue("server_type",streamingServer.getType()),
											keyValue("server_code",streamingServer.getCode()),
											keyValue("msg","Delay:" + delay.toMinutes() + " min. Attempting Restart"));
									wowzaRestService.STREAM_CLEAN(resource,streamingServer);
									wowzaRestService.STREAM_START(resource,streamingServer);
									//No need to restart  recorder! Starts by itself if originally initiated
									//wowzaRestService.STREAM_RECORD(resource,streamingServer);
									streams_restart++;
								}
								else if (stream_live_status_on_live_server == -2) {		//started but not recording
									streams_errors++;
									Duration delay = Duration.between(time_now, resource_StartTime);
									log.warn("Scheduler Log: {} {} {} {} {}",
											keyValue("action", "STREAM_RECORD_ERROR"),
											keyValue("stream_name", resource.getStreamName()),
											keyValue("server_type",streamingServer.getType()),
											keyValue("server_code",streamingServer.getCode()),
											keyValue("msg","Delay:" + delay.toMinutes() + " min. Attempting Restart Record"));
									wowzaRestService.STREAM_RECORD(resource,streamingServer);
								}
								else {
									streams_live++;
								}
							}
						}
						else {
							int stream_live_status_on_live_server = wowzaRestService.STREAM_LIVE_STATUS(resource, streamingServer);
							if (stream_live_status_on_live_server != 0) {
								streams_errors++;
								Duration delay = Duration.between(time_now, resource_StartTime);
								log.warn("Scheduler Log: {} {} {} {} {}",
										keyValue("action", "STREAM_ERROR"),
										keyValue("stream_name", resource.getStreamName()),
										keyValue("server_type",streamingServer.getType()),
										keyValue("server_code",streamingServer.getCode()),
										keyValue("msg","Delay:" + delay.toMinutes() + " min. Attempting Restart"));
								wowzaRestService.STREAM_CLEAN(resource,streamingServer);
								wowzaRestService.STREAM_START(resource,streamingServer);
								streams_restart++;
							}
							else {
								streams_live++;
							}
						}
					}
					else if (resource_EndTime.equals(time_now)) {
						//# STOP LIVE (on time)
						streams_passed++;
						streams_stop_now++;
						//# STOP on LIVE SERVER
						if (recorderServer != null && useRecorderServer) {
							log.info("Scheduler Log: {} {} {} {} {}",
									keyValue("action", "STREAM_STOP"),
									keyValue("stream_name", resource.getStreamName()),
									keyValue("server_type",streamingServer.getType()),
									keyValue("server_code",streamingServer.getCode()),
									keyValue("msg",""));
							wowzaRestService.STREAM_STOP(resource, streamingServer);
							log.info("Scheduler Log: {} {} {} {} {}",
									keyValue("action", "STREAM_STOP"),
									keyValue("stream_name", resource.getStreamName()),
									keyValue("server_type",recorderServer.getType()),
									keyValue("server_code",recorderServer.getCode()),
									keyValue("msg",""));
							wowzaRestService.STREAM_STOP(resource, recorderServer);
						}
						else {
							log.info("Scheduler Log: {} {} {} {} {}",
									keyValue("action", "STREAM_STOP"),
									keyValue("stream_name", resource.getStreamName()),
									keyValue("server_type",streamingServer.getType()),
									keyValue("server_code",streamingServer.getCode()),
									keyValue("msg",""));
							wowzaRestService.STREAM_STOP(resource, streamingServer);
						}
					}
					else if (resource_StartTime.equals(time_now)) {
						//# START LIVE (on time)
						streams_live++;
						streams_start_now++;
						//# START on LIVE SERVER
						wowzaRestService.STREAM_START(resource,streamingServer);
						log.info("Scheduler Log: {} {} {} {} {}",
								keyValue("action", "STREAM_START"),
								keyValue("stream_name", resource.getStreamName()),
								keyValue("server_type",streamingServer.getType()),
								keyValue("server_code",streamingServer.getCode()),
								keyValue("msg",""));

						//# START RECORDER (if required)
						if (resource.isRecording()) {
							//# START on REC SERVER
							if (recorderServer != null && useRecorderServer) {
								wowzaRestService.STREAM_START(resource, recorderServer);
								log.info("Scheduler Log: {} {} {} {} {}",
										keyValue("action", "STREAM_START"),
										keyValue("stream_name", resource.getStreamName()),
										keyValue("server_type",recorderServer.getType()),
										keyValue("server_code",recorderServer.getCode()),
										keyValue("msg",""));
								//# remove this if "Record All' Wowza module is enabled on REC SERVER
								wowzaRestService.STREAM_RECORD(resource, recorderServer);
								log.info("Scheduler Log: {} {} {} {} {}",
										keyValue("action", "RECORD_START"),
										keyValue("stream_name", resource.getStreamName()),
										keyValue("server_type",recorderServer.getType()),
										keyValue("server_code",recorderServer.getCode()),
										keyValue("msg",""));
							}
							else {
								wowzaRestService.STREAM_RECORD(resource, streamingServer);
								log.info("Scheduler Log: {} {} {} {} {}",
										keyValue("action", "RECORD_START"),
										keyValue("stream_name", resource.getStreamName()),
										keyValue("server_type",streamingServer.getType()),
										keyValue("server_code",streamingServer.getCode()),
										keyValue("msg",""));
							}
						}
					}
					else if (resource_StartTime.isBefore(time_now) && resource_EndTime.isBefore(time_now)) {
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
				log.info("SCHEDULER REPORT AT '{}'. No Streaming Servers configured or none enabled. Nothing to do...", localDateTime);
			}
		}
		else {
			log.info("SCHEDULER REPORT AT '{}'. No Live Streams for today!", localDateTime);
			//TODO: MAybe you should clean Scheduler.Live Collection
		}
	}

}
