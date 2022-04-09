/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.liveapp.api.structure;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.liveapp.api.common.ApiUtils;
import org.opendelos.liveapp.services.structure.SchoolService;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.structure.School;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SchoolApi {

	private final SchoolService schoolService;

	@Autowired
	public SchoolApi(SchoolService schoolService) {
		this.schoolService = schoolService;
	}

	@RequestMapping(value= "/api/v1/s2/schools.web", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAll() {
		List<School> schools = schoolService.findAllSortedByTitle();
		List<Select2GenChild> children = new ArrayList<>();
		for (School school : schools) {
			Select2GenChild child = new Select2GenChild();
			child.setId(school.getId());
			child.setText(school.getTitle());
			children.add(child);
		}
		try {
			String s2schools = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2schools, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(value="/api/v1/dt/schools.web",method = RequestMethod.GET)
	public byte[] findAllForDt() {

		List<School> schools = schoolService.findAllSortedByTitle();
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(schools);
		return b;
	}

	@RequestMapping(value = "/api/v1/school/save", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveSchool(@RequestBody School school) {

		String _id;
		try {
			if (school.getId() == null || school.getId().equals("")) {
				_id = schoolService.create(school);
			}
			else {
				schoolService.update(school);
				_id = school.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/school/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteSchool(@PathVariable("id") String id) {

		try {
			  schoolService.delete(id);
			  return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
