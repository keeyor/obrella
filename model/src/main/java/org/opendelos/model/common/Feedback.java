/* 
     Author: Michael Gatzonis - 23/3/2022 
     obrella
*/
package org.opendelos.model.common;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "opendelos.feedback",language="en")
@Getter
@Setter
public class Feedback {

	@Id
	protected String id;
	private String name;
	private String email;
	private String feedback;
	private String site;
}
