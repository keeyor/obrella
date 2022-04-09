/* 
     Author: Michael Gatzonis - 7/12/2020 
     live
*/
package org.opendelos.control.mvc.admin.content;

import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VideoSyncController {

	private final ResourceService resourceService;
	@Autowired
	public VideoSyncController(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	@GetMapping(value = {"admin/video-sync" ,"admin/video-sync/"})
	public String lectureEditor(final Model model, @RequestParam(value = "id",  required = false) String id)  {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);
		Resource resource = resourceService.findById(id);
		model.addAttribute("Resource", resource);

		return "admin/video-sync/video-sync";

	}

}
