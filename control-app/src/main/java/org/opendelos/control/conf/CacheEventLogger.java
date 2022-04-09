/* 
     Author: Michael Gatzonis - 14/2/2021 
     live
*/
package org.opendelos.control.conf;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Configuration;

@Configuration
 public class CacheEventLogger implements CacheEventListener<Object, Object> {

	private final Logger logger = LoggerFactory.getLogger(CacheEventLogger.class);

	@Override
	public void onEvent(
			CacheEvent<? extends Object, ? extends Object> cacheEvent) {
		logger.trace("Cache key " + cacheEvent.getKey() + "   oldkey: " + cacheEvent.getOldValue() + "    newvalue: " + cacheEvent.getNewValue());
	}
}
