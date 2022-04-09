/* 
     Author: Michael Gatzonis - 26/1/2021 
     live
*/
package org.opendelos.model.resources.dtos;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduledEventInfo implements Serializable  {

	private String id;
	private String title;
	private String supervisor;

	public ScheduledEventInfo(String id, String title, String supervisor) {
		this.id = id;
		this.title = title;
		this.supervisor = supervisor;
	}
	public ScheduledEventInfo(String id, String title) {
		this.id = id;
		this.title = title;
	}
	public ScheduledEventInfo() {
		this.id = "";
		this.title = "";
	}
}
