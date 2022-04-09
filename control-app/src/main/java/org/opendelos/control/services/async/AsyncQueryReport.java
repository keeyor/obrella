/* 
     Author: Michael Gatzonis - 17/11/2020 
     live
*/
package org.opendelos.control.services.async;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.common.AdminFilterResults;
import org.opendelos.model.resources.AccessPolicy;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.ResourceType;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.resources.dtos.AreaDto;
import org.opendelos.model.resources.dtos.TypeDto;
import org.opendelos.model.structure.Course;

import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class AsyncQueryReport {

	private Map<String, Course> 		CourseFilterResults;
	private Map<String, Person> 		StaffMemberFilterResults;
	private Map<String, Unit> 			DepartmentFilterResults;
	private Map<String, ScheduledEvent> ScheduledEventsFilterResults;
	private Map<String, Integer>  		CategoryFilterResults;
	private	Map<String, ResourceType> 	ResourceTypeFilterResults;
	private	Map<String, AccessPolicy>   AccessPolicyFilterResults;
	private	Map<String, AreaDto>   		AreaFilterResults;
	private	Map<String, TypeDto>   		TypeFilterResults;
	private AdminFilterResults adminFilterResults = new AdminFilterResults();
}
