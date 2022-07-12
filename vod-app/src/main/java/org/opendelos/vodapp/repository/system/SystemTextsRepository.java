package org.opendelos.vodapp.repository.system;

import java.util.List;

import org.opendelos.model.system.SystemText;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemTextsRepository extends MongoRepository<SystemText, String> {

	List<SystemText> findAllBySite(String site);
	SystemText findBySiteAndCode(String site, String code);
}