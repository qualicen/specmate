package com.specmate.connectors.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import com.specmate.common.exception.SpecmateException;
import com.specmate.config.api.IConfigService;
import com.specmate.connectors.api.IMultiConnector;
import com.specmate.connectors.api.IProjectConfigService;
import com.specmate.connectors.internal.config.MultiConnectorServiceConfig;
import com.specmate.connectors.internal.config.PollKeys;
import com.specmate.scheduler.Scheduler;
import com.specmate.scheduler.SchedulerIteratorFactory;
import com.specmate.scheduler.SchedulerTask;

/**
 * This service controls all existing multi connectors. Furthermore, it is
 * responsible for starting the sync task for multi connectors.
 */
@Component(immediate = true, configurationPid = MultiConnectorServiceConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MultiConnectorService {

	private List<IMultiConnector> multiConnectors = new ArrayList<>();

	private LogService logService;
	private IConfigService configService;
	private IProjectConfigService projectConfigService;
	private Scheduler scheduler;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {

		validateConfig(properties);

		String schedule = (String) properties.get(PollKeys.KEY_POLL_SCHEDULE);
		if (schedule == null) {
			logService.log(LogService.LOG_INFO, "Polling interval '" + PollKeys.KEY_POLL_SCHEDULE + "' not set.");
			return;
		}

		Scheduler scheduler = new Scheduler();
		scheduler.schedule(new MultiConnectorTask(this, logService), SchedulerIteratorFactory.create(schedule));
	}

	@Deactivate
	public void deactivate() {
		scheduler.cancel();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addMultiConnector(IMultiConnector multiConnector) {
		this.multiConnectors.add(multiConnector);
	}

	public void removeMultiConnector(IMultiConnector multiConnector) {
		this.multiConnectors.remove(multiConnector);
	}

	public List<IMultiConnector> getMultiConnectors() {
		return List.copyOf(multiConnectors);
	}

	private class MultiConnectorTask extends SchedulerTask {

		private MultiConnectorService multiConnectorService;
		private LogService logService;

		public MultiConnectorTask(MultiConnectorService multiConnectorService, LogService logService) {
			this.multiConnectorService = multiConnectorService;
			this.logService = logService;
		}

		@Override
		public void run() {

			for (IMultiConnector multiConnector : multiConnectorService.getMultiConnectors()) {

				logService.log(LogService.LOG_INFO, "Syncing multi connector " + multiConnector.getId());

				try {
					for (Map.Entry<String, Map<String, String>> connectorConfigEntry : multiConnector
							.getProjectConfigs().entrySet()) {

						String technicalProjectId = connectorConfigEntry.getKey();

						// get specmate name for project
						String specmateProjectId = multiConnector.getMultiProject()
								.createSpecmateProjectName(technicalProjectId);

						// add prefix to config entries
						Map<String, String> projectConfigEntries = addPrefixToConfig(specmateProjectId,
								connectorConfigEntry.getValue());

						// we have to read this again each time to avoid accidentally adding identically
						// named projects.
						List<String> configuredProjects = Arrays.asList(configService
								.getConfigurationPropertyArray(IProjectConfigService.KEY_PROJECT_IDS, new String[0]));

						if (!configuredProjects.contains(specmateProjectId)) {

							logService.log(LogService.LOG_INFO, "Adding project " + specmateProjectId);

							// update config: add project config entries
							configService.addUpdateConfigurationProperties(projectConfigEntries);

							// update config: add project id to list of activated projects
							List<String> allProjects = new ArrayList<>();
							allProjects.addAll(configuredProjects);
							allProjects.add(specmateProjectId);
							configService.addUpdateConfigurationProperty(IProjectConfigService.KEY_PROJECT_IDS,
									String.join(",", allProjects));

							// trigger project creation.
							// this could be removed in case ProjectConfigService would monitor the config!
							projectConfigService.configureProjects(new String[] { specmateProjectId });
						}

					}
				} catch (SpecmateException e) {
					logService.log(LogService.LOG_ERROR, "Error syncing projects for " + multiConnector.getId(), e);
				}

			}

		}

	}

	private Map<String, String> addPrefixToConfig(String specmateProjectId, Map<String, String> orignalConfig) {

		String prefix = IProjectConfigService.PROJECT_PREFIX + specmateProjectId + ".connector.";

		Map<String, String> newConfig = new HashMap<String, String>();

		for (Map.Entry<String, String> entry : orignalConfig.entrySet()) {
			newConfig.put(prefix + entry.getKey(), entry.getValue());
		}

		return newConfig;
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Reference
	public void setConfigService(IConfigService configService) {
		this.configService = configService;
	}

	@Reference
	public void setProjectConfigService(IProjectConfigService projectConfigService) {
		this.projectConfigService = projectConfigService;
	}

	private void validateConfig(Map<String, Object> properties) throws SpecmateException {
		SchedulerIteratorFactory.validate((String) properties.get(PollKeys.KEY_POLL_SCHEDULE));
		logService.log(LogService.LOG_DEBUG, "Multi connector service config validated.");
	}
}
