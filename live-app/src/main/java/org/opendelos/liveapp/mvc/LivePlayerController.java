/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.liveapp.mvc;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendelos.liveapp.services.i18n.OptionServices;
import org.opendelos.liveapp.services.resource.ResourceService;
import org.opendelos.liveapp.services.resource.ResourceUtils;
import org.opendelos.liveapp.services.structure.ClassroomService;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.Slide;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LivePlayerController {

	private final ResourceService resourceService;
	private final ResourceUtils resourceUtils;
	private final ClassroomService classroomService;
	private final OptionServices optionServices;

	@Autowired
	public LivePlayerController(ResourceService resourceService, ResourceUtils resourceUtils, ClassroomService classroomService, OptionServices optionServices) {
		this.resourceService = resourceService;
		this.resourceUtils = resourceUtils;
		this.classroomService = classroomService;
		this.optionServices = optionServices;
	}


	@GetMapping(value = {"/live_player", "cas/live_player"})
	public ModelAndView playVideo (final ModelMap model, @RequestParam(value = "id", required = false) String id,
												HttpServletRequest request, Locale locale, HttpServletResponse response) throws IOException {

		boolean allow_access = false;
		Resource resource;
		if (id != null && !id.isEmpty()) {
			resource = resourceService.findLiveStreamByIdOrNameInCollection(id,"Scheduler.Live");
		}
		else {
			model.addAttribute("reason", "INVALID_ID_OR_NAME");
			return new ModelAndView("forward:/nobroadcastpage",model);
		}
		if (resource != null) {
			String classroom_id = resource.getClassroom();
			Classroom classroom = classroomService.findById(classroom_id);
			if (classroom == null) {
				model.addAttribute("reason", "INVALID_CLASSROOM");
				return new ModelAndView("forward:/nobroadcastpage",model);
			}
			else {
				resource.setClassroomName(classroom.getName());
			}
		}
		else {
			Classroom classroom = classroomService.findByCode(id);
			if (classroom == null) {
				model.addAttribute("reason", "NO_LIVE_FOUND");
				return new ModelAndView("forward:/nobroadcastpage",model);
			}
			else {
				model.addAttribute("reason", "NO_LIVE_IN_CLASSROOM");
				return new ModelAndView("forward:/nobroadcastpage?code=" + id,model);
			}
		}

		switch (resource.getAccess()) {
		case "open":
			allow_access = true;
			break;
		case "sso":
			Object user_principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (user_principal instanceof OoUserDetails) {
				OoUserDetails user = (OoUserDetails) user_principal;
				if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA")) ||
						user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")) ||
						user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER")) ||
						user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT")) ||
						user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
					allow_access = true;
					user = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
					model.addAttribute("user", user);
				}
			}
			break;
		case "password":
			if (request.getSession().getAttribute("allow_access_to") != null) {
				String allow_access_to = (String) request.getSession().getAttribute("allow_access_to");
				if (allow_access_to.equals(resource.getId())) {
					allow_access = true;
				}
			}
			if (!allow_access) {
				String lid =  resource.getId();
				return new ModelAndView("redirect:/protected_player?lid=" + lid);
			}
			break;
		}

		//Languages
		String[] langList = optionServices.getLanguages(locale);
		model.addAttribute("langList",langList);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());
		model.addAttribute("page", "live_player");

		if (allow_access) {
			this.setSlidesAttribute(model, resource);
			model.addAttribute("resourse_identifier", resource.getId());
			model.addAttribute("video_url", resourceUtils.getLiveVideoUrlOfResource(resource));
			model.addAttribute("ccLicense", resourceUtils.getLicenseOfResource(resource,locale.getLanguage()));
			model.addAttribute("media_base_path", "");
			model.addAttribute("resource", resource);
			model.addAttribute("seconds_to_end", getSecondsToLiveEndInSeconds(resource));

			return new ModelAndView("live_player");
		}
		else {
			return new ModelAndView("403");
		}
	}

	@GetMapping(value = {"/nobroadcastpage"})
	public String getNoBroadcastPage (final Model model, @RequestParam(value = "code", required = false) String code) throws IOException {

		if (code != null && !code.isEmpty()) {
			Classroom classroom = classroomService.findByCode(code);
			if (classroom != null) {
				model.addAttribute("classroomName", classroom.getName());
			}
		}
		 else {
				model.addAttribute("classroomName", "INVALID_CLASSROOM");
		 }
		 return "nobroadcastpage";
	}

	@GetMapping(value = {"/protected_player"})
	public String getProtectedPage (final ModelMap model,@RequestParam(value = "lid") String lid, @RequestParam(value = "reason", required = false) String reason) throws IOException {

		 model.remove("lid");
		 model.addAttribute("reason",reason);
		 model.addAttribute("lid",lid);

		 return "confirm_access";
	}

	@PostMapping(value = {"/protected_player"})
	public ModelAndView getProtectedPage (final ModelMap model,@ModelAttribute("lid") String lid,
										  @RequestParam(value = "password", required = false) String pass,
										  HttpServletRequest request) throws IOException {

		Resource resource;
		String reason="";
		resource = resourceService.findByIdInCollection(lid, "Scheduler.Live");
		if (resource != null) {
			if (resource.getBroadcastCode() != null && resource.getBroadcastCode().equals(pass)) {
				 request.getSession().setAttribute("allow_access_to", lid);
				 return new ModelAndView("redirect:/live_player?id=" + lid);
			}
			else {
				 reason = "WRONG_PASSWORD";
			}
		}
		else {
			  reason = "NO_LIVE_FOUND";
		}
		return new ModelAndView("redirect:/protected_player?lid=" + lid + "&reason=" + reason);
	}

	private void setSlidesAttribute(ModelMap model, Resource resource) {

		boolean hasSlides 	 = false;
		boolean slidesSynced = false;
		List<Slide> slideList = new ArrayList<>();
		if (resource.getPresentation() != null && resource.getPresentation().getSlides() != null && !resource.getPresentation().getSlides().isEmpty()) {
			hasSlides = true;
			for (Slide slide : resource.getPresentation().getSlides()) {
				if (!slide.getTime().equals("-1")) {
					slidesSynced = true;
					break;
				}
			}
			if (slidesSynced) {
				for (Slide slide : resource.getPresentation().getSlides()) {
					String[] slide_times = slide.getTime().split(",");
					if (!slide.getTime().equals("-1")) {
						for (String time : slide_times) {
							Slide split_slide = new Slide();
							split_slide.setTime(time);
							split_slide.setTitle(slide.getTitle());
							split_slide.setUrl(slide.getUrl());
							slideList.add(split_slide);
						}
					}
				}
				slideList.sort(Comparator.comparing(Slide::getTime));
				// set slides index
				int index = 0;
				for (Slide slide: slideList) {
					slide.setIndex(index);
					index++;
				}
				resource.getPresentation().setSlides(slideList);
			}//< if at least one slide is synced --> filter out not synced
		}
		model.addAttribute("slidesSynced", slidesSynced);
		model.addAttribute("hasSlides", hasSlides);
	}

	private long getSecondsToLiveEndInSeconds(Resource resource) {

		long time_to_end_seconds;

		Instant resource_StartTime = resource.getDate();
		long duration_hours = Long.parseLong(resource.getRealDuration().substring(0,2));
		long duration_mins = Long.parseLong(resource.getRealDuration().substring(3,5));
		Instant resource_EndTime = resource_StartTime.plus(duration_hours, ChronoUnit.HOURS).plus(duration_mins,ChronoUnit.MINUTES);
		Instant time_now = ZonedDateTime.now().toInstant().truncatedTo(ChronoUnit.SECONDS);
		Duration res = Duration.between(time_now, resource_EndTime);
		time_to_end_seconds = res.getSeconds();
		return time_to_end_seconds;
	}
}
