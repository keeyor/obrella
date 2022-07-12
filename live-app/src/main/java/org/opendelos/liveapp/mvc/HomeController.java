/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.liveapp.mvc;

import java.util.List;
import java.util.Locale;

import org.opendelos.model.scheduler.ScheduleDTO;
import org.opendelos.model.system.SystemMessage;
import org.opendelos.liveapp.services.i18n.OptionServices;
import org.opendelos.liveapp.services.resource.ResourceService;
import org.opendelos.liveapp.services.scheduler.LiveService;
import org.opendelos.liveapp.services.scheduler.ScheduleService;
import org.opendelos.liveapp.services.structure.ClassroomService;
import org.opendelos.liveapp.services.system.SystemMessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	private final ResourceService resourceService;
	private final LiveService liveService;
	private final SystemMessageService systemMessageService;
	private final ScheduleService scheduleService;
	private final OptionServices optionServices;

	@Autowired
	public HomeController(ResourceService resourceService, LiveService liveService, SystemMessageService systemMessageService, ScheduleService scheduleService, OptionServices optionServices) {
		this.resourceService = resourceService;
		this.liveService = liveService;
		this.systemMessageService = systemMessageService;
		this.scheduleService = scheduleService;
		this.optionServices = optionServices;
	}

	@GetMapping(value = "/")
	public String getLiveHomePage(final Model model, Locale locale) {


		long no_lectures = resourceService.countPublicResourcesByType("COURSE");
		long no_events   = resourceService.countPublicResourcesByType("EVENT");

		model.addAttribute("no_lectures", no_lectures);
		model.addAttribute("no_events", no_events);

		//> Get Live List
		Long live_count = liveService.getLiveResourcesCount();
		model.addAttribute("live_counter", live_count);

		//Get Next Live Broadcast for Channel
		List<ScheduleDTO> scheduleDTOList = scheduleService.getNextLiveBroadcastToChannel(7,true);
		if (scheduleDTOList.size() >0) {
			model.addAttribute("next_live_teleton", scheduleDTOList.get(0));
		}

		long count_scheduled_today = liveService.getScheduledForTodayCoursesCount();
		model.addAttribute("scheduled_today", count_scheduled_today);

		model.addAttribute("page", "home");
		//Languages
		String[] langList = optionServices.getLanguages(locale);
		model.addAttribute("langList",langList);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		List<SystemMessage> visitorsMessages = systemMessageService.findAllByVisibleIsAndTarget(true,"visitors");
		List<SystemMessage> visitorsAllMessages = systemMessageService.findAllByVisibleIsAndTarget(true,"visitors-live");
		visitorsMessages.addAll(visitorsAllMessages);

		model.addAttribute("VisitorMessages", visitorsMessages);

		if (scheduleService.read_liveDaemonStatus())
			return "home";
		else
			return "offline";
	}


}
