package com.specmate.auth.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import com.specmate.auth.api.ISessionService;
import com.specmate.common.OSGiUtil;
import com.specmate.common.exception.SpecmateException;
import com.specmate.usermodel.AccessRights;
import com.specmate.usermodel.UserSession;

public class InMemorySessionServiceTest {
	private static ISessionService sessionService;
	private static BundleContext context;
	private String baseURL = "/services/rest/";
	private String userName = "testuser";
	private String password = "testpass";

	@BeforeClass
	public static void init() throws Exception {
		context = FrameworkUtil.getBundle(InMemorySessionServiceTest.class).getBundleContext();
		configureInMemorySessionService();
		sessionService = getSessionService();
	}

	private static void configureInMemorySessionService() throws Exception {

		ConfigurationAdmin configurationAdmin = getConfigurationAdmin();

		Dictionary<String, Object> properties = new Hashtable<>();
		String pid = "com.specmate.auth.InMemorySessionService";
		properties.put("session.maxIdleMinutes", 1);
		OSGiUtil.configureService(configurationAdmin, pid, properties);

	}

	@Test
	public void testIsAuthorized() throws SpecmateException {
		String projectName = "testIsAuthorized";
		UserSession session = sessionService.create(AccessRights.ALL, AccessRights.ALL, userName, password,
				projectName);
		assertTrue(sessionService.isAuthorizedPath(session.getId(), baseURL + projectName + "/resource1"));
		assertTrue(sessionService.isAuthorizedPath(session.getId(), baseURL + projectName + "/resource1/resource2"));
		assertTrue(sessionService.isAuthorizedPath(session.getId(), baseURL + projectName + "/"));
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL + projectName));
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL));
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL.substring(0, baseURL.length() - 1)));
	}

	@Test
	public void testRegexInjection() throws SpecmateException {
		UserSession session = sessionService.create(AccessRights.ALL, AccessRights.ALL, userName, password,
				"testRegexInjection");
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL + "project/resource1"));
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL + "project/"));
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL + "project"));

		session = sessionService.create(AccessRights.ALL, AccessRights.ALL, userName, password, "");
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL + "pro/resource1"));
		sessionService.delete(session.getId());

		session = sessionService.create(AccessRights.ALL, AccessRights.ALL, userName, password, "?");
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL + "p/resource1"));
		sessionService.delete(session.getId());

		session = sessionService.create(AccessRights.ALL, AccessRights.ALL, userName, password, ".*");
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL + "pr/resource1"));
		sessionService.delete(session.getId());

		session = sessionService.create(AccessRights.ALL, AccessRights.ALL, userName, password, ".+");
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL + "pro/resource1"));
	}

	@Test
	public void testDeleteSession() throws SpecmateException {
		String projectName = "testDeleteSession";
		UserSession session = sessionService.create(AccessRights.ALL, AccessRights.ALL, userName, password,
				projectName);
		assertTrue(sessionService.isAuthorizedPath(session.getId(), baseURL + projectName + "/resource1"));
		sessionService.delete(session.getId());
		assertFalse(sessionService.isAuthorizedPath(session.getId(), baseURL + projectName + "/resource1"));
	}

	private static ISessionService getSessionService() throws Exception {
		Filter sessionFilter = context.createFilter("(impl=volatile)");
		ServiceTracker<ISessionService, ISessionService> sessionTracker = new ServiceTracker<>(context, sessionFilter,
				null);
		sessionTracker.open();
		ISessionService sessionService;

		sessionService = sessionTracker.waitForService(10000);

		Assert.assertNotNull(sessionService);
		return sessionService;
	}

	private static ConfigurationAdmin getConfigurationAdmin() throws Exception {
		ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> adminTracker = new ServiceTracker<>(context,
				ConfigurationAdmin.class, null);
		adminTracker.open();

		ConfigurationAdmin configAdmin = adminTracker.waitForService(10000);

		Assert.assertNotNull(configAdmin);
		return configAdmin;
	}
}
