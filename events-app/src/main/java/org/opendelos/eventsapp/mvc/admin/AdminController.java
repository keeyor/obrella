/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.eventsapp.mvc.admin;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import org.opendelos.eventsapp.services.i18n.OptionServices;
import org.opendelos.eventsapp.services.resource.ResourceService;
import org.opendelos.eventsapp.services.resource.ResourceUtils;
import org.opendelos.eventsapp.services.scheduler.ScheduleService;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.scheduler.ScheduleDTO;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

	private final OptionServices optionServices;
	private final ResourceService resourceService;
 	private final ResourceUtils resourceUtils;
 	private final ScheduleService scheduleService;

	@Autowired
	public AdminController(OptionServices optionServices, ResourceService resourceService, ResourceUtils resourceUtils, ScheduleService scheduleService) {
		this.optionServices = optionServices;
		this.resourceService = resourceService;
		this.resourceUtils = resourceUtils;
		this.scheduleService = scheduleService;
	}

	@GetMapping(value = "/admin")
	public String getAdminHomePage(final Model model,Locale locale) throws UnsupportedEncodingException {

		//Languages
		String[] langList = optionServices.getLanguages(locale);
		model.addAttribute("langList",langList);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		model.addAttribute("page","adminIndex");

		return "admin/index";


	}
}
