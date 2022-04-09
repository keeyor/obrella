package org.opendelos.live.repository.structure;

import org.opendelos.live.repository.structure.extension.CourseOoRepository;
import org.opendelos.model.structure.Course;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends MongoRepository<Course, String>, CourseOoRepository {

	//List<Course> findAllByDepartmentId(String departmentId);
	//List<Course> findAllByPeriodCode(String periodCode);
	//List<Course> findAllByDepartmentIdAndPeriodCode(String departmentId, String periodCode);
	Course findByIdentity(String identity);
}
