package com.specmate.scheduler.iterators;

import java.time.LocalDateTime;
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
	private boolean lastDayOfMonth;

	public MonthlyIterator(boolean lastDayOfMonth, Date date, int... time) {
		this(lastDayOfMonth, getHourOfDay(time), getMinute(time), getSecond(time), date);
	}

	public MonthlyIterator(boolean lastDayOfMonth, int hourOfDay, int minute, int second, Date date) {
		
		this.lastDayOfMonth = lastDayOfMonth;
		
		ZoneId currentZone = ZoneId.systemDefault();
		LocalDateTime localDT = LocalDateTime.ofInstant(date.toInstant(), currentZone);
		localDT = localDT.withHour(hourOfDay).withMinute(minute).withSecond(second).withNano(0);
		
		// if we want to use the lastDayOfMonth Iterator we can set the date to the last day of the month specified by the date
		if (lastDayOfMonth) {
			localDT = localDT.with(TemporalAdjusters.lastDayOfMonth());
		}
		
		zonedDateTime = ZonedDateTime.of(localDT, currentZone);
		ZonedDateTime specifiedDate = date.toInstant().atZone(currentZone);
		
		if(zonedDateTime.isAfter(specifiedDate)) {
			// if the time of the zonedDateTime lies in the future subtract one month to get the correct scheduled time
			zonedDateTime = zonedDateTime.minusMonths(1);
		}
	}

	@Override
	public Date next() {
		// Add one month to the set date
		zonedDateTime = zonedDateTime.plusMonths(1);
		/*	If last day of previous month was the 30th then plusMonths method returns the 30th of the next month
		 * 	as we want our counter to reset at the last day of the month we call lastDayOfMonth 
		 * 	to set the date to the 31th if it exists in the current month
		 * */
		if (lastDayOfMonth) {
			zonedDateTime = zonedDateTime.with(TemporalAdjusters.lastDayOfMonth());
		}
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