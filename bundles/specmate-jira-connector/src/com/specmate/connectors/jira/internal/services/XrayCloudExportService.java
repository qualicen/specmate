package com.specmate.connectors.jira.internal.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
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
import com.specmate.rest.RestClient.EAuthType;
import com.specmate.rest.RestResult;

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

	/** URL to the xray cloud */
	private String url;

	/** Name of the jira project */
	private String xrayProjectName;

	/** Client id to access xray */
	private String clientId;

	/** Client secret to access xray */
	private String clientSecret;

	public XrayCloudExportService() {
		super("Xray Cloud");
	}

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		url = (String) properties.get(KEY_XRAY_CLOUD_URL);
		xrayProjectName = (String) properties.get(KEY_XRAY_PROJECT);
		clientId = (String) properties.get(KEY_XRAY_CLIENT_ID);
		clientSecret = (String) properties.get(KEY_XRAY_CLIENT_SECRET);
		testType = (String) properties.get(KEY_XRAY_TEST_TYPE);

		try {
			new URL(url);
		} catch (MalformedURLException e) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION, "Malformed xray cloud URL: " + url);
		}
		if (StringUtils.isBlank(xrayProjectName)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"No or empty project name given for xray cloud exporter.");
		}
		if (StringUtils.isBlank(clientId)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"No or empty client id given for xray cloud exporter.");
		}
		if (StringUtils.isBlank(clientSecret)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"No or empty client secret given for xray cloud exporter.");
		}
		if (StringUtils.isBlank(testType)) {
			logService.log(LogService.LOG_WARNING, "No test type provided for xray cloud export, assuming \"Manual\"");
			testType = "Manual";
		}
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
		RestClient restClient = new RestClient(url, 10000, logService);
		try {
			authenticate(restClient);
			JSONArray exportObjs = getExportObjects(testProcedure);
			RestResult<JSONObject> result = restClient.post(xrayPath("/import/test/bulk"), exportObjs);
			if (result.getResponse().getStatus() == Response.Status.OK.getStatusCode()) {
				return Optional.empty();
			} else {
				throw new SpecmateInternalException(ErrorCode.JIRA, "Could not export test procedure");
			}
		} finally {
			restClient.close();
		}
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
			JSONObject stepObj = JiraUtil.stepToJson(step, true);
			steps.put(stepObj);
		}
		exportObj.put("steps", steps);

		JSONArray allTests = new JSONArray();
		allTests.put(exportObj);
		return allTests;
	}

	private void authenticate(RestClient restClient) throws SpecmateException {
		JSONObject authObj = new JSONObject();
		authObj.put("client_id", clientId);
		authObj.put("client_secret", clientSecret);
		Response result = restClient.rawPost(xrayPath("/authenticate"), authObj, null, null);
		if (result.getStatus() == Response.Status.OK.getStatusCode()) {
			String token = result.readEntity(String.class);
			if (!StringUtils.isEmpty(token)) {
				token = token.replaceAll("^\"|\"$", "");
				restClient.setAuthorization(EAuthType.BEARER, token);
				return;
			}
		}
		throw new SpecmateInternalException(ErrorCode.JIRA, "Could not authenticate with Xray cloud.");
	}

	private String xrayPath(String path) {
		return "/api/v1" + path;
	}

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		// we cannot check on a per user basis, assume true
		return true;
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

}
