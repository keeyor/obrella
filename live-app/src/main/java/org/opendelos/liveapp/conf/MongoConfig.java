/* 
     Author: Michael Gatzonis - 27/12/2020 
     live
*/
package org.opendelos.liveapp.conf;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.opendelos.liveapp.services.converters.MongoLocalTimeToStringConverter;
import org.opendelos.liveapp.services.converters.StringToMongoLocalTimeConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

	@Value( "${spring.data.mongodb.host}" )
	private String mongoDbHost;
	@Value( "${spring.data.mongodb.port}" )
	private Integer mongoDbPort;
	@Value( "${spring.data.mongodb.database}" )
	private String mongoDbDatabase;
	@Value( "${spring.data.mongodb.username}" )
	private String mongoDbUsername;
	@Value( "${spring.data.mongodb.password}" )
	private String mongoDbPassword;

	@Bean
	public MongoClient mongoClient() {
		ConnectionString connectionString = new ConnectionString("mongodb://" + mongoDbHost + ":" + mongoDbPort +"/" + mongoDbDatabase);
		MongoCredential mongoCredential = MongoCredential.createCredential(mongoDbUsername,  mongoDbDatabase, mongoDbPassword.toCharArray());
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.credential(mongoCredential)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

	@Bean
	public MongoTemplate mongoTemplate() {
		return new MongoTemplate(mongoClient(), mongoDbDatabase);
	}

	@Override
	protected boolean autoIndexCreation() {
		return true;
	}

	@Override
	protected String getDatabaseName() {
		return mongoDbDatabase;
	}

/*	@Bean
	@Override
	public MongoCustomConversions customConversions() {
		List<Converter<?, ?>> converterList = new ArrayList<>();
		converterList.add(new MongoLocalTimeToStringConverter());
		converterList.add(new StringToMongoLocalTimeConverter());

		return new MongoCustomConversions(converterList);
	}*/
}
