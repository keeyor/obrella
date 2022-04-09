/* 
     Author: Michael Gatzonis - 13/11/2020 
     live
*/
package org.opendelos.vodapp.repository.structure.extension.impl;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.StudyProgram;
import org.opendelos.vodapp.repository.structure.extension.StudyProgramOoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class StudyProgramOoRepositoryImpl implements StudyProgramOoRepository {

	private final MongoTemplate mongoTemplate;

	@Autowired
	public StudyProgramOoRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}


	@Override
	public List<StudyProgram> findWithCriteria(String schoolId, String departmentId, String study) {

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
		if (!study.equals("_all")) {
			Criteria expression = new Criteria();
			expression.and("study").is(study);
			andExpression.add(expression);
		}
		if (!andExpression.isEmpty()) {
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}

		return mongoTemplate.find(query,StudyProgram.class);
	}

	@Override
	public void findAndUpdate(StudyProgram studyProgram) {

		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(studyProgram.getId()));
		Update update = new Update();
		update.set("title", studyProgram.getTitle());
		update.set("study",studyProgram.getStudy());
		if (studyProgram.getIdentity() != null) {
			update.set("identity",studyProgram.getIdentity());
		}
		if (studyProgram.getDepartmentId() != null) {
			update.set("departmentId",studyProgram.getDepartmentId());
		}
		if (studyProgram.getSchoolId() != null) {
			update.set("schoolId",studyProgram.getSchoolId());
		}
		if (studyProgram.getCustomPeriods() != null) {
			update.set("customPeriods",studyProgram.getCustomPeriods());
		}

		mongoTemplate.findAndModify(query, update, StudyProgram.class);
	}

	/* CALENDAR */
	@Override
	public void saveCustomPeriod(String id, CustomPeriod cPeriod) {
		StudyProgram studyProgram = mongoTemplate.findById(id, StudyProgram.class );
		assert studyProgram != null;
		List<CustomPeriod> customPeriods = studyProgram.getCustomPeriods();
		int index = -1;
		boolean found = false;
		for (CustomPeriod customPeriod: customPeriods) {
			index++;
			if (customPeriod.getYear().equals(cPeriod.getYear())) {
				customPeriods.set(index, cPeriod);
				found = true;
				break;
			}
		}
		if (!found) {
			customPeriods.add(cPeriod);
		}
		studyProgram.setCustomPeriods(customPeriods);
		mongoTemplate.save(studyProgram);
	}

	@Override
	public void deleteCustomPeriod(String id, String year) {
		StudyProgram studyProgram = mongoTemplate.findById(id, StudyProgram.class );
		assert studyProgram != null;
		List<CustomPeriod> customPeriods = studyProgram.getCustomPeriods();
		for (CustomPeriod customPeriod: customPeriods) {
			if (customPeriod.getYear().equals(year)) {
				customPeriods.remove(customPeriod);
				break;
			}
		}
		mongoTemplate.save(studyProgram);
	}

	@Override
	public List<CustomPeriod> getCustomPeriods(String id) {
		StudyProgram studyProgram = mongoTemplate.findById(id, StudyProgram.class );
		assert studyProgram != null;
		return studyProgram.getCustomPeriods();
	}

	@Override
	public CustomPeriod getCustomPeriod(String id, String year) {
		StudyProgram studyProgram = mongoTemplate.findById(id, StudyProgram.class );
		if (studyProgram != null) {
			List<CustomPeriod> customPeriods = studyProgram.getCustomPeriods();
			for (CustomPeriod customPeriod : customPeriods) {
				if (customPeriod.getYear().equals(year)) {
					return customPeriod;
				}
			}
		}
		return null;
	}

}
