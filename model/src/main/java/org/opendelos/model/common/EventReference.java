/* 
     Author: Michael Gatzonis - 27/9/2020 
     live
*/
package org.opendelos.model.common;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.Unit;

@Getter
@Setter
public class EventReference implements Serializable {

	private String id;
	private String title;
	private List<Unit> organizer;
	private Person supervisor;
	private Instant startDate;
	private Instant endDate;
	private String place;
	private String accessPolicy;
}