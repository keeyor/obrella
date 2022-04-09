/* 
     Author: Michael Gatzonis - 1/8/2019 
     OpenDelosDAC
*/
package org.opendelos.liveapp.services.resource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class ResourceFormat {

    public static String getDurationSpan(LocalDateTime today, LocalDateTime lc) {

            Map<String, Long> diffspan = DateDiffSpan(lc, today);

            long years   = Math.abs(diffspan.get("years"));
            long months  = Math.abs(diffspan.get("months"));
            long days    = Math.abs(diffspan.get("days"));
            long hours   = Math.abs(diffspan.get("hours"));
            long minutes = Math.abs(diffspan.get("minutes"));

            StringBuilder pasted_time = new StringBuilder();
            while (true) {

                if (years > 0) {
                    if (years == 1)  pasted_time.append(years).append(" year ");
                    else pasted_time.append(years).append(" years ");
                    if (months > 0) {
                        if (months == 1) pasted_time.append( "& ").append(months).append(" month ");
                        else pasted_time.append( "& ").append(months).append(" months ");
                    }
                    break;
                }
                else if (months > 0) {
                    if (months == 1) pasted_time.append(months).append(" month ");
                    else pasted_time.append(months).append(" months ");
                    if (days > 0) {
                        if (days == 1) pasted_time.append(" & ").append(days).append(" day ");
                        else pasted_time.append(" & ").append(days).append(" days ");
                    }
                    break;
                }
                else if (days > 0) {
                    if (days == 1) pasted_time.append(days).append(" day ");
                    else pasted_time.append(days).append(" days ");
                    if (hours > 0) {
                        if (hours == 1) pasted_time.append(" & ").append(hours).append(" hour ");
                        else pasted_time.append(" & ").append(hours).append(" hours ");
                    }
                    break;
                }
                else if (hours > 0) {
                    if (hours == 1) pasted_time.append(" & ").append(hours).append(" hour ");
                    else pasted_time.append(hours).append(" hours ");
                    if (minutes > 0) {
                        if (minutes ==1) pasted_time.append( " & ").append(minutes).append(" minute ");
                        else pasted_time.append( " & ").append(minutes).append(" minutes ");
                    }
                    break;
                }
                else if (minutes > 0) {
                    if (minutes ==1) pasted_time.append( " & ").append(minutes).append(" minute ");
                    else pasted_time.append(minutes).append(" minutes ");
                }
                break;
            }
            if (pasted_time.toString().length() == 0) {
                pasted_time.append(" just now!");
            }
            else {
                pasted_time.append(" ago");
            }


        return pasted_time.toString();
    }

    public static  Map<String,Long> DateDiffSpan(LocalDateTime fromDateTime, LocalDateTime toDateTime) {


        LocalDateTime tempDateTime = LocalDateTime.from( fromDateTime );

        long years = tempDateTime.until( toDateTime, ChronoUnit.YEARS);
        tempDateTime = tempDateTime.plusYears( years );

        long months = tempDateTime.until( toDateTime, ChronoUnit.MONTHS);
        tempDateTime = tempDateTime.plusMonths( months );

        long days = tempDateTime.until( toDateTime, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays( days );

        long hours = tempDateTime.until( toDateTime, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours( hours );

        long minutes = tempDateTime.until( toDateTime, ChronoUnit.MINUTES);
        tempDateTime = tempDateTime.plusMinutes( minutes );

        long seconds = tempDateTime.until( toDateTime, ChronoUnit.SECONDS);

        Map<String, Long> diffspan = new HashMap<>();

        diffspan.put("years", years);
        diffspan.put("months", months);
        diffspan.put("days", days);
        diffspan.put("hours", hours);
        diffspan.put("minutes", minutes);
        diffspan.put("seconds", seconds);


       return diffspan;

    }


}
