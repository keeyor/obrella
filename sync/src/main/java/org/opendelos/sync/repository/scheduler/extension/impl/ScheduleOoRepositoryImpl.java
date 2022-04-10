/* 
     Author: Michael Gatzonis - 28/1/2021 
     live
*/
package org.opendelos.sync.repository.scheduler.extension.impl;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.ScheduleQuery;
import org.opendelos.model.users.UserAccess;
import org.opendelos.sync.repository.scheduler.extension.ScheduleOoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class ScheduleOoRepositoryImpl implements ScheduleOoRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public List<Schedule> search(ScheduleQuery scheduleQuery) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		//Security Restrictions
		if (scheduleQuery.isManager()) {
			Criteria expression = this.setManagerSecurityRestrictions(scheduleQuery);
			andExpression.add(expression);
		}
		else if (scheduleQuery.isSupport()) {
			Criteria expression = this.setSupportSecurityRestrictions(scheduleQuery);
			andExpression.add(expression);
		}
 		else if (scheduleQuery.isStaffMember()) {
			Criteria expression = this.setStaffMemberRestrictions(scheduleQuery);
			andExpression.add(expression);
		}
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
		//Channel Filter
		if (scheduleQuery.isBroadcastToChannel() ) {
			Criteria expression = new Criteria();
			expression.and("broadcastToChannel").is(true);
			andExpression.add(expression);
		}
		if (scheduleQuery.getSupervisorId() != null && !scheduleQuery.getSupervisorId().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("supervisor").is(scheduleQuery.getSupervisorId());
			andExpression.add(expression);
		}
		//<< sec
		if (scheduleQuery.getCourseId() != null && !scheduleQuery.getCourseId().equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("course").is(scheduleQuery.getCourseId());
			andExpression.add(expression);
		}
		if (scheduleQuery.getYear() !=null && !scheduleQuery.getYear().equals("_all")) {
			Criteria expression = new Criteria();
			if (!scheduleQuery.getYear().contains("-")) {
				expression.and("academicYear").is(scheduleQuery.getYear());
			}
			else {
				  String[] multiple_years = scheduleQuery.getYear().split("-");
				  List<Criteria> orCourseExpression =  new ArrayList<>();
				  for (String multiple_year : multiple_years) {
					orCourseExpression.add(Criteria.where("academicYear").is(multiple_year));
				  }
				  expression.orOperator(new Criteria().orOperator(orCourseExpression.toArray(new Criteria[orCourseExpression.size()])));
			}
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
		if (scheduleQuery.getSortBy() != null) {
			String sort_field = scheduleQuery.getSortBy();
			Sort sort;
			if (sort_field.equals("dateTime")) {
				if (scheduleQuery.getSortDirection() == null || scheduleQuery.getSortDirection().equals("asc")) {
					sort = Sort.by(new Sort.Order(Sort.Direction.ASC,sort_field),
							new Sort.Order(Sort.Direction.ASC,"startTime"));
				} else {
					sort = Sort.by(new Sort.Order(Sort.Direction.DESC,sort_field),
							new Sort.Order(Sort.Direction.DESC,"startTime"));
				}
			}
			else {
				if (scheduleQuery.getSortDirection() == null || scheduleQuery.getSortDirection().equals("asc")) {
					sort = Sort.by(Sort.Order.asc(scheduleQuery.getSortBy()));
				}
				else {
					sort = Sort.by(Sort.Order.desc(scheduleQuery.getSortBy()));
				}
			}
			query = query.with(sort);
		}
		if (scheduleQuery.getLimit() != 0) {
			query = query.limit(scheduleQuery.getLimit());
		}

		return mongoTemplate.find(query, Schedule.class);
	}

//Sort.by(Sort.Order.asc(scheduleQuery.getSortBy())).and(Sort.Order.asc( "startTime"));


	/*public List<Schedule> search(ScheduleQuery scheduleQuery) {

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
	}*/

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

	@Override
	public List<Schedule> findAllCourseReferencesInScheduler(String courseId, int limit) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		Criteria expression = new Criteria();
		expression.and("course").is(courseId);
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
	public List<Schedule> findAllScheduledEventsReferencesInScheduler(String eventId, int limit) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		Criteria expression = new Criteria();
		expression.and("event").is(eventId);
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

	private Criteria setStaffMemberRestrictions(ScheduleQuery scheduleQuery) {
		Criteria expression = new Criteria();
		List<Criteria> orCourseExpression =  new ArrayList<>();
		orCourseExpression.add(Criteria.where("supervisor").is(scheduleQuery.getManagerId()));
		expression.orOperator(new Criteria().orOperator(orCourseExpression.toArray(new Criteria[orCourseExpression.size()])));
		return expression;
	}

	private Criteria setManagerSecurityRestrictions(ScheduleQuery scheduleQuery) {
		Criteria expression = new Criteria();
		if (scheduleQuery.getAuthorizedUnitIds() != null && scheduleQuery.getAuthorizedUnitIds().size()>0 && !scheduleQuery.getAuthorizedUnitIds().contains("IGNORE_UNIT")) {
			List<Criteria> orCourseExpression =  new ArrayList<>();
			orCourseExpression.add(Criteria.where("department").in(scheduleQuery.getAuthorizedUnitIds()));
			 if (scheduleQuery.isStaffMember()) {
				orCourseExpression.add(Criteria.where("supervisor").is(scheduleQuery.getManagerId()));
			}
			expression.orOperator(new Criteria().orOperator(orCourseExpression.toArray(new Criteria[orCourseExpression.size()])));
		}
		return expression;
	}

	private Criteria setSupportSecurityRestrictions(ScheduleQuery scheduleQuery) {
		Criteria expression = new Criteria();
		List<Criteria> orCourseExpression =  new ArrayList<>();

		for (UserAccess.UserRights.CoursePermission coursePermission : scheduleQuery.getAuthorized_courses()) {
			Criteria course_expression = new Criteria();
			course_expression.andOperator(Criteria.where("course").is(coursePermission.getCourseId()),
					Criteria.where("supervisor").is(coursePermission.getStaffMemberId()));
			orCourseExpression.add(course_expression);
		}
		for (UserAccess.UserRights.EventPermission eventPermission : scheduleQuery.getAuthorized_events()) {
			Criteria event_expression = new Criteria();
			event_expression.andOperator(Criteria.where("event").is(eventPermission.getEventId()),
					Criteria.where("supervisor").is(eventPermission.getStaffMemberId()));
			orCourseExpression.add(event_expression);
		}

	 	if (scheduleQuery.isStaffMember()) {
			orCourseExpression.add(Criteria.where("supervisor").is(scheduleQuery.getManagerId()));
		}
		expression.orOperator(new Criteria().orOperator(orCourseExpression.toArray(new Criteria[orCourseExpression.size()])));

		return expression;
	}
}
