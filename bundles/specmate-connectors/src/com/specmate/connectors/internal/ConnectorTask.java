package com.specmate.connectors.internal;

import java.util.List;

import org.osgi.service.log.LogService;

import com.specmate.connectors.api.IConnector;
import com.specmate.persistency.ITransaction;
import com.specmate.scheduler.SchedulerTask;

public class ConnectorTask extends SchedulerTask {
	List<IConnector> requirementsSources;
	ITransaction transaction;
	LogService logService;

	public ConnectorTask(List<IConnector> requirementsSources, ITransaction transaction,
			LogService logService) {
		super();
		this.requirementsSources = requirementsSources;
		this.transaction = transaction;
		this.logService = logService;
	}

	@Override
	public void run() {
		ConnectorUtil.syncRequirementsFromSources(requirementsSources, transaction, logService);
	}
}
