/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.eventsapp.api.structure;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.eventsapp.api.common.ApiUtils;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Device;
import org.opendelos.eventsapp.services.opUser.OpUserService;
import org.opendelos.eventsapp.services.structure.ClassroomService;
import org.opendelos.eventsapp.services.structure.CourseService;
import org.opendelos.eventsapp.services.structure.DepartmentService;

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
public class ClassroomApi {

	private final ClassroomService classroomService;
	private final DepartmentService departmentService;
	private final OpUserService opUserService;
	private final CourseService courseService;

	@Autowired
	public ClassroomApi(ClassroomService classroomService, DepartmentService departmentService, OpUserService opUserService, CourseService courseService) {
		this.classroomService = classroomService;
		this.departmentService = departmentService;
		this.opUserService = opUserService;
		this.courseService = courseService;
	}

	@RequestMapping(value = "/api/v1/room/save", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveClassroom(@RequestBody Classroom classroom) {

		String _id;
		try {
			if (classroom.getId() == null || classroom.getId().equals("")) {
				classroom.setId(null);
				_id = classroomService.create(classroom);
			}
			else {
				classroomService.findAndUpdate(classroom);
				_id = classroom.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/room/save/dep_id/{did}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveClassroom(@RequestBody Classroom classroom, @PathVariable("did") String did) {

		String _id;
		try {
			if (classroom.getId() == null || classroom.getId().equals("")) {
				classroom.setId(null);
				_id = classroomService.create(classroom);
				departmentService.addClassroomToDepartment(did,_id);
			}
			else {
				 classroomService.findAndUpdate(classroom);
				_id = classroom.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/room/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteClassroom(@PathVariable("id") String id) {
		try {
			classroomService.delete(id);
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	//NOT USED
	@RequestMapping(value = "/api/v1/room/delete/{id}/dep_id/{did}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteClassroom(@PathVariable("id") String id,@PathVariable("did") String did) {
		try {
			classroomService.delete(id);
			departmentService.removeClassroomFromDepartment(did,id);
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/device/save/room_id/{roomId}/device_idx/{deviceIdx}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveDevice(@RequestBody Device device, @PathVariable("roomId") String roomId, @PathVariable("deviceIdx") int deviceIdx) {

		try {
			classroomService.SaveClassroomDevice(roomId, deviceIdx, device);
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/device/delete/room_id/{roomId}/device_idx/{deviceIdx}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> deleteDevice(@PathVariable("roomId") String roomId, @PathVariable("deviceIdx") int deviceIdx) {

		try {
			classroomService.DeleteClassroomDevice(roomId, deviceIdx);
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/room/devices/{id}", method = RequestMethod.GET, produces =  "application/json")
	public List<Device> getClassroomDevices(@PathVariable("id") String id) {
		try {
			 return classroomService.getClassroomDevices(id);
		}
		catch(Exception e) {
			return null;
		}
	}

	@RequestMapping(value="/api/v1/dt/rooms.web",method = RequestMethod.GET)
	public byte[] findAllClassroomsForDt() {

		List<Classroom> classrooms = classroomService.findAll();
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(classrooms);
		return b;
	}

	@RequestMapping(value="/api/v1/s2/class.web/staff/{sid}/course/{cid}/usage/{usage}", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAssignedClassrooms(@PathVariable("sid") String sid, @PathVariable("cid") String cid,@PathVariable("usage") String usage) {

		List<String> departmentIds = new ArrayList<>();
		String staff_dep_id = opUserService.findById(sid).getDepartment().getId();
		departmentIds.add(staff_dep_id);
		if (!cid.equals("all")) {
			String course_dep_id = courseService.findById(cid).getDepartment().getId();
			if (!staff_dep_id.equals(course_dep_id)) {
				departmentIds.add(course_dep_id);
			}
		}

		List<Classroom> classrooms = classroomService.n_findAssignedClassrooms(departmentIds, usage);

		List<Select2GenChild> children = new ArrayList<>();
		for (Classroom classroom: classrooms) {
			Select2GenChild child = new Select2GenChild();
			child.setId(classroom.getId());
			child.setText(classroom.getName());
			child.setSubheader(" " + classroom.getDescription());
			children.add(child);
		}
		try {
			String s2class= ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2class, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}

	}


	@RequestMapping(value="/api/v1/s2/class.web", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAllClassrooms() {

		List<Classroom> classroom_list = classroomService.findAll();

		List<Select2GenChild> children = new ArrayList<>();
		for (Classroom classroom: classroom_list) {
			Select2GenChild child = new Select2GenChild();
			child.setId(classroom.getId());
			child.setText(classroom.getName());
			child.setSubheader(" " + classroom.getDescription());
			children.add(child);
		}
		try {
			String s2class= ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2class, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}
	@RequestMapping(value="/api/v1/s2/class_enabled.web", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAllEnabledClassrooms() {

		List<Classroom> classroom_list = classroomService.findAllByCalendar("true");

		List<Select2GenChild> children = new ArrayList<>();
		for (Classroom classroom: classroom_list) {
			Select2GenChild child = new Select2GenChild();
			child.setId(classroom.getId());
			child.setText(classroom.getName());
			child.setSubheader(" " + classroom.getDescription());
			children.add(child);
		}
		try {
			String s2class= ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2class, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}
	@RequestMapping(value="/api/v1/s2/class.web/usage/{usage}", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAllClassroomsByUsage(@PathVariable String usage) {

		List<Classroom> classroom_list = classroomService.findAllByUsage(usage);

		List<Select2GenChild> children = new ArrayList<>();
		for (Classroom classroom: classroom_list) {
			Select2GenChild child = new Select2GenChild();
			child.setId(classroom.getId());
			child.setText(classroom.getName());
			child.setSubheader(" " + classroom.getDescription());
			children.add(child);
		}
		try {
			String s2class= ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2class, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}
}
