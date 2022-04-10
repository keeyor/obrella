/* 
     Author: Michael Gatzonis - 22/2/2021 
     live
*/
package org.opendelos.sync.repository.scheduledEvent.extension.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.sync.repository.scheduledEvent.extension.ScheduledEventOoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class ScheduledEventOoRepositoryImpl implements ScheduledEventOoRepository {

	@Autowired
	MongoTemplate mongoTemplate;

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
	public long updateResourcesEvents(ScheduledEvent scheduledEvent) {
		ObjectId objID = new ObjectId(scheduledEvent.getId());
		Query query = new Query(Criteria.where( "event._id" ).is( objID ));
		Update update = new Update();
		update.set( "event", scheduledEvent );
		UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

		return result.getModifiedCount(); // document updated
	}

	@Override
	public List<ScheduledEvent> findAllInCollection(String collection) {

		return mongoTemplate.findAll(ScheduledEvent.class,collection);
	}

	@Override
	public ScheduledEvent findMatchInCollection(String title, Instant date, String collection) {

		Query query = new Query();
		query.addCriteria(Criteria.where("title").is(title));
		query.addCriteria(Criteria.where("startDate").is(date));

		return mongoTemplate.findOne(query, ScheduledEvent.class,collection);

	}

}
