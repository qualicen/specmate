package com.specmate.scheduler.iterators;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class SchedulerUtils {
	public static int getNumberIfExistsOrZero(int index, int... args) {
		return args.length > index ? args[index] : 0;
	}
	
	public static int normalizeInput(int time, int divisor) {
		return time % divisor;
	}
	
	public static LocalDate normalizeDate(LocalDate ld, int numberOfDay) {
		try {
			ld = ld.withDayOfMonth(numberOfDay);
		} catch (DateTimeException e) {
			if (numberOfDay<1) {
				ld = ld.with(TemporalAdjusters.firstDayOfMonth());
			} else {
				ld = ld.with(TemporalAdjusters.lastDayOfMonth());	
			}
		}
		return ld;
	}
}