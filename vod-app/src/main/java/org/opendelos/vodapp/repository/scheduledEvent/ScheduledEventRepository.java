package org.opendelos.vodapp.repository.scheduledEvent;

import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.vodapp.repository.scheduledEvent.extension.ScheduledEventOoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledEventRepository extends MongoRepository<ScheduledEvent, String>, ScheduledEventOoRepository {

	ScheduledEvent findByIdentity(String identity);
	long updateResourcesScheduledEvent(ScheduledEvent scheduledEvent);
}
