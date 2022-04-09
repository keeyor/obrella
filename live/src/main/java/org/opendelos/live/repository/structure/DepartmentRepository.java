package org.opendelos.live.repository.structure;

import java.util.List;

import org.opendelos.live.repository.structure.extension.DepartmentOoRepository;
import org.opendelos.model.structure.Department;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String>, DepartmentOoRepository {

	Department findByIdentity(String identity);
	List<Department> findAllBySchoolId(String schoolId);

}
