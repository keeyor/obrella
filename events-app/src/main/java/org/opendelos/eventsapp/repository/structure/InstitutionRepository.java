package org.opendelos.eventsapp.repository.structure;

import org.opendelos.eventsapp.repository.structure.extension.InstitutionOoRepository;
import org.opendelos.model.structure.Institution;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionRepository extends MongoRepository<Institution, String>, InstitutionOoRepository {

	Institution findByIdentity(String identity);
}
