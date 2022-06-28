/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.control.api.scheduler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.opendelos.control.api.common.ApiUtils;
import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.control.services.scheduler.ScheduleService;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.scheduler.OverlapInfo;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.TimeTableResults;
import org.opendelos.model.scheduler.common.Cancellation;
import org.opendelos.model.users.OoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
public class ScheduleApi {

	@Value( "${app.host}" )
	private String app_host;

	private final Logger logger = LoggerFactory.getLogger(ScheduleApi.class);

	private final ScheduleService scheduleService;
	private final StreamingProperties streamingProperties;
	private final ResourceService resourceService;

	@Autowired
	public ScheduleApi(ScheduleService scheduleService, StreamingProperties streamingProperties, ResourceService resourceService) {
		this.scheduleService = scheduleService;
		this.streamingProperties = streamingProperties;
		this.resourceService = resourceService;
	}

	@RequestMapping(value= "/api/v1/schedule_table/delete/{id}", method = RequestMethod.DELETE, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> DeleteScheduleEntry(@PathVariable("id") String id) {

		/* Authorize Cancellation */
		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean authorize = scheduleService.ApproveScheduledItemEdit(editor,id);
		if (!authorize) {
			return new ResponseEntity<>("Η διαγραφή απέτυχε. Δεν έχετε δικαίωμα ακύρωσης της μετάδοσης", HttpStatus.BAD_REQUEST);
		}
		try {
			scheduleService.delete(id);
		}
		catch (Exception e) {
			return new ResponseEntity<>("Η διαγραφή απέτυχε ", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("", HttpStatus.ACCEPTED);
	}

	@RequestMapping(value= "/api/v1/schedule_table/dt/{id}", method = RequestMethod.GET, produces =  "application/json")
	public byte[] CreateScheduleTimeTable(@PathVariable("id") String id) {

		Schedule schedule = scheduleService.findById(id);
		boolean cancelLive = false;
		TimeTableResults timeTableResults = scheduleService.calculateExactDaysOfRegularSchedule(schedule,cancelLive);
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(timeTableResults);
		return b;
	}

	@RequestMapping(value= "/api/v1/schedule_table/set_cancellation/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> SetCancellation(@RequestBody Cancellation new_cancellation, @PathVariable("id") String id) {

		/* Authorize Cancellation */
		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean authorize = scheduleService.ApproveScheduledItemEdit(editor,id);// id = schedule_id
		if (!authorize) {
			return new ResponseEntity<>("Η ακύρωση απέτυχε. Δεν έχετε δικαίωμα ακύρωσης της μετάδοσης", HttpStatus.BAD_REQUEST);
		}

		int result = this.setFutureCancellation(id,new_cancellation);
		if (result == -1) {
			return new ResponseEntity<>("Η ακύρωση είναι ήδη ενεργή", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("", HttpStatus.ACCEPTED);
	}

	@RequestMapping(value= "/api/v1/schedule_table/unset_cancellation/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> UnsetCancellation(@RequestBody Cancellation rm_cancellation, @PathVariable("id") String id) {

		Schedule schedule = scheduleService.findById(id);
		List<Cancellation> cancellations = new ArrayList<>();
		if (schedule.getCancellations() != null && schedule.getCancellations().size()>0) {
			cancellations.addAll(schedule.getCancellations());
		}
		boolean found = false;
		for (Cancellation cancellation: cancellations) {
			LocalDate cancellation_date = cancellation.getDate();
			if (cancellation_date.isEqual(rm_cancellation.getDate())) {
				OverlapInfo overlapInfo = scheduleService.checkScheduleDateOverlapAgainstOneTimeSchedules(schedule,rm_cancellation.getDate());
				if (overlapInfo == null) {
					cancellations.remove(cancellation);
					found = true;
					break;
				}
				else {
					return new ResponseEntity<>("Αδύνατη η ενεργοποίηση. Υπάρχει αλληλοκάλυψη με ΕΚΤΑΚΤΗ ΜΕΤΑΔΟΣΗ", HttpStatus.BAD_REQUEST);
				}
			}
		}
		if (found) {
			schedule.setCancellations(cancellations);
			scheduleService.update(schedule);
			return new ResponseEntity<>("", HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>("Η ακύρωση είναι δεν βρέθηκε", HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value= "/api/v1/schedule_table/cancel_remaining/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> CancelRemainingDates(@RequestBody String reason, @PathVariable("id") String id) {

		try {
			scheduleService.cancelRegularScheduleRemainingDates(id, reason);
			return new ResponseEntity<>("", HttpStatus.ACCEPTED);
		}
		catch ( Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value= "/api/v1/schedule_table/unset_remaining/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> UnsetRemainingDates(@PathVariable("id") String id) {

		try {
			scheduleService.unsetScheduleRemainingDates(id);
			return new ResponseEntity<>("", HttpStatus.ACCEPTED);
		}
		catch ( Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value= "/api/v1/schedule_table/stream_cancellation/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> CancelLiveStream(@RequestBody Cancellation new_cancellation, @PathVariable("id") String id) {

		Resource resource = resourceService.findByIdInCollection(id,"Scheduler.Live");

		/* Authorize Cancellation */
		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		boolean authorize = scheduleService.ApproveTodaysScheduledItemEdit(editor,resource.getScheduleId());
		if (!authorize) {
			return new ResponseEntity<>("Η διακοπή απέτυχε. Δεν έχετε δικαίωμα διακοπής της μετάδοσης", HttpStatus.BAD_REQUEST);
		}
		//>>> Authorized => Proceed to Cancellation
		String schedule_id = resource.getScheduleId();
		logger.info(" STOP Live with ID: " + id + "and Schedule Id:" + schedule_id);
		int result = this.setNewCancellation(schedule_id,new_cancellation); //careful:: no auto schedule update. We will delete it ourselves
		/*if (result == -1) { Stream seems to be cancelled already. Ignore and continue...}*/

 		String live_server = streamingProperties.getLive_server_url();
		RestTemplate restTemplate = new RestTemplate();
		String postUrl = live_server + "/api/v1/live/" + id + "/stop/" + new_cancellation.isKeepFile();
		try {
			restTemplate.exchange(postUrl, HttpMethod.POST, null, String.class);
		}
		catch (RestClientException rce) {
			return new ResponseEntity<>("Η ακύρωση απέτυχε", HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>("", HttpStatus.ACCEPTED);
	}

	@RequestMapping(value= "/api/v1/schedule_table/confirm_password/{id}/cd/{cd}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> DeleteScheduleEntry(@PathVariable("id") String id, @PathVariable("cd") String code) {

		String result = "-1";
		Resource resource = resourceService.findByIdInCollection(id,"Scheduler.Live");
		if (resource != null) {
			if (resource.getBroadcastCode().equals(code)) {
				result = "1";
			}
		}
		return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
	}

	private int setNewCancellation(String id,Cancellation new_cancellation) {

		Schedule schedule = scheduleService.findById(id);
		List<Cancellation> cancellations = new ArrayList<>();
		if (schedule.getCancellations() != null && schedule.getCancellations().size()>0) {
			cancellations.addAll(schedule.getCancellations());
		}
		boolean found_already = false;
		for (Cancellation cancellation: cancellations) {
			LocalDate cancellation_date = cancellation.getDate();
			if (cancellation_date.isEqual(new_cancellation.getDate())) {
				found_already = true;
				break;
			}
		}
		if (!found_already) {
			cancellations.add(new_cancellation);
			schedule.setCancellations(cancellations);
			scheduleService.update(schedule);
			//scheduleService.updateWithNoChangeTrigger(schedule);
			return 0;
		}
		else {
			return -1;
		}
	}

	private int setFutureCancellation(String id,Cancellation new_cancellation) {

		Schedule schedule = scheduleService.findById(id);
		List<Cancellation> cancellations = new ArrayList<>();
		if (schedule.getCancellations() != null && schedule.getCancellations().size()>0) {
			cancellations.addAll(schedule.getCancellations());
		}
		boolean found_already = false;
		for (Cancellation cancellation: cancellations) {
			LocalDate cancellation_date = cancellation.getDate();
			if (cancellation_date.isEqual(new_cancellation.getDate())) {
				found_already = true;
				break;
			}
		}
		if (!found_already) {
			cancellations.add(new_cancellation);
			schedule.setCancellations(cancellations);
			scheduleService.update(schedule);
			return 0;
		}
		else {
			return -1;
		}
	}


}
