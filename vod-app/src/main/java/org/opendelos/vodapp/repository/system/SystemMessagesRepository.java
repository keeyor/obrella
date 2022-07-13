package org.opendelos.vodapp.repository.system;


import java.util.List;

import org.opendelos.model.system.SystemMessage;
import org.opendelos.vodapp.repository.system.extension.SystemMessagesOoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMessagesRepository extends MongoRepository<SystemMessage, String>, SystemMessagesOoRepository {

	List<SystemMessage> getAllByVisibleIs(boolean visible);

}
