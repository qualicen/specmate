package specmate.dbprovider.h2.config;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.common.OSGiUtil;
import com.specmate.common.exception.SpecmateException;
import com.specmate.config.api.IConfigService;

@Component
public class H2ProviderConfig {

	public static final String PID = "com.specmate.dbprovider.h2.H2ProviderConfig";
	public static final String KEY_JDBC_CONNECTION = "h2.jdbcConnection";
	public static final int MAX_ID_LENGTH = 100;
	private ConfigurationAdmin configurationAdmin;
	private IConfigService configService;
	/** Reference to the log service */
	@Reference(service = LoggerFactory.class)
	private Logger logger;

	@Activate
	private void configure() throws SpecmateException {
		Dictionary<String, Object> properties = new Hashtable<>();

		String specmateJDBCConnection = configService.getConfigurationProperty(KEY_JDBC_CONNECTION);

		if (specmateJDBCConnection != null) {
			properties.put(KEY_JDBC_CONNECTION, specmateJDBCConnection);
		}

		logger.debug("Configuring CDO with:\n" + OSGiUtil.configDictionaryToString(properties));

		OSGiUtil.configureService(configurationAdmin, PID, properties);

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
