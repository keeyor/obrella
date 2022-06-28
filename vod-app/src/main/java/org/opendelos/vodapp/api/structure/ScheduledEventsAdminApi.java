/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.vodapp.api.structure;

import java.time.Instant;
import java.util.List;

import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Department;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.vodapp.api.common.ApiUtils;
import org.opendelos.vodapp.services.opUser.OpUserService;
import org.opendelos.vodapp.services.scheduledEvent.ScheduledEventService;
import org.opendelos.vodapp.services.structure.DepartmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduledEventsAdminApi {

	private final ScheduledEventService scheduledEventService;
	private final DepartmentService departmentService;
	private final OpUserService opUserService;

	@Autowired
	public ScheduledEventsAdminApi(ScheduledEventService scheduledEventService, DepartmentService departmentService, OpUserService opUserService) {
		this.scheduledEventService = scheduledEventService;
		this.departmentService = departmentService;
		this.opUserService = opUserService;
	}

	@RequestMapping(value = "/apiw/v1/dt/sevents.web", method = RequestMethod.GET)
	public byte[] findAllForDt() {

		List<ScheduledEvent> scheduledEvents = scheduledEventService.findAll();
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(scheduledEvents);
		return b;
	}

	@RequestMapping(value = "/apiw/v1/dt/sevents.web/authorized/{access}", method = RequestMethod.GET)
	public byte[] getAuthorizedScheduledEvents(@PathVariable String access) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<ScheduledEvent> scheduledEvents = scheduledEventService.getAuthorizedScheduledEventsByEditor(editor, access);

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(scheduledEvents);
		return b;
	}


	@RequestMapping(value = "/apiw/v1/sevent/save", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveScheduledEvent(@RequestBody ScheduledEvent scheduledEvent) {

		String _id;
		Instant now = Instant.now();
		scheduledEvent.setDateModified(now);
		//editor
		Person editor = this.getPersonFromUserId(scheduledEvent.getEditor().getId());
		scheduledEvent.setEditor(editor);
		//R-PERSON
		Person rPerson = null;
		if (scheduledEvent.getResponsiblePerson() != null && scheduledEvent.getResponsiblePerson().getId() != null &&
				!scheduledEvent.getResponsiblePerson().getId().equals("")) {
			rPerson = this.getPersonFromStaffMemberId(scheduledEvent.getResponsiblePerson().getId());
		}
		scheduledEvent.setResponsiblePerson(rPerson);
		try {
			if (scheduledEvent.getId() == null || scheduledEvent.getId().equals("")) {
				scheduledEvent.setId(null);
				_id = scheduledEventService.create(scheduledEvent);
			}
			else {
				scheduledEventService.findAndUpdate(scheduledEvent);
				_id = scheduledEvent.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/apiw/v1/sevent/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteScheduledEvent(@PathVariable("id") String id) {

		try {
			scheduledEventService.delete(id);
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/apiw/v1/sevent/{id}", method = RequestMethod.GET)
	public ResponseEntity<ScheduledEvent> getScheduledEventById(@PathVariable("id") String id) {

		try {
			ScheduledEvent scheduledEvent = scheduledEventService.findById(id);
			return new ResponseEntity<>(scheduledEvent, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	private Person getPersonFromUserId(String userId) {

		OpUser opUser = opUserService.findById(userId);
		String ooUser_departmentId = opUser.getDepartment().getId();
		Department ooUser_department = departmentService.findById(ooUser_departmentId);
		Unit ooUser_unit = new Unit(StructureType.DEPARTMENT, ooUser_departmentId, ooUser_department.getTitle());

		return new Person(opUser.getId(), opUser.getName(), opUser.getAffiliation(), ooUser_unit);
	}

	private Person getPersonFromStaffMemberId(String staffMemberId) {

		OpUser opUser = opUserService.findById(staffMemberId);
		Unit staff_unit = opUser.getDepartment();

		return new Person(opUser.getId(), opUser.getName(), opUser.getAffiliation(), staff_unit);
	}
}
