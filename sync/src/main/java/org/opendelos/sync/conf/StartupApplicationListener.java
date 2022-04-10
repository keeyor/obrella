/* 
     Author: Michael Gatzonis - 12/10/2020 
     opendelos-uoa
*/
package org.opendelos.sync.conf;

import lombok.SneakyThrows;
import org.opendelos.sync.service.SyncService;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	private final SyncService syncService;

	public StartupApplicationListener(SyncService syncService) {
		this.syncService = syncService;
	}


	@SneakyThrows
	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		syncService.SYNC();
	}
}
