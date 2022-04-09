/* 
     Author: Michael Gatzonis - 26/1/2021 
     live
*/
package org.opendelos.liveapp.services.scheduler;

import java.time.LocalDate;

import org.opendelos.liveapp.services.structure.DepartmentService;
import org.opendelos.liveapp.services.structure.InstitutionService;
import org.opendelos.liveapp.services.structure.StudyProgramService;
import org.opendelos.model.calendar.Period;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.StudyProgram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ScheduleUtils {

 	private final InstitutionService institutionService;
 	private final DepartmentService departmentService;
 	private final StudyProgramService studyProgramService;

	@Value("${default.institution.identity}")
	String institution_identity;

 	@Autowired
	public ScheduleUtils(InstitutionService institutionService, DepartmentService departmentService, StudyProgramService studyProgramService) {
		this.institutionService = institutionService;
		this.departmentService = departmentService;
		this.studyProgramService = studyProgramService;
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
	public String getDepartmentPeriodNameByDate(String departmentId, String academicYear, LocalDate localDate) {

		CustomPeriod department_periods= departmentService.getCustomPeriod(departmentId,academicYear);
		if (department_periods == null) {
			Institution institution  = institutionService.findByIdentity(institution_identity);
			department_periods = institutionService.getCustomPeriod(institution.getId(),academicYear);
		}
		for (Period period: department_periods.getPeriods().getPeriod()) {
			//default, ISO_LOCAL_DATE
			LocalDate period_startDate = LocalDate.parse(period.getStartDate());
			LocalDate period_endDate = LocalDate.parse(period.getEndDate());
			if ( (localDate.isAfter(period_startDate) || localDate.isEqual(period_startDate)) && (localDate.isBefore(period_endDate) || localDate.isEqual(period_endDate))) {
				return period.getName();
			}
		}
		return null;
	}
	public Period getDepartmentPeriodByDate(String departmentId, String academicYear, LocalDate localDate) {

		CustomPeriod department_periods= departmentService.getCustomPeriod(departmentId,academicYear);
		if (department_periods == null) {
			Institution institution  = institutionService.findByIdentity(institution_identity);
			department_periods = institutionService.getCustomPeriod(institution.getId(),academicYear);
		}
		for (Period period: department_periods.getPeriods().getPeriod()) {
			//default, ISO_LOCAL_DATE
			LocalDate period_startDate = LocalDate.parse(period.getStartDate());
			LocalDate period_endDate = LocalDate.parse(period.getEndDate());
			if ( (localDate.isAfter(period_startDate) || localDate.isEqual(period_startDate)) && (localDate.isBefore(period_endDate) || localDate.isEqual(period_endDate))) {
				return period;
			}
		}
		return null;
	}

	public String getStudyPeriodNameByDate(String studyId, String academicYear, LocalDate localDate) {

		CustomPeriod study_periods = studyProgramService.getCustomPeriod(studyId,academicYear);
		if (study_periods == null) {
			StudyProgram studyProgram = studyProgramService.findById(studyId);
			return this.getDepartmentPeriodNameByDate(studyProgram.getDepartmentId(),academicYear,localDate);
		}
		for (Period period: study_periods.getPeriods().getPeriod()) {
			//default, ISO_LOCAL_DATE
			LocalDate period_startDate = LocalDate.parse(period.getStartDate());
			LocalDate period_endDate = LocalDate.parse(period.getEndDate());
			if ( (localDate.isAfter(period_startDate) || localDate.isEqual(period_startDate)) && (localDate.isBefore(period_endDate) || localDate.isEqual(period_endDate))) {
				return period.getName();
			}
		}
		return null;
	}
	public Period getStudyPeriodByDate(String studyId, String academicYear, LocalDate localDate) {

		CustomPeriod study_periods = studyProgramService.getCustomPeriod(studyId,academicYear);
		if (study_periods == null) {
			StudyProgram studyProgram = studyProgramService.findById(studyId);
			return this.getDepartmentPeriodByDate(studyProgram.getDepartmentId(),academicYear,localDate);
		}
		for (Period period: study_periods.getPeriods().getPeriod()) {
			//default, ISO_LOCAL_DATE
			LocalDate period_startDate = LocalDate.parse(period.getStartDate());
			LocalDate period_endDate = LocalDate.parse(period.getEndDate());
			if ( (localDate.isAfter(period_startDate) || localDate.isEqual(period_startDate)) && (localDate.isBefore(period_endDate) || localDate.isEqual(period_endDate))) {
				return period;
			}
		}
		return null;
	}
}
