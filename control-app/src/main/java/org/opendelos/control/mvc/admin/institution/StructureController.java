/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.admin.institution;

import java.util.Locale;

import org.opendelos.control.services.i18n.OptionServices;
import org.opendelos.control.conf.LmsProperties;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class StructureController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_institution";
	}

	@Value("${default.institution.identity}")
	String institution_identity;

	private final OptionServices optionServices;
	private final LmsProperties lmsProperties;

	@Autowired
	public StructureController(OptionServices optionServices, LmsProperties lmsProperties) {
		this.optionServices = optionServices;
		this.lmsProperties = lmsProperties;
	}

	@GetMapping(value = {"admin/institution/structure", "admin/institution/structure/"})
	public String getAdminControlPanel(final Model model, Locale locale) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		model.addAttribute("institution_identity",institution_identity);
		//Licenses
		String[] licenseList = optionServices.getLicenses(locale);
		model.addAttribute("licenseList", licenseList);

		//LMSs'
		model.addAttribute("lms", lmsProperties);
		model.addAttribute("page", "structure");

		return "admin/institution/structure";
	}
}
