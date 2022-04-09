package org.opendelos.eventsapp.repository.scheduledEvent;

import org.opendelos.eventsapp.repository.scheduledEvent.extension.ScheduledEventOoRepository;
import org.opendelos.model.resources.ScheduledEvent;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledEventRepository extends MongoRepository<ScheduledEvent, String>, ScheduledEventOoRepository {

	ScheduledEvent findByIdentity(String identity);
	long updateResourcesScheduledEvent(ScheduledEvent scheduledEvent);
}
