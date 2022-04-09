/* 
     Author: Michael Gatzonis - 22/2/2021 
     live
*/
package org.opendelos.eventsapp.repository.scheduledEvent.extension.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.opendelos.eventsapp.repository.scheduledEvent.extension.ScheduledEventOoRepository;
import org.opendelos.model.repo.QueryScheduledEventsResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.users.UserAccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;

public class ScheduledEventOoRepositoryImpl implements ScheduledEventOoRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public QueryScheduledEventsResults searchPageableScheduledEvents(ResourceQuery resourceQuery) {

		List<ScheduledEvent> scheduledEventList;
		Query query = new Query();

		if (resourceQuery.getFt() != null && !resourceQuery.getFt().isEmpty()) {
			TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(resourceQuery.getFt());
			query = TextQuery.queryText(criteria).sortByScore();
		}
		List<Criteria> andExpression =  setQueryCriteria(resourceQuery);

		if (!andExpression.isEmpty()) {
			Criteria andCriteria = new Criteria();
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		String direction = resourceQuery.getDirection();
		String field = null;
		switch (resourceQuery.getSort()) {
		case "title":
			field = "title";
			break;
		case "date":
			field = "startDate";
			break;
		case "dateModified":
			field = "dateModified";
			break;
		case "rel":
			field = "text-search";
		}

		if (field != null && !field.equals("text-search")) {
			Sort sort;
			if (direction.equals("asc")) {
				sort = Sort.by(Sort.Order.asc(field));
			} else {
				sort = Sort.by(Sort.Order.desc(field));
			}
			query = query.with(sort);
		}

		int limit = resourceQuery.getLimit();
		long skip = resourceQuery.getSkip();

		long count;

		 count = mongoTemplate.count(query, ScheduledEvent.class);
		 scheduledEventList = mongoTemplate.find(query.limit(limit).skip(skip), ScheduledEvent.class);


		QueryScheduledEventsResults queryResults = new QueryScheduledEventsResults();
		queryResults.setLimit(limit);
		queryResults.setSkip(skip);
		queryResults.setSort(field);
		queryResults.setDirection(direction);
		queryResults.setSearchResultList(scheduledEventList);
		queryResults.setTotalResults(count);

		return queryResults;
	}

	@Override
	public List<ScheduledEvent> findAllByEditorId(String editorId, int limit) {
		ObjectId objID = new ObjectId(editorId);
		Query query = new Query();
		query.addCriteria(Criteria.where("editor._id").is(objID));
		if (limit != -1) {
			return mongoTemplate.find(query, ScheduledEvent.class);
		}
		else {
			return mongoTemplate.find(query.limit(limit), ScheduledEvent.class);
		}
	}

	@Override
	public List<ScheduledEvent> findAllByResponsiblePersonId(String editorId, int limit) {
		ObjectId objID = new ObjectId(editorId);
		Query query = new Query();
		query.addCriteria(Criteria.where("responsiblePerson._id").is(objID));

		Sort sort = Sort.by(Sort.Order.asc("title"));
		query.with(sort);
		if (limit == -1) {
			return mongoTemplate.find(query, ScheduledEvent.class);
		}
		else {
			return mongoTemplate.find(query.limit(limit), ScheduledEvent.class);
		}
	}

