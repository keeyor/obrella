/* 
     Author: Michael Gatzonis - 4/4/2021 
     obrella
*/
package org.opendelos.model.system;

import java.io.Serializable;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "System.Messages")
@Getter
@Setter
public class SystemMessage implements Serializable {
	@Id
	private String id;
	private String status;
	private String text;
	private boolean visible;
	private Instant startDate;
	private Instant finishDate;
}
