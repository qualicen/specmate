package com.specmate.testspecification.internal.services;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.metrics.ICounter;
import com.specmate.metrics.IMetricsService;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.processes.Process;
import com.specmate.model.requirements.CEGModel;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.rest.RestResult;
import com.specmate.testspecification.internal.generators.CEGTestCaseGenerator;
import com.specmate.testspecification.internal.generators.ProcessTestCaseGenerator;

/**
 * Service for generating test cases for a test specification that is linked to
 * a CEG model.
 *
 * @author junkerm
 */
@Component(immediate = true, service = IRestService.class)
public class TestGeneratorService extends RestServiceBase {

	private IMetricsService metricsService;
	private ICounter testGenCounter;
	private LogService logService;

	@Activate
	public void activate() throws SpecmateException {
		testGenCounter = metricsService.createCounter("test_generation_counter",
				"Total number of generated test specifications");
	}

	/** {@inheritDoc} */
	@Override
	public String getServiceName() {
		return "generateTests";

	}

	/** {@inheritDoc} */
	@Override
	public boolean canPost(Object target, Object object) {
		return target instanceof TestSpecification;
	}

	/** {@inheritDoc} */
	@Override
	public RestResult<?> post(Object target, Object object, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {
		TestSpecification specification = (TestSpecification) target;
		EObject container = specification.eContainer();
		if (container instanceof CEGModel) {
			List<String> withLinksParams = queryParams.get("considerLinks");
			boolean withLinks = withLinksParams != null && withLinksParams.size() > 0
					&& withLinksParams.get(0).toLowerCase().equals("true");
			new CEGTestCaseGenerator(specification, withLinks, logService).generate();
			testGenCounter.inc();
		} else if (container instanceof Process) {
			new ProcessTestCaseGenerator(specification).generate();
			testGenCounter.inc();
		} else {
			throw new SpecmateInternalException(ErrorCode.REST_SERVICE,
					"You can only generate test cases from ceg models or processes. The supplied element is of class "
							+ container.getClass().getSimpleName() + ".");
		}
		return new RestResult<>(Response.Status.NO_CONTENT);
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Reference
	public void setMetricsService(IMetricsService metricsService) {
		this.metricsService = metricsService;
	}

}
