/* 
     Author: Michael Gatzonis - 26/1/2021 
     live
*/
package org.opendelos.liveapp.services.converters;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;

public class StringToLocalTime implements Converter<String, LocalTime> {

	@Override
	public LocalTime convert(String from) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		return LocalTime.parse(from,dtf);
	}
}

