/* 
     Author: Michael Gatzonis - 10/14/2019 
     OpenDelosDAC
*/
package org.opendelos.sync.repository.structure;

import java.util.List;

import org.opendelos.model.structure.Program;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends MongoRepository<Program, String> {

	List<Program> findAllByDepartmentId(String departmentId);
	List<Program> findAllByDepartmentIdAndStudyId(String departmentId, String studyId);
}
