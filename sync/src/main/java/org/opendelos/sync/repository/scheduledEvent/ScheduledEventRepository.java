package org.opendelos.sync.repository.scheduledEvent;

import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.structure.Course;
import org.opendelos.sync.repository.scheduledEvent.extension.ScheduledEventOoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledEventRepository extends MongoRepository<ScheduledEvent, String>, ScheduledEventOoRepository {

	ScheduledEvent findByIdentity(String identity);
}
