/* 
     Author: Michael Gatzonis - 15/3/2022 
     obrella
*/
package org.opendelos.model.users;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActiveUserStore {

	public List<String> users;

	public ActiveUserStore() {
		users = new ArrayList<String>();
	}
}
