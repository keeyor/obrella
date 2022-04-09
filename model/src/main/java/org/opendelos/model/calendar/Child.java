package org.opendelos.model.calendar;

/* 
Author: Michael Gatzonis - 1/11/2019 
OpenDelosDAC
*/

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Child implements Serializable {

	private String text;
	private String id;
	private String descr;

}
