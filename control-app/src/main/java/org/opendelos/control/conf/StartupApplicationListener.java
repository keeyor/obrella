/* 
     Author: Michael Gatzonis - 12/10/2020 
     opendelos-uoa
*/
package org.opendelos.control.conf;

import org.opendelos.control.services.scheduler.LiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {


	@Value("${default.institution.identity}")
	String institution_identity;

	private static final Logger logger = LoggerFactory.getLogger(StartupApplicationListener.class);

	private final LiveService liveService;

	public StartupApplicationListener(LiveService liveService) {
		this.liveService = liveService;
	}

	@Override public void onApplicationEvent(ContextRefreshedEvent event) {

		String event_id = event.getApplicationContext().getId();
		//MG: 12-20-2020: make the distinction for event_id otherwise this is called twice on startup
		if (event_id.contains("application")) {
				try {
					//liveService.updateLiveEntries();
				}
				catch (Exception e) {
					logger.error("Couldn't update Today's Schedule. The error is:" + e.getMessage());
				}
		}
	}

}
