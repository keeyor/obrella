/* 
     Author: Michael Gatzonis - 28/1/2021 
     live
*/
package org.opendelos.live.repository.scheduler.extension.impl;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.ScheduleQuery;
import org.opendelos.live.repository.scheduler.extension.ScheduleOoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class ScheduleOoRepositoryImpl implements ScheduleOoRepository  {

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public List<Schedule> search(ScheduleQuery scheduleQuery) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		//Security Restrictions
		//1. Restrict Lectures
		if (scheduleQuery.getRestrictedUnitIds() != null ) {
			Criteria expression = new Criteria();
			expression.and("department").in(scheduleQuery.getRestrictedUnitIds());
			andExpression.add(expression);
		}
		if (scheduleQuery.getRestrictedCourseIds() != null ) {
			Criteria expression = new Criteria();
			expression.and("course").in(scheduleQuery.getRestrictedCourseIds());
			andExpression.add(expression);
		}
		else if (scheduleQuery.getRestrictedEventIds() != null ) {
			Criteria expression = new Criteria();
			expression.and("event").in(scheduleQuery.getRestrictedEventIds());
			andExpression.add(expression);
		}
		if (scheduleQuery.getSupervisorId() != null ) {
			Criteria expression = new Criteria();
			expression.and("supervisor").is(scheduleQuery.getSupervisorId());
			andExpression.add(expression);
		}
		//<< sec
		if (scheduleQuery.getYear() !=null && !scheduleQuery.getYear().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("academicYear").is(scheduleQuery.getYear());
			andExpression.add(expression);
		}
		if (scheduleQuery.getDepartmentId() !=null && !scheduleQuery.getDepartmentId().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("department").is(scheduleQuery.getDepartmentId());
			andExpression.add(expression);
		}
		if (scheduleQuery.getClassroomId() !=null && !scheduleQuery.getClassroomId().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("classroom").is(scheduleQuery.getClassroomId());
			andExpression.add(expression);
		}
		if (scheduleQuery.getType() !=null && !scheduleQuery.getType().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("type").is(scheduleQuery.getType());
			andExpression.add(expression);
		}
		if (scheduleQuery.getRepeat() !=null && !scheduleQuery.getRepeat().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("repeat").is(scheduleQuery.getRepeat());
			andExpression.add(expression);
		}
		if (scheduleQuery.getDayOfWeek() !=null && !scheduleQuery.getDayOfWeek().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("dayOfWeek").is(scheduleQuery.getDayOfWeek());
			andExpression.add(expression);
		}
		if (scheduleQuery.getPeriod() !=null && !scheduleQuery.getPeriod().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("period").is(scheduleQuery.getPeriod());
			andExpression.add(expression);
		}
		if (scheduleQuery.getEnabled() !=null && !scheduleQuery.getEnabled().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("enabled").is(Boolean.parseBoolean(scheduleQuery.getEnabled()));
			andExpression.add(expression);
		}
		if (scheduleQuery.getDate() !=null) {
			Criteria expression = new Criteria();
			expression.and("date").is(scheduleQuery.getDate().atStartOfDay());
			andExpression.add(expression);
		}

		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}

		return mongoTemplate.find(query, Schedule.class);
	}

	@Override
	public List<Schedule> findAllUserIdReferencesInScheduler(String id, int limit) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		Criteria expression = new Criteria();
		expression.orOperator(Criteria.where("supervisor").is(id),
				Criteria.where("editor").is(id));
		andExpression.add(expression);

		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		if (limit != -1) {
			return mongoTemplate.find(query, Schedule.class);
		}
		else {
			return mongoTemplate.find(query.limit(limit), Schedule.class);
		}
	}


	@Override
	public List<Schedule> findAllClassroomReferencesInScheduler(String classroomId, int limit) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		Criteria expression = new Criteria();
		expression.and("classroom").is(classroomId);
		andExpression.add(expression);

		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		if (limit != -1) {
			return mongoTemplate.find(query, Schedule.class);
		}
		else {
			return mongoTemplate.find(query.limit(limit), Schedule.class);
		}
	}

}
