/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.control.api.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opendelos.control.api.common.ApiUtils;
import org.opendelos.control.services.i18n.MultilingualServices;
import org.opendelos.control.services.i18n.OptionServices;
import org.opendelos.control.services.scheduledEvent.ScheduledEventService;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduledEventsApi {

	private final ScheduledEventService scheduledEventService;
	private final OptionServices optionServices;
	private final MultilingualServices multilingualServices;

	@Autowired
	public ScheduledEventsApi(ScheduledEventService scheduledEventService, OptionServices optionServices, MultilingualServices multilingualServices) {
		this.scheduledEventService = scheduledEventService;
		this.optionServices = optionServices;
		this.multilingualServices = multilingualServices;
	}

	@RequestMapping(value= "/api/v1/s2/scheduledEvents.web", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAll() {
		List<ScheduledEvent> scheduledEvents = scheduledEventService.findAll();
		List<Select2GenChild> children = new ArrayList<>();
		for (ScheduledEvent  scheduledEvent: scheduledEvents) {
			Select2GenChild child = new Select2GenChild();
			child.setId(scheduledEvent.getId());
			child.setText(scheduledEvent.getTitle());
			children.add(child);
		}
		try {
			String s2events = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2events, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value= "/api/v2/s2/scheduledEvents.web/authorized/{access}/{security}", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAuthorizedScheduledEvents(@PathVariable String access,@PathVariable String security) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<ScheduledEvent> scheduledEvents;
		scheduledEvents = scheduledEventService.getAuthorizedScheduledEventsByEditor(editor,access);

		List<Select2GenChild> children = new ArrayList<>();
		for (ScheduledEvent  scheduledEvent: scheduledEvents) {
					if (security.equals("active") && !scheduledEvent.getIsActive()) {
						continue;
					}
					Select2GenChild child = new Select2GenChild();
					child.setId(scheduledEvent.getId());
					child.setText(scheduledEvent.getTitle());

				    StringBuilder subHeader = new StringBuilder();
					if (scheduledEvent.getResponsiblePerson() != null) {
						Person rP = scheduledEvent.getResponsiblePerson();
						subHeader.append("Υπεύθυνος/Εκπρόσωπος: ").append(rP.getName()).append(" (Τμήμα ").append(rP.getDepartment().getTitle()).append(")");
					}
					else { //just-in-case for older entries
						subHeader.append("Οργάνωση:");
						for (Unit orgUnit : scheduledEvent.getResponsibleUnit()) {
							if (orgUnit.getStructureType().equals(StructureType.DEPARTMENT)) {
								subHeader.append(" Τμήμα ");
							}
							else if (orgUnit.getStructureType().equals(StructureType.SCHOOL)) {
								subHeader.append(" Σχολή ");
							}
							subHeader.append(" ").append(orgUnit.getTitle());
						}
					}
					child.setSubheader(subHeader.toString());
					children.add(child);
		}
		try {
			String s2events = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2events, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value= "/api/v2/dt/scheduledEvents.web/authorized/{access}/{security}", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getAuthorizedScheduledEventsDT(@PathVariable String access,@PathVariable String security) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<ScheduledEvent> scheduledEvents;
		scheduledEvents = scheduledEventService.getAuthorizedScheduledEventsByEditor(editor,access);

		List<ScheduledEvent> remove_inactive_list = new ArrayList<>();
		for (ScheduledEvent  scheduledEvent: scheduledEvents) {
			if (security.equals("active") && !scheduledEvent.getIsActive()) {
				remove_inactive_list.add(scheduledEvent);
			}
		}
		scheduledEvents.removeAll(remove_inactive_list);
		return ApiUtils.TransformResultsForDataTable(scheduledEvents);
	}

	@RequestMapping(value="/api/v1/s2/scheduledEvents.web/staffmember/{id}", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAllByResponsiblePerson(@PathVariable("id") String id) {

		List<ScheduledEvent> scheduledEvents = scheduledEventService.findAllByResponsiblePersonId(id);

		List<Select2GenChild> children = new ArrayList<>();
		for (ScheduledEvent  scheduledEvent: scheduledEvents) {
			//for (Unit unit: scheduledEvent.getResponsibleUnit()) {
					Select2GenChild child = new Select2GenChild();
					child.setId(scheduledEvent.getId());
					child.setText(scheduledEvent.getTitle());
					children.add(child);
			//}
		}
		try {
			String s2events = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2events, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value= "/api/v1/dt/scheduledEvents.web", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getAllEventsDt() {

		List<ScheduledEvent> scheduledEvents = scheduledEventService.findAll();
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(scheduledEvents);

		return b;
	}

	@RequestMapping(value= "/api/v1/s2/event_types/{area}", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getEventTypesByArea(@PathVariable("area") String area, Locale locale) {

		String[] eventTypesOfArea = optionServices.getEventTypesByArea(locale,area);

		List<Select2GenChild> children = new ArrayList<>();
		for (String type: eventTypesOfArea) {
				Select2GenChild child = new Select2GenChild();
				child.setId(type);
				child.setText(multilingualServices.getValue(type,locale));
				children.add(child);
		}
		try {
			String s2eventsTypes= ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2eventsTypes, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

}
