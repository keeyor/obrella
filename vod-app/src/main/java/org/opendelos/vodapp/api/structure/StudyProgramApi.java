/* 
     Author: Michael Gatzonis - 2/10/2020 
     live
*/
package org.opendelos.vodapp.api.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendelos.model.calendar.Periods;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.StudyProgram;
import org.opendelos.model.structure.dtos.StudyProgramDto;
import org.opendelos.vodapp.api.common.ApiUtils;
import org.opendelos.vodapp.services.i18n.MultilingualServices;
import org.opendelos.vodapp.services.structure.DepartmentService;
import org.opendelos.vodapp.services.structure.InstitutionService;
import org.opendelos.vodapp.services.structure.StudyProgramService;

import org.springframework.beans.BeanUtils;
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
public class StudyProgramApi {

	private final StudyProgramService studyProgramService;
	private final MultilingualServices multilingualServices;
	private final DepartmentService departmentService;
	private final InstitutionService institutionService;


	@Autowired
	public StudyProgramApi(StudyProgramService studyProgramService, MultilingualServices multilingualServices, DepartmentService departmentService, InstitutionService institutionService) {
		this.studyProgramService = studyProgramService;
		this.multilingualServices = multilingualServices;
		this.departmentService = departmentService;
		this.institutionService = institutionService;
	}

	@RequestMapping(value = "/apiw/v1/dt/programs.web/school/{schoolId}/department/{departmentId}/study/{study}", method = RequestMethod.GET)
	public byte[] findProgramsWithCriteria(@PathVariable("schoolId") String schoolId,
			@PathVariable("departmentId") String departmentId,
			@PathVariable("study") String study, Locale locale) {

		List<StudyProgramDto> studyProgramDtoList = new ArrayList<>();
		List<StudyProgram> studyPrograms = studyProgramService.findWithCriteria(schoolId, departmentId, study);

		for (StudyProgram studyProgram : studyPrograms) {
			StudyProgramDto studyProgramDto = new StudyProgramDto();
			BeanUtils.copyProperties(studyProgram, studyProgramDto);
			String department_program = departmentService.findById(studyProgram.getDepartmentId()).getTitle();
			studyProgramDto.setDepartmentName(department_program);

			String studyTitle = multilingualServices.getValue(studyProgram.getStudy(), locale);
			studyProgramDto.setStudyTitle(studyTitle);
			studyProgramDtoList.add(studyProgramDto);
		}
		//25-20-2021 :: NOT NEEDED Since I added under graduate studies to all departments
/*		if (study.equals("_all") || study.equals("under")) {
				StudyProgramDto studyProgramDto = new StudyProgramDto();
				studyProgramDto.setId("program_default");
				studyProgramDto.setIdentity("-1");
				studyProgramDto.setStudy("_all");
				studyProgramDto.setStudyTitle("Προπτυχιακές");
			if (!departmentId.equals("_all")) {
				studyProgramDto.setDepartmentId(departmentId);
				Department department = departmentService.findById(departmentId);
				studyProgramDto.setDepartmentName(department.getTitle());
			}
			else {
				studyProgramDto.setDepartmentId("_all");
				studyProgramDto.setDepartmentName("'Ολα τα Τμήματα");
			}
			studyProgramDto.setSchoolId("_all");
			studyProgramDto.setTitle("Προπτυχιακό Πρόγραμμα Σπουδών");
			studyProgramDtoList.add(studyProgramDto);
		}*/

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(studyProgramDtoList);
		return b;

	}

	@RequestMapping(value = "/apiw/v1/s2/programs.web/department/{departmentId}", method = RequestMethod.GET, produces = "application/json")
	public String getProgramsByDepartmentIds2(@PathVariable("departmentId") String departmentId) {

		List<StudyProgram> programList;
		if (!departmentId.equals("_all")) {
			programList = studyProgramService.findByDepartmentId(departmentId);
		}
		else {
			programList = new ArrayList<>();
		}

		List<Select2GenChild> children = new ArrayList<>();
		for (StudyProgram studyProgram : programList) {
			Select2GenChild child = new Select2GenChild();
			child.setId(studyProgram.getId());
			child.setText(studyProgram.getTitle());
			children.add(child);
		}
		// Add default programStudy
		Select2GenChild child = new Select2GenChild();
		child.setId("program_default");
		child.setText("Προπτυχιακό");
		children.add(child);
		try {
			return ApiUtils.FormatResultsForSelect2(children);
		}
		catch (Exception e) {
			return null;
		}

	}

