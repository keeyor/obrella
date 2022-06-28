/* 
     Author: Michael Gatzonis - 26/9/2020 
     live
*/
package org.opendelos.vodapp.api.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendelos.model.calendar.Argies;
import org.opendelos.model.calendar.Period;
import org.opendelos.model.calendar.Periods;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.common.Select2GenGroup;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.School;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.model.users.UserAccess;
import org.opendelos.vodapp.api.common.ApiUtils;
import org.opendelos.vodapp.services.i18n.MultilingualServices;
import org.opendelos.vodapp.services.opUser.OpUserService;
import org.opendelos.vodapp.services.structure.DepartmentService;
import org.opendelos.vodapp.services.structure.InstitutionService;
import org.opendelos.vodapp.services.structure.SchoolService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class InstitutionApi {

	private final InstitutionService institutionService;
	private final MultilingualServices multilingualServices;
	private final SchoolService schoolService;
	private final DepartmentService departmentService;
	private final OpUserService opUserService;
	@Value("${default.institution.identity}")
	String institution_identity;
	@Autowired
	Institution defaultInstitution;

	@Autowired
	public InstitutionApi(InstitutionService institutionService, MultilingualServices multilingualServices, SchoolService schoolService, DepartmentService departmentService, OpUserService opUserService) {
		this.institutionService = institutionService;
		this.multilingualServices = multilingualServices;
		this.schoolService = schoolService;
		this.departmentService = departmentService;
		this.opUserService = opUserService;
	}

	@RequestMapping(value = "/apiw/v1/institution/identity/{identity}", method = RequestMethod.GET)
	public ResponseEntity<Institution> findByIdentity(@PathVariable("identity") String identity) {

		Institution institution;
		institution = institutionService.findByIdentity(identity);
		if (institution != null) {
			return new ResponseEntity<>(institution, HttpStatus.ACCEPTED);
		}
		else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/apiw/v1/institution/update", consumes = "application/json")
	public ResponseEntity<Void> update(@RequestBody Institution institution, UriComponentsBuilder ucBuilder) {

		HttpHeaders headers = new HttpHeaders();
		try {
			institutionService.update(institution);
		}
		catch (Exception e) {
			return new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
		}
		headers.setLocation(ucBuilder.path("/{id}").buildAndExpand(institution.getId()).toUri());
		return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
	}

	@RequestMapping(value = "/apiw/v1/s2/units.web", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getAllUnitss2() {

		List<Select2GenGroup> select2GenGroupList = new ArrayList<>();
/*
		Institution institution = institutionService.findByIdentity(institution_identity);
		// Institution header
		Select2GenGroup select2InstitutionGroup = new Select2GenGroup();
		select2InstitutionGroup.setId("institution-dummy-header");
		select2InstitutionGroup.setText(institution.getTitle());
		// Institution child
		List<Select2GenChild> groupInstitutionChildren = new ArrayList<>();
		Select2GenChild select2GenChild = new Select2GenChild();
		select2GenChild.setId(institution.getId());
		select2GenChild.setText("-- 'Ολες οι Σχολές & και τα Τμήματα --");
		groupInstitutionChildren.add(select2GenChild);
 		select2InstitutionGroup.setChildren(groupInstitutionChildren);
		select2GenGroupList.add(select2InstitutionGroup);
*/

		List<School> schools = schoolService.findAllSortedByTitle();

		//School Group Header
		Select2GenGroup select2SchoolGroup = new Select2GenGroup();
		select2SchoolGroup.setId("schools-dummy-header");
		select2SchoolGroup.setText("Σχολές");

		List<Select2GenChild> groupSchoolsChildren = new ArrayList<>();
		for (School school : schools) {
			Select2GenChild select2GenChild = new Select2GenChild();
			select2GenChild.setId(school.getId());
			select2GenChild.setText(school.getTitle());
			select2GenChild.setSubheader("school");
			groupSchoolsChildren.add(select2GenChild);
		}
		select2SchoolGroup.setChildren(groupSchoolsChildren);
		select2GenGroupList.add(select2SchoolGroup);


		Select2GenGroup select2DepartmentGroup = new Select2GenGroup();
		select2DepartmentGroup.setId("departments-dummy-header");
		select2DepartmentGroup.setText("Τμήματα");
		List<Select2GenChild> groupDepartmentsChildren = new ArrayList<>();
		for (School school : schools) {
			//set children properties
			List<Department> schoolDepartments = departmentService.findBySchoolId(school.getId());
			for (Department department : schoolDepartments) {
				Select2GenChild select2GenChild = new Select2GenChild();
				select2GenChild.setId(department.getId());
				select2GenChild.setText(department.getTitle());
				select2GenChild.setSubheader("department");
				groupDepartmentsChildren.add(select2GenChild);
			}
		}
		select2DepartmentGroup.setChildren(groupDepartmentsChildren);
		select2GenGroupList.add(select2DepartmentGroup);


		try {
			//select2GenGroupList.sort(new TitleSorter());
			String s2departments = ApiUtils.FormatResultsForSelect2(select2GenGroupList);
			return new ResponseEntity<>(s2departments, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/apiw/v1/dt/units.web", method = RequestMethod.GET)
	public byte[] findAllForDt(Locale locale) {

		List<Unit> units = new ArrayList<>();
		//Institution institution = institutionService.findByIdentity(institution_identity);
		Unit inst_unit = new Unit(StructureType.INSTITUTION, defaultInstitution.getId(), multilingualServices.getValue("default.institution.title", locale));
		units.add(inst_unit);
		List<School> schools = schoolService.findAllSortedByTitle();
		for (School school : schools) {
			Unit unit = new Unit(StructureType.SCHOOL, school.getId(), school.getTitle());
			units.add(unit);
		}
		List<Department> departments = departmentService.findAll();
		for (Department department : departments) {
			Unit unit = new Unit(StructureType.DEPARTMENT, department.getId(), department.getTitle());
			units.add(unit);
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(units);
		return b;
	}

	@RequestMapping(value = "/apiw/v1/dt/authorized/units-inherited.web", method = RequestMethod.GET)
	public byte[] findAllAuthorizedUnitWithInheritanceForDt(Locale locale) {

		List<Unit> units = new ArrayList<>();
		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA"))) {
			return this.findAllForDt(locale);
		}

		List<UserAccess.UserRights.UnitPermission> editors_unit_permissions = opUserService.getManagersUnitPermissions(editor.getId());

		for (UserAccess.UserRights.UnitPermission unitPermission : editors_unit_permissions) {
			if (unitPermission.getUnitType().equals(UserAccess.UnitType.INSTITUTION)) {
				units.add(new Unit(StructureType.INSTITUTION, defaultInstitution.getId(), multilingualServices.getValue("default.institution.title", locale)));
				List<School> schools = schoolService.findAllSortedByTitle();
				for (School school : schools) {
					units.add(new Unit(StructureType.SCHOOL, school.getId(), defaultInstitution.getId(), school.getTitle()));
				}
				List<Department> departments = departmentService.findAll();
				for (Department department : departments) {
					units.add(new Unit(StructureType.DEPARTMENT, unitPermission.getUnitId(), department.getSchoolId(), department.getTitle()));
				}
				break;
			}
			else if (unitPermission.getUnitType().equals(UserAccess.UnitType.SCHOOL)) {
				Unit unit = new Unit(StructureType.SCHOOL, unitPermission.getUnitId(), defaultInstitution.getId(), unitPermission.getUnitTitle());
				units.add(unit);
				List<Department> departments = departmentService.findBySchoolId(unitPermission.getUnitId());
				for (Department department : departments) {
					units.add(new Unit(StructureType.DEPARTMENT, unitPermission.getUnitId(), department.getSchoolId(), department.getTitle()));
				}
			}
			else if (unitPermission.getUnitType().equals(UserAccess.UnitType.DEPARTMENT)) {
				Department department = departmentService.findById(unitPermission.getUnitId());
				units.add(new Unit(StructureType.DEPARTMENT, unitPermission.getUnitId(), department.getSchoolId(), unitPermission.getUnitTitle()));
			}
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(units);
		return b;
	}

	@RequestMapping(value = "/apiw/v1/dt/units-inherited.web", method = RequestMethod.GET)
	public byte[] findAllUnitWithInheritanceForDt(Locale locale) {

		List<Unit> units = new ArrayList<>();
		//Institution institution = institutionService.findByIdentity(institution_identity);
		Unit inst_unit = new Unit(StructureType.INSTITUTION, defaultInstitution.getId(), multilingualServices.getValue("default.institution.title", locale));
		units.add(inst_unit);
		List<School> schools = schoolService.findAllSortedByTitle();
		for (School school : schools) {
			Unit unit = new Unit(StructureType.SCHOOL, school.getId(), defaultInstitution.getId(), school.getTitle());
			units.add(unit);
		}
		List<Department> departments = departmentService.findAll();
		for (Department department : departments) {
			Unit unit = new Unit(StructureType.DEPARTMENT, department.getId(), department.getSchoolId(), department.getTitle());
			units.add(unit);
		}


		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(units);
		return b;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/apiw/v1/institution/{id}/calendar/update/{year}", consumes = "application/json", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> updateCalendar(@PathVariable("id") String id, @RequestBody String jsonString, @PathVariable("year") String year) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
		Periods periods;
		periods = mapper.readValue(jsonString, Periods.class);
		CustomPeriod customPeriod = new CustomPeriod();
		customPeriod.setYear(year);
		customPeriod.setPeriods(periods);
		try {
			institutionService.saveCustomPeriod(id, customPeriod);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}


	/* CALENDAR */
	@RequestMapping(method = RequestMethod.DELETE, value = "/apiw/v1/institution/{id}/calendar/delete/{year}")
	public ResponseEntity<Void> deleteCalendar(@PathVariable("id") String id, @PathVariable("year") String year) {

		HttpHeaders headers = new HttpHeaders();
		try {
			institutionService.deleteCustomPeriod(id, year);
		}
		catch (Exception e) {
			return new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/s2/institution/{id}/calendars")
	public ResponseEntity<String> getCalendars(@PathVariable("id") String id) {

		List<CustomPeriod> customPeriods;
		try {
			customPeriods = institutionService.getCustomPeriods(id);
			List<Select2GenChild> children = new ArrayList<>();
			for (CustomPeriod customPeriod : customPeriods) {
				Select2GenChild child = new Select2GenChild();
				String year = customPeriod.getYear();
				child.setId(year);
				int iyear = Integer.parseInt(year) + 1;
				child.setText("Ακαδημαϊκό Έτος: " + year + "-" + iyear);
				child.setSubheader(" ");
				children.add(child);
			}
			children.sort(new YearSorter());
			String s2calendars = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2calendars, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/s2/institution/{id}/calendar/{year}")
	public ResponseEntity<CustomPeriod> getCalendar(@PathVariable("id") String id, @PathVariable("year") String year) {

		CustomPeriod customPeriod;
		try {
			customPeriod = institutionService.getCustomPeriod(id, year);
			return new ResponseEntity<>(customPeriod, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/dt/institution/{id}/calendar/{year}")
	public byte[] getCalendarDt(@PathVariable("id") String id, @PathVariable("year") String year) {

		CustomPeriod customPeriod = institutionService.getCustomPeriod(id, year);
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(customPeriod);
		return b;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/apiw/v1/s2/institution/{id}/calendar-default/{year}")
	public ResponseEntity<String> createDefaultCalendar(@PathVariable("id") String id, @PathVariable("year") String year) {

		CustomPeriod institution_period = institutionService.getCustomPeriod(id, year);
		if (institution_period != null) {
			return new ResponseEntity<>("Το Ακαδημαϊκό Έτος υπάρχει ήδη", HttpStatus.BAD_REQUEST);
		}
		CustomPeriod customPeriod = new CustomPeriod();
		customPeriod.setYear(year);
		customPeriod.setPeriods(this.createDefaultPeriods(year));
		try {
			institutionService.saveCustomPeriod(id, customPeriod);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	private Periods createDefaultPeriods(String year) {

		int iyear = Integer.parseInt(year);
		//PERIOD names should be taken from properties files
		List<Period> periodList = new ArrayList<>();

		Period winter = new Period();
		winter.setName("winter");
		winter.setStartDate(iyear + "-09-01");
		winter.setEndDate((iyear + 1) + "-01-15");
		periodList.add(winter);

		Period intermediate = new Period();
		intermediate.setName("intervening");
		intermediate.setStartDate((iyear + 1) + "-01-16");
		intermediate.setEndDate((iyear + 1) + "-02-15");
		periodList.add(intermediate);

		Period spring = new Period();
		spring.setName("spring");
		spring.setStartDate((iyear + 1) + "-02-16");
		spring.setEndDate((iyear + 1) + "-07-31");
		periodList.add(spring);

		Period summer = new Period();
		summer.setName("summer");
		summer.setStartDate((iyear + 1) + "-08-01");
		summer.setEndDate((iyear + 1) + "-08-31");
		periodList.add(summer);

		Periods periods = new Periods();
		periods.setPeriod(periodList);

		return periods;
	}

	/* ARGIES */
	@RequestMapping(method = RequestMethod.DELETE, value = "/apiw/v1/institution/{id}/pause/delete/{year}")
	public ResponseEntity<Void> deletePause(@PathVariable("id") String id, @PathVariable("year") String year) {

		HttpHeaders headers = new HttpHeaders();
		try {
			institutionService.deleteCustomPause(id, year);
		}
		catch (Exception e) {
			return new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/s2/institution/{id}/pauses")
	public ResponseEntity<String> getPauses(@PathVariable("id") String id) {

		List<CustomPause> customPauses;
		try {
			customPauses = institutionService.getCustomPauses(id);
			List<Select2GenChild> children = new ArrayList<>();
			for (CustomPause customPause : customPauses) {
				Select2GenChild child = new Select2GenChild();
				String year = customPause.getYear();
				child.setId(year);
				int iyear = Integer.parseInt(year) + 1;
				child.setText("Ακαδημαϊκό Έτος: " + year + "-" + iyear);
				child.setSubheader(" ");
				children.add(child);
			}
			String s2calendars = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2calendars, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/s2/institution/{id}/pause/{year}")
	public ResponseEntity<CustomPause> getPause(@PathVariable("id") String id, @PathVariable("year") String year) {

		CustomPause customPause;
		try {
			customPause = institutionService.getCustomPause(id, year);
			return new ResponseEntity<>(customPause, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/dt/institution/{id}/pause/{year}")
	public byte[] getPauseDt(@PathVariable("id") String id, @PathVariable("year") String year) {

		CustomPause customPause = institutionService.getCustomPause(id, year);
		if (customPause == null) {
			customPause = new CustomPause();
			customPause.setYear(year);
			customPause.setArgies(new Argies());
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(customPause);
		return b;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/apiw/v1/institution/{id}/pause/update/{year}", consumes = "application/json", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> updatePause(@PathVariable("id") String id, @RequestBody String jsonString, @PathVariable("year") String year) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
		Argies argies;
		argies = mapper.readValue(jsonString, Argies.class);
		CustomPause customPause = new CustomPause();
		customPause.setYear(year);
		customPause.setArgies(argies);
		try {
			institutionService.saveCustomPause(id, customPause);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	private class YearSorter implements Comparator<Select2GenChild> {
		@Override
		public int compare(Select2GenChild o1, Select2GenChild o2) {
			return o2.getText().compareTo(o1.getText());
		}
	}

}
