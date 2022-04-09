/* 
     Author: Michael Gatzonis - 12/26/2018 
     OpenDelosDAC
*/
package org.opendelos.model.repo;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.users.UserAccess;

@Getter
@Setter
public class ResourceQuery implements Serializable {

    private String ft;
    private String queryString;
    private String schoolId;
    private String departmentId;
    private String staffMemberId;
    private String resourceType;
    private String courseId;
    private String eventId;
    private String clipId;
    private String categoryCode;
    private String accessPolicy;
    private String videoSource;
    private String period;
    private String study;
    private String academicYear;
    private long totalResults;
    private int limit;
    private long skip;
    private String sort;
    private String direction;
    private String display;
    private String editorId;
    private String tag;
    private String classroomId;
    private String collectionName;
    private boolean uniqueOnly;
    private boolean broadcastToChannel;
    private String eventType;
    private String eventArea;
    private boolean isFeatured;
    private String isActive;

    private String date;
    private Instant instantDate;

    // Security Constrains
    private String managerId;
    private boolean isSA;
    private boolean isManager;
    private boolean isSupport;
    private boolean isStaffMember;

    protected List<UserAccess.UserRights.CoursePermission> authorized_courses;
    protected List<UserAccess.UserRights.EventPermission> authorized_events;
    protected List<String> authorizedUnitIds;
}
