/* 
     Author: Michael Gatzonis - 4/4/2021 
     obrella
*/
package org.opendelos.liveapp.services.system;

import java.util.List;

import org.opendelos.liveapp.repository.system.FeedbackRepository;
import org.opendelos.model.common.Feedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {
	private final Logger logger = LoggerFactory.getLogger(FeedbackService.class.getName());
	private final FeedbackRepository feedbackRepository;

	@Autowired
	public FeedbackService(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}

	public List<Feedback> findAll() {
		logger.trace("Feedback.findAll");
		return feedbackRepository.findAll();
	}

	public void deleteAll() {
		logger.trace("Feedback.deleteAll");
		try {
			feedbackRepository.deleteAll();
		}
		catch (Exception e) {
			logger.error("error: Feedback.deleteAll:" + e.getMessage());
		}
	}
	public String create(Feedback feedback) {
		String generatedId = null;
		try {
			Feedback systemMessage =  feedbackRepository.save(feedback);
			generatedId = systemMessage.getId();
			logger.trace(String.format("Feedback.create: %s", feedback.getId()));
		}
		catch (Exception e) {
			logger.error("error: Feedback.create:" + e.getMessage());
		}
		return generatedId;
	}

	public Feedback findById(String id) {
		logger.trace(String.format("feedback.findById(%s)", id));
		return feedbackRepository.findById(id).orElse(null);
	}
	public void delete(String id) {
		logger.trace(String.format("Feedback.delete: %s", id));
		try {
			feedbackRepository.deleteById(id);
		}
		catch (Exception e) {
			logger.error("error: Feedback.delete:" + e.getMessage());
		}
	}

}
