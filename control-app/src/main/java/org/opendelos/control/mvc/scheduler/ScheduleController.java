/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.scheduler;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.google.api.client.auth.oauth2.Credential;
import org.opendelos.control.services.i18n.MultilingualServices;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.control.services.scheduledEvent.ScheduledEventService;
import org.opendelos.control.services.scheduler.ScheduleService;
import org.opendelos.control.services.scheduler.ScheduleUtils;
import org.opendelos.control.services.structure.CourseService;
import org.opendelos.control.services.structure.InstitutionService;
import org.opendelos.control.services.youtube.YouTubePublicationService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.scheduler.OverlapInfo;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.ScheduleDTO;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class ScheduleController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_scheduler";
	}

	@Value("${app.zone}")
	String app_zone;
	@Value("${app.language}")
	String app_lang;
	@Value("${default.institution.identity}")
	String institution_identity;

	@Autowired
	Institution defaultInstitution;
	@Autowired
	String currentAcademicYear;

	private static final String ATTRIBUTE_NAME = "ScheduleDTO";
	private static final String BINDING_RESULT_NAME = "org.springframework.validation.BindingResult." + ATTRIBUTE_NAME;

	private final ScheduleService scheduleService;
	private final InstitutionService institutionService;
	private final CourseService courseService;
	private final ScheduledEventService scheduledEventService;
	private final OpUserService opUserService;
	private final MultilingualServices multilingualServices;

	private final ScheduleUtils scheduleUtils;
	private final ScheduleRegistrationValidator scheduleRegistrationValidator;

	private final YouTubePublicationService youTubePublicationService;

	private final StreamingProperties streamingProperties;

	@Autowired
	public ScheduleController(ScheduleService scheduleService, InstitutionService institutionService, CourseService courseService, ScheduledEventService scheduledEventService, OpUserService opUserService, MultilingualServices multilingualServices, ScheduleUtils scheduleUtils, ScheduleRegistrationValidator scheduleRegistrationValidator, YouTubePublicationService youTubePublicationService, StreamingProperties streamingProperties) {
		this.scheduleService = scheduleService;
		this.institutionService = institutionService;
		this.courseService = courseService;
		this.scheduledEventService = scheduledEventService;
		this.opUserService = opUserService;
		this.multilingualServices = multilingualServices;
		this.scheduleUtils = scheduleUtils;
		this.scheduleRegistrationValidator = scheduleRegistrationValidator;
		this.youTubePublicationService = youTubePublicationService;
		this.streamingProperties = streamingProperties;
	}

	@InitBinder
	protected void initBinder(WebDataBinder binder,Locale locale) {
		binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException{
				if (text == null || text.equals("")) {
					setValue(null);
				}
				else {
					setValue(LocalDate.parse(text, DateTimeFormatter.ofPattern("dd-MM-yyyy", locale)));
				}
			}
			@Override
			public String getAsText() throws IllegalArgumentException {
				if (getValue() != null) {
					return DateTimeFormatter.ofPattern("dd-MM-yyyy").format((LocalDate) getValue());
				}
				else {
					return "";
				}
			}
		});
	}

	@GetMapping(value = {"admin/scheduler/schedule", "admin/scheduler/schedule/"})
	public String ScheduleEditor(final Model model, @RequestParam(value = "id",  required = false) String id,
												    @RequestParam(value = "cloneId", required = false) String cloneId, Locale locale, HttpServletRequest request) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);


		boolean authorize = scheduleService.ApproveScheduledItemEdit(editor,id);
		model.addAttribute("authorized", authorize);

		//Get user scheduler_search history
		if (request.getSession().getAttribute("user_scheduler_search_history") != null) {
			model.addAttribute("user_scheduler_search_history", request.getSession().getAttribute("user_scheduler_search_history"));
		}
		else {
			model.addAttribute("user_scheduler_search_history", "");
		}

		boolean future_task = false;
		Schedule schedule;
		if (!model.containsAttribute(BINDING_RESULT_NAME)) {
			if (id != null) {
				schedule = scheduleService.findById(id);
				OpUser _s_last_editor = opUserService.findById(schedule.getEditor());
				model.addAttribute("_s_last_editor", _s_last_editor.getName());
				//Check if onetime or event schedule is in the past
				if (schedule.getRepeat().equals("onetime")) {
					if (schedule.getCancellations() == null || schedule.getCancellations().size() == 0) {
						int startHour = Integer.parseInt(schedule.getStartTime().substring(0,2));
						int startMinute = Integer.parseInt(schedule.getStartTime().substring(3,5));
						LocalDateTime validateDate = schedule.getDate().atTime(startHour,startMinute);
						if (validateDate.isAfter(LocalDateTime.now())) {
							future_task = true;
							// Check for google Credentials
							OpUser editorUser = opUserService.findById(editor.getId());
							if (editorUser.getRights().getIsSa()) {
								try {
									Credential credential = youTubePublicationService.getSingedInUserGoogleCredentials();
									if (credential != null) {
										model.addAttribute("google_credentials_exist",true);
									}
									else {
										//## use client_id to call client grant access page!!!
										String google_client_id = youTubePublicationService.getGoogleClientId();
										if (google_client_id != null) {
											model.addAttribute("google_client_id",google_client_id);
										}
									}
								}
								catch (IOException | GeneralSecurityException ignored) {}
							}
						}
					}
				}
			}
			else if (cloneId == null) {
				schedule = this.createEmptyResource(currentAcademicYear, editor);
			}
			else {
				schedule = this.createClonedResource(cloneId);
			}
			ScheduleDTO scheduleDTO = scheduleService.getScheduleDTO(schedule);
			model.addAttribute("ScheduleDTO", scheduleDTO);
		}
		model.addAttribute("id", id);
		model.addAttribute("institutionId", defaultInstitution.getId());
		SetModelAttributes(model,locale, currentAcademicYear);
		model.addAttribute("page", "schedule");
		model.addAttribute("future_task", future_task);


		return "admin/scheduler/schedule";
	}

	@PostMapping(value = {"admin/scheduler/schedule" ,"admin/scheduler/schedule/"})
	public String PostSchedule(@Valid @ModelAttribute("ScheduleDTO") ScheduleDTO scheduleDTO, @RequestParam(value = "action", required = false) String action,
			final BindingResult bindingResult,HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		final String view;

		String schedule_id = scheduleDTO.getId();
		if (action != null && (action.equals("delete")) ) {
			if (schedule_id != null && !schedule_id.equals("")) {
				scheduleService.delete(schedule_id);

				String[] attr = {"msg_type", "msg_val"};
				String[] values = {"alert-success", "Η καταχώρηση διαγράφηκε!",};
				setFlashAttributes(request, response, attr, values);

				String target_tab = "#t-lectures";
				if (scheduleDTO.getType().equals("event")) {
					target_tab = "#t-events";
				}
				String user_scheduler_search_history = target_tab;
				if (request.getSession().getAttribute("user_scheduler_search_history") != null) {
					user_scheduler_search_history = (String) request.getSession().getAttribute("user_scheduler_search_history");
				}

				view = "redirect:/admin/scheduler/timetable" + user_scheduler_search_history;
				return view;
			}
		}
		Schedule schedule_o = scheduleService.findById(schedule_id); //get Schedule from Database or return empty Resource
		//Important. Re-set cause field are disabled in form
		if (scheduleDTO.getId() != null && !scheduleDTO.getId().equals("")){
			scheduleDTO.setType(schedule_o.getType());
			scheduleDTO.setRepeat(schedule_o.getRepeat());
		}
		scheduleRegistrationValidator.validate(scheduleDTO, bindingResult);
		if (bindingResult.hasErrors()) {
			String ErrorMessage = "Η αποθήκευση απέτυχε! Υπάρχουν προβλήματα στη φόρμα";
			view = this.returnViewToScheduleEditPageWithError(bindingResult,scheduleDTO,request,response,ErrorMessage);
			return view;
		} else {
			String type = scheduleDTO.getType();
			String repeat = scheduleDTO.getRepeat();

			//# Check if Schedule should be live now! and reject
			boolean ShouldBeLive = this.CheckIfScheduleShouldBeLiveNow(scheduleDTO);
			if (ShouldBeLive) {
				String ErrorMessage = "Η αποθήκευση απέτυχε! Δεν επιτρέπεται ο απευθείας προγραμματισμός ζωντανής μετάδοσης";
				view = this.returnViewToScheduleEditPageWithError(bindingResult,scheduleDTO,request,response,ErrorMessage);
				return view;
			}

			scheduleDTO.setDateModified(LocalDateTime.now());
			/* Update Editor if Needed */
			OoUserDetails ooUserDetails = (OoUserDetails) authentication.getPrincipal();
			Person editor = new Person(ooUserDetails.getId(),ooUserDetails.getName(),ooUserDetails.getAffiliation());
			scheduleDTO.setEditor(editor);


			if (type.equals("lecture")) {
				//Course
				String course_id = scheduleDTO.getCourse().getId();
				Course course = courseService.findById(course_id);
				scheduleDTO.setDepartment(course.getDepartment());
				if (repeat.equals("regular")) {
					scheduleDTO.setDate(null);
				}
				else if (repeat.equals("onetime")) {
					scheduleDTO.setDayOfWeek(LocalDate.parse(scheduleDTO.getDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd")).getDayOfWeek());
				}
				//> save existing cancellations
				if (schedule_o != null && schedule_o.getCancellations() != null && schedule_o.getCancellations().size()>0) {
					scheduleDTO.setCancellations(schedule_o.getCancellations());
				}
			}
			else if (type.equals("event")) {
				scheduleDTO.setDayOfWeek(LocalDate.parse(scheduleDTO.getDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd")).getDayOfWeek());
				//set supervisor if scheduledEvent has responsiblePerson
				String event_id = scheduleDTO.getScheduledEvent().getId();
				ScheduledEvent scheduledEvent = scheduledEventService.findById(event_id);
				//MG 10-04-2021 FORCE ENTRY OF RESP. PERSON ( & DEPARTMENT) IN SCHEDULED EVENTS
				if (scheduledEvent.getResponsiblePerson() != null && scheduledEvent.getResponsiblePerson().getId() != null) {
					OpUser event_supervisor = opUserService.findById(scheduledEvent.getResponsiblePerson().getId());
					scheduleDTO.setSupervisor(new Person(event_supervisor.getId(),event_supervisor.getName(),event_supervisor.getAffiliation()));
					scheduleDTO.setDepartment(new Unit(StructureType.DEPARTMENT,event_supervisor.getDepartment().getId(),event_supervisor.getDepartment().getTitle()));
				}
				scheduleDTO.setRepeat("onetime");
				scheduleDTO.setPeriod("");
			}
			if (!scheduleDTO.isBroadcast()) {
				scheduleDTO.setAccess("closed");
			}
			if (!scheduleDTO.isRecording()) {
				scheduleDTO.setPublication("closed");
			}
			/* Save or Update Scheduler */
			Schedule schedule = scheduleService.getScheduleFromDTO(scheduleDTO);
			/* CHECK OVERLAPS */
			if (schedule.getRepeat().equals("regular")) {
				// Allow or not overlaps over cancelled entries
				boolean allowRegularOverlaps = streamingProperties.isAllowRegularOverlaps();
				//check against other regular entries
				OverlapInfo overlapInfo = scheduleService.checkRegularScheduleOverlapsAgainstOtherRegularSchedules(schedule,allowRegularOverlaps);
				if (overlapInfo != null) {
					view = this.returnViewToScheduleEditPageWithError(bindingResult, scheduleDTO, request, response, overlapInfo.toJson());
					return view;
				}
			}
			else if (schedule.getRepeat().equals("onetime")) {
				//check against onetime entries
				OverlapInfo overlapInfo = scheduleService.checkScheduleDateOverlapAgainstOneTimeSchedules(schedule,schedule.getDate());
				if (overlapInfo != null) {
					view = this.returnViewToScheduleEditPageWithError(bindingResult, scheduleDTO, request, response, overlapInfo.toJson());
					return view;
				}
				else {
					// Allow or not overlaps over cancelled entries
					boolean allowOnetimeOverlaps = streamingProperties.isAllowOnetimeOverlaps();
					//check against other regular entries
					String institutionId = defaultInstitution.getId();
					overlapInfo = scheduleService.checkOneTimeScheduleOverlapAgainstRegularSchedules(schedule,institutionId, allowOnetimeOverlaps);
					if (overlapInfo != null) {
						view = this.returnViewToScheduleEditPageWithError(bindingResult, scheduleDTO, request, response, overlapInfo.toJson());
						return view;
					}
				}
			}

			//In all cases (new, update) :: scheduled should be re-enabled (we do not have switch button any more)
			schedule.setEnabled(true);
			if (schedule_id == null || scheduleDTO.getId().trim().equals("")) {
				schedule_id = scheduleService.create(schedule);
			}
			else {
					//!IMPORTANT: WE HAVE TO RESET CANCELLATIONS ON UPDATE OR INCONSISTENCIES MAY OCCUR
					//TODO: NOTIFY USER ON SAVE CLICK
					schedule.setCancellations(null);
					scheduleService.update(schedule);
			}
			String[] attr = {"msg_type","msg_val"};
			String[] values = {"alert-success","Η καταχώρηση αποθηκεύτηκε!"};
			this.setFlashAttributes(request, response, attr, values);

			/* Redirect to GET Resource page */
			return  "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
					request.getServletPath()).path("?id=" + schedule_id).build().toUriString();
		}
	}

	private void setFlashAttributes(HttpServletRequest request, HttpServletResponse response, String[] attr, String[] values) {

		// create a flashmap
		FlashMap flashMap = new FlashMap();
		// store the message
		for (int i=0;i<attr.length; i++) {
			flashMap.put(attr[i], values[i]);
		}
		// create a flashMapManager with `request`
		FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
		// save the flash map data in session with flashMapManager
		if (flashMapManager != null) {
			flashMapManager.saveOutputFlashMap(flashMap, request, response);
		}
	}

	private void SetModelAttributes(Model model,Locale locale, String currentAcademicYear) {

		//AcademicYears && Current Academic Year
		List<String> ayList = institutionService.getAvailableAcademicCalendarYears(defaultInstitution.getId());
		List<String> ayListText = new ArrayList<>();
		for (String at: ayList) {
			int next_ay = Integer.parseInt(at) + 1;
			ayListText.add(at + " - " + next_ay);
		}
		model.addAttribute("ayCurr", currentAcademicYear);
		model.addAttribute("ayList", ayList);
		model.addAttribute("ayListText", ayListText);
		//Periods
		HashMap<String,String> periods_hash = new HashMap<>();
		String[] periods = multilingualServices.getValue("Period.keys",locale).split(",");
		for (String period : periods) {
			periods_hash.put(period, multilingualServices.getValue(period, locale));
		}
		model.addAttribute("pdList", periods_hash);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());
	}

	private Schedule createEmptyResource(String currentAcademicYear, OoUserDetails editor) {

		Schedule schedule = new Schedule();
		schedule.setRepeat("regular");
		schedule.setType("lecture");
		schedule.setAcademicYear(currentAcademicYear);
		LocalDate localDate = LocalDate.now();
		String period_by_date = scheduleUtils.getPeriodByDate(defaultInstitution.getId(),currentAcademicYear,localDate);
		if (period_by_date != null) {
			schedule.setPeriod(period_by_date);
		}
		schedule.setDurationHours(2);
		schedule.setDurationMinutes(0);
		schedule.setEditor(editor.getId());
		schedule.setDayOfWeek(DayOfWeek.MONDAY);
		ZoneId zoneId = ZoneId.of(app_zone);
		LocalDateTime _start_of_day_now = LocalDate.now(zoneId).atStartOfDay();
		//schedule.setDate(_start_of_day_now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		schedule.setDate(_start_of_day_now.toLocalDate());

		schedule.setBroadcast(true);
		schedule.setAccess("sso");
		schedule.setRecording(true);
		schedule.setPublication("private");
		schedule.setEnabled(true);
		schedule.setBroadcastToChannel(false);
		return schedule;
	}

	private Schedule createClonedResource(String cloneId) {

		Schedule cloneSourse = scheduleService.findById(cloneId);
		Schedule cloneTarget = new Schedule();
		BeanUtils.copyProperties(cloneSourse, cloneTarget);
		cloneTarget.setId(null);
		return cloneTarget;
	}

	private String returnViewToScheduleEditPageWithError(BindingResult bindingResult, ScheduleDTO scheduleDTO, HttpServletRequest request, HttpServletResponse response,
			String ErrorString) {

		String view;
		// create a flashmap
		FlashMap flashMap = new FlashMap();
		// store the message
		flashMap.put("msg_val", ErrorString);
		flashMap.put("msg_type", "alert-danger");
		flashMap.put(BINDING_RESULT_NAME, bindingResult);
		flashMap.put(ATTRIBUTE_NAME, scheduleDTO);
		// create a flashMapManager with `request`
		FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
		// save the flash map data in session with flashMapManager
		if (flashMapManager != null) {
			flashMapManager.saveOutputFlashMap(flashMap, request, response);
		}
		if (scheduleDTO.getId() != null && !scheduleDTO.getId().equals("")) {
			view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
					request.getServletPath()).path("?id=" + scheduleDTO.getId()).build().toUriString();
		}
		else {
			view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
					request.getServletPath()).build().toUriString();
		}
		return view;
	}

	private boolean CheckIfScheduleShouldBeLiveNow(ScheduleDTO scheduleDTO) {

		boolean ShouldBeLive = false;
		String repeat = scheduleDTO.getRepeat();
		String type = scheduleDTO.getType();
		//** CHeck if Schedule should be live now! and set the day cancelled **/
		if (type.equals("event") || ( type.equals("lecture") && repeat != null && repeat.equals("onetime"))) {
			DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
			int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
			LocalDateTime broadcast_start = LocalDate.parse(scheduleDTO.getDate(), f).atTime(broadcast_hour,broadcast_min);
			LocalDateTime broadcast_end = broadcast_start.plus(scheduleDTO.getDurationHours(), ChronoUnit.HOURS).plus(scheduleDTO.getDurationMinutes(),ChronoUnit.MINUTES);
			LocalDateTime now = LocalDateTime.now();
			if (broadcast_start.isBefore(now) && broadcast_end.isAfter(now)) {
				ShouldBeLive = true;
			}
		}
		else {
			LocalDateTime now = LocalDateTime.now();
			String currentAcademicYear = institutionService.getCurrentAcademicYear();
			if (currentAcademicYear.equals(scheduleDTO.getAcademicYear())) {
				if (now.getDayOfWeek().equals(scheduleDTO.getDayOfWeek())) {
					Course course = courseService.findById(scheduleDTO.getCourse().getId());
					String departmentId = course.getDepartment().getId();
					String department_period_name = scheduleUtils.getDepartmentPeriodNameByDate(departmentId,currentAcademicYear,now.toLocalDate());
					String period = scheduleDTO.getPeriod();
					if (department_period_name.equals(period)) {
						LocalTime broadcast_start = LocalTime.parse(scheduleDTO.getStartTime());
						LocalTime broadcast_end = broadcast_start.plus(scheduleDTO.getDurationHours(),ChronoUnit.HOURS).plus(scheduleDTO.getDurationMinutes(), ChronoUnit.MINUTES);
						LocalTime now_time = LocalTime.now();
						if (broadcast_start.isBefore(now_time) && broadcast_end.isAfter(now_time)) {
							ShouldBeLive = true;
						}
					}
				}
			}
		}
		return ShouldBeLive;
	}
}
