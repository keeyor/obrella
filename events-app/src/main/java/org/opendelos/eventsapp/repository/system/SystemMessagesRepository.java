package org.opendelos.eventsapp.repository.system;

import java.util.List;

import org.opendelos.eventsapp.repository.system.extension.SystemMessagesOoRepository;
import org.opendelos.model.system.SystemMessage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMessagesRepository extends MongoRepository<SystemMessage, String>, SystemMessagesOoRepository {

	List<SystemMessage> getAllByVisibleIs(boolean visible);

}
