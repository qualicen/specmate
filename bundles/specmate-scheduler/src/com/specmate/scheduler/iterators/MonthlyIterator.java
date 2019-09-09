package com.specmate.scheduler.iterators;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * A <code>WeeklyIterator</code> returns a sequence of dates on subsequent weeks
 * representing the same time each week.
 */
public class MonthlyIterator implements ScheduleIterator {
	private ZonedDateTime zonedDateTime;

	public MonthlyIterator(Date date, int... time) {
		this(getHourOfDay(time), getMinute(time), getSecond(time), date);
	}

	public MonthlyIterator(int hourOfDay, int minute, int second, Date date) {
		
		// Get the specified date
		LocalDate localDate = date.toInstant()
			      .atZone(ZoneId.systemDefault())
			      .toLocalDate();
		
		LocalDateTime localDT = LocalDateTime.of(localDate, LocalTime.of(hourOfDay, minute, second, 0));
		
		ZoneId currentZone = ZoneId.systemDefault();
		zonedDateTime = ZonedDateTime.of(localDT, currentZone);
	}

	@Override
	public Date next() {
		// Add one month to the set date
		zonedDateTime = zonedDateTime.plusMonths(1);
		/*	If last day of previous month was the 30th then plusMonths method returns the 30th of the next month
		 * 	as we want our counter to reset at the last day of the month we call lastDayOfMonth 
		 * 	to set the date to the 31th if it exists in the current month
		 * */
		zonedDateTime = zonedDateTime.with(TemporalAdjusters.lastDayOfMonth());
		return Date.from(zonedDateTime.toInstant());
	}
	
	private static int getHourOfDay(int... time) {
		int temp = SchedulerUtils.getNumberIfExistsOrZero(0, time);
		return SchedulerUtils.normalizeInput(temp, 24);
	}

	private static int getMinute(int... time) {
		int temp = SchedulerUtils.getNumberIfExistsOrZero(1, time);
		return SchedulerUtils.normalizeInput(temp, 60);
	}

	private static int getSecond(int... time) {
		int temp = SchedulerUtils.getNumberIfExistsOrZero(2, time);
		return SchedulerUtils.normalizeInput(temp, 60);
	}
}