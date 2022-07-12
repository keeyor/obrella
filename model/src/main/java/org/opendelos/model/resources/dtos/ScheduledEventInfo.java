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
	private boolean active;

	public ScheduledEventInfo(String id, String title, String supervisor, boolean active) {
		this.id = id;
		this.title = title;
		this.supervisor = supervisor;
		this.setActive(active);
	}
	public ScheduledEventInfo(String id, String title, boolean active) {
		this.id = id;
		this.title = title;
		this.setActive(active);
	}
	public ScheduledEventInfo() {
		this.id = "";
		this.title = "";
	}
}
