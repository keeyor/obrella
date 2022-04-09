/* 
     Author: Michael Gatzonis - 4/4/2021 
     obrella
*/
package org.opendelos.vodapp.services.system;

import java.util.List;

import org.opendelos.model.system.SystemMessage;
import org.opendelos.vodapp.repository.system.SystemMessagesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemMessageService {
	private final Logger logger = LoggerFactory.getLogger(SystemMessageService.class.getName());
	private final SystemMessagesRepository systemMessagesRepository;

	@Autowired
	public SystemMessageService(SystemMessagesRepository systemMessagesRepository) {
		this.systemMessagesRepository = systemMessagesRepository;
	}

	public List<SystemMessage> findAll() {
		logger.trace("SystemMessage.findAll");
		return systemMessagesRepository.findAll();
	}

	public void deleteAll() {
		logger.trace("SystemMessage.deleteAll");
		try {
			systemMessagesRepository.deleteAll();
		}
		catch (Exception e) {
			logger.error("error: SystemMessage.deleteAll:" + e.getMessage());
		}
	}

	public String create(SystemMessage SystemMessage) {
		String generatedId = null;
		try {
			SystemMessage systemMessage =  systemMessagesRepository.save(SystemMessage);
			generatedId = systemMessage.getId();
			logger.trace(String.format("SystemMessage.create: %s", SystemMessage.getText()));
		}
		catch (Exception e) {
			logger.error("error: SystemMessage.create:" + e.getMessage());
		}
		return generatedId;
	}

	public SystemMessage findById(String id) {
		logger.trace(String.format("SystemMessage.findById(%s)", id));
		return systemMessagesRepository.findById(id).orElse(null);
	}

	public void update(SystemMessage SystemMessage) {
		logger.trace(String.format("SystemMessage.update: %s", SystemMessage.getText()));
		try {
			systemMessagesRepository.save(SystemMessage);
		}
		catch (Exception e) {
			logger.error("error: SystemMessage.update:" + e.getMessage());
		}
	}

	public void delete(String id) {
		logger.trace(String.format("SystemMessage.delete: %s", id));
		try {
			systemMessagesRepository.deleteById(id);
		}
		catch (Exception e) {
			logger.error("error: SystemMessage.delete:" + e.getMessage());
		}
	}

	public List<SystemMessage> getAllByStatus(String status) {
		return systemMessagesRepository.findAllByStatus(status);
	}

	public List<SystemMessage> getAllByVisibleIs(boolean visible) {
		return systemMessagesRepository.findAllByVisibleIs(visible);
	}
}
