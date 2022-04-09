/* 
     Author: Michael Gatzonis - 3/2/2020 
     OpenDelosDAC - n
*/
package org.opendelos.model.scheduler.common;

import java.time.Instant;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cancellation {

	protected String title;
	protected LocalDate date;
	protected boolean keepFile;
}
