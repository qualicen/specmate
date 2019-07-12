package com.specmate.metrics.internal;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
	
	public static long getDiffDay() {
		long oneDay = 1000*60*60*24;
		long now = new Date().getTime();
		long difference = now - oneDay;
		return difference;
	}
	
	public static long getDiffWeek() {
		long oneWeek = 1000*60*60*24*7;
		long now = new Date().getTime();
		long difference = now - oneWeek;
		return difference;
	}
	
	public static long getDiffMonth() {
		int daysInCurrentMonth = getDaysInMonth();
		long oneMonth = 1000*60*60*24*7*daysInCurrentMonth;
		long now = new Date().getTime();
		long difference = now - oneMonth;
		return difference;
	}
	
	public static long getDiffYear() {
		int daysInCurrentMonth = getDaysInMonth();
		int daysInCurrentYear = getDaysInYear();
		long oneYear = 1000*60*60*24*7*daysInCurrentMonth*daysInCurrentYear;
		long now = new Date().getTime();
		long difference = now - oneYear;
		return difference;
	}
	
	private static int getDaysInMonth() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		return c.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	
	private static int getDaysInYear() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		return c.getActualMaximum(Calendar.DAY_OF_YEAR);
	}
}