/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.vodapp.mvc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.Slide;
import org.opendelos.model.security.TokenInfo;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.vodapp.security.token.TokenAuthService;
import org.opendelos.vodapp.services.resource.ResourceService;
import org.opendelos.vodapp.services.resource.ResourceUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PlayerController {

	private final ResourceService resourceService;
	private final ResourceUtils resourceUtils;
	private final TokenAuthService tokenAuthService;

	@Autowired
	public PlayerController(ResourceService resourceService, ResourceUtils resourceUtils, TokenAuthService tokenAuthService) {
		this.resourceService = resourceService;
		this.resourceUtils = resourceUtils;
		this.tokenAuthService = tokenAuthService;
	}


	@GetMapping(value = "/player")
	public String playVideo(final Model model, Locale locale,
			@RequestParam(value = "rid", required = false) String identity,
			@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "token", required = false) String jwtToken) {


		Resource resource = null;
		if (id != null && !id.isEmpty()) {
			resource = resourceService.findById(id);
		}
		else if (identity != null && !identity.isEmpty()) {
			resource = resourceService.findByIdentity(identity);
		}
		if (resource == null && identity != null) {
			if (identity.lastIndexOf(".") != -1) {
				identity = identity.substring(0,identity.lastIndexOf(".")).trim();
			}
			resource = resourceService.findById(identity);
		}
		if (resource == null || resource.getAccessPolicy() == null) {
			return "redirect:404";
		}
		String resourceAccessPolicy = resource.getAccessPolicy();

		//Require jwtToken to play "PRIVATE" videos
		if (resourceAccessPolicy.equals("PRIVATE")) {
			if (jwtToken == null) {
				return "redirect:403";
			}
			TokenInfo tokenInfo = tokenAuthService.loadTokenDetails(jwtToken,"PUBLIC");
			if (tokenInfo.getDomainName() == null) {
				return "redirect:403";
			}
			if (!tokenInfo.getRId().equals(identity)) {
				return "redirect:403";
			}
		}
		resourceService.updateViews(resource);

		this.setSlidesAttribute(model, resource);
		this.setResourceIdentifier(model, resource);

		model.addAttribute("video_url", resourceUtils.getVideoUrlOfResource(resource));
		model.addAttribute("ccLicense", resourceUtils.getLicenseOfResource(resource, locale.getLanguage()));
		model.addAttribute("media_base_path", resourceUtils.getMediaBasePathOfResource(resource));
		model.addAttribute("resource", resource);


		/* find related parts */
		List<Resource> related_parts = new ArrayList<>();

		//Do not search for related resources when PRIVATE video is requested (aka requires jwtToken)
		if (resourceAccessPolicy.equals("PUBLIC") && resource.isParts()) {
			if (resource.getType().equals("COURSE")) {
				if (id != null) {
					related_parts = resourceService.findRelatedCourseResources(resource, "public");
				}
			}
		}

		model.addAttribute("related_parts", related_parts);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		model.addAttribute("page", "player");

		return "player";
	}

	private void setSlidesAttribute(Model model, Resource resource) {

		boolean hasSlides = false;
		boolean slidesSynced = false;
		List<Slide> slideList = new ArrayList<>();
		if (resource.getPresentation() != null && resource.getPresentation()
				.getSlides() != null && !resource.getPresentation().getSlides().isEmpty()) {
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
				for (Slide slide : slideList) {
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
