package com.specmate.connectors.jira.internal.services;

import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.export.api.ExporterBase;
import com.specmate.export.api.IExporter;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.export.Export;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.TestStep;
import com.specmate.rest.RestClient;

/** Exporter for jira */
@Component(immediate = true, service = IExporter.class, configurationPid = XrayCloudExportService.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class XrayCloudExportService extends ExporterBase {

	private static final String KEY_XRAY_CLOUD_URL = "xray.cloudUrl";

	private static final String KEY_XRAY_PROJECT = "xray.project";

	private static final String KEY_XRAY_CLIENT_ID = "xray.clientId";

	private static final String KEY_XRAY_CLIENT_SECRET = "xray.clientSecret";

	private static final String KEY_XRAY_TEST_TYPE = "xray.testType";

	public static final String PID = "com.specmate.connectors.jira.XrayCloudExportService";

	/** Reference to the logging service */
	private LogService logService;

	/** The issue type for tests */
	private String testType;

	/** URL to the jira instance */
	private String url;

	/** Name of the jira project */
	private String xrayProjectName;

	/** Client id to access xray */
	private String clientId;

	/** Client secret to access xray */
	private String clientSecret;

	private RestClient restClient;

	public XrayCloudExportService() {
		super("XRay");
	}

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		url = (String) properties.get(KEY_XRAY_CLOUD_URL);
		xrayProjectName = (String) properties.get(KEY_XRAY_PROJECT);
		clientId = (String) properties.get(KEY_XRAY_CLIENT_ID);
		clientSecret = (String) properties.get(KEY_XRAY_CLIENT_SECRET);
		testType = (String) properties.get(KEY_XRAY_TEST_TYPE);
		restClient = new RestClient(url, 10000, logService);
	}

	@Deactivate
	public void deactivate() {
		restClient.close();
	}

	@Override
	public boolean canExportTestSpecification() {
		return false;
	}

	@Override
	public boolean canExportTestProcedure() {
		return true;
	}

	@Override
	public Optional<Export> export(Object exportTarget) throws SpecmateException {
		if (exportTarget instanceof TestProcedure) {
			return exportTestProcedure((TestProcedure) exportTarget);
		}
		if (exportTarget instanceof TestSpecification) {
			return exportTestSpecification((TestSpecification) exportTarget);
		}
		throw new SpecmateInternalException(ErrorCode.JIRA,
				"Cannot export object of type " + exportTarget.getClass().getName());
	}

	private Optional<Export> exportTestSpecification(TestSpecification exportTarget) throws SpecmateInternalException {
		throw new SpecmateInternalException(ErrorCode.JIRA, "Test specification export to jira not supported");
	}

	public Optional<Export> exportTestProcedure(TestProcedure testProcedure) throws SpecmateException {
		String token = authenticate();
		JSONArray exportObjs = getExportObjects(testProcedure);

	}

	private JSONArray getExportObjects(TestProcedure testProcedure) {
		JSONObject exportObj = new JSONObject();
		exportObj.put("testtype", testType);

		JSONObject fields = new JSONObject();
		fields.put("summary", testProcedure.getName());
		JSONObject project = new JSONObject();
		project.put("key", xrayProjectName);
		fields.put("project", project);
		exportObj.put("fields", fields);

		JSONArray steps = new JSONArray();
		for (TestStep step : SpecmateEcoreUtil.getStepsSorted(testProcedure)) {
			JSONObject stepObj = new JSONObject();
			stepObj.put("action", step.getDescription());
			stepObj.put("result", step.getExpectedOutcome());
			steps.put(step);
		}
		exportObj.put("steps", steps);

		JSONArray allTests = new JSONArray();
		allTests.put(exportObj);
		return allTests;
	}

	private String authenticate() throws SpecmateException {
		JSONObject authObj = new JSONObject();
		authObj.put("client_id", clientId);
		authObj.put("client_secret", clientSecret);
		Response result = restClient.rawPost("/authenticate", authObj);
		if (result.getStatus() == Response.Status.OK.getStatusCode()) {
			return result.readEntity(String.class);
		} else {
			throw new SpecmateInternalException(ErrorCode.JIRA, "Could not authenticate with Xray cloud.");
		}
	}

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		try {
			return JiraUtil.authenticate(url, projectName, username, password);
		} catch (SpecmateException e) {
			logService.log(LogService.LOG_ERROR, "Exception occured when authorizing for export", e);
			return false;
		}
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

}
