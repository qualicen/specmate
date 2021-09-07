package com.specmate.connectors.internal;

import org.osgi.service.log.Logger;

import com.specmate.persistency.ITransaction;
import com.specmate.scheduler.SchedulerTask;

public class ConnectorTask extends SchedulerTask {

	private ITransaction transaction;
	private ConnectorService connectorService;
	private Logger logger;

	public ConnectorTask(ConnectorService connectorService, ITransaction transaction, Logger logger) {
		super();
		this.connectorService = connectorService;
		this.transaction = transaction;
		this.logger = logger;
	}

	@Override
	public void run() {
		ConnectorUtil.syncConnectors(connectorService.getConnectors(), transaction, logger);
	}
}
