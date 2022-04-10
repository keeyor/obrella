package org.opendelos.sync.legacyrepo.CalendarRepo.Impl;

import java.util.Calendar;
import java.util.Date;

public class DsmDateUtil {

	/**
	 * <p><strong>
	 * Create a {@link Date} object based on date, hour and minutes.</strong></p>
	 * 
	 * 
	 * @author openDelos Team
	 * @since 0.5
	 * 
	 * @param date
	 * @param hour
	 * @param minutes
	 * 
	 * @return {@link Date}
	 */
	public static Date getDate(Date date, Integer hour, Integer minutes){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (hour != null){
			calendar.set(Calendar.HOUR_OF_DAY, hour);
		}
		if (minutes != null){
			calendar.set(Calendar.MINUTE, minutes);
		}
		return calendar.getTime(); 
	}
	
	/**
	 * <p><strong>
	 * Get next Friday date.</strong></p>
	 * 
	 * 
	 * @author openDelos Team
	 * @since 0.5
	 * 
	 * @return {@link Date}
	 */
	public static Date getNextFriday() {
		Calendar today = Calendar.getInstance();
		int daysUntilNextFriday = Calendar.FRIDAY - today.get(Calendar.DAY_OF_WEEK) + 7;
		Calendar nextFriday = (Calendar)today.clone();
		nextFriday.add(Calendar.DATE, daysUntilNextFriday);
		return nextFriday.getTime();
	}
	
	/**
	 * <p><strong>
	 * Check whether date1 is after date2.</strong></p>
	 * 
	 * 
	 * @author openDelos Team
	 * @since 0.5
	 * 
	 * @param date1
	 * @param date2
	 * 
	 * @return {@link boolean}
	 */
	public static boolean isAfter(Date date1, Date date2){
		if (date1 == null || date2 == null) return false;
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(date1);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(date2);
		return date1.after(date2);
	}
	
	/**
	 * <p><strong>
	 * Check whether date1 is the same date as date2.</strong></p>
	 * 
	 * 
	 * @author openDelos Team
	 * @since 0.5
	 * 
	 * @param date1
	 * @param date2
	 * 
	 * @return {@link boolean}
	 */
	public static boolean isSameDate(Date date1, Date date2){
		if (date1 == null || date2 == null) return false;
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(date1);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(date2);
		return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
		calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
	}
	
	/**
	 * <p><strong>
	 * Check whether date1 is in the (dateFrom, dateTo) period.</strong></p>
	 * 
	 * 
	 * @author openDelos Team
	 * @since 0.5
	 * 
	 * @param date
	 * @param dateFrom
	 * @param dateTo
	 * 
	 * @return {@link boolean}
	 */
	public static boolean dateInPeriod(Date date, Date dateFrom, Date dateTo){
		return date.after(dateFrom) && date.before(dateTo) ||
		date.compareTo(dateFrom) == 0 ||
		date.compareTo(dateTo) == 0 ||
		isSameDate(date, dateFrom) ||
		isSameDate(date, dateTo);
	}
	
	/**
	 * <p><strong>
	 * Check whether the (dateFrom1, dateTo1) and (dateFrom2, dateTo2) periods
	 * are overlapping.</strong></p>
	 * 
	 * 
	 * @author openDelos Team
	 * @since 0.5
	 * 
	 * @param dateFrom1
	 * @param dateTo1
	 * @param dateFrom2
	 * @param dateTo2
	 * 
	 * @return {@link boolean}
	 */
	public static boolean periodsAreOverlapping(Date dateFrom1, Date dateTo1,
		Date dateFrom2, Date dateTo2){
		return 
		isSameDate(dateFrom1, dateFrom2) || isSameDate(dateFrom1, dateTo2) ||
		isSameDate(dateTo1, dateFrom2) || isSameDate(dateTo1, dateTo2) ||
		dateFrom1.after(dateFrom2) && dateFrom1.before(dateTo2) ||
		dateTo1.after(dateFrom2) && dateTo1.before(dateTo2)
		;
	}
	
	/**
	 * <p><strong>
	 * Check whether two times are overlapping.</strong></p>
	 * 
	 * 
	 * @author openDelos Team
	 * @since 0.5
	 * 
	 * @param hour1
	 * @param minutes1
	 * @param durationHours1
	 * @param durationMinutes1
	 * @param hour2
	 * @param minutes2
	 * @param durationHours2
	 * @param durationMinutes2
	 * 
	 * @return {@link boolean}
	 */
	public static boolean dateTimeInPeriod(
		int hour1, int minutes1, int durationHours1, int durationMinutes1,
		int hour2, int minutes2, int durationHours2, int durationMinutes2){
		//
		int start1 = hour1 * 60 + minutes1;
		int end1 = start1 + durationHours1 * 60 + durationMinutes1;
		int start2 = hour1 * 60 + minutes2;
		int end2 = start2 + durationHours2 * 60 + durationMinutes2;
		// 2 starts
		if (start1 > start2 && start2 < end1){
			return true;
		}
		// 2 ends
		if (start1 > end2 && end2 < end1){
			return true;
		}
		return false;
	}
}