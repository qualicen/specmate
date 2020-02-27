package com.specmate.metrics.internal;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.specmate.scheduler.*;
import com.specmate.common.exception.SpecmateException;
import com.specmate.metrics.IGauge;
import com.specmate.metrics.IMetricsService;
import com.specmate.metrics.IUserMetricsService;
import com.specmate.persistency.IPersistencyService;
import com.specmate.persistency.IView;
import com.specmate.usermodel.UsermodelFactory;

@Component(immediate=true)
public class UserMetricService implements IUserMetricsService {

	private IPersistencyService persistencyService;
	private IMetricsService metricsService;
	private IView sessionView;
	
	private IGauge specmate_current_day;
	private IGauge specmate_current_week;
	private IGauge specmate_current_month;
	private IGauge specmate_current_year;
	
	@Activate
	public void start() throws SpecmateException {
		this.sessionView = persistencyService.openView();
		this.specmate_current_day = metricsService.
				createGauge("login_counter_current_day", "Number of users logged in at the current day");
		this.specmate_current_week = metricsService.
				createGauge("login_counter_current_week", "Number of users logged in at the current week");
		this.specmate_current_month = metricsService.
				createGauge("login_counter_current_month", "Number of users logged in at the current month");
		this.specmate_current_year = metricsService.
				createGauge("login_counter_current_year", "Number of users logged in at the current year");
		createSchedulers();
		initializeAfterResart();
	}
	
	@Deactivate
	public void deactivate() throws SpecmateException {

		if (sessionView != null) {
			sessionView.close();
		}
	}
	
	/**
	 * 	initialize counter after restart with the currently active sessions
	 * */
	private void initializeAfterResart() {
		initializeGaugeCurrentDay();
		initializeGaugeCurrentWeek();
		initializeGaugeCurrentMonth();
		initializeGaugeCurrentYear();
	}
	
	/**
	 * 	Create different schedulers for reseting the different counters
	 * */
	private void createSchedulers() {
		try {
			String scheduleDay = "day 23 59 59";
			SchedulerTask dailyMetricTask = new MetricTask(specmate_current_day);
			//metricRunnable.run();
			Scheduler scheduler = new Scheduler();
			scheduler.schedule(dailyMetricTask, SchedulerIteratorFactory.create(scheduleDay));
			// Get the reseted counter back
			specmate_current_day = ((MetricTask) dailyMetricTask).getGauge();
			
			String scheduleWeek = "week 23 59 59";
			SchedulerTask weeklyMetricTask = new MetricTask(specmate_current_week);
			//metricRunnableWeek.run();
			Scheduler schedulerWeek = new Scheduler();
			schedulerWeek.schedule(weeklyMetricTask, SchedulerIteratorFactory.create(scheduleWeek, getNextSunday()));
			// Get the reseted counter back
			specmate_current_week = ((MetricTask) weeklyMetricTask).getGauge();

			String scheduleMonth = "monthlastday 23 59 59";
			SchedulerTask monthlyMetricTask = new MetricTask(specmate_current_month);
			//metricRunnableMonth.run();
			Scheduler schedulerMonth = new Scheduler();
			schedulerMonth.schedule(monthlyMetricTask, SchedulerIteratorFactory.create(scheduleMonth));
			// Get the reseted counter back
			specmate_current_month = ((MetricTask) monthlyMetricTask).getGauge();
			
			String scheduleYear = "year 23 59 59";
			SchedulerTask yearlyMetricTask = new MetricTask(specmate_current_year);
			//metricRunnableYear.run();
			Scheduler schedulerYear = new Scheduler();
			schedulerYear.schedule(yearlyMetricTask, SchedulerIteratorFactory.create(scheduleYear, getLastDayOfYear()));
			// Get the reseted counter back
			specmate_current_year = ((MetricTask) yearlyMetricTask).getGauge();
		} catch (SpecmateException e) {
			e.printStackTrace();
		}
	}
	
	private Date getNextSunday() {
		LocalDateTime localNow = LocalDateTime.now();

		ZoneId currentZone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localNow, currentZone);
		
