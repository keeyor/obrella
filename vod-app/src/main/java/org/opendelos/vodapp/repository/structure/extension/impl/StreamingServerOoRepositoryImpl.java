/* 
     Author: Michael Gatzonis - 13/11/2020 
     live
*/
package org.opendelos.vodapp.repository.structure.extension.impl;

import java.util.List;

import org.opendelos.model.structure.StreamingServer;
import org.opendelos.vodapp.repository.structure.extension.StreamingServerOoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class StreamingServerOoRepositoryImpl implements StreamingServerOoRepository {

	private final MongoTemplate mongoTemplate;

	@Autowired
	public StreamingServerOoRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}


	@Override
	public List<StreamingServer> findAllIdsByEnabled(String enabled) {

		Criteria expression = new Criteria();
		expression.and("enabled").is(enabled);
		Query query = new Query();
		query.addCriteria(expression);
		query.fields().include("id");

		return mongoTemplate.find(query,StreamingServer.class);
	}
	@Override
	public List<StreamingServer> findAllIdsByEnabledAndType(String enabled, String type) {

		Query query = new Query();

		Criteria expression = new Criteria();
		expression.and("enabled").is(enabled).and("type").is(type);
		query.addCriteria(expression);

		query.fields().include("id");

		List<StreamingServer> servers = mongoTemplate.find(query,StreamingServer.class);

		return  servers;
	}

	@Override
	public List<StreamingServer> findAllByEnabledAndType(String enabled, String type) {
		Query query = new Query();

		Criteria expression = new Criteria();
		expression.and("enabled").is(enabled).and("type").is(type);
		query.addCriteria(expression);

		return mongoTemplate.find(query,StreamingServer.class);
	}
}
