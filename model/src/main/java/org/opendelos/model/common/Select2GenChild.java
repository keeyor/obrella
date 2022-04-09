/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.model.common;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Select2GenChild implements Serializable {
	private String id;
	private String text;
	private String subheader;
}
