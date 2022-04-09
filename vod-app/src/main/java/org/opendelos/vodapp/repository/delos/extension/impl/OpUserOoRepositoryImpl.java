/* 
     Author: Michael Gatzonis - 13/11/2020 
     live
*/
package org.opendelos.vodapp.repository.delos.extension.impl;


import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.users.UserAccess;
import org.opendelos.vodapp.repository.delos.extension.OpUserOoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class OpUserOoRepositoryImpl implements OpUserOoRepository {

	private final MongoTemplate mongoTemplate;

	@Autowired
	public OpUserOoRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public void findAndUpdate(OpUser opUser) {

		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(opUser.getId()));
		Update update = new Update();
		update.set("name", opUser.getName());
		update.set("altName",opUser.getAltName());
		update.set("affiliation",opUser.getAffiliation());
		update.set("email",opUser.getEmail());
		update.set("uid",opUser.getUid());
		update.set("authorities",opUser.getAuthorities());
		update.set("department", opUser.getDepartment());
		update.set("active", opUser.isActive());
		if (opUser.getPassword() != null && !opUser.getPassword().trim().equals("")) {
			update.set("password", opUser.getPassword());
		}

		mongoTemplate.findAndModify(query, update, OpUser.class);
	}

	@Override
	public List<OpUser> findAllManagers() {
		Query query = new Query();
		Criteria expression = new Criteria();
		List<Criteria> criteriaList =  new ArrayList<>();

		expression.orOperator(
							  Criteria.where("authorities").in(UserAccess.UserAuthority.MANAGER),
							  Criteria.where("authorities").in(UserAccess.UserAuthority.SUPPORT));

		query.addCriteria(expression);
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);

		return mongoTemplate.find(query,OpUser.class);
	}

	@Override
	public List<OpUser> findAllSaUsers() {
		Query query =  new Query(Criteria.where("rights.isSa").is(true));
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);

		return mongoTemplate.find(query, OpUser.class);
	}

	@Override
	public List<OpUser> findAllSpUsers() {
		Query query = new Query();
		Criteria expression = new Criteria();

		expression.andOperator(Criteria.where("authorities").in(UserAccess.UserAuthority.SUPPORT),
				               Criteria.where("rights.isSa").is(false));

		query.addCriteria(expression);
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);

		return mongoTemplate.find(query,OpUser.class);
	}

	@Override
	public List<OpUser> findAllDmUsers() {
		Query query = new Query();
		Criteria expression = new Criteria();

		expression.andOperator(Criteria.where("authorities").in(UserAccess.UserAuthority.MANAGER),
				Criteria.where("rights.isSa").is(false),
				Criteria.where("rights.unitPermissions.0.unitType").ne("INSTITUTION"));

		query.addCriteria(expression);
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);

		return mongoTemplate.find(query,OpUser.class);
	}

	@Override
	public List<OpUser> findAllImUsers() {
		Query query = new Query();
		Criteria expression = new Criteria();

		expression.andOperator(Criteria.where("authorities").in(UserAccess.UserAuthority.MANAGER),
				Criteria.where("rights.isSa").is(false),
				Criteria.where("rights.unitPermissions.0.unitType").is("INSTITUTION"));

		query.addCriteria(expression);
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);

		return mongoTemplate.find(query,OpUser.class);
	}
	@Override
	public List<OpUser> findAllStaffMembersForCourseId(String courseId) {

		Query query =  new Query(Criteria.where("courses").is(courseId));
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);
		return mongoTemplate.find(query, OpUser.class);
	}
	@Override
	public long countAllStaffMembersForCourseId(String courseId) {

		Query query =  new Query(Criteria.where("courses").is(courseId));
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);
		return mongoTemplate.count(query, OpUser.class);
	}

	@Override
	public List<OpUser> findAllStaffMembersTeachingInDepartment(String departmentId) {
		ObjectId objID = new ObjectId(departmentId);
		Query query = new Query(Criteria.where("courses.department._id").is(objID));
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);
		return mongoTemplate.find(query, OpUser.class);
	}

	@Override
	public List<OpUser> findAllStaffMembersTeachingCourses(List<String> courseIds) {
		Query query = new Query(Criteria.where("courses").in(courseIds));
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);
		return mongoTemplate.find(query, OpUser.class);
	}

	@Override
	public List<OpUser> findAllStaffMembersOfDepartment(String departmentId) {

		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		ObjectId objID = new ObjectId(departmentId);
		Criteria expression = new Criteria();
		expression.and("department._id").is(objID);
		andExpression.add(expression);

		expression = new Criteria();
		expression.and("authorities").in("STAFFMEMBER");
		andExpression.add(expression);

		Query query = new Query();
		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		Sort sort = Sort.by(Sort.Order.asc("name"));
		query.with(sort);
		return mongoTemplate.find(query, OpUser.class);
	}

	@Override
	public List<String> getAllStaffMemberCourseIds(String staffMemberId) {
		List<String> courseIds = new ArrayList<>();
		OpUser opUser = mongoTemplate.findById(staffMemberId, OpUser.class);
		if (opUser != null) {
			List<String> staffMemberCourseIds = opUser.getCourses();
			if (staffMemberCourseIds != null) {
				courseIds.addAll(staffMemberCourseIds);
			}
		}
		return  courseIds;
	}

	@Override
	public List<UserAccess.UserRights.UnitPermission> getUsersUnitPermissions(String id) {
		List<UserAccess.UserRights.UnitPermission> unitPermissions = new ArrayList<>();
		OpUser opUser = mongoTemplate.findById(id,OpUser.class);

		if (opUser != null && opUser.getRights().getUnitPermissions() != null && opUser.getRights().getUnitPermissions().size()>0) {
			unitPermissions.addAll(opUser.getRights().getUnitPermissions());
		}
		return unitPermissions;
	}
	@Override
	public void AssignUnitPermissionToManager(String id, UserAccess.UserRights.UnitPermission unitPermission) {
			Update updateCmd = new Update();
			Query query = new Query();
			query.addCriteria(Criteria.where("id").is(id));
			updateCmd.addToSet("rights.unitPermissions", unitPermission);
			mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), OpUser.class);
	}
	@Override
	public void UnAssignUnitPermissionFromManager(String id, String unitId) {
		Update updateCmd = new Update();
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		updateCmd.pull("rights.unitPermissions", Query.query(Criteria.where("unitId").is(unitId)));
		mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), OpUser.class);
	}

	@Override
	public void AssignCoursePermissionToManager(String id, UserAccess.UserRights.CoursePermission coursePermission) {
		Update updateCmd = new Update();
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		updateCmd.addToSet("rights.coursePermissions", coursePermission);
		mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), OpUser.class);
	}
	@Override
	public void AssignEventPermissionToManager(String id, UserAccess.UserRights.EventPermission eventPermission) {
		Update updateCmd = new Update();
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		updateCmd.addToSet("rights.eventPermissions", eventPermission);
		mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), OpUser.class);
	}

	@Override
	public void UnAssignCoursePermissionFromManager(String id, String staffMemberId, String courseId) {
		Update updateCmd = new Update();
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		updateCmd.pull("rights.coursePermissions", Query.query(Criteria.where("staffMemberId").is(staffMemberId).and("courseId").is(courseId)));
		mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), OpUser.class);
	}
	@Override
	public void UnAssignEventPermissionFromManager(String id, String staffMemberId, String eventId) {
		Update updateCmd = new Update();
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		updateCmd.pull("rights.eventPermissions", Query.query(Criteria.where("staffMemberId").is(staffMemberId).and("eventId").is(eventId)));
		mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), OpUser.class);
	}

	@Override
	public List<UserAccess.UserRights.CoursePermission> getUsersCoursePermissions(String id) {
		List<UserAccess.UserRights.CoursePermission> coursePermissions = new ArrayList<>();
		OpUser opUser = mongoTemplate.findById(id,OpUser.class);

		if (opUser != null && opUser.getRights().getCoursePermissions() != null && opUser.getRights().getCoursePermissions().size()>0) {
			//cover null values ( backwards compatibility)
			coursePermissions.addAll(opUser.getRights().getCoursePermissions());
		}
		return coursePermissions;
	}
	@Override
	public List<UserAccess.UserRights.EventPermission> getUsersEventPermissions(String id) {
		List<UserAccess.UserRights.EventPermission> eventPermissions = new ArrayList<>();
		OpUser opUser = mongoTemplate.findById(id,OpUser.class);

		if (opUser != null && opUser.getRights().getEventPermissions() != null && opUser.getRights().getEventPermissions().size()>0) {
			//cover null values ( backwards compatibility)
			eventPermissions.addAll(opUser.getRights().getEventPermissions());
		}
		return eventPermissions;
	}

	@Override
	public void AssignCoursesToStaffMember(String staffMemberId, String[] ids) {

		for (String id : ids) {
			Update updateCmd = new Update();
			Query query = new Query();
			query.addCriteria(Criteria.where("id").is(staffMemberId));
			updateCmd.addToSet("courses", id);
			mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), OpUser.class);
		}
	}
	@Override
	public void UnAssignCourseFromStaffMember(String staffMemberId, String id) {
			Update updateCmd = new Update();
			Query query = new Query();
			query.addCriteria(Criteria.where("id").is(staffMemberId));
			updateCmd.pull("courses", id);
			mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), OpUser.class);
	}

	@Override
	public List<OpUser> getSupportPersonnelForCourseAndStaffMember(String courseId, String staffMemberId) {
		Query query = new Query();
		Criteria first = Criteria.where("rights.coursePermissions").elemMatch(Criteria.where("courseId").is(courseId).and("staffMemberId").is(staffMemberId));
		Criteria second = Criteria.where("authorities").in(UserAccess.UserAuthority.SUPPORT);
		query.addCriteria(new Criteria().andOperator(first, second));
		return mongoTemplate.find(query, OpUser.class);
	}

	// BULK UPDATE

	//SCHEDULED EVENTS
	@Override
	public long updateScheduledEventsResponsiblePersonInfo(String staffMemberId,String staffMemberName, String staffMemberAffiliation) {
		ObjectId objID = new ObjectId(staffMemberId);
		Query query = new Query(Criteria.where( "responsiblePerson._id" ).is( objID ));
		Update update = new Update();
		update.set( "responsiblePerson.name", staffMemberName );
		update.set( "responsiblePerson.affiliation", staffMemberAffiliation );
		UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.events" );

		return result.getModifiedCount(); // document updated
	}
	@Override
	public long updateScheduledEventsEditorInfo(String staffMemberId,String staffMemberName, String staffMemberAffiliation, Unit sUnit) {
		ObjectId objID = new ObjectId(staffMemberId);
		Query query = new Query(Criteria.where( "editor._id" ).is( objID ));
		Update update = new Update();
		update.set( "editor.name", staffMemberName );
		update.set( "editor.affiliation", staffMemberAffiliation );
		update.set( "editor.department", sUnit );
		UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.events" );

		return result.getModifiedCount(); // document updated
	}
	//RESOURCES
	public long updateResourcesSupervisorInfo(String staffMemberId,String staffMemberName, String staffMemberAffiliation) {
		ObjectId objID = new ObjectId(staffMemberId);
		Query query = new Query(Criteria.where( "supervisor._id" ).is( objID ));
		Update update = new Update();
		update.set( "supervisor.name", staffMemberName );
		update.set( "supervisor.affiliation", staffMemberAffiliation );
		UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

		return result.getModifiedCount(); // document updated
	}
	public long updateResourcesEditorInfo(String staffMemberId,String staffMemberName, String staffMemberAffiliation, Unit unit) {
		ObjectId objID = new ObjectId(staffMemberId);
		Query query = new Query(Criteria.where( "editor._id" ).is( objID ));
		Update update = new Update();
		update.set( "editor.name", staffMemberName );
		update.set( "editor.affiliation", staffMemberAffiliation );
		update.set( "editor.department", unit);
		UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

		return result.getModifiedCount(); // document updated
	}
	public long updateResourcesScheduledEventResponsiblePersonInfo(String staffMemberId,String staffMemberName, String staffMemberAffiliation) {
		ObjectId objID = new ObjectId(staffMemberId);
		Query query = new Query(Criteria.where( "event.responsiblePerson._id" ).is( objID ));
		Update update = new Update();
		update.set( "event.responsiblePerson.name", staffMemberName );
		update.set( "event.responsiblePerson.affiliation", staffMemberAffiliation );
		UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

		return result.getModifiedCount(); // document updated
	}
	public long updateResourcesScheduledEventEditorInfo(String staffMemberId,String staffMemberName, String staffMemberAffiliation, Unit sUnit) {
		ObjectId objID = new ObjectId(staffMemberId);
		Query query = new Query(Criteria.where( "event.editor._id" ).is( objID ));
		Update update = new Update();
		update.set( "event.editor.name", staffMemberName );
		update.set( "event.editor.affiliation", staffMemberAffiliation );
		update.set( "event.editor.department", sUnit );
		UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

		return result.getModifiedCount(); // document updated
	}
}
