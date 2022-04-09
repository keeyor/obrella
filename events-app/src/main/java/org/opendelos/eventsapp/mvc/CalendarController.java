/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.eventsapp.mvc;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.eventsapp.services.i18n.OptionServices;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.common.QueryFilter;
import org.opendelos.eventsapp.services.i18n.MultilingualServices;
import org.opendelos.eventsapp.services.opUser.OpUserService;
import org.opendelos.eventsapp.services.scheduledEvent.ScheduledEventService;
import org.opendelos.eventsapp.services.structure.ClassroomService;
import org.opendelos.eventsapp.services.structure.CourseService;
import org.opendelos.eventsapp.services.structure.DepartmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class CalendarController {

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
	private final ScheduledEventService scheduledEventService;
	private final ClassroomService classroomService;
	private final OptionServices optionServices;

	@Autowired
	public CalendarController(MultilingualServices multilingualServices, DepartmentService departmentService, CourseService courseService, OpUserService opUserService, ScheduledEventService scheduledEventService, ClassroomService classroomService, OptionServices optionServices) {
		this.courseService = courseService;
		this.multilingualServices = multilingualServices;
		this.departmentService = departmentService;
		this.opUserService = opUserService;
		this.scheduledEventService = scheduledEventService;
		this.classroomService = classroomService;
		this.optionServices = optionServices;
	}
	@GetMapping(value = {"calendar"})
	public String getWeekSchedule(final Model model, Locale locale, HttpServletRequest request,
			@RequestParam(value = "d", required = false) String d,     // Department
			@RequestParam(value = "rt", required = false) String rt,   // ResourceType
			@RequestParam(value = "c", required = false) String c,     // Course
			@RequestParam(value = "cr", required = false) String cr,     // Classroom
			@RequestParam(value = "e", required = false) String e,     // Event
			@RequestParam(value = "s", required = false) String s,     // Staff Member
			@RequestParam(value = "p", required = false) String p,     // Period  (semester)
			@RequestParam(value = "y", required = false) String y,    // Academic Year
			@RequestParam(value = "cv", required = false) String cv,   // View
			@RequestParam(value = "sd", required = false) String sd,   // View
			@RequestParam(value = "ed", required = false) String ed   // View
			) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		String queryString = request.getQueryString();
		model.addAttribute("queryString",queryString);

		model.addAttribute("institution_identity",institution_identity);
		model.addAttribute("institutionName", multilingualServices.getValue("default.institution.title",locale));

		QueryFilter departmentFilter = new QueryFilter();
		if (d != null && !d.equals("")) {
			Department department = departmentService.findById(d);
			departmentFilter.setId(department.getId());
			departmentFilter.setText(department.getTitle());
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
		QueryFilter scheduledEventFilter = new QueryFilter();
		if (e != null && !e.equals("")) {
			ScheduledEvent scheduledEvent = scheduledEventService.findById(e);
			scheduledEventFilter.setId(scheduledEvent.getId());
			scheduledEventFilter.setText(scheduledEvent.getTitle());
		}
		QueryFilter classroomFilter = new QueryFilter();
		if (cr != null && !cr.equals("")) {
			Classroom classroom = classroomService.findById(cr);
			classroomFilter.setId(classroom.getId());
			classroomFilter.setText(classroom.getName());
		}


		model.addAttribute("classRoomFilter", classroomFilter);
		model.addAttribute("departmentFilter", departmentFilter);
		model.addAttribute("courseFilter", courseFilter);
		model.addAttribute("staffMemberFilter", staffMemberFilter);
		model.addAttribute("scheduledEventFilter", scheduledEventFilter);

		model.addAttribute("currentAcademicYear" , currentAcademicYear);

		model.addAttribute("page", "calendar");
		model.addAttribute("color", "green");

		LocalDate today = LocalDate.now();
		LocalDate _30days = today.plus(30,ChronoUnit.DAYS);
		model.addAttribute("today", today);
		model.addAttribute("_30days",_30days);

		//Languages
		String[] langList = optionServices.getLanguages(locale);
		model.addAttribute("langList",langList);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		model.addAttribute("view",cv);
		model.addAttribute("sd",sd);
		model.addAttribute("ed",ed);

		return "calendar";
	}
}
