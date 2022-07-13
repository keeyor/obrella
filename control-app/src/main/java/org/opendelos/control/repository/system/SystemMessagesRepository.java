package org.opendelos.control.repository.system;

import java.util.List;

import org.opendelos.control.repository.system.extension.SystemMessagesOoRepository;
import org.opendelos.control.services.system.SystemMessageService;
import org.opendelos.model.system.SystemMessage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMessagesRepository extends MongoRepository<SystemMessage, String>, SystemMessagesOoRepository {



}
