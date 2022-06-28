/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.control.services.scheduledEvent.ScheduledEventService;
import org.opendelos.control.services.structure.CourseService;
import org.opendelos.control.services.structure.DepartmentService;
import org.opendelos.control.services.structure.SchoolService;
import org.opendelos.control.services.system.SystemMessageService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.School;
import org.opendelos.model.system.SystemMessage;
import org.opendelos.model.users.ActiveUserStore;
import org.opendelos.model.users.CourseRightDto;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.model.users.ScheduledEventRightDto;
import org.opendelos.model.users.UnitRightDto;
import org.opendelos.model.users.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;

@Controller
public class AdminController {

	private final Logger logger = LoggerFactory.getLogger(AdminController.class);

	private final OpUserService opUserService;
	private final DepartmentService departmentService;
	private final SchoolService schoolService;
	private final CourseService courseService;
	private final ResourceService resourceService;
	private final ScheduledEventService scheduledEventService;
	private final SystemMessageService systemMessageService;

	@Autowired
	Institution defaultInstitution;

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_home";
	}

	@Autowired
	ActiveUserStore activeUserStore;


	public AdminController(OpUserService opUserService, DepartmentService departmentService, SchoolService schoolService, CourseService courseService, ResourceService resourceService, ScheduledEventService scheduledEventService, SystemMessageService systemMessageService) {
		this.opUserService = opUserService;
		this.departmentService = departmentService;
		this.schoolService = schoolService;
		this.courseService = courseService;
		this.resourceService = resourceService;
		this.scheduledEventService = scheduledEventService;
		this.systemMessageService = systemMessageService;
	}

	@GetMapping(value = {"admin/", "admin"})
	public String getAdminControlPanel(final Model model) {

		boolean userIsStaffMember = false;
		boolean userIsStaffMemberOnly = false;

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);


		List<SystemMessage> adminAllMessages = systemMessageService.findAllByVisibleIsAndTarget(true,"admins-all");
		List<SystemMessage> adminStaffMessages;
		List<SystemMessage> adminUsersMessages;

		if (editor.getUserAuthorities().contains(UserAccess.UserAuthority.STAFFMEMBER) && editor.getUserAuthorities().size() == 1) {
			userIsStaffMemberOnly= true;
		}
		model.addAttribute("userIsStaffMemberOnly",userIsStaffMemberOnly);

		if (editor.getUserAuthorities().contains(UserAccess.UserAuthority.STAFFMEMBER)) {
			userIsStaffMember = true;
			adminStaffMessages = systemMessageService.findAllByVisibleIsAndTarget(true,"admins-staff");
			adminAllMessages.addAll(adminStaffMessages);
		}
		model.addAttribute("userIsStaffMember",userIsStaffMember);

		if (editor.getUserAuthorities().contains(UserAccess.UserAuthority.MANAGER)) {
			adminUsersMessages = systemMessageService.findAllByVisibleIsAndTarget(true, "admins-users");
			adminAllMessages.addAll(adminUsersMessages);
		}
		model.addAttribute("adminAllMessages", adminAllMessages);

		//Logged on Users
		model.addAttribute("logged_users_counter", activeUserStore.getUsers().size());

		return "admin/index";
	}

	@GetMapping("logout")
	public String logout(HttpServletRequest request,HttpServletResponse response, SecurityContextLogoutHandler logoutHandler) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CookieClearingLogoutHandler cookieClearingLogoutHandler = new CookieClearingLogoutHandler(SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
		cookieClearingLogoutHandler.logout(request, response, auth);
		logoutHandler.logout(request, response, auth);

		return "redirect:/";
	}


	@RequestMapping(value = "cas/secure")
	public String cashomeredirect() {

		return "redirect:admin";
	}
	@RequestMapping(value = {"cas/login"})
	public String casLoginIndex(final Model model,Locale locale) {

		model.addAttribute("localeData", locale.getDisplayName());
		return"redirect:admin";
	}

	@GetMapping(value = "admin/user_profile")
	public String getIncompleteUserProfile(final Model model) {

		OoUserDetails logged_on_user = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		boolean userIsStaffMemberOnly = false;
		if (logged_on_user.getUserAuthorities().contains(UserAccess.UserAuthority.STAFFMEMBER) && logged_on_user.getUserAuthorities().size() == 1) {
			userIsStaffMemberOnly= true;
		}
		model.addAttribute("userIsStaffMemberOnly",userIsStaffMemberOnly);
		boolean userIsStaffMember = false;
		if (logged_on_user.getUserAuthorities().contains(UserAccess.UserAuthority.STAFFMEMBER)) {
			userIsStaffMember= true;
		}
		model.addAttribute("userIsStaffMember",userIsStaffMember);

		if (logged_on_user.getUserAuthorities().contains(UserAccess.UserAuthority.STUDENT) && logged_on_user.getUserAuthorities().size() == 1) {
			model.addAttribute("user", logged_on_user);
			model.addAttribute("user_type", "STUDENT");
		}
		else {
			OpUser user_info = opUserService.findByUid(logged_on_user.getUid());
			String user_id = user_info.getId();
			model.addAttribute("user", user_info);
			model.addAttribute("user_type", "USER");
			if (user_info.getAuthorities().contains(UserAccess.UserAuthority.MANAGER) || user_info.getAuthorities().contains(UserAccess.UserAuthority.SUPPORT)
					&& !user_info.getRights().getIsSa()) {
				setUserUnitAndCourseRight(user_info, model);
				long managerAsEditorInResources;
				long managerAsEditorInScheduled;
				long managerAsEditorInEvents;
				managerAsEditorInResources = resourceService.CountResourcesByManagerAsEditor(user_id);
				model.addAttribute("ManagerAsEditorInResources",managerAsEditorInResources);
				managerAsEditorInScheduled = resourceService.CountScheduledByManagerAsEditor(user_id,"Scheduler.Schedule");
				model.addAttribute("ManagerAsEditorInScheduled",managerAsEditorInScheduled);
				managerAsEditorInEvents = scheduledEventService.CountEventsByManagerAsEditor(user_id);
				model.addAttribute("ManagerAsEditorInEvents",managerAsEditorInEvents);
			}
			long staffAsSupervisorInResources;
			long staffAsSupervisorInScheduled;
			long staffAsSupervisorInEvents;

			if (logged_on_user.getUserAuthorities().contains(UserAccess.UserAuthority.STAFFMEMBER)) {
				staffAsSupervisorInResources = resourceService.CountResourcesByStaffMemberAsSupervisor(user_id);
				model.addAttribute("StaffAsSupervisorInResources",staffAsSupervisorInResources);
				staffAsSupervisorInScheduled = resourceService.CountScheduledByStaffMemberAsSupervisor(user_id, "Scheduler.Schedule");
				model.addAttribute("StaffAsSupervisorInScheduled",staffAsSupervisorInScheduled);
				staffAsSupervisorInEvents = scheduledEventService.CountEventsByStaffMemberAsSupervisor(user_id);
				model.addAttribute("StaffAsSupervisorInEvents",staffAsSupervisorInEvents);
			}
		}

		//Override section attribute
		model.addAttribute("section","user_profile");
		return "admin/user_profile";
	}

	@PostMapping(value = "admin/user_profile")
	public String setUserProfile(final Model model, @RequestParam("departmentId") String departmentId) {

		OoUserDetails logged_on_user = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		OpUser user_info = opUserService.findById(logged_on_user.getId());
		if (user_info != null) {
			Department department = departmentService.findById(departmentId);
			if (department != null) {
				user_info.setDepartment(new Unit(StructureType.DEPARTMENT,department.getId(),department.getTitle()));
				String user_primary_affiliation = user_info.getEduPersonPrimaryAffiliation();
				if (user_primary_affiliation.equalsIgnoreCase("staff") ||  user_primary_affiliation.equalsIgnoreCase("faculty") ||
						user_primary_affiliation.equalsIgnoreCase("employees") ||  user_primary_affiliation.equalsIgnoreCase("affiliate") ) {
					List<UserAccess.UserAuthority> user_authorities = user_info.getAuthorities();
					if (!user_authorities.contains(UserAccess.UserAuthority.STAFFMEMBER)) {
						if (user_authorities.contains(UserAccess.UserAuthority.USER)) {
							user_authorities.clear();
						}
						user_authorities.add(UserAccess.UserAuthority.STAFFMEMBER);
						user_info.setAuthorities(user_authorities);
					}
				}
				opUserService.update(user_info);
			}
		}
		model.addAttribute("user", user_info);
		model.addAttribute("msg","Sign-Out Message Show");

		return "admin/user_profile";
	}

	private void setUserUnitAndCourseRight(OpUser user_info, Model model) {

		List<UserAccess.UserRights.UnitPermission> unitPermissions = user_info.getRights().getUnitPermissions();
		List<UnitRightDto> unitRightDtos = null;
		if (unitPermissions != null && unitPermissions.size()>0) {
			unitRightDtos = new ArrayList<>();
			for (UserAccess.UserRights.UnitPermission unitPermission: unitPermissions) {
				UnitRightDto unitRightDto = new UnitRightDto();
				if (unitPermission.getUnitType().equals(UserAccess.UnitType.INSTITUTION)) {
					unitRightDto.setUnitId(defaultInstitution.getId());
					unitRightDto.setUnitTitle(defaultInstitution.getTitle());
					unitRightDto.setUnitType("ΙΔΡΥΜΑ");
					unitRightDto.setContentManager(unitPermission.isContentManager());
					unitRightDto.setDataManager(unitPermission.isDataManager());
					unitRightDto.setScheduleManager(unitPermission.isScheduleManager());
					unitRightDtos.add(unitRightDto);
				}
				else if (unitPermission.getUnitType().equals(UserAccess.UnitType.SCHOOL)) {
					School school = schoolService.findById(unitPermission.getUnitId());
					if (school != null) {
						unitRightDto.setUnitId(school.getId());
						unitRightDto.setUnitTitle(school.getTitle());
						unitRightDto.setUnitType("ΣΧΟΛΗ");
						unitRightDto.setContentManager(unitPermission.isContentManager());
						unitRightDto.setDataManager(unitPermission.isDataManager());
						unitRightDto.setScheduleManager(unitPermission.isScheduleManager());
						unitRightDtos.add(unitRightDto);
					}
				}
				else if (unitPermission.getUnitType().equals(UserAccess.UnitType.DEPARTMENT)) {
					Department department = departmentService.findById(unitPermission.getUnitId());
					if (department != null) {
						unitRightDto.setUnitId(department.getId());
						unitRightDto.setUnitTitle(department.getTitle());
						unitRightDto.setUnitType("ΤΜΗΜΑ");
						unitRightDto.setContentManager(unitPermission.isContentManager());
						unitRightDto.setDataManager(unitPermission.isDataManager());
						unitRightDto.setScheduleManager(unitPermission.isScheduleManager());
						unitRightDtos.add(unitRightDto);
					}
				}
			}
		}
		model.addAttribute("User_UnitRights", unitRightDtos);
		List<UserAccess.UserRights.CoursePermission> coursePermissions = user_info.getRights().getCoursePermissions();
		List<CourseRightDto> courseRightDtos = null;
		if (coursePermissions != null && coursePermissions.size()>0) {
			courseRightDtos = new ArrayList<>();
			for (UserAccess.UserRights.CoursePermission coursePermission: coursePermissions) {
				CourseRightDto courseRightDto = new CourseRightDto();
				if (!coursePermission.getCourseId().equals("*") && !coursePermission.getCourseId().equals("ALL_COURSES")) {
					Course course = courseService.findById(coursePermission.getCourseId());
					OpUser staffMember = opUserService.findById(coursePermission.getStaffMemberId());
					if (course != null && staffMember != null) {
						courseRightDto.setCourseId(course.getId());
						courseRightDto.setCourseTitle(course.getTitle());
						courseRightDto.setDepartmentTitle(course.getDepartment().getTitle());
						courseRightDto.setStaffMemberId(staffMember.getId());
						courseRightDto.setStaffMemberName(staffMember.getName());
						courseRightDto.setContentManager(coursePermission.isContentManager());
						courseRightDto.setScheduleManager(coursePermission.isScheduleManager());
						courseRightDtos.add(courseRightDto);
					}
					else {
						logger.warn("Course Permission Warning: course {} or staff member {} error", coursePermission.getCourseId(), coursePermission.getStaffMemberId());
					}
				}
				else {
					OpUser staffMember = opUserService.findById(coursePermission.getStaffMemberId());
					if (staffMember != null) {
						courseRightDto.setCourseId("*");
						courseRightDto.setCourseTitle("-- όλα τα Μαθήματα -- ");
						courseRightDto.setDepartmentTitle("-");
						courseRightDto.setStaffMemberId(staffMember.getId());
						courseRightDto.setStaffMemberName(staffMember.getName());
						courseRightDto.setContentManager(coursePermission.isContentManager());
						courseRightDto.setScheduleManager(coursePermission.isScheduleManager());
						courseRightDtos.add(courseRightDto);
					}
				}
			}
		}
		model.addAttribute("User_CourseRights", courseRightDtos);

		//ScheduledEvents Permissions
		List<UserAccess.UserRights.EventPermission> eventPermissions = user_info.getRights().getEventPermissions();
		List<ScheduledEventRightDto> scheduledEventRightDtos = null;
		if (eventPermissions != null && eventPermissions.size()>0) {
			scheduledEventRightDtos = new ArrayList<>();
			for (UserAccess.UserRights.EventPermission eventPermission: eventPermissions) {
				ScheduledEventRightDto scheduledEventRightDto = new ScheduledEventRightDto();
				if (!eventPermission.getEventId().equals("*") && !eventPermission.getEventId().equals("ALL_EVENTS")) {
					ScheduledEvent scheduledEvent = scheduledEventService.findById(eventPermission.getEventId());
					OpUser staffMember = opUserService.findById(eventPermission.getStaffMemberId());
					if (scheduledEvent != null && staffMember != null) {
						scheduledEventRightDto.setEventId(scheduledEvent.getId());
						scheduledEventRightDto.setEventTitle(scheduledEvent.getTitle());
						scheduledEventRightDto.setStaffMemberId(staffMember.getId());
						scheduledEventRightDto.setStaffMemberName(staffMember.getName());
						scheduledEventRightDto.setContentManager(eventPermission.isContentManager());
						scheduledEventRightDto.setScheduleManager(eventPermission.isScheduleManager());
						scheduledEventRightDtos.add(scheduledEventRightDto);
					}
					else {
						logger.warn("Course Permission Warning: course {} or staff member {} error", eventPermission.getEventId(), eventPermission.getStaffMemberId());
					}
				}
				else {
					OpUser staffMember = opUserService.findById(eventPermission.getStaffMemberId());
					if (staffMember != null) {
						scheduledEventRightDto.setEventId("*");
						scheduledEventRightDto.setEventTitle("-- όλες οι Εκδηλώσεις -- ");
						scheduledEventRightDto.setStaffMemberId(staffMember.getId());
						scheduledEventRightDto.setStaffMemberName(staffMember.getName());
						scheduledEventRightDto.setContentManager(eventPermission.isContentManager());
						scheduledEventRightDto.setScheduleManager(eventPermission.isScheduleManager());
						scheduledEventRightDtos.add(scheduledEventRightDto);
					}
				}
			}
		}
		model.addAttribute("User_EventRights", scheduledEventRightDtos);
	}

}
