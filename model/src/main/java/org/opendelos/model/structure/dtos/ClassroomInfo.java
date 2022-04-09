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

	public ClassroomInfo(String id, String name, String code) {
		this.id = id;
		this.name = name;
		this.code = code;
	}
	public ClassroomInfo() {
		this.id = "";
		this.name = "";
	}
}
