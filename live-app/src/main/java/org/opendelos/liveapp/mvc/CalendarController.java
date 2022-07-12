/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.liveapp.mvc;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.opendelos.liveapp.services.i18n.MultilingualServices;
import org.opendelos.liveapp.services.i18n.OptionServices;
import org.opendelos.liveapp.services.opUser.OpUserService;
import org.opendelos.liveapp.services.scheduler.ScheduleService;
import org.opendelos.liveapp.services.structure.CourseService;
import org.opendelos.liveapp.services.structure.DepartmentService;
import org.opendelos.model.common.QueryFilter;
import org.opendelos.model.common.Select2GenGroup;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class CalendarController {

	@Value("${app.zone}")
	String app_zone;

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

	private final MultilingualServices multilingualServices;
	private final DepartmentService departmentService;
	private final CourseService courseService;
	private final OpUserService opUserService;
	private final OptionServices optionServices;
	private final ScheduleService scheduleService;

	@Autowired
	public CalendarController(MultilingualServices multilingualServices, DepartmentService departmentService, CourseService courseService, OpUserService opUserService, OptionServices optionServices, ScheduleService scheduleService) {
		this.courseService = courseService;
		this.multilingualServices = multilingualServices;
		this.departmentService = departmentService;
		this.opUserService = opUserService;
		this.optionServices = optionServices;
		this.scheduleService = scheduleService;
	}
	@GetMapping(value = {"calendar"})
	public String getWeekSchedule(final Model model, Locale locale, HttpServletRequest request,
			@RequestParam(value = "d", required = false) String d,     // Department
			@RequestParam(value = "c", required = false) String c,     // Course
			@RequestParam(value = "s", required = false) String s,     // Staff Member
			@RequestParam(value = "view", required = false, defaultValue = "listMonth") String cv,   // View
			@RequestParam(value = "sd", required = false) String sd,   // View
			@RequestParam(value = "ed", required = false) String ed,   // View
			@RequestParam(value = "cld", required = false) String cld  // Clear Department Filter - from session too
			) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		String queryString = request.getQueryString();
		model.addAttribute("queryString",queryString);

		model.addAttribute("institution_identity",institution_identity);
		model.addAttribute("institutionName", multilingualServices.getValue("default.institution.title",locale));

		HttpSession session = request.getSession();
		QueryFilter departmentFilter = new QueryFilter();
		if (d != null && !d.equals("")) {
			Department department = departmentService.findById(d);
			departmentFilter.setId(department.getId());
			departmentFilter.setText(department.getTitle());
			session.setAttribute("user_dp", department.getId());
		}
		else if (cld == null){
			String user_dp = (String) session.getAttribute("user_dp");
			if (user_dp != null) {
				Department department = departmentService.findById(user_dp);
				departmentFilter.setId(user_dp);
				departmentFilter.setText(department.getTitle());
			}
		}
		else {
			session.removeAttribute("user_dp");
		}

		QueryFilter courseFilter = new QueryFilter();
		if (c != null && !c.equals("")) {
			Course course = courseService.findById(c);
			courseFilter.setId(course.getId());
			courseFilter.setText(course.getTitle());
		}
		QueryFilter staffMemberFilter = new QueryFilter();
		if (s != null && !s.equals("")) {
			OpUser opUser = opUserService.findById(s);
			staffMemberFilter.setId(opUser.getId());
			staffMemberFilter.setText(opUser.getName());
		}

		model.addAttribute("departmentFilter", departmentFilter);
		model.addAttribute("courseFilter", courseFilter);
		model.addAttribute("staffMemberFilter", staffMemberFilter);

		model.addAttribute("currentAcademicYear" , currentAcademicYear);

		model.addAttribute("page", "live");
		model.addAttribute("color", "green");

		//Languages
		String[] langList = optionServices.getLanguages(locale);
		model.addAttribute("langList",langList);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());


		LocalDate today = LocalDate.now();
		LocalDate _30days = today.plus(30,ChronoUnit.DAYS);
		model.addAttribute("today", today);
		model.addAttribute("_30days",_30days);

		model.addAttribute("view",cv);
		model.addAttribute("sd",sd);
		model.addAttribute("ed",ed);

		List<Select2GenGroup> departmentList = departmentService.getAllDepartmentsGroupedBySchool("dummy", locale);
		model.addAttribute("departmentList",departmentList);

		if (scheduleService.read_liveDaemonStatus())
			return "calendar";
		else
			return "offline";
	}
}
