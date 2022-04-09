package org.opendelos.live.repository.structure;

import org.opendelos.model.structure.School;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends MongoRepository<School, String> {

	School findByIdentity(String identity);

}
