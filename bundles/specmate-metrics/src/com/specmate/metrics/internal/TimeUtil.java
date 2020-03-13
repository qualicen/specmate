package com.specmate.metrics.internal;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

public class TimeUtil {
	
	public static long getDiffDay() {
		LocalDateTime localNow = LocalDateTime.now();

		ZoneId currentZone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localNow, currentZone);

		// Go back to beginning of day
		zonedDateTime = zonedDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);

		return zonedDateTime.toInstant().toEpochMilli(); 
	}
	
	public static long getDiffWeek() {
		LocalDateTime localNow = LocalDateTime.now();

		ZoneId currentZone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localNow, currentZone);
		
		// Go back to beginning of current week
		zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		zonedDateTime = zonedDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);

		return zonedDateTime.toInstant().toEpochMilli(); 
	}
	
	public static long getDiffMonth() {
		LocalDateTime localNow = LocalDateTime.now();

		ZoneId currentZone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localNow, currentZone);
		
		// Go back to beginning of current month
		zonedDateTime = zonedDateTime.with(TemporalAdjusters.firstDayOfMonth());
		zonedDateTime = zonedDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);

		return zonedDateTime.toInstant().toEpochMilli(); 
	}
	
	public static long getDiffYear() {
		LocalDateTime localNow = LocalDateTime.now();

		ZoneId currentZone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localNow, currentZone);
		
		// Go back to beginning of current year
		zonedDateTime = zonedDateTime.with(TemporalAdjusters.firstDayOfYear());
		zonedDateTime = zonedDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);

		return zonedDateTime.toInstant().toEpochMilli(); 
	}
}