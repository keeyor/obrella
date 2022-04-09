/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.admin.institution;

import org.opendelos.model.users.OoUserDetails;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class ClassroomsController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_institution";
	}

	@GetMapping(value = {"admin/institution/classrooms", "admin/institution/classrooms/"})
	public String getAdminControlPanel(final Model model) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);
		model.addAttribute("page", "classroom");

		return "admin/institution/classrooms";
	}

}
