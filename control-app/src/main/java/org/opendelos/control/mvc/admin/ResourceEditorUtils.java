/* 
     Author: Michael Gatzonis - 11/12/2020 
     live
*/
package org.opendelos.control.mvc.admin;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.opendelos.control.services.structure.DepartmentService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Department;
import org.opendelos.model.properties.MultimediaProperties;
import org.opendelos.model.properties.StreamingProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceEditorUtils {

	private final DepartmentService departmentService;
	private final StreamingProperties streamingProperties;
	private final MultimediaProperties multimediaProperties;

	@Autowired
	public ResourceEditorUtils(DepartmentService departmentService, StreamingProperties streamingProperties, MultimediaProperties multimediaProperties) {
		this.departmentService = departmentService;
		this.streamingProperties = streamingProperties;
		this.multimediaProperties = multimediaProperties;
	}

	public Person getPersonFromOpUser(OpUser opUser)  {
		Person person = new Person();
		person.setId(opUser.getId());
		person.setAffiliation(opUser.getAffiliation());
		person.setName(opUser.getName());

		Department staff_department = departmentService.findById(opUser.getDepartment().getId());
		Unit staff_u = new Unit(org.opendelos.model.resources.StructureType.DEPARTMENT, staff_department.getId(), staff_department.getTitle());
		person.setDepartment(staff_u);

		return person;
	}
	public  Instant getDateTimeNow(String zone) {

		ZoneId z = ZoneId.of(zone);
		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ldt.atZone(z);

		return  zdt.toInstant();
	}

	public String getStreamingBaseUrl() {

		StringBuilder streamingBaseUrl = new StringBuilder();
		streamingBaseUrl.append(streamingProperties.getProtocol()).append("://").append(streamingProperties.getHost());
		if (!streamingProperties.getPort().equals("443")) {
				streamingBaseUrl.append(streamingProperties.getPort());
		}
		streamingBaseUrl.append(streamingProperties.getWebDir());

		return streamingBaseUrl.toString();
	}
	public String getMediaBaseUrl() {

		StringBuilder mediaBaseUrl = new StringBuilder();
		mediaBaseUrl.append(multimediaProperties.getProtocol()).append("://").append(multimediaProperties.getHost());
		if (!multimediaProperties.getPort().equals("443")) {
			mediaBaseUrl.append(multimediaProperties.getPort());
		}
		mediaBaseUrl.append(multimediaProperties.getWebDir());

		return mediaBaseUrl.toString();
	}
}
