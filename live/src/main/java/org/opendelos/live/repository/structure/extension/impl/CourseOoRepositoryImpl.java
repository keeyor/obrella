/* 
     Author: Michael Gatzonis - 13/11/2020 
     live
*/
package org.opendelos.live.repository.structure.extension.impl;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.opendelos.live.repository.structure.extension.CourseOoRepository;
import org.opendelos.model.structure.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class CourseOoRepositoryImpl implements CourseOoRepository {

	private final MongoTemplate mongoTemplate;

	@Autowired
	public CourseOoRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public List<Course> findAllByDepartmentId(String departmentId) {

		ObjectId objID = new ObjectId(departmentId);
		Query query = new Query(Criteria.where("department._id").is(objID));
		Sort sort = Sort.by(Sort.Order.asc("title"));
		query.with(sort);
		return mongoTemplate.find(query, Course.class);
	}

	@Override
	public List<Course> findCoursesAssignedToStaffMember(String staffMemberId) {
		return null;
	}

	@Override
	public List<Course> findWithCriteria(String schoolId, String departmentId, String studyId, String programId) {

		Query query = new Query();
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		if (!schoolId.equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("schoolId").is(schoolId);
			andExpression.add(expression);
		}
		if (!departmentId.equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("departmentId").is(departmentId);
			andExpression.add(expression);
		}
		if (!studyId.equals("_all") && !studyId.equals("under")) {
			Criteria expression = new Criteria();
			expression.and("study").is(studyId);
			andExpression.add(expression);
		}
		else if (studyId.equals("under")) {
			Criteria expression = new Criteria();
			expression.orOperator(
					Criteria.where("study").is(null),
					Criteria.where("study").is("under")
			);
			andExpression.add(expression);
		}
		if (!programId.equals("_all") && !programId.equals("default") && !programId.equals("-1")) {
			Criteria expression = new Criteria();
			expression.and("studyProgramId").is(programId);
			andExpression.add(expression);
		}
		else if (programId.equals("-1")) {
			Criteria expression = new Criteria();
			expression.and("studyProgramId").is(null);
			andExpression.add(expression);
		}

		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		Sort sort = Sort.by(Sort.Order.asc("title"));
		//query = query.with(sort);
		return mongoTemplate.find(query,Course.class);
	}

	@Override
	public List<Course> findFromIds(List<String> ids) {


		Query query = new Query(Criteria.where("id").in(ids));
		Sort sort = Sort.by(Sort.Order.asc("title"));
		query.with(sort);
		return mongoTemplate.find(query, Course.class);
	}
	@Override
	public List<Course> findExcludingIds(List<String> ids, String departmentId) {
		Criteria andCriteria = new Criteria();
		List<Criteria> andExpression =  new ArrayList<>();

		if (departmentId != null && !departmentId.equals("_all")) {
			ObjectId objID = new ObjectId(departmentId);
			Criteria expression = new Criteria();
			expression.and("department._id").is(objID);
			andExpression.add(expression);
		}

		Criteria expression = new Criteria();
		expression.and("id").nin(ids);
		andExpression.add(expression);

		Query query = new Query();
		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}
		Sort sort = Sort.by(Sort.Order.asc("title"));
		query.with(sort);

		return mongoTemplate.find(query, Course.class);
	}
	@Override
	public void findAndUpdate(Course course) {

		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(course.getId()));
		Update update = new Update();
		update.set("title", course.getTitle());
		update.set("identity", course.getIdentity());
		update.set("scopeId", course.getScopeId());
		update.set("institutionId", course.getInstitutionId());
		update.set("schoolId", course.getSchoolId());
		update.set("department", course.getDepartment());
		update.set("lmsReferences", course.getLmsReferences());
		update.set("study", course.getStudy());
		update.set("studyProgramId", course.getStudyProgramId());
		//skip +semester +counter

		mongoTemplate.findAndModify(query, update, Course.class);
	}

	@Override
	public long updateResourcesCourse(Course course) {
		ObjectId objID = new ObjectId(course.getId());
		Query query = new Query(Criteria.where( "course._id" ).is( objID ));
		Update update = new Update();
		update.set( "course", course );
		UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

		return result.getModifiedCount(); // document updated
	}
}
