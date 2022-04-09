/* 
     Author: Michael Gatzonis - 12/10/2020 
     opendelos-uoa
*/
package org.opendelos.live.conf;

import org.opendelos.live.services.scheduler.LiveService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {


	@Value("${default.institution.identity}")
	String institution_identity;

	private final LiveService liveService;

	@Autowired
	public StartupApplicationListener(LiveService liveService) {
		this.liveService = liveService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		liveService.UpdateTodaysSchedule();
	}
}