	@RequestMapping(value = "/apiw/v1/s21/programs.web/department/{departmentId}", method = RequestMethod.GET, produces = "application/json")
	public String getProgramsByDepartmentIds21(@PathVariable("departmentId") String departmentId) {

		List<StudyProgram> programList;
		if (!departmentId.equals("_all")) {
			programList = studyProgramService.findByDepartmentId(departmentId);
		}
		else {
			programList = new ArrayList<>();
		}

		List<Select2GenChild> children = new ArrayList<>();
		for (StudyProgram studyProgram : programList) {
			Select2GenChild child = new Select2GenChild();
			child.setId(studyProgram.getId());
			child.setText(studyProgram.getTitle());
			children.add(child);
		}
		try {
			return ApiUtils.FormatResultsForSelect2(children);
		}
		catch (Exception e) {
			return null;
		}

	}

	@RequestMapping(value = "/apiw/v1/s3/programs.web/department/{departmentId}", method = RequestMethod.GET, produces = "application/json")
	public String getProgramsByDepartmentIdentitys2(@PathVariable("departmentId") String departmentId) {

		List<StudyProgram> programList;
		if (!departmentId.equals("_all")) {
			programList = studyProgramService.findByDepartmentIdentity(departmentId);
		}
		else {
			programList = new ArrayList<>();
		}
		List<Select2GenChild> children = new ArrayList<>();
		for (StudyProgram studyProgram : programList) {
			Select2GenChild child = new Select2GenChild();
			child.setId(studyProgram.getId());
			child.setText(studyProgram.getTitle());
			children.add(child);
		}
		try {
			return ApiUtils.FormatResultsForSelect2(children);
		}
		catch (Exception e) {
			return null;
		}
	}

	@RequestMapping(value = "/apiw/v1/programs/save", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveStudyProgram(@RequestBody StudyProgram studyProgram) {

		String _id = null;
		try {
			if (studyProgram.getId() == null || studyProgram.getId().equals("")) {
				_id = studyProgramService.create(studyProgram);
			}
			else {
				studyProgramService.findAndUpdate(studyProgram); //to preserver identity field if exists
				_id = studyProgram.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(_id, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/apiw/v1/programs/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteStudyProgram(@PathVariable("id") String id) {

		try {
			studyProgramService.delete(id);
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}


	/* CALENDAR ACTION */
	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/dt/institution/{iid}/department/{did}/program/{id}/calendar/{year}")
	public byte[] getCalendarDt(@PathVariable("iid") String iid, @PathVariable("did") String did, @PathVariable("id") String id, @PathVariable("year") String year) {

		CustomPeriod customPeriod;
		if (id.equals("dummy")) {
			String json = "{" + " \"data\":" + "[]" + "}";
			return json.getBytes();
		}
		else {
			customPeriod = studyProgramService.getCustomPeriod(id, year);
			if (customPeriod == null) {
				customPeriod = departmentService.getCustomPeriod(did, year);
				if (customPeriod == null) {
					customPeriod = institutionService.getCustomPeriod(iid, year);
				}
				customPeriod.setInherited(true);
			}
			else {
				customPeriod.setInherited(false);
			}

		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(customPeriod);
		return b;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/apiw/v1/programs/{id}/calendar/update/{year}", consumes = "application/json", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> updateCalendar(@PathVariable("id") String id, @RequestBody String jsonString, @PathVariable("year") String year) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
		Periods periods;
		periods = mapper.readValue(jsonString, Periods.class);
		CustomPeriod customPeriod = new CustomPeriod();
		customPeriod.setYear(year);
		customPeriod.setPeriods(periods);
		customPeriod.setInherited(true);
		try {
			studyProgramService.saveCustomPeriod(id, customPeriod);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/apiw/v1/programs/{id}/calendar/reset/{year}", consumes = "application/json", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> resetCalendar(@PathVariable("id") String id, @PathVariable("year") String year) throws JsonProcessingException {

		try {
			studyProgramService.deleteCustomPeriod(id, year);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
