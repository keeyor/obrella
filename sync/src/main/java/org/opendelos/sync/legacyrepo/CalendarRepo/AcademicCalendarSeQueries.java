package org.opendelos.sync.legacyrepo.CalendarRepo;

import org.springframework.stereotype.Repository;

@Repository("AcademicCalendarSeQueries")
public class AcademicCalendarSeQueries {


	public String getAcademicCalendar(String academicYear)
			throws Exception {

		String xQuery;

		String dbICollection = "/db/apps/delos-uoa/Scheduler/Config/";
		String namespace = " declare namespace ns=\"http://gunet.gr/AcademicCalendarSe\"; ";

		xQuery = namespace + " for $m1 in collection('" + dbICollection + "')//ns:AcademicCalendarSe[ns:Year='"
				+ academicYear + "']" + " return $m1";

		return xQuery;
	}
	
	public String getAvailableAcademicCalendarYears()
			throws Exception {

		String xQuery = null;

		String dbICollection = "/db/apps/delos-uoa/Scheduler/Config/";
		String namespace = " declare namespace ns=\"http://gunet.gr/AcademicCalendarSe\"; ";

		xQuery = namespace + " for $m1 in collection('" + dbICollection + "')//ns:AcademicCalendarSe/ns:Year/text()" + " return $m1";


		return xQuery;
	}
}
