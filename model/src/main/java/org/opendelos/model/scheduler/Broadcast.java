/* 
     Author: Michael Gatzonis - 3/21/2019 
     OpenDelosDAC
*/
package org.opendelos.model.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.scheduler.common.YouTubeBroadcast;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Scheduler.Broadcasts")
@Getter
@Setter
public class Broadcast {

    @Id
    protected String id;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String scheduleId;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String date;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String startTime;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected LocalDateTime dateModified;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String period;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String academicYear;

    /* Broadcast Parameters */
    protected boolean broadcast;
    protected String access;
    protected boolean recording;
    protected String publication;
    protected String accessPolicy;
    protected YouTubeBroadcast youTubeBroadcast;


    /* Resource Parameters */
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String type;                          /* course, event  */
    protected String referenceId;                   /* course | scheduledEvent Id */
    protected String classroomId;
    protected String supervisorId;
    protected boolean includesPresentation;

    protected String status;                        /* scheduled, live, completed, canceled, partially_cancelled */
    protected String realDuration;
    protected int parts;


}
