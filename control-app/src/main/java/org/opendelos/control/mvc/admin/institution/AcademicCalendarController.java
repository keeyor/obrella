package org.opendelos.control.mvc.admin.institution;

import java.util.List;
import java.util.Locale;

import org.opendelos.control.services.i18n.MultilingualServices;
import org.opendelos.control.services.structure.SchoolService;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.School;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class AcademicCalendarController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_institution";
	}

	@Value("${default.institution.identity}")
	String institution_identity;

	@Autowired
	Institution defaultInstitution;

	@ModelAttribute("mInstitution")
	private Institution getInstitution()  {
		return  defaultInstitution;
	}

	private final SchoolService schoolService;
	private final MultilingualServices multilingualServices;

	public AcademicCalendarController(SchoolService schoolService, MultilingualServices multilingualServices) {
		this.schoolService = schoolService;
		this.multilingualServices = multilingualServices;
	}


	@RequestMapping(value = "admin/institution/acalendar",   method = RequestMethod.GET)
	public String getCurrentPeriod(final Model model, Locale locale) throws Exception {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		model.addAttribute("institutionName", multilingualServices.getValue("default.institution.title",locale));

		List<School> schools =  schoolService.findAllSortedByTitle();
		model.addAttribute("schools",schools);
		model.addAttribute("page", "calendar");

		return "admin/institution/academicCalendar";
    }

}