		// Set to next Sunday, if current Date is Sunday the date is not altered
		zonedDateTime = zonedDateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).withNano(0);
		
		return Date.from(zonedDateTime.toInstant());
	}
	
	private Date getLastDayOfYear() {
		LocalDateTime localNow = LocalDateTime.now();

		ZoneId currentZone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localNow, currentZone);

		zonedDateTime = zonedDateTime.with(TemporalAdjusters.lastDayOfYear()).withNano(0);

		return Date.from(zonedDateTime.toInstant());
	}
	
	/**
	 * 	Increment the different counters
	 * */
	public void loginCounter(IView sessionView, String userName) {
		if (isNewUserCurrentDay(sessionView, userName)) {
			specmate_current_day.inc();
		}
		if (isNewUserCurrentWeek(sessionView, userName)) {
			specmate_current_week.inc();
		}
		if (isNewUserCurrentMonth(sessionView, userName)) {
			specmate_current_month.inc();
		}
		if (isNewUserCurrentYear(sessionView, userName)) {
			specmate_current_year.inc();
		}
	}
	
	/**
	 * 
	 * @param sessionView
	 * @param userName
	 * @param difference 
	 * @return Returns if the user with the userName has been logged in in the specified time difference 
	 * @throws SpecmateException 
	 */
	private boolean isNewUser(IView sessionView, String userName, long difference) {
		
		String sqlQuery = "SELECT DISTINCT username FROM UserSession WHERE username=:name AND lastActive> :time";

		List<Object> results = sessionView.querySQLWithName(sqlQuery,
				UsermodelFactory.eINSTANCE.getUsermodelPackage().getUserSession(), userName, difference);

		if (results.size() > 0) {
			return false;
		}
		return true;
	}
	
	private boolean isNewUserCurrentDay(IView sessionView, String userName) {
		long difference = TimeUtil.getDiffDay();
		
		return isNewUser(sessionView, userName, difference);
	}
	
	private boolean isNewUserCurrentWeek(IView sessionView, String userName) {
		long difference = TimeUtil.getDiffWeek();
		
		return isNewUser(sessionView, userName, difference);
	}
	
	private boolean isNewUserCurrentMonth(IView sessionView, String userName) {
		long difference = TimeUtil.getDiffMonth();
		
		return isNewUser(sessionView, userName, difference);
	}
	
	private boolean isNewUserCurrentYear(IView sessionView, String userName) {
		long difference = TimeUtil.getDiffYear(); 
		
		return isNewUser(sessionView, userName, difference);
	}
	
	/**
	 * Use the session view to identify how many session existed before startup of system and set 
	 * the counter correspondingly
	 * */
	private void initializeGauge(long difference, IGauge gauge) {
		 
		String sqlQuery = "SELECT DISTINCT username FROM UserSession WHERE lastActive>:time";

		List<Object> results = sessionView.querySQL(sqlQuery,
				UsermodelFactory.eINSTANCE.getUsermodelPackage().getUserSession(), difference);
		int numberOfSessions = results.size();
	 
		gauge.set(numberOfSessions);
	}
	
	private void initializeGaugeCurrentDay() {
		IGauge gauge = getCurrentGauge(CounterType.CURRENTDAY);
		initializeGauge(TimeUtil.getDiffDay(), gauge);
	}
	
	private void initializeGaugeCurrentWeek() {
		IGauge gauge = getCurrentGauge(CounterType.CURRENTWEEK);
		initializeGauge(TimeUtil.getDiffWeek(), gauge);
	}
	
	private void initializeGaugeCurrentMonth() {
		IGauge gauge = getCurrentGauge(CounterType.CURRENTMONTH);
		initializeGauge(TimeUtil.getDiffMonth(), gauge);
	}
	
	private void initializeGaugeCurrentYear() {
		IGauge gauge = getCurrentGauge(CounterType.CURRENTYEAR);
		initializeGauge(TimeUtil.getDiffYear(), gauge);
	}
	 
	private IGauge getCurrentGauge(CounterType counterType) {
		IGauge gauge = null;
		switch (counterType) {
		case CURRENTDAY:
			gauge = specmate_current_day;
			break;
		case CURRENTWEEK:
			gauge = specmate_current_week;
			break;
		case CURRENTMONTH:
			gauge = specmate_current_month;
			break;
		case CURRENTYEAR:
			gauge = specmate_current_year; 
			break;
		}
		return gauge;
	}

	@Reference
	public void setMetricsService(IMetricsService metricsService) {
		this.metricsService = metricsService;
	}

	@Reference 
	public void setPersistencyService(IPersistencyService persistencyService) {
		this.persistencyService = persistencyService;
	}
}