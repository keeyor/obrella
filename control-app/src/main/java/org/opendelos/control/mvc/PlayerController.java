/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.control.services.resource.ResourceUtils;
import org.opendelos.control.services.structure.ClassroomService;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.Slide;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PlayerController {

	private final ResourceService resourceService;
	private final ClassroomService classroomService;
	private final ResourceUtils resourceUtils;

	@Autowired
	public PlayerController(ResourceService resourceService, ClassroomService classroomService, ResourceUtils resourceUtils) {
		this.resourceService = resourceService;
		this.classroomService = classroomService;
		this.resourceUtils = resourceUtils;
	}


	@GetMapping(value = "/player")
	public String playVideo (final Model model,
		@RequestParam(value = "rid", required = false) String identity,
		@RequestParam(value = "id", required = false) String id) {


		Resource resource = null;
		if (id != null && !id.isEmpty()) {
			resource = resourceService.updateViewsAndGetById(id); //resourceService.findById(id);
		}
		else if (identity != null && !identity.isEmpty()) {
			resource = resourceService.updateViewsAndGetByIdentity(identity);
		}
		if (resource == null) {
			return "redirect:404";
		}

		//Security Restrictions. Maybe Elaborate!
		Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!(user instanceof  OoUserDetails) && !resource.getAccessPolicy().equals("public")) {
			return "redirect:403";
		}

		this.setSlidesAttribute(model,resource);
		this.setResourceIdentifier(model,resource);

		model.addAttribute("video_url",resourceUtils.getVideoUrlOfResource(resource));
		model.addAttribute("ccLicense", resourceUtils.getLicenseOfResource(resource));
		model.addAttribute("media_base_path", resourceUtils.getMediaBasePathOfResource(resource));
		model.addAttribute("resource",resource);


		/* find related parts */
		String rel_parts_access = "public";
		if ((user instanceof  OoUserDetails)) {  // is Authenticated
			rel_parts_access = null;
		}

		List<Resource> related_parts = new ArrayList<>();
		if (resource.isParts()) {
			if (resource.getType().equals("COURSE")) {
				if (id != null) {
					related_parts = resourceService.findRelatedCourseResources(resource,rel_parts_access);
				}
			}
			else if  (resource.getType().equals("EVENT")) {
				related_parts = resourceService.findRelatedEventResourcesByEventId(resource.getEvent().getId(),rel_parts_access);
				for (Resource part: related_parts) {
					String classroomId = part.getClassroom();
					if (classroomId != null) {
						Classroom classroom = classroomService.findById(classroomId);
						part.setClassroomName(classroom.getName());
					}
				}
			}
		}
		model.addAttribute("related_parts",related_parts);

		return "player";
	}

	private void setSlidesAttribute(Model model, Resource resource) {

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

	private void setResourceIdentifier(Model model, Resource resource) {
		model.addAttribute("resourse_identifier", resource.getIdentity());// Id or Identity
	}
}