	@Override
	public List<ScheduledEvent> findAllUserIdReferencesInScheduledEvents(String id, int limit) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		ObjectId objID = new ObjectId(id);
		Criteria expression = new Criteria();
		expression.orOperator(Criteria.where("editor._id").is(objID),
		Criteria.where("responsiblePerson._id").is(objID));
		andExpression.add(expression);

		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}

		if (limit != -1) {
			return mongoTemplate.find(query, ScheduledEvent.class);
		}
		else {
			return mongoTemplate.find(query.limit(limit), ScheduledEvent.class);
		}
	}

	@Override
	public List<ScheduledEvent> findAllWhereResponsibleUnitInIdList(List<String> unitIdList) {

		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		List<ObjectId> objectIds = new ArrayList<>();
		if (!unitIdList.contains("IGNORE_UNIT")) {
			for (String unitId : unitIdList) {
				ObjectId objID = new ObjectId(unitId);
				objectIds.add(objID);
			}
			Criteria expression = new Criteria();
			expression.and("responsibleUnit._id").in(objectIds);
			andExpression.add(expression);
		}
		Query query = new Query();
		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		Sort sort = Sort.by(Sort.Order.asc("title"));
		query.with(sort);

		return mongoTemplate.find(query, ScheduledEvent.class);
	}

	@Override
	public List<ScheduledEvent> findAllWhereResponsiblePersonInIdList(List<String> personIdList) {

		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		List<ObjectId> objectIds = new ArrayList<>();
		for (String personId: personIdList) {
			ObjectId objID = new ObjectId(personId);
			objectIds.add(objID);
		}
		Criteria expression = new Criteria();
		expression.and("responsiblePerson._id").in(objectIds);
		andExpression.add(expression);

		Query query = new Query();
		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		Sort sort = Sort.by(Sort.Order.asc("title"));
		query.with(sort);

		return mongoTemplate.find(query, ScheduledEvent.class);
	}

	@Override
	public List<ScheduledEvent> findAllWhereResponsiblePersonIsInDepartmentId(List<String> unitIdList) {
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		List<ObjectId> objectIds = new ArrayList<>();
		for (String unitId: unitIdList) {
			ObjectId objID = new ObjectId(unitId);
			objectIds.add(objID);
		}
		Criteria expression = new Criteria();
		expression.and("responsiblePerson.department._id").in(objectIds);
		andExpression.add(expression);

		Query query = new Query();
		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		Sort sort = Sort.by(Sort.Order.asc("title"));
		query.with(sort);

		return mongoTemplate.find(query, ScheduledEvent.class);
	}

	@Override
	public long updateResourcesScheduledEvent(ScheduledEvent scheduledEvent) {
		ObjectId objID = new ObjectId(scheduledEvent.getId());
		Query query = new Query(Criteria.where( "event._id" ).is( objID ));
		Update update = new Update();
		update.set( "event", scheduledEvent );
		UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

		return result.getModifiedCount(); // document updated
	}

	@Override
	public long CountEventsByStaffMemberAsSupervisor(String staffId) {
		Query query = new Query();
		Criteria expression = new Criteria();

		ObjectId objID = new ObjectId(staffId);
		expression.and("responsiblePerson._id").is(objID);
		query.addCriteria(expression);

		return mongoTemplate.count(query,ScheduledEvent.class);
	}

	@Override
	public long CountEventsByManagerAsEditor(String userId) {
		Query query = new Query();
		Criteria expression = new Criteria();

		ObjectId objID = new ObjectId(userId);
		expression.and("editor._id").is(objID);
		query.addCriteria(expression);

		return mongoTemplate.count(query,ScheduledEvent.class);
	}


	private List<Criteria> setQueryCriteria(ResourceQuery resourceQuery) {

		List<Criteria> andExpression =  new ArrayList<>();

		// >>> SECURITY RESTRICTIONS
		if (resourceQuery.isManager()) {
			if (!resourceQuery.getAuthorizedUnitIds().contains("IGNORE_UNIT")) {
				Criteria expression = this.setManagerSecurityRestrictions(resourceQuery);
				andExpression.add(expression);
			}
		}
		else if (resourceQuery.isSupport()) {
			Criteria expression = this.setSupportSecurityRestrictions(resourceQuery);
			andExpression.add(expression);
		}
		else if (resourceQuery.isStaffMember()) {
			Criteria expression = this.setStaffMemberRestrictions(resourceQuery);
			andExpression.add(expression);
		}
		// <<< END - SECURITY RESTRICTIONS

		if (resourceQuery.getDepartmentId() != null && !resourceQuery.getDepartmentId().isEmpty()) {
			ObjectId objID = new ObjectId(resourceQuery.getDepartmentId());
			Criteria expression = new Criteria();
			expression.orOperator(Criteria.where("responsiblePerson.department._id").is(objID),
					Criteria.where("responsibleUnit._id").is(objID));
			andExpression.add(expression);
		}
		if (resourceQuery.getEventArea() != null && !resourceQuery.getEventArea().isEmpty()) {
			Criteria expression = new Criteria();
			expression.and("area").is(resourceQuery.getEventArea());
			andExpression.add(expression);
		}
		if (resourceQuery.getEventType()!= null && !resourceQuery.getEventType().isEmpty()) {
			Criteria expression = new Criteria();
			expression.and("type").is(resourceQuery.getEventType());
			andExpression.add(expression);
		}
		if (resourceQuery.getStaffMemberId() != null && !resourceQuery.getStaffMemberId().isEmpty()) {
			ObjectId objID = new ObjectId(resourceQuery.getStaffMemberId());
			andExpression.add(Criteria.where("responsiblePerson._id").is(objID));
		}
		if (resourceQuery.getEditorId() != null && !resourceQuery.getEditorId().isEmpty()) {
			ObjectId objID = new ObjectId(resourceQuery.getEditorId());
			andExpression.add(Criteria.where("editor._id").is(objID));

		}
		if (resourceQuery.getAcademicYear()!= null && !resourceQuery.getAcademicYear().isEmpty()) {
			Criteria expression = new Criteria();
			LocalDate from = LocalDate.parse(resourceQuery.getAcademicYear(), DateTimeFormatter.ofPattern("yyyy"));
			LocalDate to   = from.plus(1, ChronoUnit.YEARS);
			expression.and("date").gte(from).lt(to);
			andExpression.add(expression);
		}

		return andExpression;
	}
	private Criteria setStaffMemberRestrictions(ResourceQuery resourceQuery) {
		Criteria expression = new Criteria();
		List<Criteria> orCourseExpression =  new ArrayList<>();
		orCourseExpression.add(Criteria.where("responsiblePerson._id").is(new ObjectId(resourceQuery.getManagerId())));
		expression.orOperator(new Criteria().orOperator(orCourseExpression.toArray(new Criteria[orCourseExpression.size()])));
		return expression;
	}
	private Criteria setManagerSecurityRestrictions(ResourceQuery resourceQuery) {
		Criteria expression = new Criteria();

		List<ObjectId> unitIds = new ArrayList<>();

		for (String unitId : resourceQuery.getAuthorizedUnitIds()) {
			ObjectId objID = new ObjectId(unitId);
			unitIds.add(objID);
		}
		if (unitIds.size()>0) {
			List<Criteria> orCourseExpression =  new ArrayList<>();
			orCourseExpression.add(Criteria.where("responsiblePerson.department._id").in(unitIds));
			if (resourceQuery.isStaffMember()) {
				orCourseExpression.add(Criteria.where("responsiblePerson._id").is(new ObjectId(resourceQuery.getManagerId())));
			}
			expression.orOperator(new Criteria().orOperator(orCourseExpression.toArray(new Criteria[orCourseExpression.size()])));
		}
		return expression;
	}
	private Criteria setSupportSecurityRestrictions(ResourceQuery resourceQuery) {
		Criteria expression = new Criteria();
		List<Criteria> orEventExpression =  new ArrayList<>();
		for (UserAccess.UserRights.EventPermission eventPermission : resourceQuery.getAuthorized_events()) {
			Criteria event_expression = new Criteria();
			event_expression.andOperator(Criteria.where("_id").is(new ObjectId(eventPermission.getEventId())),
			Criteria.where("responsiblePerson._id").is(new ObjectId(eventPermission.getStaffMemberId())));
			orEventExpression.add(event_expression);
		}
		//# Add a dummy event to prevent null query
		Criteria event_expression = new Criteria();
		event_expression.andOperator(Criteria.where("_id").is("dummy_event_id"),
				Criteria.where("responsiblePerson._id").is("dummy_staff_id"));
		orEventExpression.add(event_expression);

		if (resourceQuery.isStaffMember()) {
			orEventExpression.add(Criteria.where("event.responsiblePerson._id").is(new ObjectId(resourceQuery.getManagerId())));
		}

		expression.orOperator(new Criteria().orOperator(orEventExpression.toArray(new Criteria[orEventExpression.size()])));

		return expression;
	}
}
