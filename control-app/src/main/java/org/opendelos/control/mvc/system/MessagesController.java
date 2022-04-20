/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.system;

import java.util.Locale;

import org.opendelos.model.structure.Institution;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MessagesController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_system";
	}

	@Autowired
	Institution defaultInstitution;

	@GetMapping(value = "admin/system/messages")
	public String getHomePage(final Model model) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);
		model.addAttribute("page","message");

		model.addAttribute("Institution", defaultInstitution);
		return "admin/system/messages";
	}

}
