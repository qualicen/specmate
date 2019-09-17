package com.specmate.schedule.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.specmate.scheduler.SchedulerIteratorFactory;
import com.specmate.scheduler.iterators.ScheduleIterator;

public class IteratorsTest {

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Test
	public void testYearlyIterator() throws Exception {
		Date date = dateFormat.parse("2018-11-01 09:30:10");

		// scheduled time before current time
		ScheduleIterator yearlyIterator = SchedulerIteratorFactory.create("year 3 20 15", date);
		Date next = yearlyIterator.next();
		Assert.assertEquals("2019-11-01 03:20:15", dateFormat.format(next));

		// scheduled time after current time
		yearlyIterator = SchedulerIteratorFactory.create("year 11 20 15", date);
		next = yearlyIterator.next();
		Assert.assertEquals("2018-11-01 11:20:15", dateFormat.format(next));

		Date endOfDay = dateFormat.parse("2018-12-31 23:30:10");
		yearlyIterator = SchedulerIteratorFactory.create("year 11 20 15", endOfDay);
		next = yearlyIterator.next();
		Assert.assertEquals("2019-12-31 11:20:15", dateFormat.format(next));
	}
	
	@Test
	public void testMonthlyIterator() throws Exception {
		Date date = dateFormat.parse("2018-11-01 09:30:10");

		// scheduled time before current time
		ScheduleIterator monthlyIterator = SchedulerIteratorFactory.create("month 4 40 50", date);
		Date next = monthlyIterator.next();
		Assert.assertEquals("2018-12-01 04:40:50", dateFormat.format(next));

		// scheduled time after current time
		monthlyIterator = SchedulerIteratorFactory.create("month 12 30 00", date);
		next = monthlyIterator.next();
		Assert.assertEquals("2018-11-01 12:30:00", dateFormat.format(next));

		Date endOfDay = dateFormat.parse("2018-12-31 23:30:10");
		// scheduled time need year jump
		monthlyIterator = SchedulerIteratorFactory.create("month 7 15 59", endOfDay);
		next = monthlyIterator.next();
		Assert.assertEquals("2019-01-31 07:15:59", dateFormat.format(next));
	}
	
	@Test
	public void testMonthlyLastDayIterator() throws Exception {
		Date date = dateFormat.parse("2019-01-31 09:30:10");
		
		// scheduled time where current month has more days than next month
		ScheduleIterator monthlyLastDayIterator = SchedulerIteratorFactory.create("monthlastday 4 40 50", date);
		Date next = monthlyLastDayIterator.next();
		Assert.assertEquals("2019-02-28 04:40:50", dateFormat.format(next));

		Date lastDayOfFeb = dateFormat.parse("2018-02-28 23:30:10");
		// scheduled time where current month has less days than next month
		monthlyLastDayIterator = SchedulerIteratorFactory.create("monthlastday 12 30 00", lastDayOfFeb);
		next = monthlyLastDayIterator.next();
		Assert.assertEquals("2018-03-31 12:30:00", dateFormat.format(next));

		Date endOfDay = dateFormat.parse("2019-04-30 23:30:10");
		// scheduled time need year jump
		monthlyLastDayIterator = SchedulerIteratorFactory.create("monthlastday 7 15 59", endOfDay);
		next = monthlyLastDayIterator.next();
		Assert.assertEquals("2019-05-31 07:15:59", dateFormat.format(next));
	}
	
	@Test
	public void testWeeklyIterator() throws Exception {
		Date date = dateFormat.parse("2018-11-01 09:30:10");

		// scheduled time before current time
		ScheduleIterator weeklyIterator = SchedulerIteratorFactory.create("week 7 45 59", date);
		Date next = weeklyIterator.next();
		Assert.assertEquals("2018-11-08 07:45:59", dateFormat.format(next));

		// scheduled time after current time
		weeklyIterator = SchedulerIteratorFactory.create("week 11 45 59", date);
		next = weeklyIterator.next();
		Assert.assertEquals("2018-11-01 11:45:59", dateFormat.format(next));

		Date endOfDay = dateFormat.parse("2018-12-31 23:30:10");
		// scheduled time need year jump
		weeklyIterator = SchedulerIteratorFactory.create("week 2 45 59", endOfDay);
		next = weeklyIterator.next();
		Assert.assertEquals("2019-01-07 02:45:59", dateFormat.format(next));
	}
	
	@Test
	public void testHourlyIterator() throws Exception {
		Date date = dateFormat.parse("2018-11-01 09:30:10");

		// scheduled time before current time
		ScheduleIterator hourlyIterator = SchedulerIteratorFactory.create("hour 20 15", date);
		Date next = hourlyIterator.next();
		Assert.assertEquals("2018-11-01 10:20:15", dateFormat.format(next));

		// scheduled time after current time
		hourlyIterator = SchedulerIteratorFactory.create("hour 40 15", date);
		next = hourlyIterator.next();
		Assert.assertEquals("2018-11-01 09:40:15", dateFormat.format(next));

		Date endOfDay = dateFormat.parse("2018-12-31 23:30:10");
		// scheduled time need day jump
		hourlyIterator = SchedulerIteratorFactory.create("hour 20 15", endOfDay);
		next = hourlyIterator.next();
		Assert.assertEquals("2019-01-01 00:20:15", dateFormat.format(next));
	}

	@Test
	public void testMinuteIterator() throws Exception {
		Date date = dateFormat.parse("2018-11-01 09:30:10");

		// scheduled time before current time
		ScheduleIterator minuteIterator = SchedulerIteratorFactory.create("minute 15", date);
		Date next = minuteIterator.next();
		Assert.assertEquals("2018-11-01 09:30:15", dateFormat.format(next));

		// scheduled time after current time
		minuteIterator = SchedulerIteratorFactory.create("minute 5", date);
		next = minuteIterator.next();
		Assert.assertEquals("2018-11-01 09:31:05", dateFormat.format(next));

		Date endOfDay = dateFormat.parse("2018-12-31 23:59:10");
		// scheduled time need day jump
		minuteIterator = SchedulerIteratorFactory.create("minute 5", endOfDay);
		next = minuteIterator.next();
		Assert.assertEquals("2019-01-01 00:00:05", dateFormat.format(next));
	}

	@Test
	public void testDaily() throws Exception {
		Date date = dateFormat.parse("2018-11-01 09:30:10");

		// scheduled time before current time
		ScheduleIterator dayIterator = SchedulerIteratorFactory.create("day 8 20 5", date);
		Date next = dayIterator.next();
		Assert.assertEquals("2018-11-02 08:20:05", dateFormat.format(next));

		// scheduled time after current time
		dayIterator = SchedulerIteratorFactory.create("day 10 20 5", date);
		next = dayIterator.next();
		Assert.assertEquals("2018-11-01 10:20:05", dateFormat.format(next));

		Date endOfDay = dateFormat.parse("2018-12-31 23:59:10");
		// scheduled minutes need year jump
		dayIterator = SchedulerIteratorFactory.create("day 10 20 5", endOfDay);
		next = dayIterator.next();
		Assert.assertEquals("2019-01-01 10:20:05", dateFormat.format(next));
	}
}