/* 
     Author: Michael Gatzonis - 26/1/2021 
     live
*/
package org.opendelos.sync.services.scheduler;

import java.time.LocalDate;

import org.opendelos.model.calendar.Period;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.sync.services.structure.InstitutionService;

import org.springframework.stereotype.Service;

@Service
public class ScheduleUtils {

 private final InstitutionService institutionService;

	public ScheduleUtils(InstitutionService institutionService) {
		this.institutionService = institutionService;
	}

	public String getPeriodByDate(String institutionId, String academicYear, LocalDate localDate) {

		CustomPeriod institution_periods= institutionService.getCustomPeriod(institutionId,academicYear);
		for (Period period: institution_periods.getPeriods().getPeriod()) {
			//default, ISO_LOCAL_DATE
			LocalDate period_startDate = LocalDate.parse(period.getStartDate());
			LocalDate period_endDate = LocalDate.parse(period.getEndDate());
			if ( (localDate.isAfter(period_startDate) || localDate.isEqual(period_startDate)) && (localDate.isBefore(period_endDate) || localDate.isEqual(period_endDate))) {
				return period.getName();
			}
		}
		return null;
	}
}
