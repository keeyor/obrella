package org.opendelos.sync.legacyrepo.CalendarRepo;

import java.text.ParseException;
import java.util.List;

import org.opendelos.legacydomain.calendar.AcademicCalendarSe;
import org.opendelos.legacydomain.calendar.Argies;
import org.opendelos.legacydomain.calendar.Period;
import org.opendelos.legacydomain.calendar.Periods;


public interface AcademicCalendarSeDAO {

	 AcademicCalendarSe getCalendarByYear(String year) throws Exception;
	
	 Periods getDefaultCalendarPeriods(AcademicCalendarSe AcademicCalendarSe);
	
	// Should get All Department Periods of the Year. If Custom not Exists, return Default in place
	 Periods  getDepartmentPeriods(String departmentId, AcademicCalendarSe AcademicCalendarSe);
	// Should get All Study Periods of the Year. If Custom not Exists, return Default in place
	 Periods  getStudyPeriods(String departmentId, String studyId, AcademicCalendarSe AcademicCalendarSe);
		
		
	 Period getDefaultPeriodByDate(String date, AcademicCalendarSe AcademicCalendarSe) throws ParseException;
	
	// Should always return one Department Period. If Custom not Exists, return Default in place
	 Period getDepartmentPeriodByDate(String departmentId,  String date, AcademicCalendarSe AcademicCalendarSe) throws ParseException;
	
	// Should always return one Study Period. If Custom not Exists, return Department's in place. Which in turn returns department's custom or default if custom not Exists.
	 Period getStudyPeriodByDate(String departmentId, String studyId,  String date, AcademicCalendarSe AcademicCalendarSe) throws ParseException;

     List<String> getAvailableAcademicCalendarYears() throws Exception;
    
    int getCurrentAcademicYear();
    /* *************** ARGIES ********************** */
     Argies getDefaultCalendarArgies(AcademicCalendarSe AcademicCalendarSe);
     Argies  getDepartmentArgies(String departmentId, AcademicCalendarSe AcademicCalendarSe);


    
}
