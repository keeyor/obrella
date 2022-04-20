/* 
     Author: Michael Gatzonis - 19/2/2021 
     live
*/
package org.opendelos.control.mvc.system;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.model.structure.Institution;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SystemUsersController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_system";
	}

	@Autowired
	Institution defaultInstitution;

	@GetMapping(value = {"admin/system/users", "admin/system/users/"})
	public String getAdminControlPanel(final Model model, HttpServletRequest request, @RequestParam(value = "t",  required = false) String type) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		if (request.getQueryString() != null) {
			request.getSession().setAttribute("userlist_search_history", "users" + "?" + request.getQueryString());
		}
		else {
			request.getSession().setAttribute("userlist_search_history", "users");
		}

		model.addAttribute("userType",type);
		model.addAttribute("page", "user");
		model.addAttribute("institution_identity", defaultInstitution.getId());

		return "admin/system/users";
	}
}
