/* 
     Author: Michael Gatzonis - 13/11/2020 
     live
*/
package org.opendelos.control.repository.resource.extension.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.opendelos.control.repository.resource.extension.ResourceOoRepository;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.users.UserAccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

public class ResourceOoRepositoryImpl implements ResourceOoRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public QueryResourceResults searchPageableLectures(ResourceQuery resourceQuery) {

		List<Resource> videoLectureList;
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
			field = "date";
			break;
		case "dateModified":
			field = "dateModified";
			break;
		case "views":
			field = "statistics";
			break;
		case "rel":
			field = "text-search";
		}

		if (field != null && !field.equals("text-search")) {
			Sort sort;
			if (!field.equals("title")) {
				if (direction.equals("asc")) {
					sort = Sort.by(Sort.Order.asc(field),Sort.Order.asc("title"));
				}
				else {
					sort = Sort.by(Sort.Order.desc(field),Sort.Order.asc("title"));
				}
			}
			else {		//Note: to get same resources close together... date is not enough
				if (direction.equals("asc")) {
					sort = Sort.by(Sort.Order.asc(field));
				}
				else {
					sort = Sort.by(Sort.Order.desc(field));
				}
			}
			query = query.with(sort);
		}

		//Sort.by(Sort.Order.asc("date"),Sort.Order.asc("partNumber"),Sort.Order.asc("classroom"));

		int limit = resourceQuery.getLimit();
		long skip = resourceQuery.getSkip();

		long count;

		if (resourceQuery.getCollectionName() != null) {
			count = mongoTemplate.count(query, Resource.class,resourceQuery.getCollectionName());
			videoLectureList = mongoTemplate.find(query.limit(limit).skip(skip), Resource.class, resourceQuery.getCollectionName());
		}
		else {
			count = mongoTemplate.count(query, Resource.class);
			videoLectureList = mongoTemplate.find(query.limit(limit).skip(skip), Resource.class);
		}

		QueryResourceResults queryResults = new QueryResourceResults();
		queryResults.setLimit(limit);
		queryResults.setSkip(skip);
		queryResults.setSort(field);
		queryResults.setDirection(direction);
		queryResults.setSearchResultList(videoLectureList);
		queryResults.setTotalResults(count);

		return queryResults;
	}

	@Override
	public List<Resource> searchLecturesOnFilters(ResourceQuery resourceQuery) {

		Query query = new Query();

		if (resourceQuery.getFt() != null && !resourceQuery.getFt().isEmpty()) {
			TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(resourceQuery.getFt());
			query = TextQuery.queryText(criteria);
		}
		List<Criteria> andExpression =  setQueryCriteria(resourceQuery);

		if (!andExpression.isEmpty()) {
			Criteria andCriteria = new Criteria();
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}

		int limit = resourceQuery.getLimit();

		query.fields().include("course");
		query.fields().include("event");
		query.fields().include("department");
		query.fields().include("supervisor");
		query.fields().include("accessPolicy");
		//query.fields().include("date");
		//query.fields().include("realDuration");

		Sort sort = Sort.by(Sort.Order.desc("date"));
		query = query.with(sort);

		if (resourceQuery.getCollectionName() != null) {
			return mongoTemplate.find(query.limit(limit), Resource.class, resourceQuery.getCollectionName());
		}
		else {
			return mongoTemplate.find(query.limit(limit), Resource.class);
		}

	}

	@Override
	public void clearCollection(String collectionName) {
		mongoTemplate.remove(new Query(), collectionName);
	}

	@Override
	public void saveToCollection(Resource resource, String collectionName) {
		mongoTemplate.save(resource,collectionName);
	}

	@Override
	public void deleteFromCollection(Resource resource, String collectionName) {
		mongoTemplate.remove(resource,collectionName);
	}

	@Override
	public List<Resource> findByScheduleIdInCollection(String id, String collectionName) {
		Criteria expression = new Criteria();
		expression.and("scheduleId").is(id);
		Query query = new Query();
		query.addCriteria(expression);
		return mongoTemplate.find(query,Resource.class, collectionName);
	}
	@Override
	public List<Resource> findByClassroomIdInCollection(String id, String collectionName) {
		Criteria expression = new Criteria();
		expression.and("classroom").is(id);
		Query query = new Query();
		query.addCriteria(expression);
		return mongoTemplate.find(query,Resource.class, collectionName);
	}

	@Override
	public List<Resource> findAllUserIdReferencesInResources(String id, int limit) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		ObjectId objID = new ObjectId(id);
		Criteria expression = new Criteria();
		expression.orOperator(Criteria.where("editor._id").is(objID),
							  Criteria.where("responsiblePerson._id").is(objID),
							  Criteria.where("supervisor._id").is(objID));
		andExpression.add(expression);

		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		if (limit != -1) {
			return mongoTemplate.find(query, Resource.class);
		}
		else {
			return mongoTemplate.find(query.limit(limit), Resource.class);
		}
	}

	@Override
	public Resource findByIdInCollection(String id, String collectionName) {
		return mongoTemplate.findById(id,Resource.class,collectionName);
	}

	@Override
	public List<Resource> findByStreamNameInCollection(String streamName, String collectionName) {

		Criteria expression = new Criteria();
		expression.and("streamName").is(streamName);
		Query query = new Query();
		query.addCriteria(expression);

		return mongoTemplate.find(query,Resource.class, collectionName);
	}

	@Override
	public Resource findByStreamIdInCollection(String streamId, String collectionName) {
		Criteria expression = new Criteria();
		expression.and("streamId").is(streamId);
		Query query = new Query();
		query.addCriteria(expression);

		return mongoTemplate.findOne(query,Resource.class, collectionName);
	}

	@Override
	public List<Resource> findLiveStreamByIdOrNameInCollection(String idOrName, String collectionName) {

		Query query;
		if (ObjectId.isValid(idOrName)) {
			Criteria expression = new Criteria();
			ObjectId objectId = new ObjectId(idOrName);
			expression.and("id").is(objectId);
			query = new Query();
			query.addCriteria(expression);
			return mongoTemplate.find(query,Resource.class, collectionName);
		}
		else {
			Criteria expression = new Criteria();
			expression.and("streamName").is(idOrName); //StartsWith classroom.code
			query = new Query();
			query.addCriteria(expression);
			return mongoTemplate.find(query,Resource.class, collectionName);
		}
	}

	@Override
	public List<Resource> findRelatedCourseResources(Resource resource, String accessPolicy) {
		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		Criteria expression = new Criteria();

		if (accessPolicy != null) {
			expression.and("accessPolicy").is(accessPolicy);
			andExpression.add(expression);
		}

		expression.andOperator(new Criteria("date").exists(true), new Criteria("date").is(resource.getDate()));
		andExpression.add(expression);

		expression.and("classroom").is(resource.getClassroom());
		andExpression.add(expression);

		ObjectId objID = new ObjectId(resource.getSupervisor().getId());
		expression.and("supervisor._id").is(objID);
		andExpression.add(expression);

		Sort sort = Sort.by(Sort.Order.asc("partNumber"));
		query = query.with(sort);

		query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		return mongoTemplate.find(query,Resource.class);
	}

	@Override
	public List<Resource> findRelatedEventResourcesByEventId(String id, String accessPolicy) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		Criteria expression = new Criteria();
		ObjectId objID = new ObjectId(id);
		expression.and("event._id").is(objID);
		andExpression.add(expression);

		if (accessPolicy != null) {
			expression.and("accessPolicy").is(accessPolicy);
			andExpression.add(expression);
		}
		query.fields().include("id");
		query.fields().include("title");
		query.fields().include("partNumber");
		query.fields().include("classroom");
		Sort sort = Sort.by(Sort.Order.asc("date"),Sort.Order.asc("partNumber"),Sort.Order.asc("classroom"));
		query = query.with(sort);

		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		return mongoTemplate.find(query,Resource.class);
	}



	@Override
	public long CountPublicResourcesByType(String type) {
		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		 Criteria expression = new Criteria();
		 expression.and("accessPolicy").is("public");
		 andExpression.add(expression);
		 expression.and("type").is(type);
		 andExpression.add(expression);
		 query.fields().include("id");
		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}

		return mongoTemplate.count(query,Resource.class);
	}

	@Override
	public long CountCollectionDocuments(String collectionName) {
		Query query = new Query();
		return mongoTemplate.count(query, Resource.class,collectionName);
	}

	@Override
	public long CountResourcesByStaffMemberAsSupervisor(String staffId) {
		Query query = new Query();
		Criteria expression = new Criteria();

		ObjectId objID = new ObjectId(staffId);
		expression.and("supervisor._id").is(objID);
		query.addCriteria(expression);

		return mongoTemplate.count(query,Resource.class);
	}
	@Override
	public long CountScheduledByStaffMemberAsSupervisor(String staffId,String collectionName) {
		Query query = new Query();
		Criteria expression = new Criteria();

		expression.and("supervisor").is(staffId);
		query.addCriteria(expression);

		return mongoTemplate.count(query,Resource.class,collectionName);
	}

	@Override
	public long CountResourcesByManagerAsEditor(String userId) {
		Query query = new Query();
		Criteria expression = new Criteria();

		ObjectId objID = new ObjectId(userId);
		expression.and("editor._id").is(objID);
		query.addCriteria(expression);

		return mongoTemplate.count(query,Resource.class);
	}
	@Override
	public long CountScheduledByManagerAsEditor(String userId, String collectionName) {
		Query query = new Query();
		Criteria expression = new Criteria();

		expression.and("editor").is(userId);
		query.addCriteria(expression);

		return mongoTemplate.count(query,Resource.class,collectionName);
	}


	private List<Criteria> setQueryCriteria(ResourceQuery resourceQuery) {

		List<Criteria> andExpression =  new ArrayList<>();

		if (resourceQuery.getAcademicYear() != null) {
			Criteria expression = new Criteria();
			expression.and("academicYear").is(resourceQuery.getAcademicYear());
			andExpression.add(expression);
		}
		if (resourceQuery.getAccessPolicy() != null) {
			if (resourceQuery.getAccessPolicy().equals("private")) {
				Criteria expression = new Criteria();
				expression.and("accessPolicy").is("private");
				andExpression.add(expression);
			}
			else if (resourceQuery.getAccessPolicy().equals("public")) {
				Criteria expression = new Criteria();
				expression.and("accessPolicy").is("public");
				andExpression.add(expression);
			}
		}
		//ResourceType
		if (resourceQuery.getResourceType() != null) {
			if (resourceQuery.getResourceType().equals("c")) {
				Criteria expression = new Criteria();
				expression.and("type").is("COURSE");
				andExpression.add(expression);
			}
			else if (resourceQuery.getResourceType().equals("e")) {
				Criteria expression = new Criteria();
				expression.and("type").is("EVENT");
				andExpression.add(expression);
			}
		}
		//Unique only
		if (resourceQuery.isUniqueOnly()) {
			Criteria expression = new Criteria();
			expression.orOperator(Criteria.where("parts").is(false), Criteria.where("partNumber").is(1));
			andExpression.add(expression);
		}
		//Featured only
		if (resourceQuery.isFeatured()) {
			Criteria expression = new Criteria();
			expression.and("event.isFeatured").is(true);
			andExpression.add(expression);
		}
		//Tag
		if (resourceQuery.getTag() != null && !resourceQuery.getTag().isEmpty()) {
			String tag = resourceQuery.getTag();
			Criteria expression = new Criteria();
			expression.and("tags." + tag).is("admin.tag.neednotready");
			andExpression.add(expression);
		}

		//SECURITY RESTRICTIONS
		if (resourceQuery.isManager() && !resourceQuery.isSA()) {
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
		// <<< SECURITY RESTRICTIONS

		if (resourceQuery.getDepartmentId() != null && !resourceQuery.getDepartmentId().isEmpty()) {
			ObjectId objID = new ObjectId(resourceQuery.getDepartmentId());
			Criteria expression = new Criteria();
			expression.orOperator(Criteria.where("course.department._id").is(objID),
					Criteria.where("course.departmentsRelated._id").is(objID),
					Criteria.where("event.responsibleUnit._id").is(objID),
					Criteria.where("event.responsiblePerson.department._id").is(objID));
			andExpression.add(expression);
		}

		if (resourceQuery.getStaffMemberId() != null && !resourceQuery.getStaffMemberId().isEmpty()) {
			ObjectId objID = new ObjectId(resourceQuery.getStaffMemberId());
			Criteria expression = new Criteria();
			expression.orOperator(Criteria.where("supervisor._id").is(objID),
					Criteria.where("event.responsiblePerson._id").is(objID));
			andExpression.add(expression);
		}
		if (resourceQuery.getEditorId() != null && !resourceQuery.getEditorId().isEmpty()) {
			ObjectId objID = new ObjectId(resourceQuery.getEditorId());
			Criteria expression = new Criteria();
			expression.orOperator(Criteria.where("editor._id").is(objID),
					Criteria.where("event.editor._id").is(objID));
			andExpression.add(expression);
		}
		if (resourceQuery.getCourseId() != null && !resourceQuery.getCourseId().isEmpty()) {
			Criteria expression = new Criteria();
			ObjectId objID = new ObjectId(resourceQuery.getCourseId());
			expression.and("course._id").is(objID);
			andExpression.add(expression);
		}
		if (resourceQuery.getEventId() != null && !resourceQuery.getEventId().isEmpty()) {
			Criteria expression = new Criteria();
			ObjectId objID = new ObjectId(resourceQuery.getEventId());
			expression.and("event._id").is(objID);
			andExpression.add(expression);
		}
		if (resourceQuery.getCategoryCode()!= null && !resourceQuery.getCategoryCode().isEmpty()) {
			List<String> cat_codes = Arrays.asList(resourceQuery.getCategoryCode().split(","));
			Criteria expression = new Criteria();
			expression.orOperator(Criteria.where("categories").in(cat_codes),
					Criteria.where("course.categories").in(cat_codes),
					Criteria.where("event.categories").in(cat_codes));
			andExpression.add(expression);
		}
		//only for ScheduledEvents (Type)
		if (resourceQuery.getEventType()!= null && !resourceQuery.getEventType().isEmpty()) {
			List<String> eventType_codes = Arrays.asList(resourceQuery.getEventType().split(","));
			Criteria expression = new Criteria();
			expression.and("event.type").in(eventType_codes);
			andExpression.add(expression);
		}
		//only for ScheduledEvents (Area)
		if (resourceQuery.getEventArea()!= null && !resourceQuery.getEventArea().isEmpty()) {
			Criteria expression = new Criteria();
			expression.and("event.area").is(resourceQuery.getEventArea());
			andExpression.add(expression);
		}
		if (resourceQuery.getAccessPolicy()!= null && !resourceQuery.getAccessPolicy().isEmpty()) {
			Criteria expression = new Criteria();
			expression.and("accessPolicy").is(resourceQuery.getAccessPolicy());
			andExpression.add(expression);
		}
		if (resourceQuery.getClassroomId()!= null && !resourceQuery.getClassroomId().isEmpty()) {
			Criteria expression = new Criteria();
			expression.and("classroom").is(resourceQuery.getClassroomId());
			andExpression.add(expression);
		}
		if (resourceQuery.getDate()!= null && !resourceQuery.getDate().isEmpty()) {
			Criteria expression = new Criteria();
			LocalDate from = LocalDate.parse(resourceQuery.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			LocalDate to   = from.plus(1, ChronoUnit.DAYS);
			expression.and("date").gte(from).lt(to);
			andExpression.add(expression);
		}
		if (resourceQuery.getInstantDate()!= null) {
			Criteria expression = new Criteria();
			expression.and("date").is(resourceQuery.getInstantDate());
			andExpression.add(expression);
		}

		return andExpression;
	}

	private Criteria setStaffMemberRestrictions(ResourceQuery resourceQuery) {
		Criteria expression = new Criteria();
		List<Criteria> orCourseExpression =  new ArrayList<>();
		orCourseExpression.add(Criteria.where("supervisor._id").is(new ObjectId(resourceQuery.getManagerId())));
		orCourseExpression.add(Criteria.where("event.responsiblePerson._id").is(new ObjectId(resourceQuery.getManagerId())));
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
				orCourseExpression.add(Criteria.where("course.department._id").in(unitIds));
				orCourseExpression.add(Criteria.where("event.responsibleUnit._id").in(unitIds));
 				if (resourceQuery.isStaffMember()) {
					orCourseExpression.add(Criteria.where("supervisor._id").is(new ObjectId(resourceQuery.getManagerId())));
					orCourseExpression.add(Criteria.where("event.responsiblePerson._id").is(new ObjectId(resourceQuery.getManagerId())));
				}
				expression.orOperator(new Criteria().orOperator(orCourseExpression.toArray(new Criteria[orCourseExpression.size()])));
		 }
		 return expression;
	}
	private Criteria setSupportSecurityRestrictions(ResourceQuery resourceQuery) {
		Criteria expression = new Criteria();
		List<Criteria> orCourseExpression =  new ArrayList<>();
		for (UserAccess.UserRights.CoursePermission coursePermission : resourceQuery.getAuthorized_courses()) {
			Criteria course_expression = new Criteria();
			if (coursePermission.getCourseId().equals("*")) {
				course_expression.and("supervisor._id").is(coursePermission.getStaffMemberId());
				orCourseExpression.add(course_expression);
			}
			else {
				course_expression.andOperator(Criteria.where("course._id").is(coursePermission.getCourseId()),
						Criteria.where("supervisor._id").is(coursePermission.getStaffMemberId()));
			}
			orCourseExpression.add(course_expression);
		}
		for (UserAccess.UserRights.EventPermission eventPermission : resourceQuery.getAuthorized_events()) {
			Criteria event_expression = new Criteria();
			event_expression.andOperator(Criteria.where("event._id").is(new ObjectId(eventPermission.getEventId())),
					Criteria.where("event.responsiblePerson._id").is(new ObjectId(eventPermission.getStaffMemberId())));
			orCourseExpression.add(event_expression);
		}
 		if (resourceQuery.isStaffMember()) {
			orCourseExpression.add(Criteria.where("supervisor._id").is(new ObjectId(resourceQuery.getManagerId())));
			orCourseExpression.add(Criteria.where("event.responsiblePerson._id").is(new ObjectId(resourceQuery.getManagerId())));
		}
		expression.orOperator(new Criteria().orOperator(orCourseExpression.toArray(new Criteria[orCourseExpression.size()])));

		return expression;
	}

}
