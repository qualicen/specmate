package com.specmate.connectors.internal;

import org.osgi.service.log.LogService;

import com.specmate.persistency.ITransaction;
import com.specmate.scheduler.SchedulerTask;

public class ConnectorTask extends SchedulerTask {

	private ITransaction transaction;
	private LogService logService;
	private ConnectorService connectorService;

	public ConnectorTask(ConnectorService connectorService, ITransaction transaction,
			LogService logService) {
		super();
		this.connectorService = connectorService;
		this.transaction = transaction;
		this.logService = logService;
	}

	@Override
	public void run() {		
		ConnectorUtil.syncConnectors(connectorService.getConnectors(), transaction, logService);
	}
}
