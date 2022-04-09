package org.opendelos.live.repository.structure;


import java.util.List;

import org.opendelos.live.repository.structure.extension.ClassroomOoRepository;
import org.opendelos.model.structure.Classroom;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends MongoRepository<Classroom, String>, ClassroomOoRepository {

	Classroom findByIdentity(String identity);
	List<Classroom> findByCode(String code);
	List<Classroom> findAllByUsage(String usage);
	List<Classroom> findAllByCalendar(String enabled);

}
