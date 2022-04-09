/* 
     Author: Michael Gatzonis - 19/11/2020 
     live
*/
package org.opendelos.model.common;

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
