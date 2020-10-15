package com.specmate.connectors.internal;

import java.util.List;

import org.osgi.service.log.LogService;

import com.specmate.connectors.api.IRequirementsSource;
import com.specmate.persistency.ITransaction;
import com.specmate.scheduler.SchedulerTask;

public class ConnectorTask extends SchedulerTask {

	ConnectorUtil connectorUtil;

	public ConnectorTask(List<IRequirementsSource> requirementsSources, ITransaction transaction,
			LogService logService) {
		super();
		connectorUtil = new ConnectorUtil(requirementsSources, transaction, logService);
	}

	@Override
	public void run() {
		connectorUtil.syncRequirementsFromSources();
	}
}
