/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.control.api.common;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class ApiUtils {

	public static String  FormatResultsForSelect2(Object o) throws Exception {

		String json;
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (o != null) {
				json = mapper.writeValueAsString(o);
			} else {
				json = "[]";
			}
		}
		catch (IOException e) {
			throw new Exception("Error: FormatResultsForSelect");
		}
		String str1 = "{";
		json = str1 + " \"results\":" + json + "}";
		//json = str1 + " \"results\":" + json + ", \"pagination\":{" + "\"more\": true" + "}}";
		return json;
	}
	public static byte[]  TransformResultsForDataTable(Object o) {

		String json = "";
		ObjectMapper mapper = new ObjectMapper()
				.registerModule(new ParameterNamesModule())
				.registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
		try {
			if (o != null) {
				json = mapper.writeValueAsString(o);
			} else {
				json = "[]";
			}
		}
		catch (IOException e) {
				System.out.println(e.getMessage());
		}
		json = "{" + " \"data\":" + json + "}";

		return json.getBytes();
	}

	public static byte[]  TransformResultsForCalendar(Object o) {

		String json = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (o != null) {
				json = mapper.writeValueAsString(o);
			} else {
				json = "[]";
			}
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
		String str1 = "{" + json + "}";

		return json.getBytes();
	}
}
