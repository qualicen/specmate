package com.specmate.export.internal.services;

import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateValidationException;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.metrics.ICounter;
import com.specmate.metrics.IMetricsService;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.export.Export;
import com.specmate.rest.RestResult;

/** Service that exports a test specification in various formats */
@Component(immediate = true, service = IRestService.class)
public class TestExportService extends RestServiceBase {
	private final String LANGUAGE_PARAM = "language";

	private IMetricsService metricsService;
	private ICounter exportCounter;
	private ExportManagerService exportManager;

	@Activate
	public void activate() throws SpecmateException {
		exportCounter = metricsService.createCounter("export_counter", "Total number of exported test specifications");
	}

	@Override
	public String getServiceName() {
		return "export";
	}

	@Override
	public boolean canGet(Object object) {
		return (object instanceof TestSpecification) || (object instanceof TestProcedure);
	}

	@Override
	public RestResult<?> get(Object object, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {
		String language = queryParams.getFirst(LANGUAGE_PARAM);
		if (language == null) {
			throw new SpecmateValidationException("Language for export not specified.");
		}
		Optional<Export> result = exportManager.export(object, language, token);
		exportCounter.inc();
		if (result.isPresent()) {
			return new RestResult<Export>(Status.OK, result.get());
		} else {
			return new RestResult<Export>(Status.NO_CONTENT);
		}
	}

	@Reference
	public void setMetricsService(IMetricsService metricsService) {
		this.metricsService = metricsService;
	}

	@Reference
	public void setExportManagerService(ExportManagerService exportManager) {
		this.exportManager = exportManager;
	}

}
