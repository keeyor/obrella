/* 
     Author: Michael Gatzonis - 19/2/2021 
     live
*/
package org.opendelos.model.users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduledEventRightDto {

	protected String eventId;
	protected String eventTitle;
	protected String staffMemberId;
	protected String staffMemberName;
	protected boolean contentManager;
	protected boolean scheduleManager;
}
