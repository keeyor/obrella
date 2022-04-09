/* 
     Author: Michael Gatzonis - 30/4/2021 
     obrella
*/
package org.opendelos.model.scheduler;


import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelProgram {

	long startTime;
	long endTime;
	String title;
	String description;
}
