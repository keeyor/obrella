package org.opendelos.control.repository.system.extension;

import java.util.List;

import org.opendelos.model.system.SystemMessage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMessagesOoRepository {

	List<SystemMessage> findAllByVisibleAndTargetAndSitesOrderByStartDateDesc(boolean visible,String target,String sites);
}
