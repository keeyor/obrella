/* 
     Author: Michael Gatzonis - 1/2/2021 
     live
*/
package org.opendelos.model.scheduler;


import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeTableResults {

	protected String message_pauses;
	protected String message_cancellations;
	protected String message_overlaps;
	protected List<ScheduleDTO> results;
}
