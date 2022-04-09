/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.admin.system;

import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class StreamingServersController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_system";
	}

	@Value("${default.institution.identity}")
	String institution_identity;

	@GetMapping(value = {"admin/system/streamers", "admin/system/streamers/"})
	public String getDepartmentCoursesPanel(final Model model) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		model.addAttribute("institution_identity",institution_identity);
		model.addAttribute("page", "streamer");

		return "admin/system/streamers";
	}
}
