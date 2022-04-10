package org.opendelos.sync.repository.resource;


import java.time.Instant;

import org.opendelos.model.resources.Resource;
import org.opendelos.sync.repository.resource.extension.ResourceOoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends MongoRepository<Resource, String>, ResourceOoRepository {

 	Resource findByIdentity(String identity);

}
