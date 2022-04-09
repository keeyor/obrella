package org.opendelos.live.repository.resource;

import org.opendelos.live.repository.extension.ResourceOoRepository;
import org.opendelos.model.resources.Resource;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends MongoRepository<Resource, String>, ResourceOoRepository {

 	Resource findByIdentity(String identity);

}
