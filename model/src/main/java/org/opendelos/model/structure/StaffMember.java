/* 
     Author: Michael Gatzonis - 19/10/2020 
     live
*/
package org.opendelos.model.structure;


import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.Unit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "StaffMembers")
@Getter
@Setter
public class StaffMember {

	@Id
	protected String id;
	@Indexed(direction = IndexDirection.ASCENDING)
	protected String identity;
	protected Unit department;
	protected String uid;
	protected String name;
	protected String altName;
	protected String email;
	protected List<String> affiliation;
	protected List<String> courses;

}

