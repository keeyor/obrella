package org.opendelos.sync.legacyrepo.CalendarRepo.Impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.exist.xmldb.EXistResource;
import org.opendelos.legacydomain.calendar.AcademicCalendarSe;
import org.opendelos.legacydomain.calendar.Argia;
import org.opendelos.legacydomain.calendar.Argies;
import org.opendelos.legacydomain.calendar.Department;
import org.opendelos.legacydomain.calendar.Period;
import org.opendelos.legacydomain.calendar.Periods;
import org.opendelos.legacydomain.calendar.Study;
import org.opendelos.sync.legacyrepo.CalendarRepo.AcademicCalendarSeDAO;
import org.opendelos.sync.legacyrepo.CalendarRepo.AcademicCalendarSeQueries;
import org.opendelos.sync.legacyrepo.ElegacyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("AcademicCalendarSeDAO")
public class AcademicCalendarSeDAOImpl implements AcademicCalendarSeDAO {

	private static final Logger logger = LoggerFactory.getLogger(AcademicCalendarSeDAOImpl.class);

	@Autowired
	private AcademicCalendarSeQueries academicCalendarSeQueries;

	@Autowired
	private ElegacyRepository elegacyRepository;

	@Value("${import_url}")
	String import_url;

	@Value("${default.institution.identity}")
	private String dilos_institution;

	@Value("${main_col}")
	String main_col;

	@Override
	public AcademicCalendarSe getCalendarByYear(String academicYear) throws Exception {
		
		AcademicCalendarSe academicCalendarSe = null;
		String dbSchedulerConfigCollection = main_col + "Scheduler/Config/";

		String xQuery = academicCalendarSeQueries.getAcademicCalendar(academicYear);
		
		try {
			academicCalendarSe = (AcademicCalendarSe) elegacyRepository.QueryDatabase(import_url, "guest", "guest",dbSchedulerConfigCollection, xQuery, AcademicCalendarSe.class);
		} catch (Exception e) {
			logger.trace("getCalendarByYear:" + academicYear + " not found...");
		}

		return academicCalendarSe;
	}


	@Override
	public Periods getDefaultCalendarPeriods(AcademicCalendarSe academicCalendarSe) {
		
		if (academicCalendarSe == null || academicCalendarSe.getInstitution() == null || academicCalendarSe.getInstitution().getPeriods() == null) return null;
		return academicCalendarSe.getInstitution().getPeriods();
	}

	@Override
	public Periods getDepartmentPeriods(String departmentId, AcademicCalendarSe academicCalendarSe) {
		
       
        Department query_department = this.findDepartmentFromPeriods(departmentId, academicCalendarSe);
        
		if (query_department != null && query_department.getPeriods().getInherit().equals("0")) {
 					  return query_department.getPeriods();
		}
		
		//if we reach this point, department with id == departmentId, was not found or should inherit. Return Institution periods instead, after setting RefId, Inherit
		Periods institution_periods = academicCalendarSe.getInstitution().getPeriods();
		institution_periods.setRefId(departmentId);
		institution_periods.setInherit("1");
		
		return institution_periods;
		
	}

	@Override
	public Periods getStudyPeriods(String departmentId, String studyId, AcademicCalendarSe academicCalendarSe) {
	   
		Study query_study= this.findStudy(departmentId, studyId, academicCalendarSe);
		
		if (query_study != null && query_study.getPeriods().getInherit().equals("0")) {
			  return query_study.getPeriods();
		}
		//if we reach this point, study with id == studytId, was not found or should inherit. Return Department periods instead, after setting RefId, Inherit
		
		Periods department_periods = this.getDepartmentPeriods(departmentId, academicCalendarSe);
		department_periods.setRefId(studyId);
		department_periods.setInherit("1");
		
		return department_periods;
	}

	private Department findDepartmentFromPeriods(String departmentId, AcademicCalendarSe academicCalendarSe) {
		
        if (academicCalendarSe == null || academicCalendarSe.getInstitution() == null || academicCalendarSe.getInstitution().getDepartments() == null || academicCalendarSe.getInstitution().getDepartments().getDepartment() == null) return null;
        
		   List<Department> department_list= academicCalendarSe.getInstitution().getDepartments().getDepartment();
			
			if (department_list != null) {
				for (Department query_department : department_list) {
					if (query_department.getPeriods() != null && query_department.getPeriods().getRefId()
							.equals(departmentId)) {
						return query_department;
					}
				}
			}
			return null;
	}
	
	private Department findDepartmentFromArgies(String departmentId, AcademicCalendarSe academicCalendarSe) {
		
        if (academicCalendarSe == null || academicCalendarSe.getInstitution() == null || academicCalendarSe.getInstitution().getDepartments() == null || academicCalendarSe.getInstitution().getDepartments().getDepartment() == null) return null;
     
		   List<Department> department_list= academicCalendarSe.getInstitution().getDepartments().getDepartment();
			
			if (department_list != null) {
				for (Department query_department : department_list) {
					if (query_department.getArgies() != null && query_department.getArgies().getRefId() != null && query_department.getArgies().getRefId()
							.equals(departmentId)) {
						return query_department;
					}
				}
			}
			return null;
	}
	
	
	private Study findStudy(String departmentId, String studyId, AcademicCalendarSe academicCalendarSe) {
		
		   Department query_department = this.findDepartmentFromPeriods(departmentId, academicCalendarSe);
		   
		   if (query_department == null || query_department.getStudies() == null || query_department.getStudies().getStudy() == null ) return null;
			    
		   List<Study> study_list = query_department.getStudies().getStudy();
			
		   if (study_list != null) {
			   for (Study query_study : study_list) {
				   if (query_study.getPeriods().getRefId().equals(studyId)) {
					   return query_study;
				   }
			   }
			}
			return null;
	}
	
