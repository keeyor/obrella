/* 
     Author: Michael Gatzonis - 4/4/2021 
     obrella
*/
package org.opendelos.liveapp.services.system;

import java.util.List;

import org.opendelos.liveapp.repository.system.SystemTextsRepository;
import org.opendelos.model.system.SystemText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemTextService {
	private final Logger logger = LoggerFactory.getLogger(SystemTextService.class.getName());
	private final SystemTextsRepository systemTextsRepository;

	@Autowired
	public SystemTextService(SystemTextsRepository systemTextsRepository) {
		this.systemTextsRepository = systemTextsRepository;
	}

	public List<SystemText> findAll() {
		logger.trace("SystemText.findAll");
		return systemTextsRepository.findAll();
	}

	public void deleteAll() {
		logger.trace("SystemText.deleteAll");
		try {
			systemTextsRepository.deleteAll();
		}
		catch (Exception e) {
			logger.error("error: SystemText.deleteAll:" + e.getMessage());
		}
	}

	public String create(SystemText systemText) {
		String generatedId = null;
		try {
			SystemText systemMessage =  systemTextsRepository.save(systemText);
			generatedId = systemMessage.getId();
			logger.trace(String.format("SystemText.create: %s", systemText.getSite()));
		}
		catch (Exception e) {
			logger.error("error: SystemText.create:" + e.getMessage());
		}
		return generatedId;
	}

	public SystemText findById(String id) {
		logger.trace(String.format("SystemText.findById(%s)", id));
		return systemTextsRepository.findById(id).orElse(null);
	}

	public void update(SystemText systemText) {
		logger.trace(String.format("SystemText.update: %s", systemText.getSite()));
		try {
			systemTextsRepository.save(systemText);
		}
		catch (Exception e) {
			logger.error("error: SystemText.update:" + e.getMessage());
		}
	}

	public void delete(String id) {
		logger.trace(String.format("SystemText.delete: %s", id));
		try {
			systemTextsRepository.deleteById(id);
		}
		catch (Exception e) {
			logger.error("error: SystemText.delete:" + e.getMessage());
		}
	}

	public List<SystemText> findAllBySite(String site) {
		return systemTextsRepository.findAllBySite(site);
	}
	public SystemText findBySiteAndCode(String site, String code) {
		return systemTextsRepository.findBySiteAndCode(site,code);
	}
}
