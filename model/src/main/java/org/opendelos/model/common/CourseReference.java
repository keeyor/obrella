/* 
     Author: Michael Gatzonis - 27/9/2020 
     live
*/
package org.opendelos.model.common;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.Unit;

@Getter
@Setter
public class CourseReference implements Serializable {

	private String id;
	private String title;
	private Person teacher;
	private Unit department;

}