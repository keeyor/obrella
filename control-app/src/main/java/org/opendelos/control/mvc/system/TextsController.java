/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.system;

import java.time.LocalDateTime;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Slf4j
@Controller
public class TextsController {


	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_system";
	}

	@Autowired
	Institution defaultInstitution;

	@GetMapping(value = "admin/system/texts")
	public String getHomePage(final Model model) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		model.addAttribute("page","texts");
		model.addAttribute("Institution", defaultInstitution);

		return "admin/system/texts";
	}

}
