/* 
     Author: Michael Gatzonis - 3/21/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.structure.Course;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Language;
import org.springframework.data.mongodb.core.mapping.TextScore;

@Document(collection = "opendelos.resources",language="en")
@Getter
@Setter
public class Resource {

    @Language
    String lang;

    @Id
    protected String id;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String identity;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String type;                          /* lecture, event, series (aka playLists) */
    @TextIndexed(weight=5)
    protected String title;
    @TextIndexed(weight=5)
    protected String description;
    protected boolean parts;
    protected int partNumber;
    protected String parentId;
    protected String[] relatedParts;
    protected String recTime;                       /* the exact time that the recording started :: use this instead in the title */

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String institution;
    protected String school;
    protected Unit department;
    protected Person supervisor;
    protected Person editor;
    protected String speakers;
    protected String ext_speakers;
    protected String language;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected Instant date;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected Instant dateModified;
    protected String[] topics;
    protected String[] categories;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String accessPolicy;
    protected String storage;
    protected String license;
    protected int statistics;
    protected ResourceStatus status;
    protected ResourceRealEditingStatus rteStatus;
    protected ResourceTags tags;
    protected PlayerOptions playerOptions;
    protected String realDuration;
    protected ResourceAccess resourceAccess;
    protected Presentation presentation;
    protected Presentation realEditingPresentation;
    protected Course course;
    protected ScheduledEvent event;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String classroom;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String period;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String academicYear;

    /*for LiveEntries */
    protected boolean broadcast;                     /* Pre-defined: YES, NO  */
    protected String access;                        /* Pre-defined: PUBLIC, SSO, PASSWORD */
    protected boolean recording;                     /* Pre-defined: YES, NO  */
    protected String publication;                   /* Pre-defined: PUBLIC, PRIVATE */
    protected String broadcastCode;
    protected String classroomName;
    protected String scheduleId;
    protected String streamingServerId;
    protected String streamingServerInfo;
    protected String recordingServerId;
    protected String streamId;
    protected String streamName;
    protected boolean broadcastToChannel;

    @TextScore
    private Float score;                        // MongoDB used for text query

    public String getIdentity() {
        if (identity == null || identity.equals("")) {
            return id;
        }
        return identity;
    }

    public String getStorage() {
        if (storage == null || storage.equals("")) {
            if (identity == null || identity.equals("")) {
                return id;
            }
            else {
                return identity; // compatibility with resources from eXist-db
            }
        }
        return storage;
    }

    public ResourceStatus getStatus() {
        if (status == null) {
            ResourceStatus status = new ResourceStatus();
            status.setInclMultimedia(-1);
            status.setInclPresentation(-1);
            status.setVideoSource("EDITOR");
        }
        return status;
    }
}
