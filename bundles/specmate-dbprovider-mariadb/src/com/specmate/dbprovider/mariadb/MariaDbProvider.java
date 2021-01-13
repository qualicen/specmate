package com.specmate.dbprovider.mariadb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.cdo.server.IStore;
import org.eclipse.emf.cdo.server.db.CDODBUtil;
import org.eclipse.emf.cdo.server.db.mapping.IMappingStrategy;
import org.eclipse.net4j.db.DBUtil;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.IDBConnectionProvider;
import org.eclipse.net4j.db.mysql.MYSQLAdapter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.dbprovider.api.DBConfigChangedCallback;
import com.specmate.dbprovider.api.DBProviderBase;
import com.specmate.dbprovider.api.IDBProvider;
import com.specmate.dbprovider.api.migration.IAttributeToSQLMapper;
import com.specmate.dbprovider.api.migration.IObjectToSQLMapper;
import com.specmate.dbprovider.mariadb.config.MariaDbProviderConfig;
import com.specmate.model.administration.ErrorCode;

@Component(service = IDBProvider.class, configurationPid = MariaDbProviderConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
		"service.ranking:Integer=2" })
public class MariaDbProvider extends DBProviderBase {

	private boolean isVirginDB;
	private Pattern databaseNotFoundPattern = Pattern.compile(".*Database \\\".*\\\" not found.*", Pattern.DOTALL);

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		isVirginDB = false;
		readConfig(properties);

		try {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		} catch (SQLException e) {
			throw new SpecmateInternalException(ErrorCode.PERSISTENCY, "Could not register H2 JDBC driver.", e);
		}
	}

	@Modified
	public void modified(Map<String, Object> properties) throws SpecmateException {
		closeConnection();
		isVirginDB = false;
		readConfig(properties);
		for (DBConfigChangedCallback cb : cbRegister) {
			cb.configurationChanged();
		}
	}

	@Deactivate
	public void deactivate() throws SpecmateException {
		closeConnection();
	}

	private void readConfig(Map<String, Object> properties) throws SpecmateException {
		jdbcConnection = (String) properties.get(MariaDbProviderConfig.KEY_JDBC_CONNECTION);

		if (StringUtils.isBlank(jdbcConnection)) {
			throw new SpecmateInternalException(ErrorCode.PERSISTENCY, "JDBC connection not defined in configuration.");
		}
	}

	@Override
	public Connection getConnection() throws SpecmateException {
		if (connection == null) {
			initiateDBConnection();
		}

		return connection;
	}

	@Override
	public boolean isVirginDB() throws SpecmateException {
		Connection connection = getConnection();
		try {
			PreparedStatement stmt = connection.prepareStatement("select * from CDO_PACKAGE_INFOS");
			stmt.execute();
		} catch (SQLException e) {
			return true;
		}

		return false;
	}

	@Override
	public IStore createStore() throws SpecmateException {
		MysqlDataSource jdataSource = new MysqlDataSource();
		jdataSource.setUrl(jdbcConnection);
		IMappingStrategy jmappingStrategy = CDODBUtil.createHorizontalMappingStrategy(true, false);
		IDBAdapter h2dbAdapter = new MYSQLAdapter();
		IDBConnectionProvider jdbConnectionProvider = DBUtil.createConnectionProvider(jdataSource);
		return CDODBUtil.createStore(jmappingStrategy, h2dbAdapter, jdbConnectionProvider);
	}

	@Override
	public IAttributeToSQLMapper getAttributeToSQLMapper(String packageName, String sourceVersion, String targetVersion)
			throws SpecmateException {
		// return new AttributeToSQLMapper(getConnection(), packageName, sourceVersion,
		// targetVersion);
		return null;
	}

	@Override
	public IObjectToSQLMapper getObjectToSQLMapper(String packageName, String sourceVersion, String targetVersion)
			throws SpecmateException {
		// return new ObjectToSQLMapper(getConnection(), packageName, sourceVersion,
		// targetVersion);
		return null;
	}

	private void initiateDBConnection() throws SpecmateException {
		try {
			connection = DriverManager.getConnection(jdbcConnection);
			isVirginDB = false;
		} catch (SQLException e) {
			throw new SpecmateInternalException(ErrorCode.PERSISTENCY,
					"Could not connect to the H2 database using the connection: " + jdbcConnection + ".", e);
		}
	}

	@Override
	public String getTrueLiteral() {
		return "true";
	}
}
