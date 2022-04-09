/* 
     Author: Michael Gatzonis - 14/10/2020 
     live
*/
package org.opendelos.model.scheduler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Scheduler.Calendar.Items")
@Getter
@Setter
public class CalendarItem {
	@Id
	protected String id;
	protected String mapId;
	protected LocalDate date;
	protected String unitId;
	protected String studyId;
	protected String dayId;
	protected String hourId;
	protected String minutes;
	protected String roomId;
	protected String record;
	protected String broadcast;
	protected String teacherId;
	protected String courseId;
	protected String periodId;
	protected String academicYear;
	protected String durationHours;
	protected String durationMinutes;
	protected String typeId;
	protected String organizationId;
	protected String actionAccessId;
	protected String comment;
	protected String publish;
	protected String screencast;
	protected String creatorId;
}
