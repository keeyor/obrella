/* 
     Author: Michael Gatzonis - 5/7/2019 
     OpenDelosDAC
*/
package org.opendelos.model.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.calendar.Argia;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.resources.dtos.ScheduledEventInfo;
import org.opendelos.model.scheduler.common.Cancellation;
import org.opendelos.model.scheduler.common.YouTubeBroadcast;
import org.opendelos.model.structure.dtos.ClassroomInfo;
import org.opendelos.model.structure.dtos.CourseInfo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
public class ScheduleDTO {

    @Id
    protected String id;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected Unit department;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected CourseInfo course;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected ScheduledEventInfo scheduledEvent;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected Person supervisor;

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String type;                    /* type = lecture, event */
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String repeat;                           /* type = regular, onetime */

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String academicYear;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String period;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected ClassroomInfo classroom;

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String date;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected DayOfWeek dayOfWeek;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String startTime;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected LocalTime endTime;
    protected int durationHours;
    protected int durationMinutes;

    protected boolean broadcast;                     /* Pre-defined: YES, NO  */
    protected String access;                        /* Pre-defined: PUBLIC, SSO, PASSWORD */
    protected boolean recording;                     /* Pre-defined: YES, NO  */
    protected String publication;                   /* Pre-defined: PUBLIC, PRIVATE */
    protected String broadcastCode;

    protected List<Cancellation> cancellations;

    @Indexed(direction = IndexDirection.ASCENDING)
    protected Person editor;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected LocalDateTime dateModified;

    protected Boolean enabled;

    protected String fromDate;
    protected String toDate;
    protected Argia argia;                      /* flag for argia in particular date */
    protected Cancellation cancellation;        /* flag for cancellation in particular date */
    protected OverlapInfo overlapInfo;

    protected boolean broadcastToChannel;
    protected YouTubeBroadcast youTubeBroadcast;
    protected String broadcast_time;

    protected String photoRelativeUrl; /* for events */
}
