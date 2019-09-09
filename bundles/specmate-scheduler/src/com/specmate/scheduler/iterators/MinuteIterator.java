package com.specmate.scheduler.iterators;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * A <code>DailyIterator</code> returns a sequence of dates on subsequent days
 * representing the same time each day.
 */
public class MinuteIterator implements ScheduleIterator {
	private ZonedDateTime zonedDateTime;

	public MinuteIterator(Date date, int... time) {
		this(getSecond(time), date);
	}

	public MinuteIterator(int second, Date date) {
		ZoneId currentZone = ZoneId.systemDefault();
		
		LocalDateTime localDT = LocalDateTime.ofInstant(date.toInstant(), currentZone);
		localDT = localDT.withSecond(second).withNano(0);

		zonedDateTime = ZonedDateTime.of(localDT, currentZone);
	}

	@Override
	public Date next() {
		zonedDateTime = zonedDateTime.plusMinutes(1);
		return Date.from(zonedDateTime.toInstant());
	}

	private static int getSecond(int... time) {
		int temp = SchedulerUtils.getNumberIfExistsOrZero(0, time);
		return SchedulerUtils.normalizeInput(temp, 60);
	}
}