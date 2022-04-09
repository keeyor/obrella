package org.opendelos.control.repository.structure;


import java.util.List;

import org.opendelos.control.repository.structure.extension.StudyProgramOoRepository;
import org.opendelos.model.structure.StudyProgram;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyProgramRepository extends MongoRepository<StudyProgram, String>, StudyProgramOoRepository {

	StudyProgram findByIdentity(String identity);
	List<StudyProgram> findByDepartmentId(String departmentId);

}
