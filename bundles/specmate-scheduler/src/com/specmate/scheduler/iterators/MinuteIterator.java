package com.specmate.scheduler.iterators;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * A <code>MinuteIterator</code> returns a sequence of dates on subsequent minutes
 * representing the same time (specified seconds) each minute.
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
		
		ZonedDateTime specifiedDate = date.toInstant().atZone(currentZone);
		
		if(zonedDateTime.isAfter(specifiedDate)) {
			// if the time of the zonedDateTime lies in the future subtract one minute to get the correct scheduled time
			zonedDateTime = zonedDateTime.minusMinutes(1);
		}	
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