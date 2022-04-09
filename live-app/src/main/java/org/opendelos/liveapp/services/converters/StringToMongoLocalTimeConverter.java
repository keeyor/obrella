/* 
     Author: Michael Gatzonis - 26/1/2021 
     live
*/
package org.opendelos.liveapp.services.converters;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.opendelos.model.scheduler.MongoLocalTime;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;

public class StringToMongoLocalTimeConverter implements Converter<String, MongoLocalTime> {

	private static final TypeDescriptor SOURCE = TypeDescriptor.valueOf(String.class);
	private static final TypeDescriptor TARGET = TypeDescriptor.valueOf(MongoLocalTime.class);

	@Override
	public MongoLocalTime convert(String source) {
		try {
			return MongoLocalTime.of(LocalTime.parse(source));
		} catch (DateTimeParseException ex) {
			throw new ConversionFailedException(SOURCE, TARGET, source, ex);
		}
	}
}
