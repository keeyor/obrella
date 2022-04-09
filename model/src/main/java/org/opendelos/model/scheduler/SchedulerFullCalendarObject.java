/* 
     Author: Michael Gatzonis - 1/2/2021 
     live
*/
package org.opendelos.model.scheduler;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchedulerFullCalendarObject {

	protected String title;
	protected String resourceId;
	protected String start;
	protected String end;
	protected String borderColor;
	protected String textColor;
	protected String backgroundColor;
	protected String[] classNames;

	protected ExtendedProps extendedProps;

	@Getter
	@Setter
	public static class ExtendedProps {
		protected String type;
		protected String repeat;
		protected String supervisor;
		protected String department;
		protected String classroomName;
		protected boolean broadcast;
		protected String access;
		protected boolean recording;
		protected String publication;

		protected String supervisorId;
		protected String departmentId;
		protected String classroomId;
	}
}
