/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.liveapp.api.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opendelos.liveapp.services.scheduler.ScheduleService;
import org.opendelos.model.scheduler.ChannelProgram;
import org.opendelos.model.scheduler.ScheduleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LiveChannelApi {

	private final Logger logger = LoggerFactory.getLogger(LiveChannelApi.class);

	private final ScheduleService scheduleService;

	@Value("${app.zone}")
	String app_zone;

	@Autowired
	public LiveChannelApi(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@RequestMapping(value= "/api/v1/channel/timetable/today", method = RequestMethod.GET, produces =  "application/json")
	public List<ChannelProgram> SearchTimeTableByEditor() {

		List<ScheduleDTO> scheduleDTOList = scheduleService.getNextLiveBroadcastToChannel(0,false);
		List<ChannelProgram> channel_timetable = new ArrayList<>();
		for (ScheduleDTO scheduleDTO: scheduleDTOList) {
			ChannelProgram channelProgram = new ChannelProgram();
			LocalDateTime program_startTime = scheduleService.getDateTimeOfSchedule(scheduleDTO);
			LocalDateTime program_endTime = program_startTime.plus(scheduleDTO.getDurationHours(), ChronoUnit.HOURS).plus(scheduleDTO.getDurationMinutes(),ChronoUnit.MINUTES);
			channelProgram.setStartTime(program_startTime.toEpochSecond(getZoneOffSetFromZoneId(ZoneId.of(app_zone))));
			channelProgram.setEndTime(program_endTime.toEpochSecond(getZoneOffSetFromZoneId(ZoneId.of(app_zone))));
			channelProgram.setTitle(scheduleDTO.getScheduledEvent().getTitle());
			channelProgram.setDescription(scheduleDTO.getClassroom().getName());
			channel_timetable.add(channelProgram);
		}
		channel_timetable.sort(new StartTimeSorter());


		return channel_timetable;
	}

	private class StartTimeSorter implements Comparator<ChannelProgram>
	{
		@Override
		public int compare(ChannelProgram o1, ChannelProgram o2) {
			 return Long.compare(o2.getEndTime(), o1.getEndTime());
		}
	}

	private ZoneOffset getZoneOffSetFromZoneId(ZoneId zoneId) {
		Instant instant = Instant.now(); //can be LocalDateTime
		return  zoneId.getRules().getOffset(instant);
	}
}
