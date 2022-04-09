package org.opendelos.control.repository.resource;

import org.opendelos.model.resources.Resource;
import org.opendelos.control.repository.resource.extension.ResourceOoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends MongoRepository<Resource, String>, ResourceOoRepository {

 	Resource findByIdentity(String identity);



}
