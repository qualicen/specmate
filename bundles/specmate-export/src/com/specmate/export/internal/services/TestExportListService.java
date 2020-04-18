package com.specmate.export.internal.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.rest.RestResult;

/** Service that exports a test specification in various formats */
@Component(immediate = true, service = IRestService.class)
public class TestExportListService extends RestServiceBase {

	private ExportManagerService exportManager;

	@Override
	public String getServiceName() {
		return "exporterlist";
	}

	@Override
	public RestResult<?> get(Object object, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {
		return new RestResult<List<String>>(Status.OK,
				exportManager.getExporters(object, token).stream().map(e -> e.getType()).collect(Collectors.toList()));
	}

	@Override
	public boolean canGet(Object object) {
		return object instanceof TestProcedure || object instanceof TestSpecification;
	}

	@Reference
	public void setExportManagerService(ExportManagerService exportManager) {
		this.exportManager = exportManager;
	}

}
