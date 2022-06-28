/* 
     Author: Michael Gatzonis - 2/10/2020 
     live
*/
package org.opendelos.control.api.users;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.control.api.common.ApiUtils;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.users.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersApi {

	private final OpUserService opUserService;

	@Autowired
	public UsersApi(OpUserService opUserService) {
		this.opUserService = opUserService;
	}

	@RequestMapping(value="/api/v1/dt/managers.web",method = RequestMethod.GET)
	public byte[] getAllManagers() {

		List<OpUser> managers;
		managers = opUserService.findAllManagers();

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(managers);
		return b;
	}

	@RequestMapping(value="/api/v1/dt/managers.web/type/{type}",method = RequestMethod.GET)
	public byte[] getAllManagersOfType(@PathVariable("type") String type) {

		List<OpUser> managers;
		managers = opUserService.findAllManagersOfType(type);

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(managers);
		return b;
	}

	@RequestMapping(value= "/api/v1/dt/managers/assigned_units/{id}", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getManagerAssignedUnits(@PathVariable("id") String id) {

		List<UserAccess.UserRights.UnitPermission> unitPermissions = opUserService.getManagersUnitPermissions(id);
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(unitPermissions);
		return b;
	}

	@RequestMapping(value= "/api/v1/dt/managers/assigned_courses/{id}", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getManagerAssignedCourses(@PathVariable("id") String id) {

		List<UserAccess.UserRights.CoursePermission> coursePermissions = opUserService.getManagersCoursePermissionsByAccessType(id,"IGNORE_ACCESS_TYPE");
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(coursePermissions);
		return b;
	}

	@RequestMapping(value= "/api/v1/dt/managers/assigned_events/{id}", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getManagerAssignedEvents(@PathVariable("id") String id) {

		List<UserAccess.UserRights.EventPermission> eventPermissions = opUserService.getManagersEventPermissionsByAccessType(id,"IGNORE_ACCESS_TYPE");
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(eventPermissions);
		return b;
	}

	@RequestMapping(value = "/api/v1/managers/assign_unit/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void AssignUnitToStaffMember(@RequestBody UserAccess.UserRights.UnitPermission unitPermission, @PathVariable("id") String id)  {
		opUserService.assignUnitPermissionToManagerById(id,unitPermission);
	}
	@RequestMapping(value = "/api/v1/managers/assign_unit_update/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void UpdateAssignUnitToStaffMember(@RequestBody UserAccess.UserRights.UnitPermission unitPermission, @PathVariable("id") String id)  {
		opUserService.assignUpdateUnitPermissionToManagerById(id,unitPermission);
	}
	@RequestMapping(value = "/api/v1/managers/change_role/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void ChangeRoleForStaffMember(@RequestBody String new_role, @PathVariable("id") String id)  {
		opUserService.changeRoleOfManagerById(id,new_role);
	}
	@RequestMapping(value = "/api/v1/managers/unassign_unit/{id}/unit/{unit_id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void UnAssignUnitFromStaffMember(@PathVariable("id") String id,@PathVariable("unit_id") String unit_id)  {
		opUserService.unassignUnitPermissionFromManagerById(id,unit_id);
	}

	@RequestMapping(value = "/api/v1/managers/assign_course/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void AssignCourseToStaffMember(@RequestBody UserAccess.UserRights.CoursePermission coursePermission, @PathVariable("id") String id)  {

		boolean courseIsAlreadyIncluded = false;
		OpUser manager = opUserService.findById(id);
		if (coursePermission.getCourseId().equals("ALL_COURSES")) {
			//remove all other courses of same staffmember
			if (manager.getRights().getCoursePermissions() != null && manager.getRights().getCoursePermissions().size()>0) {
				List<UserAccess.UserRights.CoursePermission> removeList = new ArrayList<>();
				for (UserAccess.UserRights.CoursePermission checkPermission: manager.getRights().getCoursePermissions()) {
					if (checkPermission.getStaffMemberId().equals(coursePermission.getStaffMemberId())) {
							removeList.add(checkPermission);
					}
				}
				if (removeList.size() >0) {
					manager.getRights().getCoursePermissions().removeAll(removeList);
					opUserService.update(manager);
				}
			}
		}
		else {
			// check if course is included in ALL_COURSES rule
			if (manager.getRights().getCoursePermissions() != null && manager.getRights().getCoursePermissions().size()>0) {
				for (UserAccess.UserRights.CoursePermission checkPermission: manager.getRights().getCoursePermissions()) {
					if (checkPermission.getStaffMemberId().equals(coursePermission.getStaffMemberId()) &&
							checkPermission.getCourseId().equals("ALL_COURSES")) {
						courseIsAlreadyIncluded = true;
						break;
					}
				}
			}
		}
		if (!courseIsAlreadyIncluded) {
			opUserService.assignCoursePermissionToManagerById(id, coursePermission);
		}
	}

	@RequestMapping(value = "/api/v1/managers/assign_course_update/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void UpdateAssignCourseToStaffMember(@RequestBody UserAccess.UserRights.CoursePermission coursePermission, @PathVariable("id") String id)  {
		opUserService.assignUpdateCoursePermissionToManagerById(id,coursePermission);
	}
	@RequestMapping(value = "/api/v1/managers/unassign_course/{id}/course/{course_id}/sm/{staffMemberId}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void UnAssignCourseFromStaffMember(@PathVariable("id") String id,@PathVariable("course_id") String course_id, @PathVariable("staffMemberId") String staffMemberId)  {
		opUserService.unassignCoursePermissionFromManagerById(id,staffMemberId,course_id);
	}

	@RequestMapping(value = "/api/v1/managers/assign_event/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void AssignEventToStaffMember(@RequestBody UserAccess.UserRights.EventPermission eventPermission, @PathVariable("id") String id)  {

		boolean eventIsAlreadyIncluded = false;
		OpUser manager = opUserService.findById(id);
		if (eventPermission.getEventId().equals("ALL_EVENTS")) {
			if (manager.getRights().getEventPermissions() != null && manager.getRights().getEventPermissions().size()>0) {
				//remove all other events of same staffmember
				List<UserAccess.UserRights.EventPermission> removeList = new ArrayList<>();
				for (UserAccess.UserRights.EventPermission checkPermission: manager.getRights().getEventPermissions()) {
					if (checkPermission.getStaffMemberId().equals(eventPermission.getStaffMemberId())) {
						removeList.add(checkPermission);
					}
				}
				if (removeList.size() >0) {
					manager.getRights().getEventPermissions().removeAll(removeList);
					opUserService.update(manager);
				}
			}
		}
		else {
			// check if event is included in ALL_EVENTS rule
			if (manager.getRights().getEventPermissions() != null && manager.getRights().getEventPermissions().size()>0) {
				for (UserAccess.UserRights.EventPermission checkPermission: manager.getRights().getEventPermissions()) {
					if (checkPermission.getStaffMemberId().equals(eventPermission.getStaffMemberId()) &&
							checkPermission.getEventId().equals("ALL_EVENTS")) {
						eventIsAlreadyIncluded = true;
						break;
					}
				}
			}
		}
		if (!eventIsAlreadyIncluded) {
			opUserService.assignEventPermissionToManagerById(id, eventPermission);
		}
	}
	@RequestMapping(value = "/api/v1/managers/assign_event_update/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void UpdateAssignEventToStaffMember(@RequestBody UserAccess.UserRights.EventPermission eventPermission, @PathVariable("id") String id)  {
		opUserService.assignUpdateEventPermissionToManagerById(id,eventPermission);
	}
	@RequestMapping(value = "/api/v1/managers/unassign_event/{id}/event/{event_id}/sm/{staffMemberId}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void UnAssignEventFromStaffMember(@PathVariable("id") String id,@PathVariable("event_id") String event_id, @PathVariable("staffMemberId") String staffMemberId)  {
		opUserService.unassignEventPermissionFromManagerById(id,staffMemberId,event_id);
	}

	@RequestMapping(value = "/api/v1/manager/updatestatus/{id}/status/{status}", method = RequestMethod.PUT, produces = MediaType.TEXT_HTML_VALUE)
	public void updateManagersStatus(@PathVariable("id") String id,@PathVariable("status") boolean status) throws Exception {
		opUserService.updateManagerStatus(id,status);
	}


	@RequestMapping(value = "/api/v1/manager/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteManager(@PathVariable("id") String id) {
		try {
			 OpUser manager = opUserService.findById(id);
			 List<UserAccess.UserAuthority> manager_authorities = manager.getAuthorities();
			 if (manager_authorities.contains(UserAccess.UserAuthority.STAFFMEMBER)) {
			 	//just remove MANAGER Authority
				 manager_authorities.remove(UserAccess.UserAuthority.MANAGER);
				 manager.setAuthorities(manager_authorities);
				 manager.getRights().setIsSa(false);
				 manager.getRights().setUnitPermissions(null);
				 manager.getRights().setCoursePermissions(null);
				 opUserService.update(manager);
			 }
			 else {
				 try {
					 opUserService.deleteManager(id);
					 return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
				 }
				 catch(Exception e) {
					 return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
				 }
			 }
			 return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/user/{id}", method = RequestMethod.GET)
	public @ResponseBody
	OpUser findUserById(@PathVariable("id") String id) {
		try {
			return opUserService.findById(id);
		}
		catch(Exception e) {
			return null;
		}
	}
}
