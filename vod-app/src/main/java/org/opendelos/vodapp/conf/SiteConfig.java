/* 
     Author: Michael Gatzonis - 13/2/2022 
     Balloon
*/
package org.opendelos.vodapp.conf;

import org.opendelos.model.structure.Institution;
import org.opendelos.vodapp.services.structure.InstitutionService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SiteConfig {

	@Value("${default.institution.identity}")
	String institution_identity;

	private final InstitutionService institutionService;

	public SiteConfig(InstitutionService institutionService) {
		this.institutionService = institutionService;
	}

	@Bean
	public Institution defaultInstitution() {
		return institutionService.findByIdentity(institution_identity);
	}
	@Bean
	public String currentAcademicYear() {
		return institutionService.getCurrentAcademicYear();
	}

}
