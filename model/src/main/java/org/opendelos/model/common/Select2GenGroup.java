/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.model.common;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Select2GenGroup {
	private String id;
	private String text;
	private List<Select2GenChild> children;

	public List<Select2GenChild> getChildren() {

		if (children == null) {
			children = new ArrayList<>();
		}
		return children;
	}
}
