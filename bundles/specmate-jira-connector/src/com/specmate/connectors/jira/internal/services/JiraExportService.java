package com.specmate.connectors.jira.internal.services;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.util.concurrent.Promise;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.connectors.api.IExportService;
import com.specmate.connectors.api.IProjectConfigService;
import com.specmate.connectors.jira.config.JiraConnectorConfig;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.TestStep;

@Component(immediate = true, service = IExportService.class, configurationPid = JiraConnectorConfig.EXPORTER_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class JiraExportService implements IExportService {

	private LogService logService;
	private JiraRestClient jiraClient;
	private IssueType testType;
	private String id;
	private String url;
	private String projectName;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		id = (String) properties.get(IProjectConfigService.KEY_CONNECTOR_ID);
		url = (String) properties.get(JiraConnectorConfig.KEY_JIRA_URL);
		projectName = (String) properties.get(JiraConnectorConfig.KEY_JIRA_PROJECT);
		String username = (String) properties.get(JiraConnectorConfig.KEY_JIRA_USERNAME);
		String password = (String) properties.get(JiraConnectorConfig.KEY_JIRA_PASSWORD);

		try {
			jiraClient = JiraClientFactory.createJiraRESTClient(url, username, password);
		} catch (URISyntaxException e) {
			throw new SpecmateInternalException(ErrorCode.JIRA, e);
		}

		Iterable<IssueType> issueTypes = jiraClient.getMetadataClient().getIssueTypes().claim();
		Spliterator<IssueType> issueTypesSpliterator = Spliterators.spliteratorUnknownSize(issueTypes.iterator(), 0);

		testType = StreamSupport.stream(issueTypesSpliterator, false)
				.filter(issueType -> issueType.getName().equals("Test")).findFirst().orElseGet(null);
		if (testType == null) {
			logService.log(LogService.LOG_ERROR, "Could not get Issue Type for Tests");
		}
	}

	@Override
	public void export(EObject exportTarget) throws SpecmateException {
		if (exportTarget instanceof TestProcedure) {
			exportTestProcedure((TestProcedure) exportTarget);
		}
		if (exportTarget instanceof TestSpecification) {
			exportTestSpecificatoin((TestSpecification) exportTarget);
		}
	}

	@Override
	public boolean canExportTestProceure() {
		return true;
	}

	@Override
	public boolean canExportTextSpecification() {
		return true;
	}

	private void exportTestSpecificatoin(TestSpecification exportTarget) {
		// TODO Auto-generated method stub

	}

	public void exportTestProcedure(TestProcedure testProcedure) throws SpecmateException {
		IssueInputBuilder issueBuilder = new IssueInputBuilder(projectName, testType.getId());
		issueBuilder.setSummary("Specmate Exported Test Procedure: " + testProcedure.getName());
		StringBuilder builder = new StringBuilder();
		builder.append("||Step||Name||Description||Expected Result||\n");
		List<TestStep> steps = SpecmateEcoreUtil.pickInstancesOf(testProcedure.getContents(), TestStep.class);
		steps.sort((s1, s2) -> Integer.compare(s1.getPosition(), s2.getPosition()));
		int stepNum = 0;
		for (TestStep step : steps) {
			stepNum++;
			builder.append("| " + stepNum + " | " + step.getName() + " | " + step.getDescription() + " | "
					+ step.getExpectedOutcome() + " |\n");
		}
//		builder.append("</tbody></table> </div>");
		issueBuilder.setDescription(builder.toString());
		IssueInput issueInput = issueBuilder.build();
		Promise<BasicIssue> result = jiraClient.getIssueClient().createIssue(issueInput);
		BasicIssue createdIssue;
		try {
			createdIssue = result.get();
			System.out.println(createdIssue);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		ArrayList<ComplexIssueInputFieldValue> steps = new ArrayList<ComplexIssueInputFieldValue>();
//		for (TestStep step : SpecmateEcoreUtil.pickInstancesOf(testProcedure.getContents(), TestStep.class)) {
//			Map<String, Object> stepValues = new HashMap<String, Object>();
//			stepValues.put("step", step.getDescription());
//			stepValues.put("result", step.getExpectedOutcome());
//			stepValues.put("index", step.getPosition());
//			steps.add(new ComplexIssueInputFieldValue(stepValues));
//		}
//		ComplexIssueInputFieldValue stepsFieldValue = ComplexIssueInputFieldValue.with("steps", steps);
//		issueBuilder.setFieldValue("customfield_10004", stepsFieldValue);
//		IssueInput issueInput = issueBuilder.build();
//		Promise<BasicIssue> result = jiraClient.getIssueClient().createIssue(issueInput);
//		BasicIssue createdIssue;
//		try {
//			createdIssue = result.get();
//			System.out.println(createdIssue);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		return testType != null;
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

}
