/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.control.api.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendelos.control.api.common.ApiUtils;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.control.services.scheduler.ScheduleService;
import org.opendelos.control.services.structure.CourseService;
import org.opendelos.control.services.structure.DepartmentService;
import org.opendelos.control.services.structure.SchoolService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.ScheduleDTO;
import org.opendelos.model.scheduler.ScheduleQuery;
import org.opendelos.model.scheduler.SchedulerFullCalendarObject;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.School;
import org.opendelos.model.users.OoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimeTableApi {

	private final Logger logger = LoggerFactory.getLogger(TimeTableApi.class);

	private final ScheduleService scheduleService;
	private final DepartmentService departmentService;
	private final SchoolService schoolService;
	private final OpUserService opUserService;
	private final CourseService courseService;

	@Value("${app.zone}")
	String app_zone;

	@Autowired
	public TimeTableApi(ScheduleService scheduleService, DepartmentService departmentService, SchoolService schoolService, OpUserService opUserService, CourseService courseService) {
		this.scheduleService = scheduleService;
		this.departmentService = departmentService;
		this.schoolService = schoolService;
		this.opUserService = opUserService;
		this.courseService = courseService;
	}

	@RequestMapping(value= "/api/v1/timetable/dt/authorized/{access}", method = RequestMethod.POST, produces =  "application/json")
	public byte[] SearchTimeTableByEditor(@RequestBody ScheduleQuery scheduleQuery, @PathVariable String access, HttpServletRequest request) throws JsonProcessingException {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<ScheduleDTO> scheduleDTOS = new ArrayList<>();
		//Save Query Params in Session
		//Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		//Converting the Object to JSONString

		//School filter differs cause we cannot get the title from the page: so fill it in session store
		if (scheduleQuery.getSchoolId() != null && !scheduleQuery.getSchoolId().equals("_all")) {
			School school = schoolService.findById(scheduleQuery.getSchoolId());
			scheduleQuery.setSchoolTitle(school.getTitle());
		}
		//Department filter differs cause we cannot get the title from the page: so fill it in session store
		if (scheduleQuery.getDepartmentId() != null && !scheduleQuery.getDepartmentId().equals("_all")) {
			Department department = departmentService.findById(scheduleQuery.getDepartmentId());
			scheduleQuery.setDepartmentTitle(department.getTitle());
		}
		//Supervisor Name
		if (scheduleQuery.getSupervisorId() != null && !scheduleQuery.getSupervisorId().equals("_all")) {
			OpUser opUser = opUserService.findById(scheduleQuery.getSupervisorId());
			scheduleQuery.setSupervisorName(opUser.getName());
		}
		//Course title
		if (scheduleQuery.getCourseId() != null && !scheduleQuery.getCourseId().equals("_all")) {
			Course course = courseService.findById(scheduleQuery.getCourseId());
			scheduleQuery.setCourseTitle(course.getTitle());
		}
		if (scheduleQuery.getType() != null) {
			if (scheduleQuery.getType().equals("lecture")) {
				String timetable_search_filters = mapper.writeValueAsString(scheduleQuery);
				if (timetable_search_filters != null) {
					request.getSession().setAttribute("timetable_search_history", timetable_search_filters);
				}
			}
			else if (scheduleQuery.getType().equals("event")) {
				String timetable_event_search_filters = mapper.writeValueAsString(scheduleQuery);
				if (timetable_event_search_filters != null) {
					request.getSession().setAttribute("timetable_events_search_history", timetable_event_search_filters);
				}
			}
		}
		else {
			request.getSession().setAttribute("timetable_search_history", "");
			request.getSession().setAttribute("timetable_events_search_history", "");
		}

		List<Schedule> scheduleList = scheduleService.searchScheduleByEditor(scheduleQuery, editor, access);

		for (Schedule schedule: scheduleList) {
			ScheduleDTO scheduleDTO = scheduleService.getScheduleDTO(schedule);
			if (scheduleDTO == null) {
				logger.error("Error creating DTO ScheduledDTO. Not Adding...");
			}
			else {
				scheduleDTOS.add(scheduleDTO);
			}
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(scheduleDTOS);
		return b;
	}

	//> scheduleQuery should contain fromDate+toDate
	@RequestMapping(value= "/api/v1/timetable_daterange/dt", method = RequestMethod.POST, produces =  "application/json")
	public byte[] SearchTimeTableInDataRange(@RequestBody ScheduleQuery scheduleQuery) throws JsonProcessingException {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<ScheduleDTO> scheduleDTOList = new ArrayList<>();
		if (scheduleQuery.getFromDate() != null && scheduleQuery.getToDate() != null) {
			    if (scheduleQuery.getSupervisorId() == null || scheduleQuery.getSupervisorId().equals("_all")) {
			    	scheduleQuery.setSupervisorId(null);
					scheduleDTOList.addAll(scheduleService.computeScheduleInDateRange(scheduleQuery));
				}
				else {
					scheduleDTOList.addAll(scheduleService
							.computeScheduleInDateRangeByEditor(scheduleQuery, editor, "scheduler"));
				}
		}

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(scheduleDTOList);
		return b;
	}

	@RequestMapping(value= "/api/v1/timetable_daterange/users/dt", method = RequestMethod.POST, produces =  "application/json")
	public byte[] SearchTimeTableInDataRangeForUsers(@RequestBody ScheduleQuery scheduleQuery) throws JsonProcessingException {

		List<ScheduleDTO> scheduleDTOList = new ArrayList<>();
		if (scheduleQuery.getFromDate() != null && scheduleQuery.getToDate() != null) {
				scheduleDTOList.addAll(scheduleService.computeScheduleInDateRange(scheduleQuery));
		}

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(scheduleDTOList);
		return b;
	}

	@RequestMapping(value= "/api/v1/timetable_daterange/users/cal", method = RequestMethod.GET, produces =  "application/json")
	public byte[] SearchTimeTableInDataRangeForUsersCalendar(String start, String end, String d, String c, String e, String s, String cr, String rt) throws JsonProcessingException {

		ScheduleQuery scheduleQuery = new ScheduleQuery();
		LocalDate fromDate = LocalDate.parse(start.substring(0,10));
		LocalDate endDate = LocalDate.parse(end.substring(0,10));
		int start_month = fromDate.getMonthValue(); //1 to 12
		int end_month = endDate.getMonthValue(); //1 to 12

		String academicYear = String.valueOf(fromDate.getYear());

		if (start_month < 9 && end_month >=9) {
			//Calculate dates among two different academic years
			academicYear = (fromDate.getYear()-1) + "-" + academicYear;
		}
		if (start_month < 9 && end_month < 9) {
			//subtract 1 from current Academic Year
			academicYear = String.valueOf(fromDate.getYear()-1);
		}

		scheduleQuery.setYear(academicYear);
		scheduleQuery.setFromDate(fromDate);
		scheduleQuery.setToDate(endDate);
		if (rt != null && !rt.equals("") && !rt.equals("_")) {
			scheduleQuery.setType(rt);
		}
		if (d != null && !d.equals("") && !d.equals("_")) {
			scheduleQuery.setDepartmentId(d);
		}
		if (c != null && !c.equals("") && !c.equals("_")) {
			List<String> courseIds = new ArrayList<>();
			courseIds.add(c);
			scheduleQuery.setRestrictedCourseIds(courseIds);
		}
		if (e != null && !e.equals("") && !e.equals("_")) {
			List<String> scheduledEventIds = new ArrayList<>();
			scheduledEventIds.add(e);
			scheduleQuery.setRestrictedEventIds(scheduledEventIds);
		}
		if (s != null && !s.equals("") && !s.equals("_")) {
			scheduleQuery.setSupervisorId(s);
		}
		if (cr != null && !cr.equals("") && !cr.equals("_")) {
			scheduleQuery.setClassroomId(cr);
		}
		logger.trace("Fetch Schedule from:" + scheduleQuery.getFromDate() + " to: " + scheduleQuery.getToDate() + " year: " + academicYear);
		logger.trace("Fetch Schedule params: d=" + d + " c=" + c + " e=" + e + " s=" + s + " cr=" + cr);

		List<ScheduleDTO> scheduleDTOList = new ArrayList<>();
		if (scheduleQuery.getFromDate() != null && scheduleQuery.getToDate() != null) {
			scheduleDTOList.addAll(scheduleService.computeScheduleInDateRange(scheduleQuery));
		}
		List<SchedulerFullCalendarObject> schedulerFullCalendarResults = new ArrayList<>();
		for (ScheduleDTO scheduleDTO: scheduleDTOList) {
			SchedulerFullCalendarObject schedulerFullCalendarObject = new SchedulerFullCalendarObject();

			if (scheduleDTO.getType().equals("lecture")) {
				String scheduledEventTitle = scheduleDTO.getCourse().getTitle() +
											 " - Τμήμα " + scheduleDTO.getDepartment().getTitle() + " - " + scheduleDTO.getSupervisor().getName();
				schedulerFullCalendarObject.setTitle(scheduledEventTitle);
				schedulerFullCalendarObject.setResourceId(scheduleDTO.getCourse().getId());
				schedulerFullCalendarObject.setBackgroundColor("royalblue");
				schedulerFullCalendarObject.setBorderColor("royalblue");
				if (scheduleDTO.getRepeat().equals("onetime")) {
					schedulerFullCalendarObject.setBackgroundColor("rgb(255, 178, 102)");
					schedulerFullCalendarObject.setBorderColor("rgb(255, 178, 102)");
				}
			}
			else {
				schedulerFullCalendarObject.setTitle(scheduleDTO.getScheduledEvent().getTitle());
				schedulerFullCalendarObject.setResourceId(scheduleDTO.getScheduledEvent().getId());
				schedulerFullCalendarObject.setBackgroundColor("rgb(212, 96, 137)");
				schedulerFullCalendarObject.setBorderColor("rgb(212, 96, 137)");
			}
			schedulerFullCalendarObject.setTextColor("white");
			// Set Extended Properties
			SchedulerFullCalendarObject.ExtendedProps extendedProps = new SchedulerFullCalendarObject.ExtendedProps();
			if (scheduleDTO.getDepartment() != null) {
				extendedProps.setDepartment(scheduleDTO.getDepartment().getTitle());
				extendedProps.setDepartmentId(scheduleDTO.getDepartment().getId());
			}
			if (scheduleDTO.getSupervisor() != null) {
				extendedProps.setSupervisor(scheduleDTO.getSupervisor().getName());
				extendedProps.setSupervisorId(scheduleDTO.getSupervisor().getId());
			}
			extendedProps.setType(scheduleDTO.getType());
			extendedProps.setRepeat(scheduleDTO.getRepeat());
			extendedProps.setClassroomName(scheduleDTO.getClassroom().getName());
			extendedProps.setClassroomId(scheduleDTO.getClassroom().getId());
			extendedProps.setBroadcast(scheduleDTO.isBroadcast());
			extendedProps.setRecording(scheduleDTO.isRecording());
			extendedProps.setAccess(scheduleDTO.getAccess());
			extendedProps.setPublication(scheduleDTO.getPublication());


			schedulerFullCalendarObject.setExtendedProps(extendedProps);


			LocalDate date = LocalDate.parse(scheduleDTO.getDate());
			LocalTime startTime = LocalTime.parse(scheduleDTO.getStartTime());
			LocalDateTime startDateTime = date.atTime(startTime);
			long lDurationHours = scheduleDTO.getDurationHours();
			long lDurationMinutes = scheduleDTO.getDurationMinutes();
			LocalDateTime endDateTime = startDateTime.plus(lDurationHours, ChronoUnit.HOURS).plus(lDurationMinutes,ChronoUnit.MINUTES);

			schedulerFullCalendarObject.setStart(startDateTime.toString());
			schedulerFullCalendarObject.setEnd(endDateTime.toString());
			schedulerFullCalendarResults.add(schedulerFullCalendarObject);
		}
		byte[] b;
		b = ApiUtils.TransformResultsForCalendar(schedulerFullCalendarResults);
		return b;
	}

}
