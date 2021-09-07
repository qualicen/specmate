package com.specmate.emfrest.internal;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerThread;
import org.osgi.service.log.Logger;

import com.specmate.common.exception.SpecmateException;
import com.specmate.persistency.IPersistencyService;
import com.specmate.persistency.ITransaction;

public class TransactionFactory implements Factory<ITransaction> {

	private IPersistencyService persistencyService;
	private Logger logger;

	public TransactionFactory(IPersistencyService persistencyService, Logger logger) {
		this.persistencyService = persistencyService;
		this.logger = logger;
	}

	@Override
	public void dispose(ITransaction transaction) {
		transaction.close();
	}

	@PerThread
	@Override
	public ITransaction provide() {
		try {
			logger.debug("Create new transaction.");
			return persistencyService.openTransaction();

		} catch (SpecmateException e) {
			logger.error("Transaction factory could not create new transaction.", e);
			return null;
		}
	}
}