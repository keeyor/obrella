/* 
     Author: Michael Gatzonis - 26/1/2021 
     live
*/
package org.opendelos.control.mvc.Converters;

import java.time.format.DateTimeFormatter;

import org.opendelos.model.scheduler.MongoLocalTime;

import org.springframework.core.convert.converter.Converter;

public class MongoLocalTimeToStringConverter implements Converter<MongoLocalTime, String> {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

	@Override
	public String convert(MongoLocalTime source) {
		return formatter.format(source.toLocalTime());
	}
}
