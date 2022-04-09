package org.opendelos.eventsapp.repository.system;

import org.opendelos.model.common.Feedback;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String>  {


}
