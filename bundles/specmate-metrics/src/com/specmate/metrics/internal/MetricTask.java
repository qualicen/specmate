package com.specmate.metrics.internal;

import com.specmate.metrics.IGauge;
import com.specmate.scheduler.SchedulerTask;

public class MetricTask extends SchedulerTask  {
	
	private IGauge gauge;
	
	public MetricTask (IGauge gauge) {
		this.gauge = gauge;
	}

	@Override
	public void run() {
		gauge.set(0);
	}
	
	public IGauge getGauge() {
		return this.gauge;
	}
}
