package com.specmate.auth.config;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.auth.internal.InMemorySessionService;
import com.specmate.auth.internal.PersistentSessionService;
import com.specmate.common.OSGiUtil;
import com.specmate.common.exception.SpecmateException;
import com.specmate.config.api.IConfigService;

@Component(immediate = true)
public class SessionServiceConfig {

	/**
	 * Config key for the maximum number of minutes a user session is allowed to be
	 * idle
	 */
	public static final String SESSION_MAX_IDLE_MINUTES = "session.maxIdleMinutes";

	/** Config key for deciding whether the sessions should be persisted or not */
	public static final String SESSION_PERSISTENT = "session.persistent";

	/** The configuration admin instance */
	private ConfigurationAdmin configurationAdmin;

	/** The config service instance */
	private IConfigService configService;

	@Activate
	public void activate() throws SpecmateException {
		Dictionary<String, Object> properties = new Hashtable<>();

		Integer maxIdleMinutes = configService.getConfigurationPropertyInt(SESSION_MAX_IDLE_MINUTES);
		if (maxIdleMinutes == null) {
			return;
		}
		boolean isPersistentSession = Boolean.parseBoolean(configService.getConfigurationProperty(SESSION_PERSISTENT));

		String pid = InMemorySessionService.PID;
		if (isPersistentSession) {
			pid = PersistentSessionService.PID;
		}

		properties.put(SESSION_MAX_IDLE_MINUTES, maxIdleMinutes);
		OSGiUtil.configureService(configurationAdmin, pid, properties);
	}

	/** Service reference for config admin */
	@Reference
	public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
		this.configurationAdmin = configurationAdmin;
	}

	/** Service reference for config service */
	@Reference
	public void setConfigurationService(IConfigService configService) {
		this.configService = configService;
	}

}
