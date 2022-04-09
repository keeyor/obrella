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
import org.opendelos.model.scheduler.common.Cancellation;
import org.opendelos.model.scheduler.common.YouTubeBroadcast;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Scheduler.Schedule")
@Getter
@Setter
public class Schedule {

    @Id
    protected String id;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String department;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String course;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String event;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String supervisor;

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String type;                              /* type = lecture, event */
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String repeat;                           /* type = REGULAR, EXTRA */

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String academicYear;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String period;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String classroom;

    @Indexed(direction = IndexDirection.ASCENDING)
    protected LocalDate date;
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
    protected String editor;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected LocalDateTime dateModified;

    protected boolean enabled;
    protected String disabledReason;

    protected boolean broadcastToChannel;
    protected YouTubeBroadcast youTubeBroadcast;
}
