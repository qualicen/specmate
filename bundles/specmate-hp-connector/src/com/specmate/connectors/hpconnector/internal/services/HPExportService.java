package com.specmate.connectors.hpconnector.internal.services;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.connectors.hpconnector.internal.config.HPServerProxyConfig;
import com.specmate.connectors.hpconnector.internal.util.HPProxyConnection;
import com.specmate.export.api.IExporter;
import com.specmate.export.api.ExporterBase;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.export.Export;

@Component(service = IExporter.class, configurationPid = HPServerProxyConfig.EXPORTER_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HPExportService extends ExporterBase {

	private static final String EXPORTER_NAME = "HP ALM";
	private HPProxyConnection hpConnection;
	private LogService logService;

	public HPExportService() {
		super(EXPORTER_NAME);
	}

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		String host = (String) properties.get(HPServerProxyConfig.KEY_HOST);
		String port = (String) properties.get(HPServerProxyConfig.KEY_PORT);
		int timeout = Integer.parseInt((String) properties.get(HPServerProxyConfig.KEY_TIMEOUT));
		hpConnection = new HPProxyConnection(host, port, timeout, logService);
	}

	@Override
	public Optional<Export> export(Object target) throws SpecmateException {
		if (target instanceof TestProcedure) {
			hpConnection.exportTestProcedure((TestProcedure) target);
			return Optional.empty();
		}
		throw new SpecmateInternalException(ErrorCode.HP_PROXY,
				"Attempt to export object to HP ALM which is not a test procedure.");
	}

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		return hpConnection.authenticateExport(username, password);
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Override
	public boolean canExportTestSpecification() {
		return false;
	}

	@Override
	public boolean canExportTestProcedure() {
		return true;
	}
}
