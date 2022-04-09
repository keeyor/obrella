/* 
     Author: Michael Gatzonis - 13/11/2020 
     live
*/
package org.opendelos.liveapp.repository.structure.extension.impl;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.liveapp.repository.structure.extension.ClassroomOoRepository;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class ClassroomOoRepositoryImpl implements ClassroomOoRepository {

	private final MongoTemplate mongoTemplate;

	@Autowired
	public ClassroomOoRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public void findAndUpdate(Classroom classroom) {

		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(classroom.getId()));
		Update update = new Update();
		update.set("name", classroom.getName());
		update.set("code", classroom.getCode());
		update.set("usage", classroom.getUsage());
		update.set("description", classroom.getDescription());
		if (classroom.getAvailableTo() != null) {
			update.set("availableTo",classroom.getAvailableTo());
		}
		//Check if a valid device eXists. Disable classroom Calendar if not
		if (classroom.getDevices() != null) {
			Device device = classroom.getDevices().get(0);
			if ( (device.getType() == null || device.getType().trim().equals("")) ||
				 //(device.getDescription() == null || device.getDescription().trim().equals("")) ||
				 (device.getStreamAccessUrl() == null || device.getStreamAccessUrl().trim().equals(""))) {
				update.set("calendar", "false");
			}
			else {
				update.set("calendar", classroom.getCalendar());
			}
			update.set("devices", classroom.getDevices());
		}
		else {
			update.set("calendar", "false");
		}
		mongoTemplate.findAndModify(query, update, Classroom.class);
	}

	@Override
	public List<Classroom> findAllExcludingIds(List<String> ids) {
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		Criteria expression = new Criteria();
		expression.and("id").nin(ids);
		andExpression.add(expression);

		Query query = new Query();
		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);

		return mongoTemplate.find(query, Classroom.class);
	}

	@Override
	public List<Classroom> findByUsage(String usage) {
		if (usage.equals("both")) {
			return mongoTemplate.findAll(Classroom.class);
		}
		else {
			Query query = new Query();
			Criteria andCriteria = new Criteria();
			List<Criteria> Expression =  new ArrayList<>();

			Criteria expression = new Criteria();
			expression.orOperator(Criteria.where("usage").is(usage), Criteria.where("usage").is("both"));
			Expression.add(expression);

			//# Get only classrooms with enabled calendar
			expression = new Criteria();
			expression.and("calendar").is("true");
			Expression.add(expression);

			if (!Expression.isEmpty()) {
				query.addCriteria(andCriteria.andOperator(Expression.toArray(new Criteria[Expression.size()])));
			}
			return mongoTemplate.find(query, Classroom.class);
		}
	}

	@Override
	public List<Classroom> n_findAssignedClassrooms(List<String> departmentIds, String usage) {
		Query query = new Query();
		List<Criteria> andExpression =  new ArrayList<>();

		if (usage != null) {
			Criteria expression = new Criteria();
			expression.orOperator(Criteria.where("usage").is(usage), Criteria.where("usage").is("both"));
			andExpression.add(expression);
		}

		 //# Get only classrooms with enabled calendar
		 Criteria expression = new Criteria();
		 expression.and("calendar").is("true");
		 andExpression.add(expression);

		if (!andExpression.isEmpty()) {
			Criteria andCriteria = new Criteria();
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}

		return mongoTemplate.find(query, Classroom.class);

	}

	@Override
	public void updateClassroomDevice(String id, int device_idx, Device device) {

		Classroom classroom = mongoTemplate.findById(id,Classroom.class);
		List<Device> deviceList;
		assert classroom != null;
		if (classroom.getDevices() == null) {
			deviceList = new ArrayList<>();
			classroom.setDevices(deviceList);
		}
		if (device_idx == 99) { // new device
			classroom.getDevices().add(device);
		}
		else {
			classroom.getDevices().set(device_idx,device);
		}
		mongoTemplate.save(classroom);
	}
	@Override
	public void deleteClassroomDevice(String id, int device_idx) {

		Classroom classroom = mongoTemplate.findById(id,Classroom.class);
		assert classroom != null;
		classroom.getDevices().remove(device_idx);
		mongoTemplate.save(classroom);
	}
	@Override
	public List<Device> getClassroomDevices(String id) {

		List<Device> deviceList = new ArrayList<>();
		Classroom classroom = mongoTemplate.findById(id,Classroom.class);
		if (classroom != null && classroom.getDevices() != null) {
			deviceList.addAll(classroom.getDevices());
		}
		return deviceList;
	}

	@Override
	public String getClassroomNameById(String id) {
		String classroomName = "Not Found";
		Classroom classroom = mongoTemplate.findById(id,Classroom.class);
		if (classroom != null && classroom.getName() != null) {
			classroomName = classroom.getName();
		}
		return classroomName;
	}

}
