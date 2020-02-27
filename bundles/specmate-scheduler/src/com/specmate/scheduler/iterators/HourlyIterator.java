package com.specmate.scheduler.iterators;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * A <code>HourlyIterator</code> returns a sequence of dates on subsequent hours
 * representing the same time each hour.
 */
public class HourlyIterator implements ScheduleIterator {
	private ZonedDateTime zonedDateTime;

	public HourlyIterator(Date date, int... time) {
		this(getMinute(time), getSecond(time), date);
	}

	public HourlyIterator(int minute, int second, Date date) {
		ZoneId currentZone = ZoneId.systemDefault();
		
		LocalDateTime localDT = LocalDateTime.ofInstant(date.toInstant(), currentZone);
		localDT = localDT.withMinute(minute).withSecond(second).withNano(0);

		zonedDateTime = ZonedDateTime.of(localDT, currentZone);
		ZonedDateTime specifiedDate = date.toInstant().atZone(currentZone);
		
		if(zonedDateTime.isAfter(specifiedDate)) {
			// if the time of the zonedDateTime lies in the future subtract one hour to get the correct scheduled time
			zonedDateTime = zonedDateTime.minusHours(1);
		}
	}

	@Override
	public Date next() {
		zonedDateTime = zonedDateTime.plusHours(1);
		return Date.from(zonedDateTime.toInstant());
	}

	private static int getMinute(int... time) {
		int temp = SchedulerUtils.getNumberIfExistsOrZero(0, time);
		return SchedulerUtils.normalizeInput(temp, 60);
	}

	private static int getSecond(int... time) {
		int temp = SchedulerUtils.getNumberIfExistsOrZero(1, time);
		return SchedulerUtils.normalizeInput(temp, 60);
	}
}