	@Override
	public org.opendelos.legacydomain.calendar.Period getDefaultPeriodByDate(String date, AcademicCalendarSe AcademicCalendarSe) throws ParseException {
				
		Periods default_periods = this.getDefaultCalendarPeriods(AcademicCalendarSe);
		
		for (Period period : default_periods.getPeriod()) {
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date formDate  = format.parse(period.getStartDate());
			Date toDate    = format.parse(period.getEndDate());
			Date checkDate = format.parse(date);
			
			if (DsmDateUtil.dateInPeriod(checkDate, formDate, toDate)) {
				return period;
			}
		}
		
		return null;
	}
	
	@Override
	public Period getDepartmentPeriodByDate(String departmentId, String date, AcademicCalendarSe AcademicCalendarSe) throws ParseException {
				
		Periods  department_periods = this.getDepartmentPeriods(departmentId, AcademicCalendarSe);
		
		for (Period period : department_periods.getPeriod()) {
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date formDate  = format.parse(period.getStartDate());
			Date toDate    = format.parse(period.getEndDate());
			Date checkDate = format.parse(date);
			
			if (DsmDateUtil.dateInPeriod(checkDate, formDate, toDate)) {
				return period;
			}
		}
		
		return null;
	}

	@Override
	public Period getStudyPeriodByDate(String departmentId, String studyId, String date,
			AcademicCalendarSe AcademicCalendarSe) throws ParseException {
		
		Periods study_periods = this.getStudyPeriods(departmentId, studyId, AcademicCalendarSe);
		
		for (Period period : study_periods.getPeriod()) {
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date formDate  = format.parse(period.getStartDate());
			Date toDate    = format.parse(period.getEndDate());
			Date checkDate = format.parse(date);
			
			if (DsmDateUtil.dateInPeriod(checkDate, formDate, toDate)) {
				return period;
			}
		}
		
		return null;
	}



	@Override
	public List<String> getAvailableAcademicCalendarYears() throws Exception {
		 
		List<String> year_list = new ArrayList<>();
		
		String dbSchedulerConfigCollection = main_col + "/Scheduler/Config/";

		String xQuery = academicCalendarSeQueries.getAvailableAcademicCalendarYears();
		
		ResourceSet result = null;
		try {
			Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest",dbSchedulerConfigCollection);
			 result = elegacyRepository.QueryDatabaseByQueryString(col, xQuery);
		} catch (Exception e) {
				logger.error("getAvailableAcademicCalendarYears Query:" + e.getMessage());
		}

		 ResourceIterator i;
				Resource res=null;
				try {
					assert result != null;
					i = result.getIterator();
					/* Parse Results */
					while (i.hasMoreResources()) {
						try {
						res = i.nextResource();
						year_list.add(res.getContent().toString());
					} finally {
						//dont forget to cleanup resources
							if (res != null)
								try { ((EXistResource)res).freeResources(); } catch(XMLDBException ignored) {}
					}
					}

				} catch (XMLDBException e) {
					logger.error("getAvailableAcademicCalendarYears. Error parsing results:" + e.getMessage());
				}

		return year_list;
	}

	@Override
	public int getCurrentAcademicYear() {

		int current_academic_year;
		Date date = new Date();
		// Choose time zone in which you want to interpret your Date
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH); //zero-based

		int seasonEndYear;
		if (month>7 && month < 12) { //September  to December
			seasonEndYear = year+1;
		}
		else {
			seasonEndYear = year;
		}

		current_academic_year = seasonEndYear-1;

		return  current_academic_year;
	}

	@Override
	public Argies getDefaultCalendarArgies(AcademicCalendarSe academicCalendarSe) {
		
		if (academicCalendarSe == null || academicCalendarSe.getInstitution() == null || academicCalendarSe.getInstitution().getArgies() == null) {
			Argies argies = new Argies();
			argies.getArgia().addAll(new ArrayList<>());
			argies.setRefId(dilos_institution);
			return argies;
		}
		
		Argies argies = academicCalendarSe.getInstitution().getArgies();
		
		List<Argia> argies_list = argies.getArgia();
		
		argies_list.sort(new CustomComparator());
	
		return argies;
	}

	public static class CustomComparator implements Comparator<Argia> {
	    @Override
	    public int compare(Argia o1, Argia o2) {
	        return o1.getEndDate().compareTo(o2.getEndDate());
	    }
	}
	
	@Override
	public Argies getDepartmentArgies(String departmentId, AcademicCalendarSe academicCalendarSe) {
		
		    Department query_department = this.findDepartmentFromArgies(departmentId, academicCalendarSe);
	        
			if (query_department != null && query_department.getArgies() != null) {
	 					  return query_department.getArgies();
			}
			
			Argies argies = new Argies();
			argies.getArgia().addAll(new ArrayList<>());
			argies.setRefId(departmentId);
			
			return argies;
	}

}
