/* 
     Author: Michael Gatzonis - 27/1/2021 
     live
*/
package org.opendelos.model.scheduler;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.opendelos.model.users.UserAccess;

@Getter
@Setter
public class ScheduleQuery implements Serializable {

	private String queryString;
	protected String year;
	protected String departmentId;
	protected String departmentTitle;
	protected String schoolId;
	protected String schoolTitle;
	protected String studyProgramId;
	protected String courseId;
	protected String courseTitle;
	protected String supervisorId;
	protected String supervisorName;
	protected String scheduledEventId;
	protected String classroomId;
	protected String type;
	protected String repeat;
	protected String dayOfWeek;
	protected LocalDate date;
	protected String period;
	protected LocalDate fromDate;
	protected LocalDate toDate;
	protected String enabled;
	protected String event_type;
	protected String status_type;
	protected String sortBy;
	protected String sortDirection;
	protected int limit;

	private String managerId;
	//private String resourceTypeOnly;
	private boolean isSA;
	private boolean isManager;
	private boolean isSupport;
	private boolean isStaffMember;
	protected List<UserAccess.UserRights.CoursePermission> authorized_courses;
	protected List<UserAccess.UserRights.EventPermission> authorized_events;
	protected List<String> authorizedUnitIds;

	//Security Restrictions
	protected List<String> restrictedUnitIds;
	protected List<String> restrictedCourseIds;
	protected List<String> restrictedSupervisorIds;
	protected List<String> restrictedEventIds;

	protected boolean broadcastToChannel;
	protected boolean isBroadcasting;
}
