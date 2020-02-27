package com.specmate.scheduler.iterators;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * A <code>WeeklyIterator</code> returns a sequence of dates on subsequent weeks
 * representing the same time each week.
 */
public class YearlyIterator implements ScheduleIterator {
	private ZonedDateTime zonedDateTime;

	public YearlyIterator(Date date, int... time) {
		this(getHourOfDay(time), getMinute(time), getSecond(time), date);
	}

	public YearlyIterator(int hourOfDay, int minute, int second, Date date) {
		ZoneId currentZone = ZoneId.systemDefault();
		
		LocalDateTime localDT = LocalDateTime.ofInstant(date.toInstant(), currentZone);
		localDT = localDT.withHour(hourOfDay).withMinute(minute).withSecond(second).withNano(0);
		
		zonedDateTime = ZonedDateTime.of(localDT, currentZone);
		ZonedDateTime specifiedDate = date.toInstant().atZone(currentZone);
		
		if(zonedDateTime.isAfter(specifiedDate)) {
			// if the time of the zonedDateTime lies in the future subtract one year to get the correct scheduled time
			zonedDateTime = zonedDateTime.minusYears(1);
		}
	}

	@Override
	public Date next() {
		// Add one year to the set date
		zonedDateTime = zonedDateTime.plusYears(1);
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