/* 
     Author: Michael Gatzonis - 27/9/2020 
     live
*/
package org.opendelos.model.common;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassroomReference implements Serializable {

	private String id;
	private String name;
	private String code;

	public ClassroomReference() {
	}

	public ClassroomReference(String id, String name, String code) {
		this.id = id;
		this.name = name;
		this.code = code;
	}
}
