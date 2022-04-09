/* 
     Author: Michael Gatzonis - 26/1/2021 
     live
*/
package org.opendelos.model.scheduler;

import java.time.LocalTime;

public class MongoLocalTime implements Comparable<MongoLocalTime> {

	private LocalTime localTime;

	private MongoLocalTime(LocalTime localTime) {
		this.localTime = localTime;
	}

	public static MongoLocalTime of(LocalTime localTime) {
		return new MongoLocalTime(localTime);
	}

	public static MongoLocalTime of(int hour, int minute, int second) {
		return new MongoLocalTime(LocalTime.of(hour, minute, second));
	}

	public int getHour() {
		return this.localTime.getHour();
	}

	public int getMinute() {
		return this.localTime.getMinute();
	}

	public int getSecond() {
		return this.localTime.getSecond();
	}

	public LocalTime toLocalTime() {
		return this.localTime;
	}

	@Override
	public int compareTo(MongoLocalTime other) {
		return this.localTime.compareTo(other.localTime);
	}

	public String toString() {
		return  this.localTime.toString();
	}
}
