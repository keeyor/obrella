package org.opendelos.eventsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class EventsAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventsAppApplication.class, args);
	}

}
