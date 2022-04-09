package org.opendelos.liveapp.repository.structure;

import java.util.List;

import org.opendelos.liveapp.repository.structure.extension.StreamingServerOoRepository;
import org.opendelos.model.structure.StreamingServer;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreamingServerRepository extends MongoRepository<StreamingServer, String>, StreamingServerOoRepository {

	StreamingServer findByIdentity(String identity);
	List<StreamingServer> findAllByEnabled(String enabled);
	StreamingServer findByCode(String code);
}
