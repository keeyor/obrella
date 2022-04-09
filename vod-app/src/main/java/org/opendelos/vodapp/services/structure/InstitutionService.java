/*
     Author: Michael Gatzonis - 10/22/2018
     OpenDelosDAC
*/
package org.opendelos.vodapp.services.structure;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.Institution;
import org.opendelos.vodapp.repository.structure.InstitutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
////@CacheConfig(cacheNames = "institution")
public class InstitutionService {

	private final Logger logger = LoggerFactory.getLogger(InstitutionService.class.getName());
	private final InstitutionRepository institutionRepository;

	@Autowired
	public InstitutionService(InstitutionRepository institutionRepository) {
		this.institutionRepository = institutionRepository;
	}

	public List<Institution> findAll() {
		logger.trace("Institution.findAll");
		return institutionRepository.findAll();
	}
	////@CacheEvict(allEntries = true)
	public void deleteAll() {
		logger.trace("Institution.deleteAll");
		try {
			institutionRepository.deleteAll();
			logger.trace("Institution.deleteAll");
		}
		catch (Exception e) {
			logger.error("error: Institution.deleteAll:" + e.getMessage());
		}
	}
//	//@CacheEvict(allEntries = true)
	public String create(Institution institution) {
		String generatedId = null;
		try {
			Institution nInstitution = institutionRepository.save(institution);
			generatedId = nInstitution.getId();
			logger.trace(String.format("Institution.create with id: %s:", generatedId));
		}
		catch (Exception e) {
			logger.error("error: Institution.create:" + e.getMessage());
		}
		return generatedId;
	}
	////@Cacheable
	public Institution findById(String id) {
		logger.trace(String.format("Institution.findById(%s)", id));
		return institutionRepository.findById(id).orElse(null);
	}
//	@CachePut(key = "#institution.id")
	public void update(Institution institution) {
		logger.trace(String.format("Institution.update: %s", institution.getId()));
		try {
			institutionRepository.findAndUpdate(institution);
		}
		catch (Exception e) {
			logger.error("error: Institution.update:" + e.getMessage());
		}
	}
//	//@CacheEvict(key = "#id")
	public void delete(String id) {
		logger.trace(String.format("Institution.delete: %s", id));
		try {
			institutionRepository.deleteById(id);
		}
		catch (Exception e) {
			logger.error("error: Institution.delete:" + e.getMessage());
		}
	}

	public Institution findByIdentity(String identity) {
		logger.trace(String.format("Institutions.findByIdentity(%s)", identity));
		return institutionRepository.findByIdentity(identity);
	}

	/* Calendar  */
	public List<String> getAvailableAcademicCalendarYears(String id) {

		List<String> year_list = new ArrayList<>();
		List<CustomPeriod> customPeriods = institutionRepository.getCustomPeriods(id);
		for (CustomPeriod customPeriod:  customPeriods) {
			year_list.add(customPeriod.getYear());
		}
		return year_list;
	}
	public String getCurrentAcademicYear() {
		int current_academic_year;
		Date date = new Date();
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());		// Choose time zone in which you want to interpret your Date
		cal.setTime(date);
		int currentYear = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH); //zero based month
		if (currentMonth>7) {
			currentYear++; //academic year ends at year+1. e.g. 1) for 10-8-2020 academic year ends at 31-09-2021, 2) for 20-08-2020 academic year ends at 31-08-2020
		}
		current_academic_year = currentYear-1; //academic year start at currentYear-1. e.g 1) academic year starts at 1-10-2020, 2) academic year starts at 01-10-2019

		return  String.valueOf(current_academic_year);
	}

	public void saveCustomPeriod(String id, CustomPeriod customPeriod){
		institutionRepository.saveCustomPeriod(id,customPeriod);
	}
	public void deleteCustomPeriod(String id, String year) {
		institutionRepository.deleteCustomPeriod(id,year);
	}
	public List<CustomPeriod> getCustomPeriods(String id) {
		return  institutionRepository.getCustomPeriods(id);
	}
	public CustomPeriod getCustomPeriod(String id, String year){
		return  institutionRepository.getCustomPeriod(id,year);
	}

	/* Argies */
	public void saveCustomPause(String id, CustomPause customPause){
		institutionRepository.saveCustomPause(id,customPause);
	}
	public void deleteCustomPause(String id, String year) {
		institutionRepository.deleteCustomPause(id,year);
	}

	public List<CustomPause> getCustomPauses(String id) {
		return  institutionRepository.getCustomPauses(id);
	}
	public CustomPause getCustomPause(String id, String year){
		return  institutionRepository.getCustomPause(id,year);
	}

}
