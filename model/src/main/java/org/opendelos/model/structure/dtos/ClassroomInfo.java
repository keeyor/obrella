/* 
     Author: Michael Gatzonis - 26/1/2021 
     live
*/
package org.opendelos.model.structure.dtos;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassroomInfo implements Serializable {

	private String id;
	private String name;
	private String code;
	private boolean active;

	public ClassroomInfo(String id, String name, String code, boolean active) {
		this.id = id;
		this.name = name;
		this.code = code;
		this.active = active;
	}
	public ClassroomInfo() {
		this.id = "";
		this.name = "";
	}
}
