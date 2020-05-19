package com.specmate.connectors.jira.internal.services;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.export.api.IExporter;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestStep;
import com.specmate.rest.RestClient;
import com.specmate.rest.RestClient.EAuthType;
import com.specmate.rest.RestResult;

@Component(immediate = true, service = IExporter.class, configurationPid = XrayServerExportService.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class XrayServerExportService extends JiraExportServiceBase {

	public static final String PID = "com.specmate.connectors.jira.XrayServerExportService";

	public XrayServerExportService() {
		super("Xray");
	}

	@Override
	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		super.activate(properties);
	}

	@Override
	protected void exportTestStepsPost(TestProcedure procedure, BasicIssue issue) throws SpecmateException {
		String basicAuth = Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes());
		RestClient restClient = new RestClient(url, EAuthType.BASIC, basicAuth, 10000, logService);

		try (restClient) {
			List<TestStep> steps = SpecmateEcoreUtil.getStepsSorted(procedure);
			for (TestStep step : steps) {
				JSONObject stepObj = JiraUtil.stepToJson(step, false);
				RestResult<JSONObject> result = restClient.put("/rest/raven/1.0/api/test/" + issue.getKey() + "/step",
						stepObj);
				if (result.getResponse().getStatus() != Status.OK.getStatusCode()) {
					throw new SpecmateInternalException(ErrorCode.JIRA, "Error while exporting test step");
				}
			}
		}
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

}
