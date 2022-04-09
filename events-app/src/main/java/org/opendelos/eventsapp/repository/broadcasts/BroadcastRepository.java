package org.opendelos.eventsapp.repository.broadcasts;

import org.opendelos.model.scheduler.Broadcast;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BroadcastRepository extends MongoRepository<Broadcast, String>{


}
