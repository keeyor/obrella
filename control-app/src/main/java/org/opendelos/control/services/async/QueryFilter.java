/* 
     Author: Michael Gatzonis - 19/11/2020 
     live
*/
package org.opendelos.control.services.async;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryFilter {

	private String id;
	private String text;

	public QueryFilter() {
		this.id = "";
		this.text = "";
	}
}
