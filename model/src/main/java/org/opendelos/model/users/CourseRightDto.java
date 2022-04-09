/* 
     Author: Michael Gatzonis - 19/2/2021 
     live
*/
package org.opendelos.model.users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRightDto {

	protected String courseId;
	protected String courseTitle;
	protected String staffMemberId;
	protected String staffMemberName;
	protected String departmentTitle;
	protected boolean contentManager;
	protected boolean scheduleManager;
}
