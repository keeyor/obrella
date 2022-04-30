package org.opendelos.control.repository.structure;

import org.opendelos.model.structure.Course;
import org.opendelos.control.repository.structure.extension.CourseOoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends MongoRepository<Course, String>, CourseOoRepository {

	Course findByIdentity(String identity);
}
