package org.opendelos.liveapp.repository.system;

import java.util.List;

import org.opendelos.model.system.SystemMessage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMessagesRepository extends MongoRepository<SystemMessage, String>  {

	List<SystemMessage> findAllByVisibleIs(boolean visible);
	List<SystemMessage> findAllByStatus(String status);
}
