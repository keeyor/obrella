/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.eventsapp.mvc;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import org.opendelos.eventsapp.services.i18n.OptionServices;
import org.opendelos.eventsapp.services.resource.ResourceService;
import org.opendelos.eventsapp.services.resource.ResourceUtils;
import org.opendelos.eventsapp.services.scheduler.ScheduleService;
import org.opendelos.eventsapp.services.system.SystemMessageService;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.scheduler.ScheduleDTO;
import org.opendelos.model.system.SystemMessage;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	private final OptionServices optionServices;
	private final ResourceService resourceService;
 	private final ResourceUtils resourceUtils;
 	private final ScheduleService scheduleService;
 	private final SystemMessageService systemMessageService;

	@Autowired
	public HomeController(OptionServices optionServices, ResourceService resourceService, ResourceUtils resourceUtils, ScheduleService scheduleService, SystemMessageService systemMessageService) {
		this.optionServices = optionServices;
		this.resourceService = resourceService;
		this.resourceUtils = resourceUtils;
		this.scheduleService = scheduleService;
		this.systemMessageService = systemMessageService;
	}

	@GetMapping(value = "/")
	public String getHomePage(final Model model,Locale locale) throws UnsupportedEncodingException {

		ResourceQuery recentQuery = new ResourceQuery();
		recentQuery.setLimit(8);
		recentQuery.setSkip(0);
		recentQuery.setSort("date");
		recentQuery.setDirection("desc");
		recentQuery.setResourceType("e");
		//recentQuery.setAccessPolicy("public");
		recentQuery.setUniqueOnly(true);

		QueryResourceResults queryRecentResults = resourceService.searchPageableLectures(recentQuery);
		recentQuery.setTotalResults(queryRecentResults.getTotalResults());
		model.addAttribute("QR_RECENT", queryRecentResults);
		model.addAttribute("resourceQuery",recentQuery);

		ResourceQuery popQuery = new ResourceQuery();
		BeanUtils.copyProperties(recentQuery,popQuery);
		popQuery.setSort("views");
		QueryResourceResults queryPopResults = resourceService.searchPageableLectures(popQuery);
		popQuery.setTotalResults(queryPopResults.getTotalResults());
		model.addAttribute("QR_POP", queryPopResults);

		ResourceQuery featuredQuery = new ResourceQuery();
		BeanUtils.copyProperties(recentQuery,featuredQuery);
		featuredQuery.setSort("date");
		featuredQuery.setFeatured(true);
		featuredQuery.setLimit(10);
		QueryResourceResults queryFeaturedResults = resourceService.searchPageableLectures(featuredQuery);
		featuredQuery.setTotalResults(queryFeaturedResults.getTotalResults());
		model.addAttribute("QR_FEATURED", queryFeaturedResults);

		List<ScheduleDTO> scheduleDTOList = scheduleService.getNextScheduledEvents();
		model.addAttribute("NextScheduledEvents", scheduleDTOList);

		//Multimedia Web BaseDir (for all Resources)
		//resourceUtils.setStreamingProperties(streamingProperties);
		StringBuilder multimedia_base_web_dir = resourceUtils.getMultimediaBaseWebPath();
		model.addAttribute("mBaseWedDir",multimedia_base_web_dir.toString());
		StringBuilder support_base_web_dir = resourceUtils.getSupportFilesBaseWebPath();
		model.addAttribute("sSupportWedDir",support_base_web_dir.toString());
		StringBuilder events_base_web_path = resourceUtils.getEventFilesBaseWebPath();
		model.addAttribute("mEventsWedDir",events_base_web_path.toString());
		//Languages
		String[] langList = optionServices.getLanguages(locale);
		model.addAttribute("langList",langList);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		//Messages

		List<SystemMessage> visitorsMessages = systemMessageService.findAllByVisibleIsAndTargetAndSites(true,"visitors","events");
		model.addAttribute("VisitorMessages", visitorsMessages);

		model.addAttribute("page","home");

		return "home";
	}

	@GetMapping(value = "/logout")
	public String getLogoutPage() {

		return "redirect:/";
	}
}
