/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.scheduler;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.control.services.i18n.MultilingualServices;
import org.opendelos.control.services.scheduler.ScheduleUtils;
import org.opendelos.control.services.structure.DepartmentService;
import org.opendelos.control.services.structure.SchoolService;
import org.opendelos.model.calendar.Period;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.School;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class TimetableSmController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_scheduler";
	}

	@Value("${default.institution.identity}")
	String institution_identity;

	@Autowired
	Institution defaultInstitution;

	@ModelAttribute("mInstitution")
	private Institution getInstitution()  {
		return  defaultInstitution;
	}

	@Autowired
	String currentAcademicYear;

	private final DepartmentService departmentService;
	private final SchoolService schoolService;
	private final MultilingualServices multilingualServices;
	private final ScheduleUtils scheduleUtils;

	public TimetableSmController(DepartmentService departmentService, SchoolService schoolService, MultilingualServices multilingualServices, ScheduleUtils scheduleUtils) {
		this.departmentService = departmentService;
		this.schoolService = schoolService;
		this.multilingualServices = multilingualServices;
		this.scheduleUtils = scheduleUtils;
	}

	@GetMapping(value = {"admin/scheduler/sm/timetable", "admin/scheduler/sm/timetable/"})
	public String getAdminControlPanel(final Model model, Locale locale, HttpServletRequest request) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		boolean isEditorStaffMember = editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"));
		model.addAttribute("isEditorStaffMember", isEditorStaffMember);

		String currentPeriod = scheduleUtils.getPeriodByDate(defaultInstitution.getId(),currentAcademicYear, LocalDate.now());
		model.addAttribute("currentPeriod", currentPeriod);

		model.addAttribute("institutionName", multilingualServices.getValue("default.institution.title",locale));

		List<School> schools =  schoolService.findAllSortedByTitle();
		model.addAttribute("schools",schools);

		String timetable_search_history = (String) request.getSession().getAttribute("timetable_search_history");
		if (timetable_search_history != null) {
			model.addAttribute("timetable_search_history", timetable_search_history);
		}
		else {
			model.addAttribute("timetable_search_history", "");
		}
		String timetable_events_search_history = (String) request.getSession().getAttribute("timetable_events_search_history");
		if (timetable_events_search_history != null) {
			model.addAttribute("timetable_events_search_history", timetable_events_search_history);
		}
		else {
			model.addAttribute("timetable_events_search_history", "");
		}

		//Periods
		HashMap<String,String> periods_hash = new HashMap<>();
		String[] periods = multilingualServices.getValue("Period.keys",locale).split(",");
		for (String period : periods) {
			periods_hash.put(period, multilingualServices.getValue(period, locale));
		}


		Period period = scheduleUtils.getDepartmentPeriodByDate(editor.getDepartmentId(),currentAcademicYear,LocalDate.now());
		model.addAttribute("current_period", period.getName());
		model.addAttribute("currentAcademicYear", currentAcademicYear);

		Department department = departmentService.findById(editor.getDepartmentId());
		model.addAttribute("editor_deparment_id", department.getId());
		model.addAttribute("editor_department_title",department.getTitle(locale));


		model.addAttribute("pdList", periods_hash);
		model.addAttribute("page", "timetable");

		return "admin/scheduler/sm/timetable";
	}



}
