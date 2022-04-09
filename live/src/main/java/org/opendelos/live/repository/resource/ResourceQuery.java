/* 
     Author: Michael Gatzonis - 12/26/2018 
     OpenDelosDAC
*/
package org.opendelos.live.repository.resource;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

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
    //secret
    private String date;
    // Security Constrains
    private boolean isSA;
    private boolean isManager;
    private boolean isStaffMember;
    private List<String> authorizedUnitIds;
}
