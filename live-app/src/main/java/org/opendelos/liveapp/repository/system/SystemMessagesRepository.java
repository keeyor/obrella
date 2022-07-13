package org.opendelos.liveapp.repository.system;

import org.opendelos.liveapp.repository.system.extension.SystemMessagesOoRepository;
import org.opendelos.model.system.SystemMessage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMessagesRepository extends MongoRepository<SystemMessage, String>, SystemMessagesOoRepository {



}
