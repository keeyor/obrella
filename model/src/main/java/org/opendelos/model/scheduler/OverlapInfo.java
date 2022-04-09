/* 
     Author: Michael Gatzonis - 1/2/2021 
     live
*/
package org.opendelos.model.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverlapInfo {

	protected String msg;
	protected String title;
	protected String type;
	protected String repeat;
	protected DayOfWeek dayOfWeek;	/* for regular */
	protected LocalDate date;		/* for onetime */
	protected String startTime;
	protected String endTime;


	public String toJson() throws JsonProcessingException {
	    //Creating the ObjectMapper object
		 ObjectMapper mapper = new ObjectMapper();
		 //Converting the Object to JSONString
		return mapper.writeValueAsString(this);
	}
}
